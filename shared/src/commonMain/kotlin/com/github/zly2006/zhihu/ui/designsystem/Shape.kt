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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 形状阶梯（仿知乎官方 App）。
 *
 * 官方的圆角语言克制且偏小：卡片 12dp、弹层 16dp、按钮和 chip 是药丸形。
 * 页面里不要再写 `RoundedCornerShape(4.dp)` 这类随手取的圆角，
 * 否则同一屏会出现 4/8/12/16 混用。
 */
val ZhihuShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(percent = 50),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

/**
 * 常用形状别名。
 *
 * 语义化命名让调用点表达「这是什么容器」而不是「圆角多少 dp」，
 * 后续调整整体观感时只需要改这里。
 */
object ZhihuShapeTokens {
    /** 卡片 / 列表容器：官方信息流卡片的中等圆角。 */
    val Card = RoundedCornerShape(12.dp)

    /** 信息流卡片：与 [Card] 同档，保留独立别名以便单独微调。 */
    val FeedCard = RoundedCornerShape(12.dp)

    /** 次级容器（缩略图、图片、代码块）：官方缩略图的小圆角。 */
    val Thumbnail = RoundedCornerShape(6.dp)

    /** 小控件（tag、徽章）：官方徽标为药丸形。 */
    val Badge = RoundedCornerShape(percent = 50)

    /** 设置/内容分组容器。 */
    val Section = RoundedCornerShape(12.dp)

    /** 底部弹层：顶部圆角，底部贴边。 */
    val BottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

    /** 药丸形（筛选 chip、投票按钮）。 */
    val Pill = RoundedCornerShape(percent = 50)

    /** 对话框。 */
    val Dialog = RoundedCornerShape(16.dp)
}
