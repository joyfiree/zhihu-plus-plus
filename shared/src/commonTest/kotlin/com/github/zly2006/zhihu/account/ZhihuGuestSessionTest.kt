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

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ZhihuGuestSessionTest {
    @AfterTest
    fun clearTestOverrides() {
        accountHttpClientEngineForTesting = null
        phoneLoginCookiesForTesting = null
        phoneLoginDeviceInfoForTesting = null
        phoneLoginNowEpochSecondsForTesting = null
    }

    @Test
    fun ensureGuestSessionInitializesAndPersistsOfficialGuestCredentials() = runTest {
        val requests = mutableListOf<String>()
        withGuestAccountEngine(requests) {
            val sessionStore = GuestInMemoryAccountSessionStore()
            val repository = ZhihuAccountRepository(sessionStore)
            val accountStore = ZhihuAccountStore(repository)
            try {
                assertEquals(GuestSessionState.IDLE, accountStore.guestSessionState.value)

                assertTrue(accountStore.ensureGuestSession())

                val session = accountStore.session
                assertFalse(session.login)
                assertEquals("", session.username)
                assertEquals("guest-access", session.mobileAccessToken)
                assertEquals("bearer", session.mobileTokenType)
                assertEquals("device-id", session.mobileDeviceId)
                assertEquals("web-device-cookie", session.cookies["d_c0"])
                assertEquals("guest-z-cookie", session.cookies["z_c0"])
                assertEquals(GuestSessionState.READY, accountStore.guestSessionState.value)
                assertTrue(accountStore.hasGuestSession)
                assertEquals(listOf("/udid", "/api/account/prod/init/udid_guest"), requests)
                // 游客会话必须真正落盘，冷启动重启后才能复用。
                assertEquals(session, ZhihuAccountRepository(sessionStore).load())
            } finally {
                accountStore.close()
            }
        }
    }

    @Test
    fun ensureGuestSessionIsSingleFlightWhenCalledConcurrently() = runTest {
        val requests = mutableListOf<String>()
        withGuestAccountEngine(requests) {
            val accountStore = ZhihuAccountStore(ZhihuAccountRepository(GuestInMemoryAccountSessionStore()))
            try {
                val results = listOf(
                    async { accountStore.ensureGuestSession() },
                    async { accountStore.ensureGuestSession() },
                ).awaitAll()

                assertEquals(listOf(true, true), results)
                assertEquals(listOf("/udid", "/api/account/prod/init/udid_guest"), requests)
            } finally {
                accountStore.close()
            }
        }
    }

    @Test
    fun ensureGuestSessionSkipsNetworkWhenCredentialsAlreadyStored() = runTest {
        val requests = mutableListOf<String>()
        withGuestAccountEngine(requests) {
            val sessionStore = GuestInMemoryAccountSessionStore()
            ZhihuAccountRepository(sessionStore).saveAccounts(
                ZhihuAccounts(guestSession = usableGuestSession()),
            )
            val accountStore = ZhihuAccountStore(ZhihuAccountRepository(sessionStore))
            try {
                assertEquals(GuestSessionState.READY, accountStore.guestSessionState.value)
                assertTrue(accountStore.ensureGuestSession())
                assertTrue(requests.isEmpty())
                assertEquals("device-cookie", accountStore.session.cookies["d_c0"])
            } finally {
                accountStore.close()
            }
        }
    }

    @Test
    fun ensureGuestSessionWhileSignedInOnlyStoresGuestForLogout() = runTest {
        val requests = mutableListOf<String>()
        withGuestAccountEngine(requests) {
            val sessionStore = GuestInMemoryAccountSessionStore()
            ZhihuAccountRepository(sessionStore).saveAccounts(
                ZhihuAccounts(activeAccountId = "alice", accounts = listOf(ZhihuSavedAccount("alice", ALICE))),
            )
            val accountStore = ZhihuAccountStore(ZhihuAccountRepository(sessionStore))
            val signedInClient = accountStore.client
            try {
                assertTrue(accountStore.ensureGuestSession())

                // 已登录时不能把活动会话换成游客，只预存游客会话供退出登录后复用。
                assertTrue(accountStore.session.login)
                assertSame(signedInClient, accountStore.client)
                val guest = ZhihuAccountRepository(sessionStore).loadAccounts().guestSession
                assertNotNull(guest)
                assertEquals("guest-access", guest.mobileAccessToken)
                assertEquals("device-id", guest.mobileDeviceId)
                assertEquals(listOf("/udid", "/api/account/prod/init/udid_guest"), requests)
            } finally {
                accountStore.close()
            }
        }
    }

    @Test
    fun ensureGuestSessionFailureIsSilentAndKeepsSignedInAccount() = runTest {
        val requests = mutableListOf<String>()
        withGuestAccountEngine(requests, failGuestInit = true) {
            val sessionStore = GuestInMemoryAccountSessionStore()
            ZhihuAccountRepository(sessionStore).saveAccounts(
                ZhihuAccounts(activeAccountId = "alice", accounts = listOf(ZhihuSavedAccount("alice", ALICE))),
            )
            val accountStore = ZhihuAccountStore(ZhihuAccountRepository(sessionStore))
            try {
                assertFalse(accountStore.ensureGuestSession())

                assertEquals(GuestSessionState.FAILED, accountStore.guestSessionState.value)
                assertTrue(accountStore.session.login)
                assertEquals("Alice", accountStore.session.username)
                assertNull(ZhihuAccountRepository(sessionStore).loadAccounts().guestSession)
                assertEquals(
                    listOf("alice"),
                    ZhihuAccountRepository(sessionStore).loadAccounts().accounts.map { it.id },
                )
                // 失败只会尝试一次，不无限重试；下次前台/启动由调用方再次触发。
                assertEquals(listOf("/udid"), requests)
            } finally {
                accountStore.close()
            }
        }
    }

    @Test
    fun repositoryKeepsGuestAndSignedInSessionsSeparately() {
        val sessionStore = GuestInMemoryAccountSessionStore()
        val repository = ZhihuAccountRepository(sessionStore)
        val guest = usableGuestSession()

        repository.save(guest)
        assertEquals(guest, repository.load())

        repository.save(ALICE)
        assertEquals(ALICE, repository.load())
        assertEquals(guest, repository.loadAccounts().guestSession)

        // 旧语义在 login=false 时 clear() 整个文件；现在再次保存游客会话不能清掉登录账号。
        repository.save(guest)
        assertEquals(listOf("alice"), repository.loadAccounts().accounts.map { it.id })
        assertEquals(ALICE, repository.load())
        assertEquals(guest, repository.loadAccounts().guestSession)
    }

    @Test
    fun logoutSwitchesToStoredGuestSessionWithoutClearingIt() = runTest {
        withGuestAccountEngine {
            val sessionStore = GuestInMemoryAccountSessionStore()
            val guest = usableGuestSession()
            ZhihuAccountRepository(sessionStore).saveAccounts(
                ZhihuAccounts(
                    activeAccountId = "alice",
                    accounts = listOf(ZhihuSavedAccount("alice", ALICE)),
                    guestSession = guest,
                ),
            )
            val accountStore = ZhihuAccountStore(ZhihuAccountRepository(sessionStore))
            try {
                accountStore.clear()

                assertEquals(false, accountStore.session.login)
                assertEquals(guest, accountStore.session)
                assertTrue(accountStore.accounts.isEmpty())
                assertTrue(accountStore.hasGuestSession)
                assertEquals(guest, ZhihuAccountRepository(sessionStore).loadAccounts().guestSession)
                assertEquals(guest, ZhihuAccountRepository(sessionStore).load())
            } finally {
                accountStore.close()
            }
        }
    }

    @Test
    fun guestSessionSurvivesLoginAndLogoutCycle() = runTest {
        val requests = mutableListOf<String>()
        withGuestAccountEngine(requests) {
            val sessionStore = GuestInMemoryAccountSessionStore()
            val accountStore = ZhihuAccountStore(ZhihuAccountRepository(sessionStore))
            try {
                assertTrue(accountStore.ensureGuestSession())
                val guestCookies = accountStore.session.cookies.toMap()

                assertTrue(accountStore.login(mutableMapOf("z_c0" to "alice-token")))
                assertTrue(accountStore.session.login)
                assertEquals("Alice", accountStore.session.username)
                val persistedGuestCookies = ZhihuAccountRepository(sessionStore)
                    .loadAccounts()
                    .guestSession
                    ?.cookies
                    ?.toMap()
                    ?: error("登录后游客会话丢失")
                assertEquals(guestCookies, persistedGuestCookies)

                accountStore.clear()

                assertFalse(accountStore.session.login)
                assertEquals(guestCookies, accountStore.session.cookies)
                assertEquals(GuestSessionState.READY, accountStore.guestSessionState.value)
                assertEquals(
                    listOf("/udid", "/api/account/prod/init/udid_guest", "/api/v4/me"),
                    requests,
                )
            } finally {
                accountStore.close()
            }
        }
    }

    @Test
    fun switchingSignedInAccountsDoesNotOverwriteGuestSession() = runTest {
        withGuestAccountEngine(meResponse = BOB_PROFILE_RESPONSE) {
            val guest = usableGuestSession()
            val sessionStore = GuestInMemoryAccountSessionStore()
            ZhihuAccountRepository(sessionStore).saveAccounts(
                ZhihuAccounts(
                    activeAccountId = "alice",
                    accounts = listOf(ZhihuSavedAccount("alice", ALICE), ZhihuSavedAccount("bob", BOB)),
                    guestSession = guest,
                ),
            )
            val accountStore = ZhihuAccountStore(ZhihuAccountRepository(sessionStore))
            try {
                assertTrue(accountStore.switchAccount("bob"))

                assertEquals("bob", accountStore.session.profile?.id)
                assertEquals(guest, ZhihuAccountRepository(sessionStore).loadAccounts().guestSession)
            } finally {
                accountStore.close()
            }
        }
    }

    private suspend fun <T> withGuestAccountEngine(
        requests: MutableList<String> = mutableListOf(),
        failGuestInit: Boolean = false,
        meResponse: String = ALICE_PROFILE_RESPONSE,
        block: suspend () -> T,
    ): T {
        check(accountHttpClientEngineForTesting == null)
        val engine = MockEngine { request ->
            requests += request.url.encodedPath
            when (request.url.encodedPath) {
                "/udid" ->
                    if (failGuestInit) {
                        respondJson("""{"error":"mock init failure"}""", HttpStatusCode.InternalServerError)
                    } else {
                        respond(
                            content = "web-device-id",
                            headers = headersOf(
                                HttpHeaders.SetCookie,
                                "d_c0=web-device-cookie; Path=/; Domain=zhihu.com",
                            ),
                        )
                    }

                "/api/account/prod/init/udid_guest" -> respondJson(GUEST_INIT_RESPONSE)
                "/api/v4/me" -> respondJson(meResponse)
                else -> error("Unexpected request: ${request.method} ${request.url}")
            }
        }
        accountHttpClientEngineForTesting = engine
        return try {
            block()
        } finally {
            accountHttpClientEngineForTesting = null
            engine.close()
        }
    }

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )

    private companion object {
        const val GUEST_INIT_RESPONSE =
            """{"udid":"device-id","guest":{"access_token":"guest-access","token_type":"bearer",""" +
                """"cookie":{"z_c0":"guest-z-cookie","q_c0":"guest-q-cookie"}}}"""

        const val ALICE_PROFILE_RESPONSE =
            """{"id":"alice","name":"Alice","url_token":"alice","user_type":"people"}"""

        const val BOB_PROFILE_RESPONSE =
            """{"id":"bob","name":"Bob","url_token":"bob","user_type":"people"}"""

        val ALICE = ZhihuAccountSession(
            login = true,
            username = "Alice",
            cookies = mutableMapOf("z_c0" to "alice-z-cookie"),
            profile = ZhihuAccountProfileSnapshot(id = "alice", name = "Alice", urlToken = "alice"),
        )

        val BOB = ZhihuAccountSession(
            login = true,
            username = "Bob",
            cookies = mutableMapOf("z_c0" to "bob-z-cookie"),
            profile = ZhihuAccountProfileSnapshot(id = "bob", name = "Bob", urlToken = "bob"),
        )

        fun usableGuestSession() = ZhihuAccountSession(
            login = false,
            cookies = mutableMapOf("d_c0" to "device-cookie", "z_c0" to "guest-z-cookie"),
            mobileAccessToken = "guest-access",
            mobileTokenType = "bearer",
            mobileDeviceId = "device-id",
        )
    }
}

private class GuestInMemoryAccountSessionStore(
    var text: String? = null,
) : ZhihuAccountSessionStore {
    override fun readText(): String? = text

    override fun writeText(text: String) {
        this.text = text
    }

    override fun delete() {
        text = null
    }
}
