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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.zly2006.zhihu.ui.designsystem.zhihuSurfaces

/**
 * 全应用统一的顶栏配色。
 *
 * M3 规定顶栏属于「surface」层级：未滚动时与页面同色（靠内容滚动产生分离感），
 * 滚动后被内容推起的部分换成 `surfaceContainer`，形成轻微的高度暗示。
 * 早期页面各自用默认配色（primary 背景或纯 surface），滚动时也没有颜色变化，
 * 这正是各个页面观感不一致的主要来源之一。
 */
@Composable
fun zhihuTopAppBarColors(
    containerColor: Color = zhihuSurfaces.page,
    scrolledContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    actionIconContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = containerColor,
    scrolledContainerColor = scrolledContainerColor,
    titleContentColor = titleContentColor,
    navigationIconContentColor = navigationIconContentColor,
    actionIconContentColor = actionIconContentColor,
)

/**
 * 标准顶部应用栏（左对齐标题）。
 *
 * 与 `Material3.TopAppBar` 参数一致，只是把配色、标题排版和滚动行为收敛到一处，
 * 页面只需要关心标题内容和右侧动作。新增页面请直接使用它，不要再手写颜色。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZhihuTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = zhihuTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior,
        windowInsets = windowInsets,
    )
}

/**
 * 居中标题的顶部应用栏。
 *
 * 用于详情页（私信、通知、单条内容）——这类页面没有同级 tab，居中标题比左对齐更稳。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZhihuCenterAlignedTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = zhihuTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    CenterAlignedTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior,
        windowInsets = windowInsets,
    )
}

/**
 * 大标题顶部应用栏。
 *
 * 设置类页面用它把页面标题放大成 `headlineMedium`，滚动后收成普通工具栏高度。
 * 这是 M3 里「一级页面」的标准表达，比所有页面都用 64dp 小标题更有层次。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZhihuLargeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = zhihuTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    LargeTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior,
        windowInsets = windowInsets,
    )
}
