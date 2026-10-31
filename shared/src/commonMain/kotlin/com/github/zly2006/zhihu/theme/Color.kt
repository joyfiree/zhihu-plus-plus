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
 * 主题种子色（计划文档附录 A 白名单第 1 项）。
 *
 * 这是**唯一**允许出现的主题色字面量：整套 `ColorScheme` 由它推导，
 * 页面与组件里不得再写 `Color(0xFF...)`。`ThemeManager` 的初始值也引用它，
 * 保证「内存默认值」和「持久化默认值」只有一个来源。
 */
val ZhihuSeedColor = Color(0xFF2196F3)

/**
 * 功能强调色的种子（计划文档 4.1 节）。
 *
 * 这些颜色只描述内容语义，不参与 `ColorScheme`；实际取值由
 * `ExtendedColors.kt` 用种子色 `harmonize` 推导，深色主题再提亮。
 */
internal object ZhihuBrandPalette {
    /** 赞同 / 喜欢。 */
    val Like = Color(0xFF3671EE)

    /** 收藏。 */
    val Favorite = Color(0xFFF57C00)

    /** 朗读进行中。 */
    val Tts = Color(0xFF4CAF50)
}
