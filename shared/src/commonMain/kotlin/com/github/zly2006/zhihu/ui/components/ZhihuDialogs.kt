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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.github.zly2006.zhihu.ui.designsystem.Motion
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens

/**
 * 全站统一的确认类弹窗。
 *
 * 意图：把「圆角、按钮排布、内容进入方式」三件事收在一个地方，
 * 避免同一份 App 里出现 24dp / 28dp 混用、或者一个弹窗有淡入、另一个直接砸在屏幕上的情况。
 *
 * 参数：
 * - 圆角固定 [ZhihuShapeTokens.Dialog]（28dp，M3 extraLarge）——这是 MD3 对 dialog 的容器语汇；
 * - 主体文字在窗口出现后 200ms 淡入 + 4dp 上浮：压缩的是「弹窗只是闪了一下」的廉价感，
 *   而且不改变布局尺寸（只动 alpha/translation），不会触发二次测量；
 * - 按钮保持 M3 默认的右对齐排布（dismiss 在左、confirm 在右），
 *   这是 Material 的破坏性/非破坏性动作约定，不在这里按业务改写。
 *
 * 何时不要用它：需要完全自定义版式的弹层（颜色选择器、分享面板）继续直接使用 `AlertDialog(content = …)`。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZhihuAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    containerColor: androidx.compose.ui.graphics.Color = AlertDialogDefaults.containerColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, Motion.fadeTween())
    }
    val animatedBody: (@Composable () -> Unit)? = text?.let { body ->
        {
            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * 4f
                },
            ) {
                body()
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = animatedBody,
        shape = ZhihuShapeTokens.Dialog,
        containerColor = containerColor,
        iconContentColor = AlertDialogDefaults.iconContentColor,
        titleContentColor = AlertDialogDefaults.titleContentColor,
        textContentColor = AlertDialogDefaults.textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
    )
}

/**
 * 删除收藏夹的二次确认弹窗。
 *
 * `CollectionScreen` 与 `CollectionBrowseScreen` 曾各自写了一份逐字相同的实现；
 * 收在这里之后两边只传各自的 testTag 前缀和回调。确认按钮用 error 色——
 * 这是破坏性操作，不该跟「取消」长得一样。
 */
@Composable
fun CollectionDeleteDialog(
    collectionTitle: String,
    isDeleting: Boolean,
    errorMessage: String?,
    dialogTestTag: String,
    confirmTestTag: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    ZhihuAlertDialog(
        modifier = Modifier.testTag(dialogTestTag),
        onDismissRequest = {
            if (!isDeleting) onDismiss()
        },
        title = { Text("删除收藏夹") },
        text = {
            Column {
                Text("删除后无法恢复，确认删除收藏夹“$collectionTitle”吗？")
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                modifier = Modifier.testTag(confirmTestTag),
            ) {
                Text(if (isDeleting) "删除中…" else "删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text("取消")
            }
        },
    )
}
