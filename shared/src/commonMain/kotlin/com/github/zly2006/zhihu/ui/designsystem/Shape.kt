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
 * Material Design 3 形状阶梯。
 *
 * M3 用一组固定圆角表达「组件层级」：越小越贴内容（输入框、chip），
 * 越大越像独立容器（卡片、弹层）。页面里不要再写 `RoundedCornerShape(4.dp)` 这类
 * 随手取的圆角，否则同一屏会出现 4/8/12/16 混用。
 */
val ZhihuShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * 常用形状别名。
 *
 * 语义化命名让调用点表达「这是什么容器」而不是「圆角多少 dp」，
 * 后续调整整体观感时只需要改这里。
 */
object ZhihuShapeTokens {
    /** 卡片 / 列表容器：M3 large。 */
    val Card = RoundedCornerShape(16.dp)

    /** 强调型卡片（duo3 外观），比标准卡片更圆一些。 */
    val CardExpressive = RoundedCornerShape(20.dp)

    /** 次级容器（缩略图、图片、代码块）：M3 medium。 */
    val Thumbnail = RoundedCornerShape(12.dp)

    /** 小控件（tag、徽章）：M3 small。 */
    val Badge = RoundedCornerShape(8.dp)

    /** 底部弹层：顶部大圆角，底部贴边。 */
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    /** 药丸形（筛选 chip、分组总开关）。 */
    val Pill = RoundedCornerShape(percent = 50)

    /** 对话框：M3 extraLarge。 */
    val Dialog = RoundedCornerShape(28.dp)
}
