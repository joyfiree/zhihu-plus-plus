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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.platform.rememberSettingsStore
import com.github.zly2006.zhihu.ui.designsystem.Motion
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.rememberObservedSetting
import com.github.zly2006.zhihu.ui.subscreens.DEFAULT_SHOW_CONTENT_END_MARKER
import com.github.zly2006.zhihu.ui.subscreens.PREF_SHOW_CONTENT_END_MARKER
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

val ProgressIndicatorFooter: @Composable (LazyListState) -> Unit = { state ->
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 下拉刷新期间这颗转圈会和顶部指示器重复，所以用淡出而不是直接摘掉——
        // 直接摘掉会让 footer 高度瞬间从 56dp 塌成 16dp，列表内容跟着跳一下。
        AnimatedVisibility(
            visible = LocalPullToRefreshViewModel.current?.isPullToRefresh != true,
            enter = fadeIn(Motion.fadeTween()),
            exit = fadeOut(Motion.fadeTween()),
        ) {
            CircularProgressIndicator()
        }
    }
}

private const val DUPLICATE_KEY_PREFIX = "PaginatedListDuplicateKey"

/** 最多参与错峰的条目数：再多就会让第 9 项之后明显「等到」才出现。 */
private const val MAX_STAGGERED_ITEMS = 8

/** 相邻条目的入场间隔（ms）——28ms ≈ 两帧，足够被读成「依次」，又不至于让首屏显得慢。 */
private const val ITEM_STAGGER_MILLIS = 28

internal fun <T> uniquePaginatedListKeys(
    items: List<T>,
    key: (T) -> Any,
): List<Any> {
    val originalKeys = items.map(key)
    val reservedKeys = originalKeys.toSet()
    val usedKeys = mutableSetOf<Any>()
    val duplicateOccurrences = mutableMapOf<Any, Int>()

    return originalKeys.map { original ->
        if (usedKeys.add(original)) {
            original
        } else {
            val occurrence = duplicateOccurrences.getOrElse(original) { 1 }
            duplicateOccurrences[original] = occurrence + 1
            uniqueDuplicatePaginatedListKey(original, occurrence, reservedKeys, usedKeys)
        }
    }
}

private fun uniqueDuplicatePaginatedListKey(
    original: Any,
    occurrence: Int,
    reservedKeys: Set<Any>,
    usedKeys: MutableSet<Any>,
): Any {
    var collision = 0
    while (true) {
        val candidate = "$DUPLICATE_KEY_PREFIX:$occurrence:$collision:$original"
        if (candidate !in reservedKeys && usedKeys.add(candidate)) {
            return candidate
        }
        collision++
    }
}

@Composable
private fun <T> LazyItemScope.PaginatedListItem(
    item: T,
    index: Int,
    animateItemPlacement: Boolean,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    // 首屏错峰淡入 + 上移；滚动回来的 item 因为 remember(item) 已为 true 不再重播。
    var appeared by remember(item) { mutableStateOf(false) }
    LaunchedEffect(item) {
        if (!appeared) {
            val staggerMillis = (index.coerceAtMost(MAX_STAGGERED_ITEMS)) * ITEM_STAGGER_MILLIS
            if (staggerMillis > 0) {
                // 用动画时钟等待而不是 delay(ms)：系统把「动画时长」设为 0 时，
                // 动画时钟会立刻收敛到终值，错峰延迟随之消失（delay 不会）。
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = staggerMillis, easing = LinearEasing),
                ) { _, _ -> }
            }
            appeared = true
        }
    }
    val alpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = Motion.fadeTween(),
        label = "list_item_alpha",
    )
    val offset by animateDpAsState(
        targetValue = if (appeared) 0.dp else 18.dp,
        animationSpec = Motion.springMedium(),
        label = "list_item_offset",
    )
    Box(
        modifier = Modifier
            .animateItem(
                fadeInSpec = Motion.springMedium(),
                fadeOutSpec = Motion.springMedium(),
                placementSpec = if (animateItemPlacement) Motion.springMedium() else null,
            ).graphicsLayer {
                this.alpha = alpha
                translationY = offset.toPx()
            },
    ) {
        itemContent(item)
    }
}

