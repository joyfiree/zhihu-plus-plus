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
 * 触控目标一律不小于 48dp（M3 无障碍下限）；图标视觉尺寸按官方实测取值，
 * 点击区域通过最小触控约束或外层留白补齐。
 */
object ZhihuSizes {
    val MinTouchTarget = 48.dp
    val ListItemMinHeight = 56.dp

    /** 底部导航栏高度（不含系统手势区）。官方实测 159px ≈ 60.6dp。 */
    val NavigationBarHeight = 60.dp
    val Icon = 24.dp
    val IconSmall = 20.dp
    val AvatarSmall = 24.dp
    val AvatarLarge = 40.dp
    val DividerThickness = 1.dp

    // ---- 首页 ----

    /** 首页搜索胶囊行高（官方 ZHCardView 高 38dp，行盒 40dp）。 */
    val HomeSearchRowHeight = 40.dp
    val HomeSearchPillRadius = 20.dp

    /** 首页左上账号头像。 */
    val HomeAvatarSize = 38.dp

    /** 首页搜索胶囊内「搜索」按钮视觉尺寸。 */
    val HomeSearchButtonWidth = 56.dp
    val HomeSearchButtonHeight = 30.dp

    /** 首页分类 Tab 行高与单项宽度。 */
    val HomeTabRowHeight = 40.dp
    val HomeTabItemWidth = 50.dp
    val HomeTabIndicatorWidth = 30.dp
    val HomeTabIndicatorHeight = 3.dp

    /** 首页卡片左右内边距。 */
    val FeedCardHorizontalGutter = 16.dp

    /** 首页卡片作者头像。 */
    val FeedAvatarSize = 20.dp

    /** 首页卡片操作行图标与行高。 */
    val FeedActionIconSize = 20.dp
    val FeedActionRowHeight = 24.dp

    /** 首页卡片图片高度（4:3 横滑图，官方实测 363px ≈ 138dp）。 */
    val FeedImageHeight = 138.dp

    /** 信息流缩略图统一尺寸（4:3），三种卡片排版与骨架屏共用，避免真实卡片加载后跳尺寸。 */
    val FeedThumbnailWidth = 112.dp
    val FeedThumbnailHeight = 84.dp

    /** 底栏图标视觉尺寸（官方实测 74px ≈ 28dp）。 */
    val BottomBarIconSize = 28.dp

    // ---- 问题回答页 ----

    /** 回答卡作者头像（用户实测比对官方后由 36dp 调整为 32dp）。 */
    val AnswerAvatarSize = 32.dp

    /** 回答操作行图标与行高。 */
    val AnswerActionIconSize = 26.dp
    val AnswerActionRowHeight = 48.dp

    /** 吸底操作条高度（官方 126px ≈ 48dp）。 */
    val StickyBarHeight = 48.dp

    /** 回答页悬浮工具栏行高（官方 115px ≈ 44dp）。 */
    val AnswerToolbarHeight = 44.dp

    /** 「+ 关注」胶囊。 */
    val FollowPillWidth = 72.dp
    val FollowPillHeight = 30.dp

    /** 「欢迎参与讨论」胶囊。 */
    val DiscussPillHeight = 36.dp

    /** 「下一篇内容」悬浮按钮尺寸。 */
    val NextAnswerFabSize = 36.dp

    /** 长回答收起高度占屏幕可视高度的比例（官方 0.618）。 */
    const val ANSWER_COLLAPSED_HEIGHT_RATIO = 0.618f
}
