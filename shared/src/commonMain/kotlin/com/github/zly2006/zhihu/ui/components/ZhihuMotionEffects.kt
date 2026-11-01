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

package com.github.zly2006.zhihu.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.github.zly2006.zhihu.ui.designsystem.Motion

/**
 * 状态切换时的「弹一下」反馈。
 *
 * 用于点赞、收藏这类二元状态：选中时先快速放大到 1.28x，再用带阻尼的 spring 回落到 1x。
 * 相比把 scale 直接绑到状态上（会一直停在放大态），这种一次性回弹更接近系统级反馈。
 *
 * 意图：确认「这一下点到了」，而不是持续强调。参数见 [Motion.bouncePopTween] / [Motion.bounceSettle]。
 *
 * @param trigger 触发键，值变化即播放一次；`null` / `false` 不播放。
 */
@Composable
fun BounceOnChange(
    trigger: Any?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(trigger) {
        if (trigger == null || trigger == false) return@LaunchedEffect
        scale.animateTo(
            targetValue = Motion.Gesture.BOUNCE_POP_SCALE,
            animationSpec = Motion.bouncePopTween(),
        )
        // 连点时 animateTo 会被打断并从上一次被打断的进度继续，因此不会出现「跳回 1x 再放大」的抖动。
        scale.animateTo(targetValue = 1f, animationSpec = Motion.bounceSettle())
    }
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    ) {
        content()
    }
}

/**
 * 计数值变化时的滚动数字。
 *
 * 意图：数字从 12 变 13 时如果直接替换，用户只会看到「闪了一下」；滚过去能让人确认「是我这一下造成的」。
 * 只在数值真正变化时播放（220ms），滚动期间不占用组合期帧预算——`Text` 的重组范围仅限这个数字。
 */
@Composable
fun animatedCountLabel(count: Int): Int {
    val animated by animateIntAsState(
        targetValue = count,
        animationSpec = Motion.countTween(),
        label = "animated_count",
    )
    return animated
}

/**
 * 统一选中态动效的筛选 chip。
 *
 * 意图：一排 chip 里「哪个是选中的」必须在一眼内被识别，所以选中瞬间做 4% 放大 + 容器色过渡。
 * 参数：
 * - 缩放 [Motion.Gesture.CHIP_SELECTED_SCALE]（1.04）——再多会顶动相邻 chip，造成整行抖动；
 * - 曲线 `springSnappy`（无过冲），避免 chip 在弹性收尾时反复晃动；
 * - 颜色走 200ms 标准曲线，与容器色梯度（surfaceContainerLow → secondaryContainer）配合，
 *   让「选中」是一条连续的色阶而不是硬切。
 *
 * 注意：这里同时把 `containerColor` 与 `selectedContainerColor` 赋成同一个动画值，
 * 让 M3 内部的选中色动画失效，从而由本组件统一控制曲线，避免双重动画互相拉扯。
 */
@Composable
fun ZhihuFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) Motion.Gesture.CHIP_SELECTED_SCALE else 1f,
        animationSpec = Motion.springSnappy(),
        label = "chip_selected_scale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = Motion.fadeTween(),
        label = "chip_container",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = Motion.fadeTween(),
        label = "chip_label",
    )
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
        leadingIcon = leadingIcon,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            selectedContainerColor = containerColor,
            labelColor = labelColor,
            selectedLabelColor = labelColor,
        ),
    )
}

/**
 * 选中态可缩放的 SuggestionChip。
 *
 * 与 [ZhihuFilterChip] 的分工：本组件保留 SuggestionChip「无填充 + 文字着色」的轻量外观，
 * 只补上选中态 1.04x 缩放（[Motion.Gesture.CHIP_SELECTED_SCALE]）；文字颜色由调用方用
 * `animateColorAsState` 提供，避免统一成药丸填充后改变既有视觉语言。
 *
 * 缩放写在 `graphicsLayer` 里，只影响绘制、不占布局，因此不会引起相邻 chip 位移。
 */
@Composable
fun ZhihuSuggestionChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) Motion.Gesture.CHIP_SELECTED_SCALE else 1f,
        animationSpec = Motion.springSnappy(),
        label = "suggestion_chip_selected_scale",
    )
    SuggestionChip(
        onClick = onClick,
        label = label,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        enabled = enabled,
    )
}
