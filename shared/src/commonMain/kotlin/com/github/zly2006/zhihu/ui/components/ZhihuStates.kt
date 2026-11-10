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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.ui.designsystem.Motion
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing

/**
 * 空状态。
 *
 * M3 的空状态不是一行灰色小字，而是「图标 + 标题 + 说明 + 可选动作」的居中区块。
 * 图标放在 `surfaceContainerHighest` 的圆形底上，保证在深浅主题下都有明确的视觉落点。
 */
@Composable
fun ZhihuEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    action: (@Composable () -> Unit)? = null,
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val enter by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = Motion.fadeTween(),
        label = "empty_state_enter",
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = enter
                translationY = (1f - enter) * 24f
            }.padding(ZhihuSpacing.Huge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = ZhihuSpacing.Large),
        )
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = ZhihuSpacing.Small),
            )
        }
        action?.let {
            Box(modifier = Modifier.padding(top = ZhihuSpacing.Large)) {
                it()
            }
        }
    }
}

/**
 * 加载状态。
 *
 * 用 M3 的 `CircularProgressIndicator` 加一句说明，避免整屏只有一个转圈导致页面「空掉」。
 */
@Composable
fun ZhihuLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ZhihuSpacing.Huge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
        if (!message.isNullOrBlank()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = ZhihuSpacing.Medium),
            )
        }
    }
}

/**
 * 错误状态。
 *
 * 与空状态共用同一套骨架，只把图标换成「离线」，并提供一个重试按钮；
 * 这样「没数据」和「加载失败」在视觉上是同一个家族，用户不会误判。
 */
@Composable
fun ZhihuErrorState(
    modifier: Modifier = Modifier,
    title: String = "加载失败",
    description: String? = null,
    retryText: String = "重试",
    onRetry: (() -> Unit)? = null,
) {
    val retry = onRetry
    val action: (@Composable () -> Unit)? = if (retry != null) {
        {
            // 重试是「文字按钮」而不是有色块的按钮：官方语言里动作靠文字色表达，不靠底色块。
            TextButton(onClick = retry) {
                Text(text = retryText)
            }
        }
    } else {
        null
    }
    ZhihuEmptyState(
        title = title,
        modifier = modifier,
        description = description,
        icon = Icons.Outlined.CloudOff,
        action = action,
    )
}

/**
 * 列表底部的一行提示。
 *
 * 用于「没有更多了」「已加载全部」这类弱信息，颜色降到 `onSurfaceVariant`，
 * 并保留足够的下边距，避免和系统手势条贴在一起。
 */
@Composable
fun ZhihuListFooter(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = ZhihuSpacing.ExtraLarge),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
