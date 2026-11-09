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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import com.github.zly2006.zhihu.ui.designsystem.contrastRatio
import com.github.zly2006.zhihu.ui.designsystem.hslColor
import com.github.zly2006.zhihu.ui.designsystem.toHsl

/**
 * 用户自定义背景色的判断。
 *
 * 设置项里的默认值（浅色纯白 / 深色 #121212）视为「没有自定义」，
 * 这时直接使用 M3 推导出的色调阶梯；只有用户真的挑了别的颜色才重算表面色，
 * 否则纯白会把 M3 的 surfaceContainer 阶梯压平，整个界面又退回「白底 + 灰线」。
 */
internal fun isCustomBackground(color: Color, darkTheme: Boolean): Boolean {
    val argb = color.toArgb()
    return if (darkTheme) argb != 0xFF191B1F.toInt() else argb != 0xFFFFFFFF.toInt()
}

/**
 * 把「用户选的背景色」映射成整套 M3 表面阶梯（计划文档 T1.1）。
 *
 * 做法不是简单地把 `surface` 替换成自定义色（那会让所有容器色与背景脱节，
 * 出现白底 + 蓝卡片的割裂感），而是：
 *
 * 1. 从参考配色里取出每个 surface 角色相对 `surface` 的**明度差**；
 * 2. 把这段明度差平移到用户选的背景色上，色相/饱和度保持用户所选；
 * 3. 前景色用 WCAG 对比度兜底：优先沿用参考配色的 onSurface/onSurfaceVariant，
 *    若在所选背景上达不到 4.5:1 / 3:1，则换成黑或白中对比度更高的一侧。
 *
 * 第 2 步只变明度、不变色相，保证 surfaceContainer 阶梯的明度严格单调，
 * 不会出现「容器色越往上越亮又忽然变暗」的层级断裂。
 */
internal fun buildZhihuColorScheme(
    base: ColorScheme,
    lightReference: ColorScheme,
    darkReference: ColorScheme,
    background: Color,
    darkTheme: Boolean,
): ColorScheme {
    if (!isCustomBackground(background, darkTheme)) return base

    val backgroundHsl = background.toHsl()
    // 用「黑/白哪边对比度更高」判定背景极性：品牌蓝这类中明度背景用深色文字可读性最好。
    val prefersDarkForeground =
        contrastRatio(Color.Black, background) >= contrastRatio(Color.White, background)
    val reference = if (prefersDarkForeground) lightReference else darkReference
    // 强调色优先沿用当前主题（可能是系统动态取色），只有在明暗极性被反转时才换参考配色。
    val accent = if (prefersDarkForeground == !darkTheme) base else reference
    val referenceSurfaceLightness = reference.surface.toHsl().lightness
    val strongForeground = if (prefersDarkForeground) Color.Black else Color.White

    fun tonal(role: Color): Color {
        val delta = role.toHsl().lightness - referenceSurfaceLightness
        return hslColor(
            hue = backgroundHsl.hue,
            saturation = backgroundHsl.saturation,
            lightness = (backgroundHsl.lightness + delta).coerceIn(0f, 1f),
        )
    }

    fun foreground(preferred: Color, minimumContrast: Float): Color =
        if (contrastRatio(preferred, background) >= minimumContrast) preferred else strongForeground

    val onSurface = foreground(reference.onSurface, 4.5f)
    val onBackground = foreground(reference.onBackground, 4.5f)
    val secondaryCandidate = onSurface.copy(alpha = SECONDARY_FOREGROUND_ALPHA).compositeOver(background)
    val onSurfaceVariant = if (contrastRatio(secondaryCandidate, background) >= 3f) {
        secondaryCandidate
    } else {
        onSurface
    }

    return accent.copy(
        background = background,
        onBackground = onBackground,
        surface = background,
        onSurface = onSurface,
        surfaceVariant = tonal(reference.surfaceVariant),
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = accent.primary,
        inverseSurface = reference.inverseSurface,
        inverseOnSurface = reference.inverseOnSurface,
        error = accent.error,
        onError = accent.onError,
        errorContainer = accent.errorContainer,
        onErrorContainer = accent.onErrorContainer,
        outline = reference.outline,
        outlineVariant = reference.outlineVariant,
        scrim = reference.scrim,
        surfaceBright = tonal(reference.surfaceBright),
        surfaceDim = tonal(reference.surfaceDim),
        surfaceContainer = tonal(reference.surfaceContainer),
        surfaceContainerHigh = tonal(reference.surfaceContainerHigh),
        surfaceContainerHighest = tonal(reference.surfaceContainerHighest),
        surfaceContainerLow = tonal(reference.surfaceContainerLow),
        surfaceContainerLowest = tonal(reference.surfaceContainerLowest),
    )
}

/** onSurfaceVariant 相对 onSurface 的透明度：既能弱化层级，又能保住 3:1 辅助文本对比度。 */
private const val SECONDARY_FOREGROUND_ALPHA = 0.72f
