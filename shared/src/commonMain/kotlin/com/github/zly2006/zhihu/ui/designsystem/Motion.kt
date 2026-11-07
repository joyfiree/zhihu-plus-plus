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
     * 计数值滚动（赞同数、关注数、评论数）。
     *
     * 比 [fadeTween] 长 20ms 的原因：数字只有 1～4 位，220ms 才能让「+1」被眼睛捕捉到，
     * 又不会长到和药丸缩放（spring）脱节；曲线用标准曲线，末段收敛平缓，避免数字「急停」。
     */
    fun <T> countTween(): TweenSpec<T> = tween(
        durationMillis = 220,
        easing = StandardEasing,
    )

    /**
     * 图片解码完成后的淡入。
     *
     * 240ms 是「明显在过渡但不拖沓」的经验值：短于 150ms 会像硬切，
     * 长于 300ms 会让连续滚动的信息流产生落后感。
     */
    fun <T> imageFadeTween(): TweenSpec<T> = tween(
        durationMillis = 240,
        easing = StandardEasing,
    )

    /** 二元状态回弹的「放大」段：短、快、只用减速曲线，让反馈在按下后半秒内完成。 */
    fun <T> bouncePopTween(): TweenSpec<T> = tween(
        durationMillis = 110,
        easing = EmphasizedDecelerateEasing,
    )

    /** 二元状态回弹的「回落」段：带轻过冲，收尾干净。 */
    fun <T> bounceSettle(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /**
     * 手势相关的物理常量（阻尼、阈值、吸附）。
     *
     * 收敛在这里的目的：回答切换的「拉多远算切换」和下拉刷新的「拉多远算刷新」
     * 必须是同一套物理直觉，否则用户会在两个手势之间学到互相矛盾的阈值。
     * 单位统一为 dp / 无量纲系数，调用点自己按屏幕密度换算；语义由 `answerSwitchSensitivity` 线性缩放。
     */
    object Gesture {
        /** 单次手势的最大可视位移（dp）。tanh 会继续压缩更大的拖动，避免整页被拖走。 */
        const val OVERSCROLL_MAX_DP = 200f

        /** 触发「松手切换回答」的位移阈值（dp），实际阈值再按 `answerSwitchSensitivity` 线性缩放。 */
        const val OVERSCROLL_TRIGGER_DP = 80f

        /** tanh 阻尼系数：越大位移增长越慢、手感越「拉不动」。 */
        const val OVERSCROLL_DAMPING = 1.2f

        /** 横向手势的最大可视位移（dp）。水平方向可拖动空间更大，取比纵向更宽的档位。 */
        const val HORIZONTAL_OVERSCROLL_MAX_DP = 300f

        /** 横向触发切换的位移阈值（dp），比纵向更短：横滑的物理预期是「轻轻一甩就换」。 */
        const val HORIZONTAL_OVERSCROLL_TRIGGER_DP = 46f

        /** 横向 tanh 阻尼系数。 */
        const val HORIZONTAL_OVERSCROLL_DAMPING = 1.2f

        /** 二元状态回弹的峰值缩放：官方风格的克制反馈，只放大一成出头。 */
        const val BOUNCE_POP_SCALE = 1.12f

        /** 卡片 / 磁贴按下时的缩放。 */
        const val PRESSED_SCALE = 0.975f

        /** 筛选 chip 选中态缩放：只做 4%，再多就会顶动相邻 chip。 */
        const val CHIP_SELECTED_SCALE = 1.04f

        /** 底栏自动隐藏/出现的位移阈值（px）；小于它视为手指轻微抖动。 */
        const val BOTTOM_BAR_SCROLL_THRESHOLD_PX = 12f

        /**
         * 底栏状态切换后的静默期：这段时间内的反向**触摸**滚动不翻转可见性，避免快速上下滑时闪烁。
         * 翻页按钮/键盘翻页（`NestedScrollSource.SideEffect` 合成滚动）与惯性不在此列：
         * 它们是离散意图，被拦会让「翻页时自动隐藏底栏」失效。
         */
        const val BOTTOM_BAR_SETTLE_MILLIS = 300L
    }

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
