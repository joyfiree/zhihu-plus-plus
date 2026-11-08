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

package com.github.zly2006.zhihu

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.zly2006.zhihu.data.MOBILE_NOTIFICATION_MESSAGE_URL
import com.github.zly2006.zhihu.test.MainActivityComposeRule
import com.github.zly2006.zhihu.test.ZhihuMockApi
import com.github.zly2006.zhihu.test.resetAppPreferences
import com.github.zly2006.zhihu.test.setScreenContent
import com.github.zly2006.zhihu.ui.HOME_NOTIFICATION_BADGE_TAG
import com.github.zly2006.zhihu.ui.HOME_NOTIFICATION_BUTTON_TAG
import com.github.zly2006.zhihu.ui.HomeScreen
import com.github.zly2006.zhihu.ui.PREFERENCE_NAME
import io.ktor.http.HttpMethod
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.ceil
import kotlin.math.floor

@RunWith(AndroidJUnit4::class)
class HomeNotificationPixelInstrumentedTest {
    @get:Rule
    val composeRule: MainActivityComposeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        composeRule.resetAppPreferences()
        ZhihuMockApi.mockJson(
            method = HttpMethod.Get,
            url = "$MOBILE_NOTIFICATION_MESSAGE_URL?limit=20",
            body =
                """
                {
                  "head": [{"detail_title": "评论转发@", "unread_count": 2}],
                  "column_head": [{"title": "邀请回答", "unread_count": 3}],
                  "data": [{"id": "messages", "unread_count": 4}],
                  "unread": {
                    "message": {"count": 9, "show_count": true}
                  }
                }
                """.trimIndent(),
        )
        composeRule.activity.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit(commit = true) {
            putBoolean("duo3_home_account", false)
            putBoolean("showRefreshFab", false)
        }
        composeRule.setScreenContent {
            HomeScreen(
                scrollToTopTrigger = 0,
                innerPadding = PaddingValues(),
            )
        }
    }

    /**
     * Regression: https://github.com/zly2006/zhihu-plus-plus/issues/696
     * Fixed by: https://github.com/zly2006/zhihu-plus-plus/pull/709
     * Target state: an unread notification badge is fully visible with rounded corners.
     *
     * 产品设计稿把消息入口从底栏移到搜索区左侧的 24×26 方框，角标改压在方框右上角。
     * 断言因此改为「角标完整落在可点区域内部」——这正是原回归（父级裁剪掉角标）的契约，
     * 同时保留像素级检查：角标必须有实心填充、四边描边可见、两个上角保持圆角。
     */
    @Test
    fun unreadNotificationBadgeIsRenderedWithoutParentClipping() {
        composeRule.waitUntil("Expected unread count request", timeoutMillis = 5_000) {
            ZhihuMockApi.requestCount(HttpMethod.Get, "$MOBILE_NOTIFICATION_MESSAGE_URL?limit=20") > 0
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("9", useUnmergedTree = true).assertExists()

        val badgeBounds = composeRule
            .onNodeWithTag(HOME_NOTIFICATION_BADGE_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val buttonBounds = composeRule
            .onNodeWithTag(HOME_NOTIFICATION_BUTTON_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(
            "Badge must stay inside its message entry button; badge=$badgeBounds button=$buttonBounds",
            badgeBounds.left >= buttonBounds.left - 0.5f &&
                badgeBounds.top >= buttonBounds.top - 0.5f &&
                badgeBounds.right <= buttonBounds.right + 0.5f &&
                badgeBounds.bottom <= buttonBounds.bottom + 0.5f,
        )

        val rootImage = composeRule.onRoot().captureToImage()
        val pixels = rootImage.toPixelMap()

        val badgeLeft = floor(badgeBounds.left).toInt().coerceIn(0, pixels.width - 1)
        val badgeRight = ceil(badgeBounds.right).toInt().minus(1).coerceIn(badgeLeft, pixels.width - 1)
        val badgeTop = floor(badgeBounds.top).toInt().coerceIn(0, pixels.height - 1)
        val badgeBottom = ceil(badgeBounds.bottom).toInt().minus(1).coerceIn(badgeTop, pixels.height - 1)
        val badgeCenterX = (badgeLeft + badgeRight) / 2
        val badgeCenterY = (badgeTop + badgeBottom) / 2
        val foreground = { x: Int, y: Int -> isBadgeForeground(pixels[x, y]) }
        assertTrue("badge must have a visible fill", foreground(badgeCenterX, badgeCenterY))
        assertTrue("badge top edge must be rendered", foreground(badgeCenterX, badgeTop))
        assertTrue("badge bottom edge must be rendered", foreground(badgeCenterX, badgeBottom))
        assertTrue("badge left edge must be rendered", foreground(badgeLeft, badgeCenterY))
        assertTrue("badge right edge must be rendered", foreground(badgeRight, badgeCenterY))
        assertTrue("badge top-left corner must remain rounded", !foreground(badgeLeft, badgeTop))
        assertTrue("badge top-right corner must remain rounded", !foreground(badgeRight, badgeTop))
    }

    private fun isBadgeForeground(color: Color): Boolean =
        color.alpha > 0.5f &&
            color.red > color.green + 0.12f &&
            color.red > color.blue + 0.12f
}
