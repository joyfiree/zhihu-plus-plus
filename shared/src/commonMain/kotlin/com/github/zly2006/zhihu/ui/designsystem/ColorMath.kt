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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.abs

/**
 * 主题推导用的颜色数学工具。
 *
 * MD3 的 ColorScheme 由 material-kolor 生成，但在两处需要自己做 HSL 运算：
 * 1. 用户自定义背景色时，把 surface 阶梯按相对明度差平移到自定义色上（T1.1）；
 * 2. 功能强调色在深色主题下提亮（T1.2）。
 *
 * 集中在这里避免两个文件各写一份转换代码。
 */
internal data class Hsl(
    val hue: Float,
    val saturation: Float,
    val lightness: Float,
)

internal fun Color.toHsl(): Hsl {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val lightness = (max + min) / 2f
    if (max - min < 1e-6f) return Hsl(0f, 0f, lightness)
    val delta = max - min
    val saturation = if (lightness > 0.5f) {
        delta / (2f - max - min)
    } else {
        delta / (max + min)
    }
    val rawHue = when (max) {
        r -> ((g - b) / delta) % 6f
        g -> ((b - r) / delta) + 2f
        else -> ((r - g) / delta) + 4f
    } * 60f
    val hue = if (rawHue < 0f) rawHue + 360f else rawHue
    return Hsl(hue, saturation.coerceIn(0f, 1f), lightness)
}

internal fun hslColor(hue: Float, saturation: Float, lightness: Float): Color {
    val chroma = (1f - abs(2f * lightness - 1f)) * saturation
    val sector = ((hue % 360f) + 360f) % 360f / 60f
    val second = chroma * (1f - abs(sector % 2f - 1f))
    val (r, g, b) = when {
        sector < 1f -> Triple(chroma, second, 0f)
        sector < 2f -> Triple(second, chroma, 0f)
        sector < 3f -> Triple(0f, chroma, second)
        sector < 4f -> Triple(0f, second, chroma)
        sector < 5f -> Triple(second, 0f, chroma)
        else -> Triple(chroma, 0f, second)
    }
    val match = lightness - chroma / 2f
    return Color(
        red = (r + match).coerceIn(0f, 1f),
        green = (g + match).coerceIn(0f, 1f),
        blue = (b + match).coerceIn(0f, 1f),
    )
}

/**
 * WCAG 2.1 相对对比度（1:1～21:1）。
 *
 * 用来保证自定义背景色路径上的文字可读性：前景色与背景色的亮度比低于阈值时，
 * 主题要把它替换成同极性的强前景色，而不是继续沿用 M3 默认前景。
 */
internal fun contrastRatio(first: Color, second: Color): Float {
    val firstLuminance = first.luminance()
    val secondLuminance = second.luminance()
    val lighter = maxOf(firstLuminance, secondLuminance)
    val darker = minOf(firstLuminance, secondLuminance)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/** 沿最短弧混合两个色相，[weightB] 为第二个色相的权重。 */
internal fun blendHue(hueA: Float, hueB: Float, weightB: Float): Float {
    var diff = hueB - hueA
    if (diff > 180f) diff -= 360f
    if (diff < -180f) diff += 360f
    var result = hueA + diff * weightB
    if (result < 0f) result += 360f
    if (result >= 360f) result -= 360f
    return result
}

internal fun Color.darken(amount: Float): Color {
    val hsl = toHsl()
    return hslColor(hsl.hue, hsl.saturation, (hsl.lightness - amount).coerceIn(0f, 1f))
}

internal fun Color.lighten(amount: Float): Color {
    val hsl = toHsl()
    return hslColor(hsl.hue, hsl.saturation, (hsl.lightness + amount).coerceIn(0f, 1f))
}
