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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.data.DataHolder
import com.github.zly2006.zhihu.data.OfficialBadge
import com.github.zly2006.zhihu.data.VoteUpState
import com.github.zly2006.zhihu.data.officialBadge
import com.github.zly2006.zhihu.markdown.RenderMarkdown
import com.github.zly2006.zhihu.theme.ZhihuTypography
import com.github.zly2006.zhihu.ui.AnswerEndorsementChip
import com.github.zly2006.zhihu.ui.designsystem.ContentFirstColors
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSizes
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.util.formatCompactCount
import com.github.zly2006.zhihu.util.formatFullDateTime
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import zhihu.shared.generated.resources.Res
import zhihu.shared.generated.resources.ic_vote_down_24dp
import zhihu.shared.generated.resources.ic_vote_up_24dp

const val ANSWER_CARD_TAG_PREFIX = "answer_card"
const val ANSWER_CARD_EXPAND_TAG_PREFIX = "answer_card_expand"
const val ANSWER_CARD_CONTENT_TAG_PREFIX = "answer_card_content"

/** 作者行高度（官方实测 47.6dp）。 */
private val AnswerAuthorRowHeight = 47.6.dp

/** 「N 人赞同了该回答」行高度（官方实测 28.6dp）。 */
private val AnswerVoteLineHeight = 28.6.dp

/** 截断处渐变遮罩高度（target-spec §3.3 ⑤：24dp）。 */
private val CollapseFadeHeight = 24.dp

/** 「欢迎参与讨论」胶囊宽度（官方实测 132dp，高度取 [ZhihuSizes.DiscussPillHeight]）。 */
private val DiscussPillWidth = 132.dp

/** 展开 / 收起动画时长（target-spec §3.3 ⑤：250–300ms）。 */
private const val EXPAND_ANIMATION_MILLIS = 300

/** 分享图标的触控区域（视觉 24dp，见 target-spec §3.3 ①）。 */
private val ShareTouchTarget = 40.dp

/** 热评卡圆角（target-spec §3.3 ⑧：8dp）。 */
private val HotCommentCornerRadius = 8.dp

/**
 * 回答瀑布流中的单条回答卡（target-spec §3.3）。
 *
 * 官方 11.14 的回答卡是「扁平连续列表」中的一段：作者行 → 赞同行 → 收录 chip → 正文（0.618 屏后截断）
 * → 元信息 → 48dp 操作行 → 可选热评卡，卡片之间用通栏发丝线 + 16dp 留白分界，不画卡片底色。
 * 正文默认 17sp / 24dp 行高，与阅读设置解耦但设置仍可覆盖。
 */
