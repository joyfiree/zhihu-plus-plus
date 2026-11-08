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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.data.DataHolder
import com.github.zly2006.zhihu.data.VoteUpState
import com.github.zly2006.zhihu.markdown.RenderMarkdown
import com.github.zly2006.zhihu.theme.ZhihuTypography
import com.github.zly2006.zhihu.ui.designsystem.ContentFirstColors
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.util.formatCompactCount
import com.github.zly2006.zhihu.util.formatFullDateTime

const val ANSWER_CARD_TAG_PREFIX = "answer_card"
const val ANSWER_CARD_EXPAND_TAG_PREFIX = "answer_card_expand"
const val ANSWER_CARD_CONTENT_TAG_PREFIX = "answer_card_content"

/** 长回答的收起高度：产品设计稿规定内容区最高为屏幕可视高度的 0.618（黄金比）。 */
private const val COLLAPSED_VIEWPORT_RATIO = 0.618f

/** 截断处的渐隐高度：足够让最后一行自然消失，又不会吃掉太多正文。 */
private val CollapseFadeHeight = 64.dp

/** 展开 / 收起动画时长（产品设计稿 §2.3.1 建议 300ms）。 */
private const val EXPAND_ANIMATION_MILLIS = 300

/**
 * 回答瀑布流中的单条回答卡片（产品设计稿 §2.2）。
 *
 * 这是一张逻辑卡片：不画边框、不设底色，与邻居之间只用通栏分割线分界，自上而下依次是
 * 作者行 → 内容区（0.618 屏截断）→ 尾注（时间 · IP 属地）→ 操作栏 → 条件热评。
 *
 * 与回答详情页的分工：这里不做「关注作者 / 分享 / N 人赞同了该回答」这类元信息，
 * 操作栏也只用线性图标 + 数字，视觉上退到正文之后。
 */
