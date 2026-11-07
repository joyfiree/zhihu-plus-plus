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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.github.zly2006.zhihu.ui.designsystem.Motion
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.rememberHapticController
import com.github.zly2006.zhihu.viewmodel.ArticleViewModel.CachedAnswerContent
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.tanh

/**
 * 上下 overscroll 切换回答容器。
 * 当内容滚动到顶部/底部边界后继续滑动，显示预览卡片并提供切换。
 * 预览卡片固定在视口边缘，主内容滑动露出卡片。
 */
@Composable
fun AnswerVerticalOverscroll(
    previousAnswer: CachedAnswerContent?,
    nextAnswer: CachedAnswerContent?,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    isAtTop: () -> Boolean,
    isAtBottom: () -> Boolean,
    scrollState: ScrollState,
    answerSwitchSensitivity: Float = DEFAULT_ANSWER_SWITCH_SENSITIVITY,
    isContentNonScrollable: Boolean = scrollState.maxValue == 0,
    onOverscrollOffsetChange: (Float) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val haptics = rememberHapticController()
    val coroutineScope = rememberCoroutineScope()

    val maxOverscrollPx = with(density) {
        Motion.Gesture.OVERSCROLL_MAX_DP.dp
            .toPx()
    }
    val triggerThresholdPx = with(density) {
        (Motion.Gesture.OVERSCROLL_TRIGGER_DP / normalizedAnswerSwitchSensitivity(answerSwitchSensitivity)).dp.toPx()
    }

    val overscrollOffset = remember { Animatable(0f) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    var rawDragAccumulator by remember { mutableFloatStateOf(0f) }
    val currentOnOverscrollOffsetChange by rememberUpdatedState(onOverscrollOffsetChange)

    LaunchedEffect(overscrollOffset) {
        snapshotFlow { overscrollOffset.value }
            .collect { currentOnOverscrollOffsetChange(it) }
    }

    // nestedScrollConnection 没有带 key 重新 remember，因此用 rememberUpdatedState 保证它总能读到最新值。
    val currentCanGoPrevious by rememberUpdatedState(previousAnswer != null)
    val currentCanGoNext by rememberUpdatedState(nextAnswer != null)
    val currentOnNavigatePrevious by rememberUpdatedState(onNavigatePrevious)
    val currentOnNavigateNext by rememberUpdatedState(onNavigateNext)
    val currentTriggerThresholdPx by rememberUpdatedState(triggerThresholdPx)

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (overscrollOffset.value != 0f && !overscrollOffset.isRunning) {
                    val delta = available.y
                    val currentOffset = overscrollOffset.value
                    if ((currentOffset > 0 && delta < 0) || (currentOffset < 0 && delta > 0)) {
                        val newRaw = rawDragAccumulator + delta
                        if ((rawDragAccumulator > 0 && newRaw <= 0) || (rawDragAccumulator < 0 && newRaw >= 0)) {
                            rawDragAccumulator = 0f
                            coroutineScope.launch { overscrollOffset.snapTo(0f) }
                            hasTriggeredHaptic = false
                            return Offset(0f, delta)
                        }
                        rawDragAccumulator = newRaw
                        val newOffset = dampedOverscrollOffset(
                            rawDelta = rawDragAccumulator,
                            maxOverscrollPx = maxOverscrollPx,
                            dampingFactor = Motion.Gesture.OVERSCROLL_DAMPING,
                        )
                        coroutineScope.launch { overscrollOffset.snapTo(newOffset) }
                        if (hasTriggeredHaptic && abs(newOffset) < currentTriggerThresholdPx) {
                            hasTriggeredHaptic = false
                        }
                        return Offset(0f, delta)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                val delta = available.y
                if (delta == 0f) return Offset.Zero

                val atTop = isAtTop()
                val atBottom = isAtBottom()
                val canOverscrollDown = delta > 0 && atTop && currentCanGoPrevious
                val canOverscrollUp = delta < 0 && atBottom && currentCanGoNext
                if (canOverscrollDown ||
                    canOverscrollUp ||
                    (overscrollOffset.value > 0 && delta > 0) ||
                    (overscrollOffset.value < 0 && delta < 0)
                ) {
                    rawDragAccumulator += delta
                    val newOffset = dampedOverscrollOffset(
                        rawDelta = rawDragAccumulator,
                        maxOverscrollPx = maxOverscrollPx,
                        dampingFactor = Motion.Gesture.OVERSCROLL_DAMPING,
                    )
                    coroutineScope.launch { overscrollOffset.snapTo(newOffset) }
                    if (!hasTriggeredHaptic && abs(newOffset) >= currentTriggerThresholdPx) {
                        hasTriggeredHaptic = true
                        haptics.gestureThreshold()
                    }
                    return Offset(0f, delta)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                if (overscrollOffset.value != 0f) {
                    val currentOffset = overscrollOffset.value
                    var didNavigate = false
                    if (abs(currentOffset) >= currentTriggerThresholdPx) {
                        if (currentOffset > 0 && currentCanGoPrevious) {
                            currentOnNavigatePrevious()
                            didNavigate = true
                        } else if (currentOffset < 0 && currentCanGoNext) {
                            currentOnNavigateNext()
                            didNavigate = true
                        }
                    }
                    rawDragAccumulator = 0f
                    hasTriggeredHaptic = false
                    if (!didNavigate) {
                        overscrollOffset.animateTo(
                            0f,
                            animationSpec = Motion.overscrollSettle(),
                        )
                    }
                    return available
                }
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .nestedScroll(nestedScrollConnection)
            .then(
                if (isContentNonScrollable) {
                    // 使用 Unit key 避免手势处理中途重建 handler；current* 引用负责读取实时更新。
                    Modifier.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                rawDragAccumulator = 0f
                                hasTriggeredHaptic = false
                                var dragStarted = false
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break

                                    val delta = change.positionChange().y
                                    if (!dragStarted) {
                                        val totalDx = change.position.x - down.position.x
                                        val totalDy = change.position.y - down.position.y
                                        if (totalDy.absoluteValue <= viewConfiguration.touchSlop ||
                                            totalDy.absoluteValue <= totalDx.absoluteValue
                                        ) {
                                            continue
                                        }
                                        dragStarted = true
                                    }

                                    val canDragDown = delta > 0 && currentCanGoPrevious
                                    val canDragUp = delta < 0 && currentCanGoNext
                                    val continuing = (overscrollOffset.value > 0 && delta > 0) ||
                                        (overscrollOffset.value < 0 && delta < 0)
                                    val reversing = (overscrollOffset.value > 0 && delta < 0) ||
                                        (overscrollOffset.value < 0 && delta > 0)

                                    if (canDragDown || canDragUp || continuing || reversing) {
                                        change.consume()
                                        rawDragAccumulator += delta
                                        if ((rawDragAccumulator > 0 && !currentCanGoPrevious) ||
                                            (rawDragAccumulator < 0 && !currentCanGoNext)
                                        ) {
                                            rawDragAccumulator = 0f
                                        }
                                        val newOffset = dampedOverscrollOffset(
                                            rawDelta = rawDragAccumulator,
                                            maxOverscrollPx = maxOverscrollPx,
                                            dampingFactor = Motion.Gesture.OVERSCROLL_DAMPING,
                                        )
                                        coroutineScope.launch { overscrollOffset.snapTo(newOffset) }
                                        if (!hasTriggeredHaptic && abs(newOffset) >= currentTriggerThresholdPx) {
                                            hasTriggeredHaptic = true
                                            haptics.gestureThreshold()
                                        }
                                        if (hasTriggeredHaptic && abs(newOffset) < currentTriggerThresholdPx) {
                                            hasTriggeredHaptic = false
                                        }
                                    }
                                }

                                val currentOffset = overscrollOffset.value
                                var didNavigate = false
                                if (abs(currentOffset) >= currentTriggerThresholdPx) {
                                    if (currentOffset > 0 && currentCanGoPrevious) {
                                        currentOnNavigatePrevious()
                                        didNavigate = true
                                    } else if (currentOffset < 0 && currentCanGoNext) {
                                        currentOnNavigateNext()
                                        didNavigate = true
                                    }
                                }
                                rawDragAccumulator = 0f
                                hasTriggeredHaptic = false
                                if (!didNavigate) {
                                    coroutineScope.launch {
                                        overscrollOffset.animateTo(
                                            0f,
                                            animationSpec = Motion.overscrollSettle(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        // 预览卡片在底层，被主内容覆盖，主内容滑动时露出。

        // 下面四个布尔量都用 derivedStateOf 收敛：`overscrollOffset` 每帧都在变，
        // 直接在组合期读它会让整棵子树按帧重组；收敛成「是否出现 / 是否越阈」之后，
        // 拖动全程只在跨过 0 与阈值这两个瞬间各重组一次。
        val showPreviousCard by remember(previousAnswer, triggerThresholdPx) {
            derivedStateOf { overscrollOffset.value > 0f && previousAnswer != null }
        }
        val showNextCard by remember(nextAnswer, triggerThresholdPx) {
            derivedStateOf { overscrollOffset.value < 0f && nextAnswer != null }
        }
        val previousTriggered by remember(triggerThresholdPx) {
            derivedStateOf { overscrollOffset.value >= triggerThresholdPx }
        }
        val nextTriggered by remember(triggerThresholdPx) {
            derivedStateOf { overscrollOffset.value <= -triggerThresholdPx }
        }

        // 上方预览卡片（上一个回答）—— 固定在顶部
        if (showPreviousCard && previousAnswer != null) {
            AnswerPreviewCard(
                authorName = previousAnswer.authorName,
                excerpt = previousAnswer.content.toPlainExcerpt(),
                avatarUrl = previousAnswer.authorAvatarUrl,
                label = "${previousAnswer.sourceLabel}的上一个回答",
                icon = Icons.Filled.ArrowUpward,
                isTriggered = previousTriggered,
                progressProvider = { (overscrollOffset.value / triggerThresholdPx).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()),
            )
        }

        // 下方预览卡片（下一个回答）—— 固定在底部，内容顺序反转
        if (showNextCard && nextAnswer != null) {
            AnswerPreviewCard(
                authorName = nextAnswer.authorName,
                excerpt = nextAnswer.content.toPlainExcerpt(),
                avatarUrl = nextAnswer.authorAvatarUrl,
                label = "${nextAnswer.sourceLabel}的下一个回答",
                icon = Icons.Filled.ArrowDownward,
                isTriggered = nextTriggered,
                progressProvider = { (abs(overscrollOffset.value) / triggerThresholdPx).coerceIn(0f, 1f) },
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()),
            )
        }

        // 主内容在最上层，跟随 overscroll 偏移，滑动露出预览卡片
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .offset { IntOffset(0, overscrollOffset.value.roundToInt()) },
        ) {
            content()
        }
    }
}

@Composable
private fun AnswerPreviewCard(
    authorName: String,
    excerpt: String,
    avatarUrl: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isTriggered: Boolean,
    progressProvider: () -> Float,
    reverseLayout: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // 「到达阈值」是一次状态切换，颜色要过渡而不是硬切：200ms 标准曲线与遮罩/文字淡入同档。
    val bgColor by animateColorAsState(
        targetValue = if (isTriggered) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = Motion.fadeTween(),
        label = "answer_preview_bg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isTriggered) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = Motion.fadeTween(),
        label = "answer_preview_content",
    )

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(ZhihuShapeTokens.Card)
            .background(bgColor)
            // alpha 在绘制阶段读取，拖动过程中不触发重组。
            .graphicsLayer { alpha = progressProvider().coerceIn(0f, 1f) }
            .padding(12.dp),
    ) {
        val labelRow: @Composable () -> Unit = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    // 图标纯装饰，旁边的 Text 已经把 label 念出来了，避免无障碍重复朗读。
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isTriggered) "松手切换" else label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = if (isTriggered) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
        val authorRow: @Composable () -> Unit = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (authorName.isNotEmpty()) {
                        Text(
                            text = authorName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                        )
                    }
                    if (excerpt.isNotEmpty()) {
                        Text(
                            text = excerpt,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }
        }
        Column {
            if (reverseLayout) {
                authorRow()
                Spacer(Modifier.height(8.dp))
                labelRow()
            } else {
                labelRow()
                Spacer(Modifier.height(8.dp))
                authorRow()
            }
        }
    }
}

/** 纵向手势的物理常量已上收到 `Motion.Gesture`；此处只保留 tanh 阻尼的实现。 */

internal fun dampedOverscrollOffset(
    rawDelta: Float,
    maxOverscrollPx: Float,
    dampingFactor: Float,
): Float {
    val sign = if (rawDelta >= 0) 1f else -1f
    return sign * maxOverscrollPx * tanh(abs(rawDelta) / (maxOverscrollPx * dampingFactor))
}

/**
 * 预览卡摘要只是给用户一个"这是哪个回答"的提示，不需要完整渲染 HTML。
 * 去掉标签与多余空白，截取前 60 个字符即可。
 */
private fun String.toPlainExcerpt(maxLength: Int = 60): String {
    val plain = replace(Regex("<[^>]*>"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
    return if (plain.length > maxLength) plain.take(maxLength) + "…" else plain
}