@Composable
fun AnswerWaterfallCard(
    itemStableKey: String,
    answerId: Long?,
    answer: DataHolder.Answer?,
    fallbackAuthorName: String?,
    fallbackAuthorAvatar: String?,
    fallbackAuthorBadge: DataHolder.BadgeV2?,
    fallbackSummary: String?,
    voteUpState: VoteUpState,
    voteUpCount: Int,
    isFavorited: Boolean,
    isAuthorFollowed: Boolean,
    hotComment: DataHolder.Comment?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onVoteUp: () -> Unit,
    onVoteDown: () -> Unit,
    onFavorite: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onFollowAuthor: () -> Unit,
    onHotCommentClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onActionRowPositionChanged: (topPx: Float, bottomPx: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = contentFirstColors
    val screenHeightPx = LocalWindowInfo.current.containerSize.height
    val collapsedLimitPx = (screenHeightPx * ZhihuSizes.ANSWER_COLLAPSED_HEIGHT_RATIO).toInt()
    val author = answer?.author
    val endorsements = answer?.endorsementItems.orEmpty()
    val meta = answer?.let { loaded ->
        buildString {
            append(formatFullDateTime(loaded.createdTime).substringBeforeLast(':'))
            loaded.ipInfo?.takeIf { it.isNotBlank() }?.let {
                append(" · ")
                append(it)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("$ANSWER_CARD_TAG_PREFIX:$itemStableKey"),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = ZhihuSpacing.ScreenGutter)) {
            Spacer(Modifier.height(ZhihuSpacing.Large))
            AnswerAuthorRow(
                avatarSrc = author?.avatarUrl ?: fallbackAuthorAvatar,
                authorName = author?.name ?: fallbackAuthorName,
                signature = author?.headline.orEmpty(),
                badge = (author?.badgeV2 ?: fallbackAuthorBadge)?.officialBadge(),
                colors = colors,
                isFollowed = isAuthorFollowed,
                showFollow = author != null,
                onFollow = onFollowAuthor,
                onShare = onShare,
                onAuthorClick = onAuthorClick,
            )
            if (voteUpCount > 0) {
                AnswerVoteSummary(voteUpCount = voteUpCount, colors = colors)
            }
            if (endorsements.isNotEmpty()) {
                Spacer(Modifier.height(ZhihuSpacing.Small))
                Row(horizontalArrangement = Arrangement.spacedBy(ZhihuSpacing.Small)) {
                    endorsements.take(2).forEach { endorsement ->
                        AnswerEndorsementChip(endorsement = endorsement)
                    }
                }
            }
            Spacer(Modifier.height(ZhihuSpacing.Small))
            CollapsibleAnswerBody(
                contentKey = itemStableKey,
                answerId = answerId,
                html = answer?.content,
                plainText = fallbackSummary,
                isExpanded = isExpanded,
                collapsedLimitPx = collapsedLimitPx,
                colors = colors,
                onToggleExpand = onToggleExpand,
            )
            if (meta != null) {
                Spacer(Modifier.height(ZhihuSpacing.Small))
                Text(
                    text = meta,
                    style = ZhihuTypography.AnswerMeta,
                    color = colors.tertiaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(ZhihuSpacing.Small))
            AnswerActionRow(
                answerId = answerId,
                voteUpState = voteUpState,
                voteUpCount = voteUpCount,
                isFavorited = isFavorited,
                commentCount = answer?.commentCount ?: 0,
                colors = colors,
                onVoteUp = onVoteUp,
                onVoteDown = onVoteDown,
                onFavorite = onFavorite,
                onComment = onComment,
                onShare = onShare,
                onActionRowPositionChanged = onActionRowPositionChanged,
            )
            hotComment?.let { comment ->
                Spacer(Modifier.height(ZhihuSpacing.Medium))
                AnswerHotComment(comment = comment, colors = colors, onClick = onHotCommentClick)
            }
            Spacer(Modifier.height(ZhihuSpacing.Large))
        }
        HorizontalDivider(thickness = ZhihuSizes.DividerThickness, color = colors.divider)
    }
}

@Composable
private fun AnswerAuthorRow(
    avatarSrc: String?,
    authorName: String?,
    signature: String,
    badge: OfficialBadge?,
    colors: ContentFirstColors,
    isFollowed: Boolean,
    showFollow: Boolean,
    onFollow: () -> Unit,
    onShare: () -> Unit,
    onAuthorClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = AnswerAuthorRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).clickable(role = Role.Button, onClick = onAuthorClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(avatarSrc = avatarSrc, colors = colors)
            Spacer(Modifier.width(ZhihuSpacing.Small))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = authorName.orEmpty(),
                        style = ZhihuTypography.ContentAuthor,
                        color = colors.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (badge != null) {
                        Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
                        AuthorBadge(badge = badge)
                    }
                }
                if (signature.isNotBlank()) {
                    Text(
                        text = signature,
                        style = ZhihuTypography.ContentSignature,
                        color = colors.tertiaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (showFollow) {
            Spacer(Modifier.width(ZhihuSpacing.Small))
            FollowPill(isFollowed = isFollowed, onClick = onFollow)
        }
        Box(
            modifier = Modifier
                .size(ShareTouchTarget)
                .clickable(role = Role.Button, onClick = onShare)
                .semantics { contentDescription = "分享回答" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = null,
                tint = colors.secondaryText,
                modifier = Modifier.size(ZhihuSizes.Icon),
            )
        }
    }
}

/** 未关注：品牌蓝文字 + 12% 蓝底；已关注：次要文字 + 功能底（target-spec §3.3 ①）。 */
@Composable
internal fun FollowPill(
    isFollowed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = contentFirstColors
    Row(
        modifier = modifier
            .width(ZhihuSizes.FollowPillWidth)
            .height(ZhihuSizes.FollowPillHeight)
            .clip(ZhihuShapeTokens.Pill)
            .background(if (isFollowed) colors.functionalSurface else colors.accent.copy(alpha = 0.12f))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isFollowed) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = if (isFollowed) "已关注" else "关注",
            style = ZhihuTypography.FeedMeta,
            color = if (isFollowed) colors.secondaryText else colors.accent,
        )
    }
}

