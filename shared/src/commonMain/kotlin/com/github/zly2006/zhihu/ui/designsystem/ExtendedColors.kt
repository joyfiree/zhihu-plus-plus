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

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.github.zly2006.zhihu.theme.ZhihuBrandPalette
import com.materialkolor.ktx.harmonize

/**
 * MD3 之外的功能强调色（计划文档 4.1 节）。
 *
 * 这些颜色承载的是**内容语义**（赞同蓝、收藏橙），`ColorScheme` 没有对应角色。
 * 它们不再以字面量散落在页面里，而是由种子色 `harmonize` 推导后统一从这里取，
 * 保证换主题色后仍然协调，同时保留「点赞是蓝、收藏是橙」的既有产品认知。
 */
@Immutable
data class ExtendedColors(
    /** 赞同 / 喜欢（正文操作栏、双击点赞）。 */
    val like: Color,
    /** 收藏态（收藏按钮、收藏夹列表）。 */
    val favorite: Color,
)

private val DefaultExtendedColors = ExtendedColors(
    like = Color(0xFF3671EE),
    favorite = Color(0xFFF57C00),
)

internal val LocalExtendedColors = staticCompositionLocalOf { DefaultExtendedColors }

/** 当前主题的功能强调色。只能在 `@Composable` 作用域读取。 */
val extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

/**
 * 由当前配色推导功能强调色。
 *
 * `harmonize` 把品牌色向主题 primary 靠拢（保留色相辨识度的同时降低冲突），
 * 深色主题下再提亮一档，保证在深色 surface 上仍有足够对比度。
 */
internal fun extendedColorsFor(scheme: ColorScheme): ExtendedColors {
    val dark = scheme.surface.luminance() < 0.5f
    val like = ZhihuBrandPalette.Like.harmonize(scheme.primary).let { if (dark) it.lighten(0.12f) else it }
    val favorite = ZhihuBrandPalette.Favorite.harmonize(scheme.primary).let { if (dark) it.lighten(0.10f) else it }
    return ExtendedColors(
        like = like,
        favorite = favorite,
    )
}
