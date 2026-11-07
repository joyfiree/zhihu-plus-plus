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

import androidx.compose.ui.unit.dp

/**
 * 4dp 基准的间距阶梯。
 *
 * 页面里 `padding(13.dp)` 这类随手值会让各行节奏对不齐。统一走这个阶梯后，
 * 列表、卡片、设置页的垂直节奏才是同一套网格。
 */
object ZhihuSpacing {
    val None = 0.dp
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val Huge = 32.dp

    /** 页面左右内容边距（列表、设置页、正文统一 16dp）。 */
    val ScreenGutter = 16.dp

    /** 信息流卡片之间的垂直间隙，骨架屏与真实卡片共用，避免两处各写一份而跳版。 */
    val CardGap = 5.dp
}

/**
 * 组件尺寸。
 *
 * 触控目标一律不小于 48dp（M3 无障碍下限），图标视觉尺寸 24dp，
 * 避免某些按钮只有 16dp 可点区域。
 */
object ZhihuSizes {
    val MinTouchTarget = 48.dp
    val ListItemMinHeight = 56.dp
    val TopAppBarHeight = 64.dp
    val MediumTopAppBarHeight = 112.dp
    val LargeTopAppBarHeight = 152.dp
    val NavigationBarHeight = 64.dp
    val Icon = 24.dp
    val IconSmall = 20.dp
    val AvatarSmall = 24.dp
    val AvatarMedium = 32.dp
    val AvatarLarge = 40.dp
    val DividerThickness = 1.dp

    /** 信息流缩略图统一尺寸（4:3），三种卡片排版与骨架屏共用，避免真实卡片加载后跳尺寸。 */
    val FeedThumbnailWidth = 112.dp
    val FeedThumbnailHeight = 84.dp
}
