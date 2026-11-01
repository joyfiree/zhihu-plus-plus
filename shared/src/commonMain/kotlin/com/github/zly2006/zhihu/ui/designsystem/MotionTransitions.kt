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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * 导航共享轴（Shared Axis X）转场。
 *
 * 意图：让「进入下一层」和「回到上一层」在空间上可解释——新页面从右侧进入、
 * 旧页面同时左移 1/4 并淡出，两层一起动，形成纵深而不是两个独立动画拼接。
 *
 * 参数理由：
 * - 位移用 `EmphasizedDecelerate`（进）/ `EmphasizedAccelerate`（出），这是 MD3 对「有方向的位移」的指定曲线；
 * - 淡入淡出与位移共用同一个 tween 时长，两者同时结束。若淡入更短，页面会「先透明地滑一段再显形」，观感是撕裂的；
 * - 旧页只走 1/4 屏而不是整屏：整屏位移会让用户以为列表被整块替换，1/4 保留了「它还在左边」的空间记忆。
 *
 * 可中断性：转场全部由 `AnimatedContentTransitionScope` 驱动，进入后立刻返回时目标值反转，
 * Compose 会让当前动画从**当前进度**反向，而不是跳到起点重播。
 */
fun AnimatedContentTransitionScope<*>.sharedAxisXEnter(): EnterTransition =
    slideInHorizontally(Motion.enterTween()) { width -> width } +
        fadeIn(Motion.enterTween())

/** 旧页面在被覆盖时的退出：左移 1/4 + 淡出。 */
fun AnimatedContentTransitionScope<*>.sharedAxisXExit(): ExitTransition =
    slideOutHorizontally(Motion.exitTween()) { width -> -width / 4 } +
        fadeOut(Motion.exitTween())

/** 返回时被恢复的页面：从左侧 1/4 处回到原位。 */
fun AnimatedContentTransitionScope<*>.sharedAxisXPopEnter(): EnterTransition =
    slideInHorizontally(Motion.enterTween()) { width -> -width / 4 } +
        fadeIn(Motion.enterTween())

/** 返回时被弹出的页面：向右整屏移出。 */
fun AnimatedContentTransitionScope<*>.sharedAxisXPopExit(): ExitTransition =
    slideOutHorizontally(Motion.exitTween()) { width -> width } +
        fadeOut(Motion.exitTween())
