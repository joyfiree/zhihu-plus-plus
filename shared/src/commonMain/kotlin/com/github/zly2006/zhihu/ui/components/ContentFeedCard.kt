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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.data.DataHolder
import com.github.zly2006.zhihu.data.Feed
import com.github.zly2006.zhihu.data.FeedDisplayItem
import com.github.zly2006.zhihu.data.sourceLabel
import com.github.zly2006.zhihu.data.target
import com.github.zly2006.zhihu.theme.ZhihuTypography
import com.github.zly2006.zhihu.ui.designsystem.ContentFirstColors
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSizes
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.util.formatCompactCount
import com.github.zly2006.zhihu.util.parseEmphasizedHtmlTextWithTheme
import org.jetbrains.compose.resources.painterResource
import zhihu.shared.generated.resources.Res
import zhihu.shared.generated.resources.ic_vote_up_24dp

const val CONTENT_FEED_CARD_TAG_PREFIX = "content_feed_card"
const val CONTENT_FEED_CARD_DISMISS_TAG_PREFIX = "content_feed_card_dismiss"

/**
 * 首页推荐流的内容卡片（官方知乎 11.14 版式，见 `reports/official-align/target-spec.md` §2.3）。
 *
 * 这是一张「逻辑卡片」：不画底色、不设描边，与页面同色，靠左右 16dp 内边距和间距分块。
 * 信息层级严格按官方顺序——标题（18sp Bold 2 行）→ 作者行（20dp 头像 + 作者名 + 关注数）
 * → 摘要（15sp/22dp 3 行）→ 138dp 横滑图 → 24dp 操作行。卡片之间不画分割线（官方靠间距分块）。
 *
 * 与 [FeedCard] 的分工：本组件只服务首页关注/推荐流这一种版式；搜索结果、话题、收藏夹、
 * 热榜等其它信息流继续使用 [FeedCard]，两者不要互相加分支。
 *
 * @param onDismiss 点击右下角 ✕「不感兴趣」。为 null 时不展示该按钮。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentFeedCard(
    item: FeedDisplayItem,
    modifier: Modifier = Modifier,
    showImages: Boolean = true,
    onClick: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    menuItems: @Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit = { _ -> },
) {
    val colors = contentFirstColors
    val images = if (showImages) item.cardImageUrls else emptyList()
    val summary = item.summary.orEmpty()
    val authorName = item.authorName
    val author = item.feed?.target?.author
    val sourceLabel = item.feed?.sourceLabel
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth().testTag("$CONTENT_FEED_CARD_TAG_PREFIX:${item.stableKey}")) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // 长按保留既有的屏蔽菜单入口；✕ 只承担「不感兴趣」这一个动作。
                    .combinedClickable(
                        role = Role.Button,
                        onClick = onClick,
                        onLongClick = { showMenu = true },
                    ).padding(horizontal = ZhihuSizes.FeedCardHorizontalGutter)
                    .padding(top = 13.dp, bottom = 14.dp),
            ) {
                if (!sourceLabel.isNullOrBlank()) {
                    Box(modifier = Modifier.height(22.dp), contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = sourceLabel,
                            style = ZhihuTypography.FeedMeta,
                            color = colors.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(ZhihuSpacing.Small))
                }

                if (item.title.isNotBlank()) {
                    Text(
                        text = parseEmphasizedHtmlTextWithTheme(item.title),
                        style = ZhihuTypography.FeedTitle,
                        color = colors.primaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                }

                if (!authorName.isNullOrBlank()) {
                    ContentFeedAuthorRow(
                        avatarSrc = item.avatarSrc,
                        authorName = authorName,
                        followerCount = author?.followersCount ?: 0,
                        colors = colors,
                    )
                    Spacer(Modifier.height(4.dp))
                }

                if (summary.isNotBlank()) {
                    Text(
                        text = parseEmphasizedHtmlTextWithTheme(summary),
                        style = ZhihuTypography.FeedSummary,
                        color = colors.secondaryText,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (images.isNotEmpty()) {
                    Spacer(Modifier.height(ZhihuSpacing.Small))
                    ContentFeedImagePager(images = images)
                }

                Spacer(Modifier.height(ZhihuSpacing.Small))
                ContentFeedActionRow(
                    metrics = item.cardMetrics,
                    colors = colors,
                    itemKey = item.stableKey,
                    onDismiss = onDismiss,
                )
            }
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            menuItems { showMenu = false }
        }
    }
}

@Composable
private fun ContentFeedAuthorRow(
    avatarSrc: String?,
    authorName: String,
    followerCount: Int,
    colors: ContentFirstColors,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!avatarSrc.isNullOrBlank()) {
            ZhihuNetworkImage(
                model = avatarSrc,
                contentDescription = "作者头像",
                modifier = Modifier.size(ZhihuSizes.FeedAvatarSize).clip(CircleShape),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(ZhihuSizes.FeedAvatarSize)
                    .clip(CircleShape)
                    .background(colors.functionalSurface),
            )
        }
        Spacer(Modifier.width(ZhihuSpacing.Small))
        Text(
            text = authorName,
            style = ZhihuTypography.FeedAuthorName,
            color = colors.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (followerCount > 0) {
            Spacer(Modifier.width(ZhihuSpacing.Small))
            // 官方 11.14 的关注数是 1dp 描边 chip（无底色），不是裸文字。
            Text(
                text = "${formatCompactCount(followerCount).replace(" ", "")}人关注",
                style = ZhihuTypography.FeedMeta,
                color = colors.secondaryText,
                maxLines = 1,
                modifier = Modifier
                    .border(1.dp, colors.divider, ZhihuShapeTokens.Thumbnail)
                    .padding(horizontal = 6.dp),
            )
        }
    }
}

/** 138dp 高的横滑图：4:3 单页、6dp 圆角、4dp 间距，右下角保留页圆点指示。 */
@Composable
private fun ContentFeedImagePager(images: List<String>) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(ZhihuSizes.FeedImageHeight),
            contentPadding = PaddingValues(0.dp),
            pageSize = PageSize.Fixed(ZhihuSizes.FeedImageHeight * 4f / 3f),
            pageSpacing = ZhihuSpacing.ExtraSmall,
        ) { page ->
            ZhihuNetworkImage(
                model = images[page],
                contentDescription = "配图 ${page + 1}/${images.size}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(ZhihuShapeTokens.Thumbnail),
                contentScale = ContentScale.Crop,
            )
        }
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 6.dp)
                    .clip(ZhihuShapeTokens.Badge)
                    .background(Color.Black.copy(alpha = 0.32f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(ZhihuSpacing.ExtraSmall),
            ) {
                repeat(images.size) { index ->
                    val active = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (active) 5.dp else 4.dp)
                            .clip(CircleShape)
                            .background(if (active) Color.White else Color.White.copy(alpha = 0.48f)),
                    )
                }
            }
        }
    }
}

