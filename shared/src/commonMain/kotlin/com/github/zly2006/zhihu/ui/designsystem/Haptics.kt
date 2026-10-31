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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.github.zly2006.zhihu.platform.rememberSettingsStore

/**
 * 触觉反馈总开关的偏好键（计划文档 T4.1）。
 *
 * 默认开启；关闭后 [HapticController] 的全部语义函数静默。
 * 图片查看器内部用 `NoopHapticFeedback` 屏蔽的那处震动不属于本开关管辖（见 audit 白名单）。
 */
const val HAPTIC_FEEDBACK_ENABLED_PREFERENCE_KEY = "hapticFeedbackEnabled"

/**
 * 统一的触觉反馈出口（计划文档 4.5 节）。
 *
 * 四级语义与 MD3 触觉规范对应：
 * - [confirm]：确认性操作成功（点赞、收藏、开关切换、复制成功）
 * - [tick]：轻量档位变化（底栏 tab 切换、Pager 切页、字号步进）
 * - [gestureThreshold]：手势越阈（回答切换到达阈值、下拉刷新触发）
 * - [longPress]：长按菜单（评论/正文/图片）
 *
 * 使用原则：**克制**。每个触点只震一次，连续滚动手势不重复震动，
 * 震动与对应的动画确认同帧触发。不要绕过这个类直接调 `performHapticFeedback`。
 *
 * 关于 [tick]：MD3 期望更轻的 segment tick，但该常量在跨平台 Compose 上并非所有目标版本都提供，
 * 因此统一回退到 `Confirm`（计划文档 4.5 节允许的低版本回退策略），避免平台分支。
 */
@Stable
class HapticController internal constructor(
    private val hapticFeedback: HapticFeedback,
    private val isEnabled: () -> Boolean,
) {
    fun confirm() = perform(HapticFeedbackType.Confirm)

    fun tick() = perform(HapticFeedbackType.Confirm)

    fun gestureThreshold() = perform(HapticFeedbackType.LongPress)

    fun longPress() = perform(HapticFeedbackType.LongPress)

    private fun perform(type: HapticFeedbackType) {
        if (isEnabled()) {
            hapticFeedback.performHapticFeedback(type)
        }
    }
}

/**
 * 取得当前组合的触觉控制器。
 *
 * 开关状态在**触发瞬间**读取，因此关闭开关后无需重组即可立即静默；
 * 控制器本身在组合内保持稳定，可以安全地捕获进 `onClick` 闭包。
 */
@Composable
fun rememberHapticController(): HapticController {
    val hapticFeedback = LocalHapticFeedback.current
    val settings = rememberSettingsStore()
    return remember(hapticFeedback, settings) {
        HapticController(hapticFeedback) {
            settings.getBoolean(HAPTIC_FEEDBACK_ENABLED_PREFERENCE_KEY, true)
        }
    }
}
