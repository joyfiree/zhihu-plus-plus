/*
 * Zhihu++ - Free & Ad-Free Zhihu client for all platforms.
 * Copyright (C) 2024-2026, zly2006 <i@zly2006.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.zly2006.zhihu.account

import androidx.compose.runtime.Composable
import com.github.zly2006.zhihu.data.fetchVerifiedZhihuSession
import com.github.zly2006.zhihu.data.installZhihuCommonClientConfig
import com.github.zly2006.zhihu.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ZhihuAccountClient internal constructor(
    private val accountState: ZhihuAccountState,
) : AutoCloseable {
    private val cookies = accountState.value.session.cookies
        .toMutableMap()
    private val client = createAccountHttpClient(cookies, accountState.value.session.userAgent) {
        accountState.save(load().copy(cookies = cookies.toMutableMap()))
    }

    fun load(): ZhihuAccountSession = accountState.value.session

    fun save(session: ZhihuAccountSession) {
        require(load().hasSameIdentityAs(session)) {
            "ZhihuAccountClient 只能刷新同一账号的 session；切换身份必须替换 client"
        }
        persistSession(session)
    }

    private fun persistSession(session: ZhihuAccountSession) {
        val updatedCookies = session.cookies.toMap()
        cookies.clear()
        cookies.putAll(updatedCookies)
        accountState.save(session.copy(cookies = cookies.toMutableMap()))
    }

    fun httpClient(): HttpClient = client

    fun temporaryHttpClient(cookies: MutableMap<String, String>): HttpClient =
        createAccountHttpClient(cookies, load().userAgent)

    suspend fun refreshAndSaveProfile(): ZhihuAccountSession? {
        val current = load()
        val refreshed = fetchVerifiedZhihuSession(client, current.cookies, current.userAgent) ?: return null
        return refreshed
            .copy(
                mobileAccessToken = current.mobileAccessToken,
                mobileRefreshToken = current.mobileRefreshToken,
                mobileTokenType = current.mobileTokenType,
                mobileTokenExpiresAt = current.mobileTokenExpiresAt,
                mobileDeviceId = current.mobileDeviceId,
            ).also(::persistSession)
    }

    suspend fun <T> withAuthenticatedClient(
        block: suspend (client: HttpClient, cookies: Map<String, String>) -> T,
    ): T = block(client, cookies)

    override fun close() {
        client.close()
    }
}

/**
 * 游客会话初始化状态；未登录首屏可据此等待游客凭证就绪后再发首个只读请求。
 *
 * IDLE/INITIALIZING 表示尚无可用凭证；READY 表示可直接浏览；
 * FAILED 表示上次初始化失败，保持游客态空会话静默降级，下次前台/启动时重试。
 */
enum class GuestSessionState {
    IDLE,
    INITIALIZING,
    READY,
    FAILED,
}

