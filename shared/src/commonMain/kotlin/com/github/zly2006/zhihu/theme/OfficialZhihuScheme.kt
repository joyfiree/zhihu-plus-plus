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
 * 官方 App 的观感来自一组固定值，而不是任何种子色推导算法：
 * - 浅色：`#F6F6F6` 灰底 + 纯白卡片 + `#056DE8` 品牌蓝 + 近黑正文；
 * - 深色：`#121212` 底 + `#1E1E1E` 卡片 + 提亮一档的蓝用于文字/链接；
 * - 次要文本是带蓝调的灰（`#76808F`），比官方略深以保证在灰底上仍有 3:1 对比度；
 * - 发丝分割线 `#EBEBEB`，输入框描边 `#C9CDD4`。
 *
 * 只有当种子色等于 [ZhihuSeedColor]（用户没有自定义主题色）时才使用这套手调值；
 * 自定义种子 / 动态取色仍走 material-kolor 推导，保证这两个既有能力不回退。
 */
internal fun officialZhihuColorScheme(isDark: Boolean): ColorScheme =
    if (isDark) officialZhihuDarkColorScheme() else officialZhihuLightColorScheme()

internal fun officialZhihuLightColorScheme(): ColorScheme = lightColorScheme(
    primary = Color(0xFF056DE8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8F1FD),
    onPrimaryContainer = Color(0xFF0A4FB4),
    inversePrimary = Color(0xFF4E87EE),
    secondary = Color(0xFF5A6472),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0F2F5),
    onSecondaryContainer = Color(0xFF3D4756),
    tertiary = Color(0xFFB5831F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFF3DC),
    onTertiaryContainer = Color(0xFF6E4A0E),
    background = Color(0xFFF6F6F6),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0F1F3),
    onSurfaceVariant = Color(0xFF76808F),
    surfaceTint = Color(0xFF056DE8),
    inverseSurface = Color(0xFF2E3138),
    inverseOnSurface = Color(0xFFF5F5F5),
    error = Color(0xFFF04142),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFDE9E9),
    onErrorContainer = Color(0xFF8C1D24),
    outline = Color(0xFFC9CDD4),
    outlineVariant = Color(0xFFEBEBEB),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFDCDCDC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainer = Color(0xFFF6F6F6),
    surfaceContainerHigh = Color(0xFFEFEFEF),
    surfaceContainerHighest = Color(0xFFE9EAEC),
)

internal fun officialZhihuDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = Color(0xFF4E87EE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF15335F),
    onPrimaryContainer = Color(0xFFC9DEFA),
    inversePrimary = Color(0xFF056DE8),
    secondary = Color(0xFFA9B2C0),
    onSecondary = Color(0xFF20242B),
    secondaryContainer = Color(0xFF343A44),
    onSecondaryContainer = Color(0xFFDDE2EA),
    tertiary = Color(0xFFD9BC8A),
    onTertiary = Color(0xFF3A2E10),
    tertiaryContainer = Color(0xFF57451D),
    onTertiaryContainer = Color(0xFFF6DFAF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFEDEDED),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF26282B),
    onSurfaceVariant = Color(0xFF9AA3B2),
    surfaceTint = Color(0xFF4E87EE),
    inverseSurface = Color(0xFFEDEDED),
    inverseOnSurface = Color(0xFF2A2A2A),
    error = Color(0xFFF0625D),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF5C1A18),
    onErrorContainer = Color(0xFFF9DEDC),
    outline = Color(0xFF5A6069),
    outlineVariant = Color(0xFF33363B),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF3F3F45),
    surfaceDim = Color(0xFF0E0E0E),
    surfaceContainerLowest = Color(0xFF121212),
    surfaceContainerLow = Color(0xFF1E1E1E),
    surfaceContainer = Color(0xFF232327),
    surfaceContainerHigh = Color(0xFF2A2A2E),
    surfaceContainerHighest = Color(0xFF313136),
)
