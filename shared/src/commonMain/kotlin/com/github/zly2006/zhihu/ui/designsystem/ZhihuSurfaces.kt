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
 * 页面与容器的底色分工（对标知乎官方 11.14 的「同色底 + 功能面」语言）。
 *
 * 官方首页/回答页是**扁平**的：页面与卡片同色（浅色 `#FFFFFF` / 深色 `#191B1F`），
 * 靠间距和发丝线分块，不再有「亮卡浮在灰底上」的浮层感；分组页面（设置、账号）
 * 仍需要一层比页面略强的容器色，因此 [cardStrong] 取 `surfaceContainer`。
 */
@Immutable
data class ZhihuSurfaceColors(
    /** 页面底色。 */
    val page: Color,
    /** 常规卡片（信息流、通知行、设置行）。 */
    val card: Color,
    /** 需要更强调的容器（设置分组背景、内嵌容器）。 */
    val cardStrong: Color,
)

internal val LocalZhihuSurfaceColors = staticCompositionLocalOf {
    ZhihuSurfaceColors(Color.Unspecified, Color.Unspecified, Color.Unspecified)
}

val zhihuSurfaces: ZhihuSurfaceColors
    @Composable
    @ReadOnlyComposable
    get() = LocalZhihuSurfaceColors.current

internal fun zhihuSurfaceColorsFor(scheme: ColorScheme, darkTheme: Boolean): ZhihuSurfaceColors {
    // 首页/回答页与官方一致：page 与 card 同色；分组页用 cardStrong 做容器。
    return ZhihuSurfaceColors(
        page = scheme.background,
        card = scheme.surface,
        cardStrong = scheme.surfaceContainer,
    )
}
