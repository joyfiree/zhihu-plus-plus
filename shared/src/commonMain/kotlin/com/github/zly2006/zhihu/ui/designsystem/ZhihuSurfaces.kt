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

/**
 * 页面与卡片的底色配对（对标知乎官方「灰底白卡」）。
 *
 * M3 的角色里 `surface` 比 `surfaceContainer*` 更亮，直接把卡片放在 `surface` 上、
 * 页面用 `surfaceContainer` 才能得到「白卡浮在灰底上」的观感；
 * 早期实现是反过来的（页面最亮、卡片更灰），所以卡片看着像灰块而不是卡片。
 *
 * 深色模式反过来：页面用最暗层，卡片比页面亮一档。
 */
@Immutable
data class ZhihuSurfaceColors(
    /** 页面底色。 */
    val page: Color,
    /** 常规卡片（信息流、设置分组、通知行）。 */
    val card: Color,
    /** 需要更强调的卡片（选中态、内嵌容器）。 */
    val cardStrong: Color,
)

internal val LocalZhihuSurfaceColors = staticCompositionLocalOf {
    ZhihuSurfaceColors(Color.Unspecified, Color.Unspecified, Color.Unspecified)
}

val zhihuSurfaces: ZhihuSurfaceColors
    @Composable
    @ReadOnlyComposable
    get() = LocalZhihuSurfaceColors.current

internal fun zhihuSurfaceColorsFor(scheme: ColorScheme, darkTheme: Boolean): ZhihuSurfaceColors =
    if (darkTheme) {
        ZhihuSurfaceColors(
            page = scheme.surfaceContainerLowest,
            card = scheme.surfaceContainerLow,
            cardStrong = scheme.surfaceContainerHigh,
        )
    } else {
        // 浅色下 surfaceContainerLow 被夹在「纯白 surface」与「灰底 page」之间，只能是近白
        // （surface 阶梯要保持明度单调）；强调卡用更灰一档的 High，才能在白卡上读出层次。
        ZhihuSurfaceColors(
            page = scheme.surfaceContainer,
            card = scheme.surface,
            cardStrong = scheme.surfaceContainerHigh,
        )
    }