@Composable
fun AnswerWaterfallCard(
    itemStableKey: String,
    answerId: Long?,
    answer: DataHolder.Answer?,
    fallbackAuthorName: String?,
    fallbackAuthorAvatar: String?,
    fallbackSummary: String?,
    voteUpState: VoteUpState,
    voteUpCount: Int,
    isFavorited: Boolean,
    hotComment: DataHolder.Comment?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onVoteUp: () -> Unit,
    onVoteDown: () -> Unit,
    onFavorite: () -> Unit,
    onComment: () -> Unit,
    onHotCommentClick: () -> Unit,
    onAuthorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = contentFirstColors
    val screenHeightPx = LocalWindowInfo.current.containerSize.height
    val collapsedLimitPx = (screenHeightPx * COLLAPSED_VIEWPORT_RATIO).toInt()
    val authorName = answer?.author?.name ?: fallbackAuthorName
    val avatarSrc = answer?.author?.avatarUrl ?: fallbackAuthorAvatar
    val signature = answer?.author?.headline.orEmpty()
    val html = answer?.content
    val plainText = fallbackSummary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("$ANSWER_CARD_TAG_PREFIX:$itemStableKey"),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = ZhihuSpacing.ScreenGutter)) {
            Spacer(Modifier.height(16.dp))
            AnswerAuthorRow(
                avatarSrc = avatarSrc,
                authorName = authorName,
                signature = signature,
                colors = colors,
                onClick = onAuthorClick,
            )
            Spacer(Modifier.height(12.dp))

            CollapsibleAnswerBody(
                contentKey = itemStableKey,
                answerId = answerId,
                html = html,
                plainText = plainText,
                isExpanded = isExpanded,
                collapsedLimitPx = collapsedLimitPx,
                oneScreenPx = screenHeightPx,
                colors = colors,
                onToggleExpand = onToggleExpand,
            )

            answer?.let { loaded ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = buildString {
                        append(formatFullDateTime(loaded.createdTime).substringBefore(' '))
                        loaded.ipInfo?.takeIf { it.isNotBlank() }?.let {
                            append(" · IP 属地")
                            append(it)
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondaryText,
                )
            }

            Spacer(Modifier.height(12.dp))
            AnswerActionRow(
                voteUpState = voteUpState,
                voteUpCount = voteUpCount,
                isFavorited = isFavorited,
                commentCount = answer?.commentCount ?: 0,
                colors = colors,
                answerId = answerId,
                onVoteUp = onVoteUp,
                onVoteDown = onVoteDown,
                onFavorite = onFavorite,
                onComment = onComment,
            )
            Spacer(Modifier.height(12.dp))

            hotComment?.let { comment ->
                // 点热评 = 看这条回答的评论区，与操作栏的评论按钮同一入口。
                AnswerHotComment(
                    comment = comment,
                    colors = colors,
                    onClick = onHotCommentClick,
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
    }
}

@Composable
private fun AnswerAuthorRow(
    avatarSrc: String?,
    authorName: String?,
    signature: String,
    colors: ContentFirstColors,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, enabled = !authorName.isNullOrBlank(), onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!avatarSrc.isNullOrBlank()) {
            ZhihuNetworkImage(
                model = avatarSrc,
                contentDescription = "作者头像",
                modifier = Modifier.size(32.dp).clip(CircleShape),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.functionalSurface),
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = authorName.orEmpty(),
                style = ZhihuTypography.ContentAuthor,
                color = colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (signature.isNotBlank()) {
                Text(
                    text = signature,
                    style = ZhihuTypography.ContentSignature,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 可折叠的回答正文。
 *
 * 只有「完整高度超过一屏」的回答才截断到 [collapsedLimitPx]：完整高度在一屏以内的短回答直接铺满，
 * 不出现「展开」。展开 / 收起用 `animateContentSize` 驱动 300ms 的高度过渡（产品设计稿 §2.3.1），
 * 因此截断点不会硬切；内容尺寸在测量阶段就能得到，首帧不会先铺满再收起。
 */
@Composable
private fun CollapsibleAnswerBody(
    contentKey: String,
    answerId: Long?,
    html: String?,
    plainText: String?,
    isExpanded: Boolean,
    collapsedLimitPx: Int,
    oneScreenPx: Int,
    colors: ContentFirstColors,
    onToggleExpand: () -> Unit,
) {
    SubcomposeLayout(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(durationMillis = EXPAND_ANIMATION_MILLIS, easing = FastOutSlowInEasing))
            .clipToBounds(),
    ) { constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
        val bodyPlaceable = subcompose("answer_body") {
            if (html != null) {
                RenderMarkdown(
                    html = html,
                    modifier = Modifier.testTag("$ANSWER_CARD_CONTENT_TAG_PREFIX:${answerId ?: contentKey}"),
                    enableScroll = false,
                    selectable = true,
                )
            } else {
                Text(
                    text = plainText.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.primaryText,
                )
            }
        }.first().measure(looseConstraints)
        val needsCollapse = bodyPlaceable.height > oneScreenPx
        val layoutHeight = if (needsCollapse && !isExpanded) {
            collapsedLimitPx.coerceAtMost(bodyPlaceable.height)
        } else {
            bodyPlaceable.height
        }
        val fadeHeightPx = CollapseFadeHeight.roundToPx()
        val showCollapsedDecorations = needsCollapse && !isExpanded
        val fadePlaceable = if (showCollapsedDecorations) {
            subcompose("answer_fade") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CollapseFadeHeight)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, colors.fadeMask.copy(alpha = 0.9f), colors.fadeMask),
                            ),
                        ),
                )
            }.first().measure(Constraints(maxWidth = constraints.maxWidth, maxHeight = layoutHeight.coerceAtLeast(0)))
        } else {
            null
        }
        val expandPlaceable = if (showCollapsedDecorations) {
            subcompose("answer_expand") {
                Row(
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onToggleExpand)
                        .testTag("$ANSWER_CARD_EXPAND_TAG_PREFIX:${answerId ?: contentKey}")
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "展开",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.accent,
                    )
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }.first().measure(looseConstraints)
        } else {
            null
        }
        layout(width = constraints.maxWidth, height = layoutHeight) {
            bodyPlaceable.place(0, 0)
            fadePlaceable?.place(0, (layoutHeight - fadeHeightPx).coerceAtLeast(0))
            expandPlaceable?.place(0, (layoutHeight - expandPlaceable.height).coerceAtLeast(0))
        }
    }
}

