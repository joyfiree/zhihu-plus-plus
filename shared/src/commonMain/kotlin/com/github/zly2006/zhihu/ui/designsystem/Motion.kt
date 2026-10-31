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

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * MD3 动效 token（计划文档 4.4 节）。
 *
 * 迁移规则：
 * - `tween(200)` → [fadeTween] / `tween(Motion.DURATION_SHORT, easing = Motion.StandardEasing)`
 * - `tween(300)`（进入）→ [enterTween]；退出用 [exitTween]
 * - overscroll、回弹、列表位移一律用 [springMedium] / [springSnappy]，不用 tween
 *
 * 组件里不要再写字面量时长或自定义 `CubicBezierEasing`；确需特殊档位时在调用点注释理由。
 */
object Motion {
    /** 微交互：开关、淡入淡出、图标态切换。 */
    const val DURATION_SHORT = 200

    /** 导航转场、BottomSheet。 */
    const val DURATION_MEDIUM = 300

    /** 全屏转场、大面积形变。 */
    const val DURATION_LONG = 450

    /** MD3 强调曲线：大范围位移的首选。 */
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** 强调减速：元素进入。 */
    val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** 强调加速：元素退出。 */
    val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** 标准曲线：不强调方向的常态过渡。 */
    val StandardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** 列表 item 位移、可见性切换：柔和、无回弹。 */
    fun <T> springMedium(): SpringSpec<T> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = null,
    )

    /** 快速回弹、overscroll 归位：比 [springMedium] 更快收敛。 */
    fun <T> springSnappy(): SpringSpec<T> = spring(
        stiffness = Spring.StiffnessMedium,
        visibilityThreshold = null,
    )

    /** 进入动画：中时长 + 强调减速。 */
    fun <T> enterTween(): TweenSpec<T> = tween(
        durationMillis = DURATION_MEDIUM,
        easing = EmphasizedDecelerateEasing,
    )

    /** 退出动画：中时长 + 强调加速。 */
    fun <T> exitTween(): TweenSpec<T> = tween(
        durationMillis = DURATION_MEDIUM,
        easing = EmphasizedAccelerateEasing,
    )

    /** 淡入淡出等微交互。 */
    fun <T> fadeTween(): TweenSpec<T> = tween(
        durationMillis = DURATION_SHORT,
        easing = StandardEasing,
    )

    /**
     * overscroll 归位弹簧。
     *
     * 带轻微过冲（MediumBouncy），让用户在阈值附近回拉时能感到「已经拉过头」，
     * 与 `gestureThreshold` 触觉同帧触发；stiffness 用 Medium，避免回弹拖沓。
     */
    fun <T> overscrollSettle(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}
