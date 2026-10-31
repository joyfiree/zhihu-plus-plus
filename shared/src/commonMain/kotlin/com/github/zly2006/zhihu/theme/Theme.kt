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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.github.zly2006.zhihu.ui.designsystem.LocalExtendedColors
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapes
import com.github.zly2006.zhihu.ui.designsystem.extendedColorsFor
import com.materialkolor.dynamicColorScheme

/**
 * 应用主题入口。
 *
 * 配色来源有三档，优先级从高到低：
 * 1. 系统动态取色（Android 12+，且用户开启「使用 Material You 动态取色」）；
 * 2. 用户自选种子色推导出的完整 M3 配色；
 * 3. 默认知乎蓝种子色。
 *
 * 三档最终都会经过 [buildZhihuColorScheme]，把「自定义背景色」映射到整套表面阶梯，
 * 因此无论走哪一档，`surface` / `surfaceContainer*` / `onSurface` 的层级关系都是一致的。
 *
 * 主题同时提供：
 * - [ZhihuShapes]：M3 五档圆角；
 * - [Typography]：针对中文调整过行高与字距的 M3 排版；
 * - [LocalExtendedColors]：赞同、收藏、朗读等 M3 没有覆盖的功能强调色。
 */
@Composable
fun ZhihuTheme(content: @Composable () -> Unit) {
    val useDynamicColor = ThemeManager.getUseDynamicColor()
    val customBackgroundColor = ThemeManager.getBackgroundColor()
    val seedColor = ThemeManager.getCustomColor()
    val darkTheme = ThemeManager.isDarkTheme()
    val platformScheme = platformDynamicColorScheme(darkTheme)

    val baseScheme = if (useDynamicColor && platformScheme != null) {
        platformScheme
    } else {
        dynamicColorScheme(seedColor = seedColor, isDark = darkTheme, isAmoled = false)
    }
    // 参考配色只用于两件事：取表面阶梯的相对明度差，以及在背景明暗与主题相反时提供同极性前景色。
    val lightReference = dynamicColorScheme(seedColor = seedColor, isDark = false, isAmoled = false)
    val darkReference = dynamicColorScheme(seedColor = seedColor, isDark = true, isAmoled = false)

    val colorScheme = remember(baseScheme, lightReference, darkReference, customBackgroundColor, darkTheme) {
        buildZhihuColorScheme(
            base = baseScheme,
            lightReference = lightReference,
            darkReference = darkReference,
            background = customBackgroundColor,
            darkTheme = darkTheme,
        )
    }
    // 功能强调色由种子色 harmonize 推导；直接调用（而非 remember）以兼容其可能的 @Composable 实现。
    val extended = extendedColorsFor(colorScheme)

    PlatformSystemBarEffect(darkTheme)

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = ZhihuShapes,
            content = content,
        )
    }
}

@Composable
expect fun currentSystemInDarkTheme(): Boolean

@Composable
expect fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
expect fun PlatformSystemBarEffect(darkTheme: Boolean)
