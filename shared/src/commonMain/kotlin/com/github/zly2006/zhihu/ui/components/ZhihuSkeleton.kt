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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSizes
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.zhihuSurfaces

private const val SHIMMER_FROM = -600f
private const val SHIMMER_TO = 1400f
private const val SHIMMER_BAND = 420f
private const val SHIMMER_MILLIS = 1400

/** 扫光重复次数。故意有限，见 [rememberShimmerOffset] 的说明。 */
private const val SHIMMER_REPEATS = 3

/**
 * 骨架屏的流光位移。
 *
 * 用一条从右向左移动的高光模拟内容正在加载，比转圈更能表达「这里将会出现内容」。
 *
 * 两点刻意的选择：
 * 1. **不用 [androidx.compose.animation.core.rememberInfiniteTransition]**。无限动画会让
 *    Compose 测试的 `waitForIdle()` 永远等不到空闲——首页加载期骨架常驻时整条用例会挂死。
 *    这里改为扫 [SHIMMER_REPEATS] 轮后停在终点，之后骨架是静态占位，不再产生帧。
 * 2. 返回 [State] 而不是 `Float`。调用方在 `drawBehind {}` 里读 `.value`，位移每帧变化
 *    只触发重绘，不会重组骨架卡片。
 */
@Composable
private fun rememberShimmerOffset(): State<Float> {
    val offset = remember { Animatable(SHIMMER_FROM) }
    LaunchedEffect(offset) {
        repeat(SHIMMER_REPEATS) {
            offset.snapTo(SHIMMER_FROM)
            offset.animateTo(
                targetValue = SHIMMER_TO,
                animationSpec = tween(durationMillis = SHIMMER_MILLIS, easing = LinearEasing),
            )
        }
    }
    return offset.asState()
}

/**
 * 骨架占位的高光底色。位移与画刷都在绘制期构造，组合期只透传颜色。
 */
private fun Modifier.shimmerPlaceholder(
    shimmer: State<Float>,
    base: Color,
    highlight: Color,
): Modifier = drawBehind {
    val offset = shimmer.value
    drawRect(
        Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(offset, 0f),
            end = Offset(offset + SHIMMER_BAND, 0f),
        ),
    )
}

/**
 * 信息流卡片的骨架占位。
 *
 * 结构刻意与 [FeedCard] 的官方版式一致（两行标题 / 摘要 + 右侧缩略图 / 底部作者行），
 * 这样加载完成时不会发生跳版。旧的「16:9 通栏封面」版式对应早已下线的 M3 Expressive 卡片，
 * 与真实卡片从未对齐过，加载完成会整卡跳版。
 */
@Composable
fun ZhihuSkeletonCard(
    modifier: Modifier = Modifier,
) {
    val shimmer = rememberShimmerOffset()
    val shimmerBase = MaterialTheme.colorScheme.surfaceContainerHighest
    val shimmerHighlight = MaterialTheme.colorScheme.surfaceContainerLow
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ZhihuShapeTokens.FeedCard,
        colors = CardDefaults.cardColors(containerColor = zhihuSurfaces.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(ZhihuSpacing.Large)) {
            SkeletonBar(shimmer, shimmerBase, shimmerHighlight, Modifier.fillMaxWidth(0.92f), height = 20)
            Spacer(Modifier.height(ZhihuSpacing.Small))
            SkeletonBar(shimmer, shimmerBase, shimmerHighlight, Modifier.fillMaxWidth(0.58f), height = 20)
            Spacer(Modifier.height(ZhihuSpacing.Medium))
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonBar(shimmer, shimmerBase, shimmerHighlight, Modifier.fillMaxWidth(), height = 14)
                    Spacer(Modifier.height(ZhihuSpacing.Small))
                    SkeletonBar(shimmer, shimmerBase, shimmerHighlight, Modifier.fillMaxWidth(0.92f), height = 14)
                    Spacer(Modifier.height(ZhihuSpacing.Small))
                    SkeletonBar(shimmer, shimmerBase, shimmerHighlight, Modifier.fillMaxWidth(0.68f), height = 14)
                }
                Spacer(Modifier.width(ZhihuSpacing.Medium))
                Box(
                    modifier = Modifier
                        .size(width = 112.dp, height = 84.dp)
                        .clip(ZhihuShapeTokens.Thumbnail)
                        .shimmerPlaceholder(shimmer, shimmerBase, shimmerHighlight),
                )
            }
            Spacer(Modifier.height(ZhihuSpacing.Medium))
            // 底部作者行：头像 + 名字，右侧预留赞同/评论计数。
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(ZhihuSizes.AvatarSmall)
                        .clip(CircleShape)
                        .shimmerPlaceholder(shimmer, shimmerBase, shimmerHighlight),
                )
                Spacer(Modifier.width(ZhihuSpacing.Small))
                SkeletonBar(shimmer, shimmerBase, shimmerHighlight, Modifier.width(72.dp), height = 12)
                Spacer(Modifier.weight(1f))
                SkeletonBar(shimmer, shimmerBase, shimmerHighlight, Modifier.width(96.dp), height = 12)
            }
        }
    }
}

@Composable
private fun SkeletonBar(
    shimmer: State<Float>,
    base: Color,
    highlight: Color,
    width: Modifier,
    height: Int,
) {
    Box(
        modifier = width
            .height(height.dp)
            .clip(RoundedCornerShape(6.dp))
            .shimmerPlaceholder(shimmer, base, highlight),
    )
}