@Composable
fun <T> PaginatedList(
    items: List<T>,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    listState: LazyListState = rememberLazyListState(),
    reverseLayout: Boolean = false,
    animateItemPlacement: Boolean = true,
    isEnd: () -> Boolean = { false },
    /** 首屏为空且加载失败时展示的错误信息；非空会用 [ZhihuErrorState] 取代骨架屏，并给出重试入口。 */
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    footer: @Composable ((LazyListState) -> Unit)? = null,
    key: ((T) -> Any)? = null,
    topContent: LazyListScope.() -> Unit = {},
    bottomContent: LazyListScope.() -> Unit = {},
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    val settings = rememberSettingsStore()
    val showContentEndMarker by rememberObservedSetting(settings, PREF_SHOW_CONTENT_END_MARKER) {
        getBoolean(PREF_SHOW_CONTENT_END_MARKER, DEFAULT_SHOW_CONTENT_END_MARKER)
    }
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.last()
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 3
            }
        }
    }

    LaunchedEffect(shouldLoadMore, items.size, isEnd()) {
        while (shouldLoadMore && items.isNotEmpty() && !isEnd()) {
            onLoadMore()
            // 留一帧给调用方发布新数据或标记列表结束。
            delay(50.milliseconds)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
    ) {
        topContent(this)

        // 首屏空表 + 未结束 = 正在加载：用与卡片版式同构的骨架屏占位，避免白屏和跳版。
        // 关键点：骨架用**独立 key**（skeleton-N）。若沿用索引 key，数据到达时同一个槽位会被
        // 真卡片「原地替换」，Lazy 列表认为 key 没变，于是骨架硬切成卡片（就是「骨架突然变卡片」）。
        // 换 key 之后，骨架走 fadeOut、卡片走 fadeIn，得到一次真正的交叉淡入。
        if (items.isEmpty() && errorMessage != null) {
            item {
                ZhihuErrorState(
                    description = errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.fillParentMaxHeight(0.7f),
                )
            }
        } else if (items.isEmpty() && !isEnd()) {
            items(count = 4, key = { index -> "zhihu_skeleton_$index" }) {
                ZhihuSkeletonCard(
                    modifier = Modifier
                        .animateItem(fadeOutSpec = Motion.fadeTween())
                        .padding(horizontal = 16.dp, vertical = ZhihuSpacing.CardGap),
                )
            }
        }
        // 确实没有内容（已结束且空）：给统一的空态，而不是一块白。
        if (items.isEmpty() && isEnd() && errorMessage == null) {
            item {
                ZhihuEmptyState(
                    title = "这里还什么都没有",
                    description = "下拉刷新，或稍后再来看看",
                    modifier = Modifier.fillParentMaxHeight(0.7f),
                )
            }
        }

        if (key != null) {
            val itemKeys = uniquePaginatedListKeys(items, key)
            itemsIndexed(items, key = { index, _ -> itemKeys[index] }) { index, item ->
                PaginatedListItem(item, index, animateItemPlacement, itemContent)
            }
        } else {
            itemsIndexed(items) { index, item ->
                PaginatedListItem(item, index, animateItemPlacement, itemContent)
            }
        }

        bottomContent(this)

        item(key = "zhihu_list_footer") {
            // 「加载中 → 已经到底啦」不是两种内容，而是同一个位置的两个状态：
            // 用 AnimatedContent 做交叉淡入，避免转圈突然变成一行文字造成的视线跳动。
            AnimatedContent(
                targetState = isEnd(),
                transitionSpec = {
                    fadeIn(Motion.fadeTween()) togetherWith fadeOut(Motion.fadeTween())
                },
                label = "list_footer_state",
            ) { ended ->
                if (ended) {
                    if (!showContentEndMarker) {
                        ZhihuListFooter(text = "已经到底啦")
                    }
                } else {
                    footer?.invoke(listState)
                }
            }
        }
        if (isEnd() && items.isNotEmpty() && showContentEndMarker) {
            pageTurnContentEndMarker()
        }
    }
}