/**
 * 操作栏（§2.2 ⑤）：赞同 / 点踩 / 收藏 / 评论，四个按钮居左等间距。
 *
 * 数字以小字号排在图标上方（§2.6「数字以小字号悬于图标右上角」），点赞态只把描边变主题蓝、不加底色。
 */
@Composable
private fun AnswerActionRow(
    voteUpState: VoteUpState,
    voteUpCount: Int,
    isFavorited: Boolean,
    commentCount: Int,
    colors: ContentFirstColors,
    answerId: Long?,
    onVoteUp: () -> Unit,
    onVoteDown: () -> Unit,
    onFavorite: () -> Unit,
    onComment: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnswerActionButton(
            icon = Icons.Outlined.ThumbUp,
            count = voteUpCount,
            tint = if (voteUpState == VoteUpState.Up) colors.accent else colors.secondaryText,
            label = "赞同",
            tag = "answer_action_vote_up:${answerId ?: 0}",
            onClick = onVoteUp,
        )
        AnswerActionButton(
            icon = Icons.Outlined.ThumbDown,
            count = null,
            tint = if (voteUpState == VoteUpState.Down) colors.accent else colors.secondaryText,
            label = "点踩",
            tag = "answer_action_vote_down:${answerId ?: 0}",
            onClick = onVoteDown,
        )
        AnswerActionButton(
            icon = Icons.Outlined.BookmarkBorder,
            count = null,
            tint = if (isFavorited) colors.accent else colors.secondaryText,
            label = "收藏",
            tag = "answer_action_favorite:${answerId ?: 0}",
            onClick = onFavorite,
        )
        AnswerActionButton(
            icon = Icons.Outlined.ChatBubbleOutline,
            count = commentCount,
            tint = colors.secondaryText,
            label = "评论",
            tag = "answer_action_comment:${answerId ?: 0}",
            onClick = onComment,
        )
    }
}

@Composable
private fun AnswerActionButton(
    icon: ImageVector,
    count: Int?,
    tint: Color,
    label: String,
    tag: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag)
            .semantics { contentDescription = if (count != null) "$label $count" else label }
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (count != null) {
            Text(
                text = formatCompactCount(count).replace(" ", ""),
                style = MaterialTheme.typography.labelSmall,
                color = tint,
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** 热评小卡片（§2.2 ⑥）：有条件展示、最多一条，浅灰底、层级弱于正文，点击进入评论区。 */
@Composable
private fun AnswerHotComment(
    comment: DataHolder.Comment,
    colors: ContentFirstColors,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.functionalSurface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(12.dp),
    ) {
        Text(
            text = "热评",
            style = MaterialTheme.typography.labelSmall,
            color = colors.secondaryText,
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (comment.author.avatarUrl.isNotBlank()) {
                ZhihuNetworkImage(
                    model = comment.author.avatarUrl,
                    contentDescription = "热评作者头像",
                    modifier = Modifier.size(18.dp).clip(CircleShape),
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = comment.author.name,
                style = MaterialTheme.typography.labelMedium,
                color = colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodySmall,
            color = colors.primaryText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = colors.secondaryText,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = formatCompactCount(comment.likeCount).replace(" ", ""),
                style = MaterialTheme.typography.labelSmall,
                color = colors.secondaryText,
            )
        }
    }
}