class ZhihuAccountStore internal constructor(
    repository: ZhihuAccountRepository,
) : AutoCloseable {
    private val accountState = ZhihuAccountState(repository)
    private var mutableClient = ZhihuAccountClient(accountState)
    private val guestSessionMutex = Mutex()
    private val mutableGuestSessionState = MutableStateFlow(
        if (accountState.value.guestSession?.hasGuestCredentials() == true) {
            GuestSessionState.READY
        } else {
            GuestSessionState.IDLE
        },
    )

    val accountsState: StateFlow<ZhihuAccounts> = accountState.state.asStateFlow()
    val session: ZhihuAccountSession
        get() = accountState.value.session
    val accounts: List<ZhihuSavedAccount>
        get() = accountState.value.accounts
    val client: ZhihuAccountClient
        get() = mutableClient

    /** 已持久化且凭证齐全、可直接复用的游客会话。 */
    val hasGuestSession: Boolean
        get() = accountState.value.guestSession?.hasGuestCredentials() == true

    /** 游客会话初始化状态，供未登录首屏在等待凭证时显示加载态。 */
    val guestSessionState: StateFlow<GuestSessionState> = mutableGuestSessionState.asStateFlow()

    fun save(session: ZhihuAccountSession) {
        if (this.session.hasSameIdentityAs(session)) {
            mutableClient.save(session)
        } else {
            replaceSession(session)
        }
    }

    fun replaceSession(session: ZhihuAccountSession) {
        accountState.saveCurrent(session)
        replaceClient()
    }

    fun clear() {
        accountState.removeCurrent()
        replaceClient()
    }

    /**
     * 确保存在可用的官方游客会话（未登录只读浏览使用）。
     *
     * 已有有效游客会话时直接返回；并发调用串行化并复用第一次初始化结果；失败返回 false 不抛出。
     * 当前已登录时只把游客会话写入存储，供退出登录后复用，不替换登录中会话的客户端。
     */
    suspend fun ensureGuestSession(): Boolean {
        if (hasGuestSession) return true
        return guestSessionMutex.withLock {
            if (hasGuestSession) return@withLock true
            mutableGuestSessionState.value = GuestSessionState.INITIALIZING
            val guest = try {
                initializeGuestSession()
            } catch (cancelled: CancellationException) {
                mutableGuestSessionState.value = GuestSessionState.IDLE
                throw cancelled
            } catch (error: Exception) {
                Log.w("ZhihuAccountStore", "初始化游客会话失败，等待下次重试", error)
                mutableGuestSessionState.value = GuestSessionState.FAILED
                return@withLock false
            }
            if (session.login || accountState.value.activeAccountId != null) {
                // 已登录（或登录恰好与初始化并发完成）：只预留游客会话供退出登录后复用，
                // 不替换登录会话的客户端。
                accountState.saveGuestSession(guest)
            } else {
                // 空游客会话与游客会话身份相同，沿用现有客户端，避免关闭首屏正在使用的请求。
                save(guest)
            }
            mutableGuestSessionState.value = GuestSessionState.READY
            true
        }
    }

    private suspend fun initializeGuestSession(): ZhihuAccountSession {
        val loginClient = ZhihuPhoneLoginClient()
        return try {
            val guest = loginClient.initializeGuestSession()
            ZhihuAccountSession(
                login = false,
                cookies = guest.cookies.toMutableMap(),
                mobileAccessToken = guest.accessToken,
                mobileTokenType = guest.tokenType,
                mobileDeviceId = guest.udid,
            )
        } finally {
            loginClient.close()
        }
    }

    suspend fun switchAccount(id: String): Boolean {
        if (id == accountState.value.activeAccountId) return true
        val savedSession = accountState.value.accounts
            .firstOrNull { it.id == id }
            ?.session ?: return false
        val verified = verifyCandidateSession(savedSession.cookies.toMutableMap(), savedSession.userAgent)
            ?.copy(
                mobileAccessToken = savedSession.mobileAccessToken,
                mobileRefreshToken = savedSession.mobileRefreshToken,
                mobileTokenType = savedSession.mobileTokenType,
                mobileTokenExpiresAt = savedSession.mobileTokenExpiresAt,
                mobileDeviceId = savedSession.mobileDeviceId,
            ) ?: return false
        accountState.select(id, verified)
        replaceClient()
        return true
    }

    fun removeAccount(id: String) {
        val activeAccountId = accountState.value.activeAccountId
        accountState.remove(id)
        if (id == activeAccountId) replaceClient()
    }

    suspend fun login(cookies: MutableMap<String, String>): Boolean {
        val session = verifyCandidateSession(cookies) ?: return false
        selectVerifiedSession(session)
        return true
    }

    suspend fun login(token: ZhihuMobileLoginToken): Boolean {
        val session = verifyCandidateSession(token.cookies.toMutableMap())?.copy(
            mobileAccessToken = token.accessToken,
            mobileRefreshToken = token.refreshToken,
            mobileTokenType = token.tokenType,
            mobileTokenExpiresAt = token.expiresAt,
        ) ?: return false
        selectVerifiedSession(session)
        return true
    }

    private fun selectVerifiedSession(session: ZhihuAccountSession) {
        accountState.selectOrAdd(session)
        replaceClient()
    }

    private suspend fun verifyCandidateSession(
        cookies: MutableMap<String, String>,
        userAgent: String = session.userAgent,
    ): ZhihuAccountSession? {
        val client = createAccountHttpClient(cookies, userAgent)
        return try {
            fetchVerifiedZhihuSession(client, cookies, userAgent)
        } finally {
            client.close()
        }
    }

    private fun replaceClient() {
        mutableClient.close()
        mutableClient = ZhihuAccountClient(accountState)
    }

    override fun close() = mutableClient.close()
}

