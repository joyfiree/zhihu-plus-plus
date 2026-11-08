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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.github.zly2006.zhihu.data.target
import com.github.zly2006.zhihu.theme.ZhihuTypography
import com.github.zly2006.zhihu.ui.designsystem.ContentFirstColors
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.util.formatCompactCount
import com.github.zly2006.zhihu.util.parseEmphasizedHtmlTextWithTheme

const val CONTENT_FEED_CARD_TAG_PREFIX = "content_feed_card"
const val CONTENT_FEED_CARD_DISMISS_TAG_PREFIX = "content_feed_card_dismiss"

/**
 * 首页推荐流的内容卡片（产品设计稿 §1.3.3）。
 *
 * 这是一张「逻辑卡片」：不画边框、不设底色，铺在页面底色上，靠通栏细线与上下留白和邻居分界。
 * 信息层级严格按四级排布——问题标题（粗、主色）→ 作者行与摘要（次要色）→ 图片区 → 操作行数据。
 *
 * 与 [FeedCard] 的分工：本组件服务首页推荐流这一种版式，不提供卡片/分割线两种外观切换，
 * 也不展示认证标与更多菜单；其它信息流（关注、搜索、收藏夹）继续使用 [FeedCard]。
 *
 * @param showDivider 是否在底部画通栏分割线。列表最后一项传 false，由页面自己收尾。
 * @param onDismiss 点击右下角 ✕「不感兴趣」。为 null 时不展示该按钮。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentFeedCard(
    item: FeedDisplayItem,
    modifier: Modifier = Modifier,
    showImages: Boolean = true,
    showDivider: Boolean = true,
    onClick: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    menuItems: @Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit = { _ -> },
) {
    val colors = contentFirstColors
    val images = if (showImages && !item.isFiltered) item.cardImageUrls else emptyList()
    val metrics = item.cardMetrics
    val title = item.title
    val summary = item.summary.orEmpty()
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth().testTag("$CONTENT_FEED_CARD_TAG_PREFIX:${item.stableKey}")) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // 长按保留既有的屏蔽菜单入口：产品稿只规定 ✕ 不感兴趣，长按是同一批治理能力的隐藏入口。
                    .combinedClickable(
                        role = Role.Button,
                        onClick = onClick,
                        onLongClick = { showMenu = true },
                    ).padding(horizontal = ZhihuSpacing.ScreenGutter, vertical = 14.dp),
            ) {
                if (title.isNotBlank()) {
                    Text(
                        text = parseEmphasizedHtmlTextWithTheme(title),
                        style = ZhihuTypography.FeedTitle,
                        color = colors.primaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(ZhihuSpacing.Small))
                }

                val authorName = item.authorName
                if (!authorName.isNullOrBlank()) {
                    ContentFeedAuthorRow(
                        avatarSrc = item.avatarSrc,
                        authorName = authorName,
                        colors = colors,
                    )
                    Spacer(Modifier.height(ZhihuSpacing.Small))
                }

                if (summary.isNotBlank()) {
                    Text(
                        text = parseEmphasizedHtmlTextWithTheme(summary),
                        style = ZhihuTypography.FeedSummary,
                        color = colors.secondaryText,
                        // 纯文字卡片没有图片区，摘要多展示一行（产品设计稿 §1.3.3）。
                        maxLines = if (images.isEmpty()) 3 else 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (images.isNotEmpty()) {
                    Spacer(Modifier.height(ZhihuSpacing.Medium))
                    ContentFeedImagePager(images = images, colors = colors)
                }

                Spacer(Modifier.height(ZhihuSpacing.Medium))
                ContentFeedActionRow(
                    metrics = metrics,
                    colors = colors,
                    itemKey = item.stableKey,
                    onDismiss = onDismiss,
                )
            }
            if (showDivider) {
                // 通栏细线：卡片之间唯一的视觉边界，不缩进、不加粗。
                HorizontalDivider(thickness = 1.dp, color = colors.divider)
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
    colors: ContentFirstColors,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!avatarSrc.isNullOrBlank()) {
            ZhihuNetworkImage(
                model = avatarSrc,
                contentDescription = "作者头像",
                modifier = Modifier.size(24.dp).clip(CircleShape),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors.functionalSurface),
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = authorName,
            style = ZhihuTypography.FeedMeta,
            color = colors.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContentFeedImagePager(
    images: List<String>,
    colors: ContentFirstColors,
) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(ZhihuShapeTokens.Thumbnail),
            pageSpacing = ZhihuSpacing.ExtraSmall,
            contentPadding = PaddingValues(0.dp),
        ) { page ->
            ZhihuNetworkImage(
                model = images[page],
                contentDescription = "配图 ${page + 1}/${images.size}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .clip(ZhihuShapeTokens.Badge)
                    .background(Color.Black.copy(alpha = 0.32f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
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

@Composable
private fun ContentFeedActionRow(
    metrics: ContentCardMetrics,
    colors: ContentFirstColors,
    itemKey: String,
    onDismiss: (() -> Unit)?,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        metrics.voteUp?.let {
            ContentFeedMetric(Icons.Outlined.ThumbUp, it, colors, "赞同")
        }
        metrics.favorite?.let {
            if (metrics.voteUp != null) Spacer(Modifier.width(ZhihuSpacing.Large))
            ContentFeedMetric(Icons.Outlined.StarBorder, it, colors, "收藏")
        }
        metrics.comment?.let {
            if (metrics.voteUp != null || metrics.favorite != null) Spacer(Modifier.width(ZhihuSpacing.Large))
            ContentFeedMetric(Icons.Outlined.ChatBubbleOutline, it, colors, "评论")
        }
        Spacer(Modifier.weight(1f))
        if (onDismiss != null) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onDismiss)
                    .testTag("$CONTENT_FEED_CARD_DISMISS_TAG_PREFIX:$itemKey")
                    .semantics { contentDescription = "不感兴趣" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = colors.secondaryText,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun ContentFeedMetric(
    icon: ImageVector,
    count: Int,
    colors: ContentFirstColors,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics { contentDescription = "$label $count" },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = formatCompactCount(count).replace(" ", ""),
            style = ZhihuTypography.FeedMeta,
            color = colors.secondaryText,
        )
    }
}

/** 操作行需要展示的计数；某个目标类型不提供某项时留空，由界面直接跳过而不是显示 0。 */
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
    get() = when (val target = feed?.target) {
        is Feed.AnswerTarget -> target.thumbnails.ifEmpty { listOfNotNull(target.thumbnail) }
        is Feed.PinTarget ->
            target.content
                .filterIsInstance<DataHolder.Pin.ContentImage>()
                .map { it.feedThumbnailUrl }
                .filter { it.isNotBlank() }
        else -> emptyList()
    }
