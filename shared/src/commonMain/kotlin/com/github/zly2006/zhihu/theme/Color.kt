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

import androidx.compose.ui.graphics.Color

/**
 * 主题种子色（知乎官方品牌蓝）。
 *
 * 这是**唯一**允许出现的主题色字面量：整套 `ColorScheme` 由它推导，页面与组件里不得再写
 * `Color(0xFF...)`。`ThemeManager` 的初始值也引用它，保证「内存默认值」和「持久化默认值」
 * 只有一个来源。
 *
 * 当种子色等于它（即用户没有自定义主题色）时，`ZhihuTheme` 直接使用
 * `OfficialZhihuScheme.kt` 里的手调官方配色，而不是走种子推导；
 * 用户换种子色后仍回到 material-kolor 推导路径。
 */
val ZhihuSeedColor = Color(0xFF056DE8)

/**
 * 功能强调色的种子。
 *
 * 这些颜色只描述内容语义，不参与 `ColorScheme`；实际取值由
 * `ExtendedColors.kt` 用种子色 `harmonize` 推导，深色主题再提亮。
 */
internal object ZhihuBrandPalette {
    /** 赞同 / 喜欢：官方赞同蓝，与 [ZhihuSeedColor] 同源。 */
    val Like = Color(0xFF056DE8)

    /** 收藏：官方收藏星标的金黄色。 */
    val Favorite = Color(0xFFF5A623)

    /** 朗读进行中。 */
    val Tts = Color(0xFF4CAF50)
}
