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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 「内容优先」页面的专用色板（首页推荐流 / 回答瀑布流）。
 *
 * 与 [ZhihuSurfaceColors] 的区别：卡片在这里不再是「有色块的容器」，而是无边框、无底色的逻辑卡片，
 * 因此页面只有一层底色，靠 [divider] 与留白分界。主文字对页面背景取最大对比，操作元素一律退到
 * [secondaryText]，主题蓝 [accent] 只出现在可行动点（搜索按钮、选中态、展开/收起）。
 *
 * 色值固定按官方 11.14 实测值取值（深色页面 `#191B1F`、浅色页面 `#FFFFFF`），不跟随种子色推导：
 * 这两页的观感依赖「最亮/最暗正文 + 蓝调灰次要信息」的固定关系，动态取色会破坏最大对比这一前提。
 */
@Immutable
data class ContentFirstColors(
    /** 页面底色：深色 #191B1F，浅色 #FFFFFF。 */
    val page: Color,
    /** 主文字 / 标题。 */
    val primaryText: Color,
    /** 次要文字（摘要、作者名、计数、签名、时间）。 */
    val secondaryText: Color,
    /** 三级信息（IP 属地、禁止转载、更弱的辅助说明）。 */
    val tertiaryText: Color,
    /** 主题蓝：搜索按钮、选中态、展开/收起、链接。 */
    val accent: Color,
    /** 角标红点 / 「热」标记。 */
    val badge: Color,
    /** 通栏分割线，低对比度，是卡片之间唯一的视觉边界。 */
    val divider: Color,
    /** 功能底（搜索胶囊、热评小卡片、chip、讨论胶囊）。 */
    val functionalSurface: Color,
    /** 半透明悬浮控件底（悬浮返回、收起按钮、顶部浮层）。 */
    val floatingSurface: Color,
    /** 正文截断渐隐的终点色，与 [page] 同色，避免渐隐处出现色带。 */
    val fadeMask: Color,
    /** 实心品牌蓝填充（官方搜索按钮等大面积色块，深色下仍用 `#1772F6`）。 */
    val accentFill: Color,
    /**
     * [accentFill] 上的前景色。
     *
     * 必须和 [accentFill] 成对使用：`accentFill` 是固定品牌值，而 `colorScheme.onPrimary` 会随
     * 用户自选种子色变化，浅色种子下会出现「深字压深蓝」的低对比组合。
     */
    val onAccentFill: Color,
    /** 已赞同态。 */
    val liked: Color,
    /** 已收藏态。 */
    val favorited: Color,
)

private val LightContentFirstColors = ContentFirstColors(
    page = Color(0xFFFFFFFF),
    primaryText = Color(0xFF191B1F),
    secondaryText = Color(0xFF535861),
    tertiaryText = Color(0xFF81858F),
    accent = Color(0xFF1772F6),
    badge = Color(0xFFF5533D),
    divider = Color(0xFFEBECED),
    functionalSurface = Color(0xFFF8F8FA),
    floatingSurface = Color(0xE6FFFFFF),
    fadeMask = Color(0xFFFFFFFF),
    accentFill = Color(0xFF1772F6),
    onAccentFill = Color(0xFFFFFFFF),
    liked = Color(0xFF1772F6),
    favorited = Color(0xFFFA9D14),
)

private val DarkContentFirstColors = ContentFirstColors(
    page = Color(0xFF191B1F),
    primaryText = Color(0xFFFFFFFF),
    secondaryText = Color(0xFFC2C6CF),
    tertiaryText = Color(0xFF9196A1),
    accent = Color(0xFF558EFF),
    badge = Color(0xFFF5533D),
    divider = Color(0xFF282B30),
    functionalSurface = Color(0xFF212429),
    floatingSurface = Color(0xD9191B1F),
    fadeMask = Color(0xFF191B1F),
    accentFill = Color(0xFF1772F6),
    onAccentFill = Color(0xFFFFFFFF),
    liked = Color(0xFF558EFF),
    favorited = Color(0xFFFA9D14),
)

internal val LocalContentFirstColors = staticCompositionLocalOf { LightContentFirstColors }

/** 当前明暗模式下的「内容优先」色板。只能在 `@Composable` 作用域读取。 */
val contentFirstColors: ContentFirstColors
    @Composable
    @ReadOnlyComposable
    get() = LocalContentFirstColors.current

internal fun contentFirstColorsFor(darkTheme: Boolean): ContentFirstColors =
    if (darkTheme) DarkContentFirstColors else LightContentFirstColors