@Composable
private fun Avatar(avatarSrc: String?, colors: ContentFirstColors) {
    if (!avatarSrc.isNullOrBlank()) {
        ZhihuNetworkImage(
            model = avatarSrc,
            contentDescription = "作者头像",
            modifier = Modifier.size(ZhihuSizes.AnswerAvatarSize).clip(CircleShape),
        )
    } else {
        Box(
            modifier = Modifier
                .size(ZhihuSizes.AnswerAvatarSize)
                .clip(CircleShape)
                .background(colors.functionalSurface),
        )
    }
}

@Composable
private fun AnswerVoteSummary(voteUpCount: Int, colors: ContentFirstColors) {
    Row(
        modifier = Modifier.fillMaxWidth().height(AnswerVoteLineHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${formatCompactCount(voteUpCount).replace(" ", "")} 人赞同了该回答",
            style = ZhihuTypography.AnswerVoteLine,
            color = colors.tertiaryText,
            maxLines = 1,
        )
        Spacer(Modifier.width(2.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.tertiaryText,
            modifier = Modifier.size(10.dp),
        )
    }
}

/**
 * 可折叠的回答正文（target-spec §3.3 ④⑤）。
 *
 * 完整高度超过 [collapsedLimitPx] 时截断到 0.618 屏，右下角压渐变遮罩 + 「展开 ⌄」；
 * 展开后文末出现「收起 ⌃」。高度变化用 [animateContentSize] 驱动 300ms 过渡。
 */
@Composable
private fun CollapsibleAnswerBody(
    contentKey: String,
    answerId: Long?,
    html: String?,
    plainText: String?,
    isExpanded: Boolean,
    collapsedLimitPx: Int,
    colors: ContentFirstColors,
    onToggleExpand: () -> Unit,
) {
    // 收起时把「展开」按钮带回视口附近（官方行为：收起后停在该回答的截断处而不是跳回顶部）。
    val collapseAnchor = remember(contentKey) { BringIntoViewRequester() }
    var wasExpanded by remember(contentKey) { mutableStateOf(isExpanded) }
    LaunchedEffect(isExpanded) {
        if (wasExpanded && !isExpanded) {
            delay(EXPAND_ANIMATION_MILLIS + 32L)
            collapseAnchor.bringIntoView()
        }
        wasExpanded = isExpanded
    }
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
                    bodyTextStyle = ZhihuTypography.AnswerBody,
                )
            } else {
                Text(
                    text = plainText.orEmpty(),
                    style = ZhihuTypography.AnswerBody,
                    color = colors.primaryText,
                )
            }
        }.first().measure(looseConstraints)
        val needsCollapse = bodyPlaceable.height > collapsedLimitPx
        val togglePlaceable = if (needsCollapse) {
            subcompose("answer_toggle") {
                if (isExpanded) {
                    Row(
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = onToggleExpand)
                            .testTag("$ANSWER_CARD_EXPAND_TAG_PREFIX:${answerId ?: contentKey}")
                            .padding(start = ZhihuSpacing.Small, top = ZhihuSpacing.ExtraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "收起", style = ZhihuTypography.AnswerVoteLine, color = colors.accent)
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                } else {
                    // 「展开」压一层横向渐变：官方把按钮放在正文最后一行的右侧，文字从按钮处淡出而不是被硬切。
                    val overlayBackground = Brush.horizontalGradient(
                        listOf(Color.Transparent, colors.fadeMask, colors.fadeMask),
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                            .background(overlayBackground)
                            .bringIntoViewRequester(collapseAnchor)
                            .clickable(role = Role.Button, onClick = onToggleExpand)
                            .testTag("$ANSWER_CARD_EXPAND_TAG_PREFIX:${answerId ?: contentKey}")
                            .padding(start = ZhihuSpacing.Large, end = ZhihuSpacing.ExtraSmall, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "展开", style = ZhihuTypography.AnswerVoteLine, color = colors.accent)
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }.first().measure(looseConstraints)
        } else {
            null
        }
        val endToggleHeightPx = if (needsCollapse && isExpanded) togglePlaceable?.height ?: 0 else 0
        val layoutHeight = if (needsCollapse && !isExpanded) {
            collapsedLimitPx.coerceAtMost(bodyPlaceable.height)
        } else {
            bodyPlaceable.height + endToggleHeightPx
        }
        val fadePlaceable = if (needsCollapse && !isExpanded) {
            subcompose("answer_fade") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CollapseFadeHeight)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, colors.fadeMask.copy(alpha = 0.86f), colors.fadeMask),
                            ),
                        ),
                )
            }.first().measure(Constraints(maxWidth = constraints.maxWidth, maxHeight = layoutHeight.coerceAtLeast(0)))
        } else {
            null
        }
        layout(width = constraints.maxWidth, height = layoutHeight) {
            bodyPlaceable.place(0, 0)
            fadePlaceable?.place(0, (layoutHeight - fadePlaceable.height).coerceAtLeast(0))
            togglePlaceable?.place(
                x = (constraints.maxWidth - togglePlaceable.width).coerceAtLeast(0),
                y = if (isExpanded) bodyPlaceable.height else (layoutHeight - togglePlaceable.height).coerceAtLeast(0),
            )
        }
    }
}

