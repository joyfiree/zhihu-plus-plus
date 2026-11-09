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

import com.github.zly2006.zhihu.data.ZhihuJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

const val DEFAULT_ZHIHU_USER_AGENT =
    "Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/540.0 (KHTML, like Gecko) Ubuntu/10.10 Chrome/9.1.0.0 Safari/540.0"

@Serializable
data class ZhihuAccountProfileSnapshot(
    val id: String = "",
    val name: String = "",
    val urlToken: String? = null,
    val userType: String = "",
    val avatarUrl: String? = null,
)

@Serializable
data class ZhihuAccountSession(
    val login: Boolean = false,
    val username: String = "",
    val cookies: MutableMap<String, String> = mutableMapOf(),
    val userAgent: String = DEFAULT_ZHIHU_USER_AGENT,
    val profile: ZhihuAccountProfileSnapshot? = null,
    val self: JsonElement? = null,
    val mobileAccessToken: String? = null,
    val mobileRefreshToken: String? = null,
    val mobileTokenType: String? = null,
    val mobileTokenExpiresAt: Long? = null,
    /** 官方 `init/udid_guest` 返回的设备标识；游客会话后续可作移动端请求头 `x-udid`。 */
    val mobileDeviceId: String? = null,
)

@Serializable
data class ZhihuSavedAccount(
    val id: String,
    val session: ZhihuAccountSession,
)

@Serializable
data class ZhihuAccounts(
    val activeAccountId: String? = null,
    val accounts: List<ZhihuSavedAccount> = emptyList(),
    /** 未登录游客会话：没有激活账号时 [session] 回退到它，退出登录后仍可只读浏览。 */
    val guestSession: ZhihuAccountSession? = null,
) {
    val session: ZhihuAccountSession
        get() = accounts.firstOrNull { it.id == activeAccountId }?.session
            ?: guestSession
            ?: emptyZhihuAccountSession
}

private val emptyZhihuAccountSession = ZhihuAccountSession()

interface ZhihuAccountSessionStore {
    fun readText(): String?

    fun writeText(text: String)

    fun delete()
}

class ZhihuAccountRepository(
    private val store: ZhihuAccountSessionStore,
    private val json: Json = ZhihuJson.json,
) {
    fun loadAccounts(): ZhihuAccounts = runCatching {
        val text = store.readText()?.takeIf { it.isNotBlank() } ?: return@runCatching ZhihuAccounts()
        val element = json.parseToJsonElement(text)
        if (element is JsonObject && "accounts" in element) {
            json.decodeFromString<ZhihuAccounts>(text)
        } else {
            val session = json.decodeFromString<ZhihuAccountSession>(text)
            if (session.login) {
                val id = session.accountSlotId()
                ZhihuAccounts(id, listOf(ZhihuSavedAccount(id, session)))
            } else {
                // 旧版本只落盘登录会话；万一读到游客会话，也只在凭证齐全时迁移。
                ZhihuAccounts(guestSession = session.takeIf { it.hasGuestCredentials() })
            }
        }
    }.getOrDefault(ZhihuAccounts())

    fun load(): ZhihuAccountSession = loadAccounts().session

    /**
     * 保存会话：登录账号写入账号列表，游客会话写入 [ZhihuAccounts.guestSession]，两者互不清除。
     * 退出登录只移除账号，不再因为 `login == false` 清空游客凭证。
     */
    fun save(session: ZhihuAccountSession) {
        val accounts = loadAccounts()
        if (session.login) {
            val id = session.accountSlotId()
            saveAccounts(
                accounts.copy(
                    activeAccountId = id,
                    accounts = listOf(ZhihuSavedAccount(id, session)),
                ),
            )
        } else {
            saveAccounts(accounts.copy(guestSession = session))
        }
    }

    fun saveAccounts(accounts: ZhihuAccounts) {
        if (accounts.accounts.isEmpty() && accounts.guestSession == null) {
            store.delete()
        } else {
            store.writeText(json.encodeToString(accounts))
        }
    }

    fun clear() {
        store.delete()
    }
}

/** 游客会话至少要有可签名 Cookie、游客 token 与设备标识才能复用，缺一就重新初始化。 */
internal fun ZhihuAccountSession.hasGuestCredentials(): Boolean =
    !login &&
        !mobileAccessToken.isNullOrBlank() &&
        !mobileDeviceId.isNullOrBlank() &&
        !cookies["d_c0"].isNullOrBlank()

internal fun ZhihuAccountSession.accountIdentityKey(): String? = profile?.id?.takeIf(String::isNotBlank)
    ?: profile?.urlToken?.takeIf(String::isNotBlank)
    ?: username.takeIf(String::isNotBlank)

internal fun ZhihuAccountSession.accountSlotId(): String = accountIdentityKey() ?: "account"
