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
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.zly2006.zhihu.test.MainActivityComposeRule
import com.github.zly2006.zhihu.test.resetAppPreferences
import com.github.zly2006.zhihu.test.setScreenContent
import com.github.zly2006.zhihu.ui.HOME_SEARCH_ACTION_TAG
import com.github.zly2006.zhihu.ui.HOME_SEARCH_BUTTON_TAG
import com.github.zly2006.zhihu.ui.HOME_SEARCH_PLACEHOLDER_TAG
import com.github.zly2006.zhihu.ui.HOME_TOP_ACTIONS_TAG
import com.github.zly2006.zhihu.ui.HOME_TOP_MENU_LOGIN_TAG
import com.github.zly2006.zhihu.ui.HOME_TOP_MENU_PUBLISH_TAG
import com.github.zly2006.zhihu.ui.HOME_TOP_MENU_SETTINGS_TAG
import com.github.zly2006.zhihu.ui.HOME_TOP_MENU_TAG
import com.github.zly2006.zhihu.ui.HomeScreen
import com.github.zly2006.zhihu.ui.PREFERENCE_NAME
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * 首页顶部搜索区布局契约。
 *
 * 新版顶部 = 40dp 蓝描边搜索胶囊 + 右侧「更多」菜单入口（不再有账号头像，也没有消息方框）：
 * 底栏只保留「首页 / 我的」，发布、消息、收藏夹、历史与设置收进这个菜单。
 * 这里锁住空间关系，并断言搜索按钮保持品牌蓝填充；菜单打开后必须至少能看到「设置」与
 * （发布或登录）入口，避免以后误删菜单内容。
 */
@RunWith(AndroidJUnit4::class)
class HomeSearchAreaPixelInstrumentedTest {
    @get:Rule
    val composeRule: MainActivityComposeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        composeRule.resetAppPreferences()
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

    @Test
    fun topSearchAreaMatchesLayoutContract() {
        composeRule.waitForIdle()

        // 旧的消息方框已移除（消息入口在右侧更多菜单里）。
        composeRule
            .onAllNodes(hasTestTag("home_notification_button"), useUnmergedTree = true)
            .assertCountEquals(0)

        val topRow = composeRule
            .onNodeWithTag(HOME_TOP_ACTIONS_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val pill = composeRule
            .onNodeWithTag(HOME_SEARCH_BUTTON_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val searchAction = composeRule
            .onNodeWithTag(HOME_SEARCH_ACTION_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val placeholder = composeRule
            .onNodeWithTag(HOME_SEARCH_PLACEHOLDER_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val menu = composeRule
            .onNodeWithTag(HOME_TOP_MENU_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot

        val pillHeightDp = with(composeRule.density) { pill.height.toDp().value }
        assertTrue("搜索胶囊高度应为 40dp（官方实测），实际 $pillHeightDp", abs(pillHeightDp - 40f) <= 1.5f)
        assertTrue(
            "搜索胶囊应从左侧内容边距开始；pill=$pill topRow=$topRow",
            pill.left <= topRow.left + 1f,
        )
        assertTrue(
            "更多菜单按钮必须位于搜索胶囊右侧；menu=$menu pill=$pill",
            menu.left >= pill.right - 0.5f,
        )
        assertTrue(
            "更多菜单按钮应贴右侧内容边距；menu=$menu topRow=$topRow",
            menu.right <= topRow.right + 1f,
        )
        assertTrue(
            "搜索按钮必须完全落在胶囊内部；action=$searchAction pill=$pill",
            searchAction.left >= pill.left - 0.5f && searchAction.right <= pill.right + 0.5f,
        )
        assertTrue(
            "搜索按钮必须位于胶囊右半侧；action=$searchAction pill=$pill",
            searchAction.left > pill.center.x,
        )
        assertTrue(
            "热词占位必须位于搜索按钮左侧；placeholder=$placeholder action=$searchAction",
            placeholder.right <= searchAction.left + 0.5f,
        )

        // 搜索按钮填充为品牌蓝 #1772F6（浅/深主题一致）；取按钮左内侧一点避开文字。
        val pixels = composeRule.onRoot().captureToImage().toPixelMap()
        val sampleX = (searchAction.left + 6f).toInt().coerceIn(0, pixels.width - 1)
        val sampleY = ((searchAction.top + searchAction.bottom) / 2f).toInt().coerceIn(0, pixels.height - 1)
        val color = pixels[sampleX, sampleY]
        assertTrue(
            "搜索按钮应为品牌蓝填充（#1772F6），实际 $color",
            color.blue > 0.7f && color.red < 0.35f && color.green in 0.3f..0.65f,
        )

        // 菜单可打开，且包含「设置」与（发布 或 登录）入口。
        composeRule.onNodeWithTag(HOME_TOP_MENU_TAG).performClick()
        composeRule.onNodeWithTag(HOME_TOP_MENU_SETTINGS_TAG).assertExists()
        val hasPublish = composeRule.onAllNodes(hasTestTag(HOME_TOP_MENU_PUBLISH_TAG)).fetchSemanticsNodes().isNotEmpty()
        val hasLogin = composeRule.onAllNodes(hasTestTag(HOME_TOP_MENU_LOGIN_TAG)).fetchSemanticsNodes().isNotEmpty()
        assertTrue(
            "更多菜单应包含发布（登录态）或登录（游客态）入口，实际 publish=$hasPublish login=$hasLogin",
            hasPublish || hasLogin,
        )
    }
}