/** 回答操作行（target-spec §3.3 ⑦）：讨论胶囊 + 赞同/反对/收藏/评论/分享 五组等宽图标。 */
@Composable
private fun AnswerActionRow(
    answerId: Long?,
    voteUpState: VoteUpState,
    voteUpCount: Int,
    isFavorited: Boolean,
    commentCount: Int,
    colors: ContentFirstColors,
    onVoteUp: () -> Unit,
    onVoteDown: () -> Unit,
    onFavorite: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onActionRowPositionChanged: (topPx: Float, bottomPx: Float) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ZhihuSizes.AnswerActionRowHeight)
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                onActionRowPositionChanged(bounds.top, bounds.bottom)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DiscussPill(onClick = onComment, colors = colors)
        Spacer(Modifier.width(ZhihuSpacing.Small))
        AnswerActionGroup(
            modifier = Modifier.weight(1f),
            count = voteUpCount,
            label = "赞同",
            tag = "answer_action_vote_up:${answerId ?: 0}",
            tint = if (voteUpState == VoteUpState.Up) colors.liked else colors.tertiaryText,
            onClick = onVoteUp,
        ) {
            VoteIcon(up = true)
        }
        AnswerActionGroup(
            modifier = Modifier.weight(1f),
            count = null,
            label = "反对",
            tag = "answer_action_vote_down:${answerId ?: 0}",
            tint = if (voteUpState == VoteUpState.Down) colors.liked else colors.tertiaryText,
            onClick = onVoteDown,
        ) {
            VoteIcon(up = false)
        }
        AnswerActionGroup(
            modifier = Modifier.weight(1f),
            count = null,
            label = "收藏",
            tag = "answer_action_favorite:${answerId ?: 0}",
            tint = if (isFavorited) colors.favorited else colors.tertiaryText,
            onClick = onFavorite,
        ) {
            Icon(
                imageVector = Icons.Outlined.StarBorder,
                contentDescription = null,
                modifier = Modifier.size(ZhihuSizes.AnswerActionIconSize),
            )
        }
        AnswerActionGroup(
            modifier = Modifier.weight(1f),
            count = commentCount,
            label = "评论",
            tag = "answer_action_comment:${answerId ?: 0}",
            tint = colors.tertiaryText,
            onClick = onComment,
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(ZhihuSizes.AnswerActionIconSize),
            )
        }
        AnswerActionGroup(
            modifier = Modifier.weight(1f),
            count = null,
            label = "分享",
            tag = "answer_action_share:${answerId ?: 0}",
            tint = colors.tertiaryText,
            onClick = onShare,
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = null,
                modifier = Modifier.size(ZhihuSizes.AnswerActionIconSize),
            )
        }
    }
}

