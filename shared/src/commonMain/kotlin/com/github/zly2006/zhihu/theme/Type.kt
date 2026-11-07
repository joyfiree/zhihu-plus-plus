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

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material Design 3 排版体系。
 *
 * 这里保留 M3 的 15 个语义角色（display/headline/title/body/label × 大中小），
 * 但与 Google 的拉丁字母默认值相比做了两点针对中文阅读的调整：
 *
 * 1. **行高更大**。汉字没有西文的下伸部，M3 默认行高在中文段落里会显得拥挤；
 *    这里把正文行高按 1.55～1.6 倍字号取值，标题按 1.35 倍取值。
 * 2. **字距归零**。M3 默认的正字距是为拉丁字母设计的，中文逐字等宽，额外的字距只会让文字散开。
 *
 * 页面不要再直接写 `fontSize = 14.sp` 这类硬编码，应该选一个语义角色，
 * 让字号、行高、字重和字距在全局保持一致。
 */
private val Default = FontFamily.Default

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontFamily = Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

val Typography = Typography(
    // 展示级：仅用于空状态标题、引导页等大字号场景。
    displayLarge = style(57, 64),
    displayMedium = style(45, 52),
    displaySmall = style(36, 44),
    // 标题级：页面大标题、设置分组首页。
    headlineLarge = style(32, 40),
    headlineMedium = style(28, 38),
    headlineSmall = style(24, 34),
    // 小标题：AppBar 标题（官方顶栏约 17-18sp）、卡片标题、设置分组标题。
    titleLarge = style(18, 26, FontWeight.Medium),
    titleMedium = style(16, 24, FontWeight.Medium),
    titleSmall = style(14, 22, FontWeight.Medium),
    // 正文：内容主体。
    bodyLarge = style(16, 26),
    bodyMedium = style(14, 22),
    bodySmall = style(12, 18),
    // 标签：按钮（官方按钮约 15sp）、导航栏、辅助信息。
    labelLarge = style(15, 22, FontWeight.Medium),
    labelMedium = style(12, 18, FontWeight.Medium),
    labelSmall = style(11, 16, FontWeight.Medium),
)
