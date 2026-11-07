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

package com.github.zly2006.zhihu.ui.designsystem

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * design system token 回归（计划文档 T0.2）。
 *
 * token 是全局视觉一致性的唯一来源，数值改动应当是有意识的；用测试把当前档位锁住，
 * 避免后续顺手调参造成「设置页和导航栏节奏对不上」这类难以定位的漂移。
 */
class DesignSystemTokensTest {
    @Test
    fun motionDurationsKeepTheirTiers() {
        assertEquals(200, Motion.DURATION_SHORT)
        assertEquals(300, Motion.DURATION_MEDIUM)
        assertEquals(450, Motion.DURATION_LONG)
    }

    @Test
    fun spacingLadderStaysOnTheFourDpGrid() {
        assertEquals(4.dp, ZhihuSpacing.ExtraSmall)
        assertEquals(8.dp, ZhihuSpacing.Small)
        assertEquals(12.dp, ZhihuSpacing.Medium)
        assertEquals(16.dp, ZhihuSpacing.Large)
        assertEquals(24.dp, ZhihuSpacing.ExtraLarge)
        assertEquals(32.dp, ZhihuSpacing.Huge)
        assertEquals(16.dp, ZhihuSpacing.ScreenGutter)
    }

    @Test
    fun componentMinimumsStayTouchable() {
        assertEquals(48.dp, ZhihuSizes.MinTouchTarget)
        assertEquals(56.dp, ZhihuSizes.ListItemMinHeight)
        assertEquals(64.dp, ZhihuSizes.NavigationBarHeight)
        assertEquals(24.dp, ZhihuSizes.Icon)
    }
}