/**
 * 操作行的单组图标（数字在上、图标在下）。
 *
 * [tint] 同时用于数字与图标：图标通过 `LocalContentColor` 继承，避免每个调用点重复传色。
 */
@Composable
internal fun AnswerActionGroup(
    count: Int?,
    label: String,
    tag: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag)
            .semantics {
                contentDescription = if (count != null) "$label ${formatCompactCount(count)}" else label
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (count != null) {
            Text(
                text = formatCompactCount(count).replace(" ", ""),
                style = ZhihuTypography.ActionCount,
                color = tint,
                maxLines = 1,
            )
        } else {
            Spacer(Modifier.height(16.dp))
        }
        CompositionLocalProvider(LocalContentColor provides tint) {
            content()
        }
    }
}

/** 赞同 / 反对图标沿用官方三角描边矢量；颜色继承 [androidx.compose.material3.LocalContentColor]。 */
@Composable
internal fun VoteIcon(up: Boolean) {
    Icon(
        painter = painterResource(if (up) Res.drawable.ic_vote_up_24dp else Res.drawable.ic_vote_down_24dp),
        contentDescription = null,
        modifier = Modifier.size(ZhihuSizes.AnswerActionIconSize),
    )
}

@Composable
private fun DiscussPill(onClick: () -> Unit, colors: ContentFirstColors) {
    Box(
        modifier = Modifier
            .width(DiscussPillWidth)
            .height(ZhihuSizes.DiscussPillHeight)
            .clip(ZhihuShapeTokens.Pill)
            .background(colors.functionalSurface)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "欢迎参与讨论",
            style = ZhihuTypography.DiscussPillLabel,
            color = colors.tertiaryText,
            maxLines = 1,
        )
    }
}

/** 热评卡（target-spec §3.3 ⑧）：功能底 + 8dp 圆角，头像 20dp / 昵称 13sp / 内容 14sp 两行。 */
@Composable
private fun AnswerHotComment(
    comment: DataHolder.Comment,
    colors: ContentFirstColors,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HotCommentCornerRadius))
            .background(colors.functionalSurface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = ZhihuSpacing.Medium, vertical = ZhihuSpacing.Small),
    ) {
        if (comment.author.avatarUrl.isNotBlank()) {
            ZhihuNetworkImage(
                model = comment.author.avatarUrl,
                contentDescription = "热评作者头像",
                modifier = Modifier.size(20.dp).clip(CircleShape),
            )
            Spacer(Modifier.width(ZhihuSpacing.Small))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.author.name,
                style = ZhihuTypography.AnswerMeta,
                color = colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = comment.content,
                    style = ZhihuTypography.AnswerVoteLine,
                    color = colors.secondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(ZhihuSpacing.Small))
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = colors.tertiaryText,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = formatCompactCount(comment.likeCount).replace(" ", ""),
                    style = ZhihuTypography.ActionCount,
                    color = colors.tertiaryText,
                )
            }
        }
    }
}
