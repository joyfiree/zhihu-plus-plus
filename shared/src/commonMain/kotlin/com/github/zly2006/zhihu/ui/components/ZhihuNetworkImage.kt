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

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.github.zly2006.zhihu.ui.designsystem.Motion

/**
 * 带占位底色与淡入的图片。
 *
 * 直接把 `AsyncImage` 铺在界面上会出现两个问题：加载期间是一块空白（列表里像空洞），
 * 加载完成又是瞬间跳出来。这里先铺一层容器色占位，成功后 240ms 淡入，
 * 观感上更接近系统应用的图片加载。
 *
 * 关于格式：淡入由 `onSuccess` 驱动，而不是解码器回调，因此 AVIF / WebP（部分平台走独立解码器）
 * 与普通 JPEG 走同一条路径——解码完成的那一刻才开始淡入，不会出现「先透明后闪」。
 * 占位色默认与骨架屏同一档（`surfaceContainerHighest`），弱网下图片位不会出现白块。
 *
 * 尺寸与裁剪由调用方通过 [modifier] 决定（例如 `size(20.dp).clip(CircleShape)`）。
 *
 * @param fadeInDelayMillis 淡入前的额外等待，仅用于图片墙的逐张错峰；不影响解码与占位。
 *   等待走动画时钟（`animate`）而不是 `delay`，因此系统「动画时长 0」时这一步会立即完成。
 */
@Composable
fun ZhihuNetworkImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    fadeInDelayMillis: Int = 0,
) {
    var decoded by remember(model) { mutableStateOf(false) }
    var revealed by remember(model) { mutableStateOf(false) }
    // 错峰只是「晚一点开始淡入」，不改变总时长，因此连续图片的淡入速度看起来是一致的。
    LaunchedEffect(decoded, model) {
        if (!decoded) return@LaunchedEffect
        if (fadeInDelayMillis > 0) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = fadeInDelayMillis,
                    easing = Motion.StandardEasing,
                ),
            ) { _, _ -> }
        }
        revealed = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = Motion.imageFadeTween(),
        label = "zhihu_image_alpha",
    )
    Box(modifier = modifier.background(placeholderColor)) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            onSuccess = { decoded = true },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha },
        )
    }
}
