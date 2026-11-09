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

package com.github.zly2006.zhihu.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material Design 3 排版体系。
 *
 * 这里保留 M3 的 15 个语义角色（display/headline/title/body/label × 大中小），
 * 但与 Google 的拉丁字母默认值相比做了两点针对中文阅读的调整：
 *
 * 1. **行高更大**。汉字没有西文的下伸部，M3 默认行高在中文段落里会显得拥挤；
 *    这里把正文行高按 1.55～1.6 倍字号取值，标题按 1.35 倍取值。
 * 2. **字距归零**。M3 默认的正字距是为拉丁字母设计的，中文逐字等宽，额外的字距只会让文字散开。
 *
 * 页面不要再直接写 `fontSize = 14.sp` 这类硬编码，应该选一个语义角色，
 * 让字号、行高、字重和字距在全局保持一致。
 */
private val Default = FontFamily.Default

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontFamily = Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

val Typography = Typography(
    // 展示级：仅用于空状态标题、引导页等大字号场景。
    displayLarge = style(57, 64),
    displayMedium = style(45, 52),
    displaySmall = style(36, 44),
    // 标题级：页面大标题、设置分组首页。
    headlineLarge = style(32, 40),
    headlineMedium = style(28, 38),
    headlineSmall = style(24, 34),
    // 小标题：AppBar 标题、卡片标题、设置分组标题。
    titleLarge = style(18, 26, FontWeight.Medium),
    titleMedium = style(16, 24, FontWeight.Medium),
    titleSmall = style(14, 22, FontWeight.Medium),
    // 正文：内容主体。官方回答正文为 17sp / 约 24dp 行高，这里保持 17sp 基准。
    bodyLarge = style(17, 24),
    bodyMedium = style(14, 22),
    bodySmall = style(12, 18),
    // 标签：按钮、导航栏、辅助信息。
    labelLarge = style(15, 22, FontWeight.Medium),
    labelMedium = style(12, 18, FontWeight.Medium),
    labelSmall = style(11, 16, FontWeight.Medium),
)

/**
 * `Typography` 15 个 M3 语义角色之外、仿官方场景专用的补充令牌。
 *
 * 数值来自官方 11.14 真机实测（1080×2400 @2.625x，行高由文字节点边界反推），
 * 页面不得再为这些场景写裸 `fontSize`。
 */
object ZhihuTypography {
    /** 问题页问题标题 / 回答页展开态内容标题：22sp、Bold，行高按 1.4 倍留足中文间距。 */
    val ContentTitle = style(22, 31, FontWeight.Bold)

    /** 首页信息流卡片问题标题：18sp、Bold、2 行，官方实测行盒 25dp。 */
    val FeedTitle = style(18, 25, FontWeight.Bold)

    /** 首页卡片摘要：15sp / 22dp，最多 3 行。 */
    val FeedSummary = style(15, 22)

    /** 首页卡片作者名：15sp / 20dp、Medium。 */
    val FeedAuthorName = style(15, 20, FontWeight.Medium)

    /** 首页卡片元信息（关注数、赞同/收藏/评论计数）：13sp / 18dp。 */
    val FeedMeta = style(13, 18, FontWeight.Medium)

    /** 首页分类 Tab 文字：17sp / 24dp；选中态在调用处 copy(fontWeight = Bold)。 */
    val TabTitle = style(17, 24)

    /** 首页搜索胶囊热词：15sp / 20dp。 */
    val HotWord = style(15, 20)

    /** 底栏文字：12sp / 16dp。 */
    val BottomNavLabel = style(12, 16, FontWeight.Medium)

    /** 回答页作者名：17sp / 21dp、SemiBold。 */
    val ContentAuthor = style(17, 21, FontWeight.SemiBold)

    /** 回答页作者签名：13sp / 18dp。 */
    val ContentSignature = style(13, 18)

    /** 问题页问题头的紧凑标题：20sp / 26dp、Bold，最多 2 行（比官方展开态标题矮，首屏能直接看到第一条回答）。 */
    val QuestionHeaderTitle = style(20, 26, FontWeight.Bold)

    /** 问题页回答卡作者名（列表态）：16sp / 21dp、SemiBold。 */
    val AnswerAuthorName = style(16, 21, FontWeight.SemiBold)

    /** 问题页回答卡作者签名：12sp / 17dp。 */
    val AnswerAuthorSignature = style(12, 17)

    /** 回答正文：17sp / 24dp（官方行高约 1.43，与正文阅读设置解耦，设置仍可覆盖）。 */
    val AnswerBody = style(17, 24)

    /** 回答页「N 人赞同了该回答 / N 人听过」行：14sp / 20dp。 */
    val AnswerVoteLine = style(14, 20)

    /** 回答页元信息（时间 · IP · 禁止转载）与操作行文字：13sp / 18dp。 */
    val AnswerMeta = style(13, 18)

    /** 操作行数字（赞同/收藏/评论计数）：12sp / 16dp。 */
    val ActionCount = style(12, 16, FontWeight.Medium)

    /** 「欢迎参与讨论」胶囊文字：14sp / 20dp。 */
    val DiscussPillLabel = style(14, 20)
}