internal class ZhihuAccountState(
    private val repository: ZhihuAccountRepository,
) {
    val state = MutableStateFlow(repository.loadAccounts())
    val value: ZhihuAccounts
        get() = state.value

    fun save(session: ZhihuAccountSession) {
        saveCurrent(session)
    }

    fun saveCurrent(session: ZhihuAccountSession) {
        val current = value
        val activeId = current.activeAccountId
        val updated = if (activeId == null) {
            if (session.login) {
                val id = session.accountSlotId()
                current.copy(
                    activeAccountId = id,
                    accounts = current.accounts + ZhihuSavedAccount(id, session),
                )
            } else {
                current.copy(guestSession = session)
            }
        } else {
            current.copy(
                accounts = current.accounts.map { account ->
                    if (account.id == activeId) account.copy(session = session) else account
                },
            )
        }
        persist(updated)
    }

    fun saveGuestSession(session: ZhihuAccountSession) {
        persist(value.copy(guestSession = session))
    }

    fun selectOrAdd(session: ZhihuAccountSession) {
        val existing = value.accounts.firstOrNull { it.session.hasSameIdentityAs(session) }
        if (existing != null) {
            persist(
                value.copy(
                    activeAccountId = existing.id,
                    accounts = value.accounts.map { if (it.id == existing.id) it.copy(session = session) else it },
                ),
            )
        } else {
            val id = session.accountSlotId()
            persist(
                value.copy(
                    activeAccountId = id,
                    accounts = value.accounts + ZhihuSavedAccount(id, session),
                ),
            )
        }
    }

    fun select(id: String, session: ZhihuAccountSession) {
        require(value.accounts.any { it.id == id }) { "找不到要切换的登录账号" }
        persist(
            value.copy(
                activeAccountId = id,
                accounts = value.accounts.map { if (it.id == id) it.copy(session = session) else it },
            ),
        )
    }

    fun remove(id: String) {
        val remaining = value.accounts.filterNot { it.id == id }
        val activeId = if (value.activeAccountId == id) remaining.firstOrNull()?.id else value.activeAccountId
        persist(value.copy(activeAccountId = activeId, accounts = remaining))
    }

    fun removeCurrent() {
        // 游客态没有可退出的账号；保留 guestSession 供继续只读浏览。
        value.activeAccountId?.let(::remove)
    }

    private fun persist(accounts: ZhihuAccounts) {
        repository.saveAccounts(accounts)
        state.value = accounts
    }
}

internal fun createAccountHttpClient(
    cookies: MutableMap<String, String>,
    userAgent: String,
    onCookieChanged: () -> Unit = {},
): HttpClient {
    val configure: io.ktor.client.HttpClientConfig<*>.() -> Unit = {
        installZhihuCommonClientConfig(cookies, userAgent, onCookieChanged)
    }
    return accountHttpClientEngineForTesting?.let { HttpClient(it, configure) }
        ?: HttpClient(accountHttpClientEngineFactory, configure)
}

private fun ZhihuAccountSession.hasSameIdentityAs(other: ZhihuAccountSession): Boolean {
    if (login != other.login) return false
    if (!login) return true
    val identity = accountIdentityKey()
    val otherIdentity = other.accountIdentityKey()
    return identity != null && identity == otherIdentity
}

internal expect val accountHttpClientEngineFactory: HttpClientEngineFactory<*>

var accountHttpClientEngineForTesting: HttpClientEngine? = null

@Composable
expect fun rememberZhihuAccountStore(): ZhihuAccountStore