/** 操作行固定三组：赞同 / 收藏 / 评论（官方即使计数为 0 也显示），右端是可选的 ✕ 不感兴趣。 */
@Composable
private fun ContentFeedActionRow(
    metrics: ContentCardMetrics,
    colors: ContentFirstColors,
    itemKey: String,
    onDismiss: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ZhihuSizes.FeedActionRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 官方三组操作的图标起点是屏幕 x=16/86/160dp（内容左边距 16dp），因此前两组占固定宽度槽位：
        // 第一组 70dp、第二组 74dp，第三组自然宽度。
        Box(modifier = Modifier.width(70.dp), contentAlignment = Alignment.CenterStart) {
            ContentFeedMetric(count = metrics.voteUp ?: 0, label = "赞同", colors = colors) {
                Icon(
                    painter = painterResource(Res.drawable.ic_vote_up_24dp),
                    contentDescription = null,
                    tint = colors.tertiaryText,
                    modifier = Modifier.size(ZhihuSizes.FeedActionIconSize),
                )
            }
        }
        Box(modifier = Modifier.width(74.dp), contentAlignment = Alignment.CenterStart) {
            ContentFeedMetric(count = metrics.favorite ?: 0, label = "收藏", colors = colors) {
                Icon(
                    imageVector = Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = colors.tertiaryText,
                    modifier = Modifier.size(ZhihuSizes.FeedActionIconSize),
                )
            }
        }
        ContentFeedMetric(count = metrics.comment ?: 0, label = "评论", colors = colors) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = colors.tertiaryText,
                modifier = Modifier.size(ZhihuSizes.FeedActionIconSize),
            )
        }
        Spacer(Modifier.weight(1f))
        if (onDismiss != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onDismiss)
                    .testTag("$CONTENT_FEED_CARD_DISMISS_TAG_PREFIX:$itemKey")
                    .semantics { contentDescription = "不感兴趣" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = colors.tertiaryText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ContentFeedMetric(
    count: Int,
    label: String,
    colors: ContentFirstColors,
    icon: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics { contentDescription = "$label $count" },
    ) {
        icon()
        Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
        Text(
            text = formatCompactCount(count).replace(" ", ""),
            style = ZhihuTypography.FeedMeta,
            color = colors.tertiaryText,
        )
    }
}

