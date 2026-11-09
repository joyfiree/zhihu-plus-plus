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

package com.github.zly2006.zhihu.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 仿知乎官方 App 的手调配色（默认主题）。
 *
 * 官方 11.14 的 UI 颜色收敛到一套名为「Map」的固定令牌（来源：官方 APK
 * `resources.arsc`，见 `reports/official-align/apk-analysis.md`），本文件按真机截图校准：
 * - 浅色：`#FFFFFF` 页面/卡片 + `#1772F6` 品牌蓝 + `#191B1F` 近黑正文；
 * - 深色：`#191B1F` 页面/卡片（顶栏、底栏同色）+ 提亮的 `#558EFF` 蓝 + `#FFFFFF` 正文；
 * - 次级文字浅色 `#535861` / 深色 `#C2C6CF`，三级信息 `#81858F` / `#9196A1`；
 * - 功能面（搜索胶囊、banner、评论卡、chip）浅色 `#F8F8FA` / 深色 `#212429`；
 * - 发丝分割线浅色 `#EBECED` / 深色 `#282B30`。
 *
 * 只有当种子色等于 [ZhihuSeedColor]（用户没有自定义主题色）时才使用这套手调值；
 * 自定义种子 / 动态取色仍走 material-kolor 推导，保证这两个既有能力不回退。
 */
internal fun officialZhihuColorScheme(isDark: Boolean): ColorScheme =
    if (isDark) officialZhihuDarkColorScheme() else officialZhihuLightColorScheme()

internal fun officialZhihuLightColorScheme(): ColorScheme = lightColorScheme(
    primary = Color(0xFF1772F6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8F1FD),
    onPrimaryContainer = Color(0xFF0A4FB4),
    inversePrimary = Color(0xFF558EFF),
    secondary = Color(0xFF535861),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0F1F3),
    onSecondaryContainer = Color(0xFF373A40),
    tertiary = Color(0xFFFA9D14),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFF3DC),
    onTertiaryContainer = Color(0xFF6E4A0E),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF191B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191B1F),
    surfaceVariant = Color(0xFFF8F8FA),
    onSurfaceVariant = Color(0xFF535861),
    surfaceTint = Color(0xFF1772F6),
    inverseSurface = Color(0xFF2E3138),
    inverseOnSurface = Color(0xFFF5F5F5),
    error = Color(0xFFF1403C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFDE9E9),
    onErrorContainer = Color(0xFF8C1D24),
    outline = Color(0xFFC4C7CE),
    outlineVariant = Color(0xFFEBECED),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFDCDCDC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F8FA),
    surfaceContainer = Color(0xFFF8F8FA),
    surfaceContainerHigh = Color(0xFFF0F1F3),
    surfaceContainerHighest = Color(0xFFEBECED),
)

internal fun officialZhihuDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = Color(0xFF558EFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF15335F),
    onPrimaryContainer = Color(0xFFC9DEFA),
    inversePrimary = Color(0xFF1772F6),
    secondary = Color(0xFFC2C6CF),
    onSecondary = Color(0xFF20242B),
    secondaryContainer = Color(0xFF282B30),
    onSecondaryContainer = Color(0xFFDDE2EA),
    tertiary = Color(0xFFFA9D14),
    onTertiary = Color(0xFF20242B),
    tertiaryContainer = Color(0xFF57451D),
    onTertiaryContainer = Color(0xFFF6DFAF),
    background = Color(0xFF191B1F),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF191B1F),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF212429),
    onSurfaceVariant = Color(0xFFC2C6CF),
    surfaceTint = Color(0xFF558EFF),
    inverseSurface = Color(0xFFEDEDED),
    inverseOnSurface = Color(0xFF2A2A2A),
    error = Color(0xFFF1403C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF5C1A18),
    onErrorContainer = Color(0xFFF9DEDC),
    outline = Color(0xFF3A3E45),
    outlineVariant = Color(0xFF282B30),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF2E3138),
    surfaceDim = Color(0xFF121418),
    surfaceContainerLowest = Color(0xFF191B1F),
    surfaceContainerLow = Color(0xFF212429),
    surfaceContainer = Color(0xFF212429),
    surfaceContainerHigh = Color(0xFF282B30),
    surfaceContainerHighest = Color(0xFF2E3138),
)
