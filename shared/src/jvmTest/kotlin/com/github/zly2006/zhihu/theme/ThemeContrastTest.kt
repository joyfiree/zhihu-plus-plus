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

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 主题对比度回归（计划文档 T1.4）。
 *
 * 关注点是**自定义背景色**这条路径：`buildZhihuColorScheme` 会把用户选的颜色平移到
 * 整套 surface 阶梯上，一旦平移把前景色和背景色压到同一明度，正文就会不可读。
 * 这里用 WCAG 2.1 对比度公式对典型背景色做断言，避免以后调整推导系数时静默劣化。
 *
 * 阈值：正文/标题 ≥ 4.5:1，辅助文本 ≥ 3:1（与计划 4.1 节一致）。
 */
class ThemeContrastTest {
    private val lightReference = lightColorScheme()
    private val darkReference = darkColorScheme()

    private val lightBackgrounds = listOf(
        "纯白" to Color(0xFFFFFFFF),
        "知乎蓝" to Color(0xFF2196F3),
        "米黄" to Color(0xFFF5F1E8),
        "浅灰" to Color(0xFFEEEEEE),
    )

    private val darkBackgrounds = listOf(
        "纯黑" to Color(0xFF000000),
        "深灰" to Color(0xFF121212),
        "深蓝" to Color(0xFF102A43),
    )

    private fun contrast(foreground: Color, background: Color): Float {
        val first = max(foreground.luminance(), background.luminance())
        val second = min(foreground.luminance(), background.luminance())
        return (first + 0.05f) / (second + 0.05f)
    }

    private fun assertContrast(
        minimum: Float,
        foreground: Color,
        background: Color,
        label: String,
    ) {
        val ratio = contrast(foreground, background)
        assertTrue(ratio >= minimum, "$label 对比度 $ratio，低于 $minimum")
    }

    @Test
    fun lightBackgroundsKeepBodyTextReadable() {
        for ((name, background) in lightBackgrounds) {
            val scheme = buildZhihuColorScheme(
                base = lightReference,
                lightReference = lightReference,
                darkReference = darkReference,
                background = background,
                darkTheme = false,
            )
            assertContrast(4.5f, scheme.onSurface, scheme.surface, "浅色/$name onSurface")
            assertContrast(4.5f, scheme.onBackground, scheme.background, "浅色/$name onBackground")
            assertContrast(3.0f, scheme.onSurfaceVariant, scheme.surface, "浅色/$name onSurfaceVariant")
        }
    }

    @Test
    fun darkBackgroundsKeepBodyTextReadable() {
        for ((name, background) in darkBackgrounds) {
            val scheme = buildZhihuColorScheme(
                base = darkReference,
                lightReference = lightReference,
                darkReference = darkReference,
                background = background,
                darkTheme = true,
            )
            assertContrast(4.5f, scheme.onSurface, scheme.surface, "深色/$name onSurface")
            assertContrast(4.5f, scheme.onBackground, scheme.background, "深色/$name onBackground")
            assertContrast(3.0f, scheme.onSurfaceVariant, scheme.surface, "深色/$name onSurfaceVariant")
        }
    }

    @Test
    fun surfaceLadderStaysOrdered() {
        for ((name, background) in lightBackgrounds + darkBackgrounds) {
            val base = if (background.luminance() < 0.5f) darkReference else lightReference
            val scheme = buildZhihuColorScheme(
                base = base,
                lightReference = lightReference,
                darkReference = darkReference,
                background = background,
                darkTheme = base === darkReference,
            )
            val ladder = listOf(
                scheme.surfaceContainerLowest,
                scheme.surfaceContainerLow,
                scheme.surfaceContainer,
                scheme.surfaceContainerHigh,
                scheme.surfaceContainerHighest,
            ).map { it.luminance() }
            val ascending = ladder.zipWithNext().all { (a, b) -> b >= a - 0.001f }
            val descending = ladder.zipWithNext().all { (a, b) -> b <= a + 0.001f }
            assertTrue(ascending || descending, "背景/$name 的 surface 阶梯明度不单调：$ladder")
        }
    }
}