/**
 * 操作行需要展示的计数；某个目标类型不提供某项时留空。
 *
 * 首页卡片固定展示三组（缺失按 0 渲染），问题回答页等其它调用方仍可据此判断字段是否可用。
 */
internal data class ContentCardMetrics(
    val voteUp: Int?,
    val favorite: Int?,
    val comment: Int?,
)

internal val FeedDisplayItem.cardMetrics: ContentCardMetrics
    get() {
        val structured = when (val target = feed?.target) {
            is Feed.AnswerTarget -> ContentCardMetrics(
                voteUp = target.voteupCount.takeIf { it >= 0 },
                favorite = target.favoriteCount,
                comment = target.commentCount.takeIf { it >= 0 },
            )
            is Feed.ArticleTarget -> ContentCardMetrics(
                voteUp = target.voteupCount,
                favorite = target.favoriteCount,
                comment = target.commentCount,
            )
            is Feed.PinTarget -> ContentCardMetrics(
                voteUp = target.likeCount,
                favorite = target.favoriteCount,
                comment = target.commentCount,
            )
            is Feed.VideoTarget -> ContentCardMetrics(
                voteUp = target.voteCount.takeIf { it >= 0 },
                favorite = null,
                comment = target.commentCount,
            )
            else -> null
        }
        // 手机版推荐卡片没有结构化 target，只在 details 里留下「2777 赞同 · 523 评论 · 673 收藏」，
        // 这里做一次兜底解析，保证操作行在任何推荐源下都有数据而不是只剩一个 ✕。
        return structured?.takeIf { it.voteUp != null || it.favorite != null || it.comment != null }
            ?: details.metricsFromDetailsText()
    }

/** 从「N 赞同 / N 评论 / N 收藏」这类计数文本里取操作行数据；解析不到的项留空。 */
private fun String.metricsFromDetailsText(): ContentCardMetrics {
    fun countOf(segment: String, suffix: String): Int? {
        if (!segment.endsWith(suffix)) return null
        val raw = segment.removeSuffix(suffix).trim()
        val multiplier = if (raw.endsWith("万")) 10_000 else 1
        val numeric = raw.removeSuffix("万").trim().toDoubleOrNull() ?: return null
        return (numeric * multiplier).toInt()
    }
    val segments = split('·').map { it.trim() }
    return ContentCardMetrics(
        voteUp = segments.firstNotNullOfOrNull { countOf(it, "赞同") },
        favorite = segments.firstNotNullOfOrNull { countOf(it, "收藏") },
        comment = segments.firstNotNullOfOrNull { countOf(it, "评论") },
    )
}

/** 卡片图片区取图顺序：回答的多图 > 回答单图 > 想法的图集；其余目标类型没有可横滑的图。 */
internal val FeedDisplayItem.cardImageUrls: List<String>
    get() = if (isFiltered) {
        emptyList()
    } else {
        when (val target = feed?.target) {
            is Feed.AnswerTarget -> target.thumbnails.ifEmpty { listOfNotNull(target.thumbnail) }
            is Feed.PinTarget ->
                target.content
                    .filterIsInstance<DataHolder.Pin.ContentImage>()
                    .map { it.feedThumbnailUrl }
                    .filter { it.isNotBlank() }
            else -> emptyList()
        }
    }
