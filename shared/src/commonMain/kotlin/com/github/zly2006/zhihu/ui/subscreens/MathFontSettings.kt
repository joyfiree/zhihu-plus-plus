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

package com.github.zly2006.zhihu.ui.subscreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.markdown.MAX_MATH_FONT_BYTES
import com.github.zly2006.zhihu.markdown.rememberCustomMathFonts
import com.github.zly2006.zhihu.markdown.rememberSelectedMathFont
import com.github.zly2006.zhihu.platform.rememberFileImporter
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun MathFontSettings() {
    val fonts = rememberCustomMathFonts()
    val selected = rememberSelectedMathFont()
    val messages = rememberUserMessageSink()
    var busy by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val picker = rememberFileImporter(
        title = "导入公式字体",
        extensions = listOf("otf", "ttf"),
        maxBytes = MAX_MATH_FONT_BYTES,
    ) { name, bytes ->
        busy = true
        try {
            withContext(Dispatchers.Default) { fonts.import(name, bytes) }
            messages.showShortMessage("公式字体已导入")
        } finally {
            busy = false
        }
    }
    Column {
        Text("用于 Compose 正文中的公式；请选择含 MATH 表的 OTF 或 TTF 数学字体（不超过 20 MB）。")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            // 官方语言里没有描边按钮：两个动作都做成无边框药丸（功能底 + 文字色），
            // 与设置页搜索胶囊同一形态；Surface 自带 48dp 最小触控区与点击涟漪。
            Surface(
                onClick = { expanded = true },
                enabled = !busy,
                modifier = Modifier.weight(1f).testTag("mathFont.select"),
                shape = ZhihuShapeTokens.Pill,
                color = contentFirstColors.functionalSurface,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = ZhihuSpacing.Large, vertical = ZhihuSpacing.Medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selected?.name ?: "默认字体",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (busy) contentFirstColors.tertiaryText else contentFirstColors.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = contentFirstColors.secondaryText,
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("默认字体") }, onClick = {
                            fonts.select(null)
                            expanded = false
                        })
                        fonts.list().forEach { font ->
                            DropdownMenuItem(text = { Text(font.name) }, onClick = {
                                fonts.select(font)
                                expanded = false
                            })
                        }
                    }
                }
            }
            Surface(
                onClick = picker,
                enabled = !busy,
                modifier = Modifier.testTag("mathFont.import"),
                shape = ZhihuShapeTokens.Pill,
                color = contentFirstColors.functionalSurface,
            ) {
                Text(
                    text = if (busy) "正在导入…" else "导入字体",
                    modifier = Modifier.padding(horizontal = ZhihuSpacing.Large, vertical = ZhihuSpacing.Medium),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (busy) contentFirstColors.tertiaryText else contentFirstColors.accent,
                )
            }
        }
    }
}
