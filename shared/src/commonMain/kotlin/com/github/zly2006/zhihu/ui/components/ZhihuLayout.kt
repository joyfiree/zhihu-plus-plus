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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSizes
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing

/**
 * 列表分组标题。
 *
 * M3 的列表靠「间距 + 字号 + 字重」区分层级，而不是靠居中加粗文字加两侧横线。
 * 分组标题统一用 `titleSmall` + `primary`，说明文字用 `bodySmall` + `onSurfaceVariant`，
 * 右侧可以放「全部 / 更多」之类的文字按钮。
 *
 * 设置页请继续使用 `SettingItemGroup`（它内置了高亮跳转与折叠动画）。
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = ZhihuSpacing.ScreenGutter,
                top = ZhihuSpacing.Large,
                end = ZhihuSpacing.ScreenGutter,
                bottom = ZhihuSpacing.Small,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (!supporting.isNullOrBlank()) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailing?.invoke()
    }
}

/**
 * 列表分隔线。
 *
 * 统一 1dp + `outlineVariant`，并按 M3 列表规范从内容起始位置缩进
 * （有头像/图标的行，分割线从文字起始处开始，而不是贯穿整屏）。
 */
@Composable
fun ZhihuListDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = ZhihuSpacing.ScreenGutter,
) {
    HorizontalDivider(
        modifier = modifier.padding(start = startIndent),
        thickness = ZhihuSizes.DividerThickness,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}
