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

package com.github.zly2006.zhihu.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fleeksoft.ksoup.Ksoup
import com.github.zly2006.zhihu.data.Collection
import com.github.zly2006.zhihu.data.DataHolder
import com.github.zly2006.zhihu.data.Feed
import com.github.zly2006.zhihu.data.FeedDisplayItem
import com.github.zly2006.zhihu.data.VoteUpState
import com.github.zly2006.zhihu.data.decodeQuestionContentDetail
import com.github.zly2006.zhihu.data.decodeZhihuCommentData
import com.github.zly2006.zhihu.data.navDestination
import com.github.zly2006.zhihu.data.target
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.ArticleType
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.NavDestination
import com.github.zly2006.zhihu.navigation.Person
import com.github.zly2006.zhihu.navigation.Question
import com.github.zly2006.zhihu.navigation.Topic
import com.github.zly2006.zhihu.navigation.WriteAnswer
import com.github.zly2006.zhihu.platform.rememberSettingsStore
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.platform.rememberZhihuWebUrlOpener
import com.github.zly2006.zhihu.reading.RegisterReadingQueueSource
import com.github.zly2006.zhihu.theme.ZhihuTypography
import com.github.zly2006.zhihu.ui.components.AnswerWaterfallCard
import com.github.zly2006.zhihu.ui.components.BounceOnChange
import com.github.zly2006.zhihu.ui.components.CollectionDialogComponent
import com.github.zly2006.zhihu.ui.components.CommentScreenComponent
import com.github.zly2006.zhihu.ui.components.FeedPullToRefresh
import com.github.zly2006.zhihu.ui.components.PaginatedList
import com.github.zly2006.zhihu.ui.components.ProgressIndicatorFooter
import com.github.zly2006.zhihu.ui.components.ShareDialog
import com.github.zly2006.zhihu.ui.components.ZhihuFilterChip
import com.github.zly2006.zhihu.ui.components.ZhihuTopAppBar
import com.github.zly2006.zhihu.ui.components.cardMetrics
import com.github.zly2006.zhihu.ui.components.getShareText
import com.github.zly2006.zhihu.ui.components.handleShareAction
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.components.rememberShareActionExecutor
import com.github.zly2006.zhihu.ui.components.zhihuTopAppBarColors
import com.github.zly2006.zhihu.ui.designsystem.Motion
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.ui.designsystem.rememberHapticController
import com.github.zly2006.zhihu.util.formatCompactCount
import com.github.zly2006.zhihu.util.jsonObject
import com.github.zly2006.zhihu.viewmodel.ContentLoadEnvironment
import com.github.zly2006.zhihu.viewmodel.addReadHistory
import com.github.zly2006.zhihu.viewmodel.feed.QuestionFeedViewModel
import com.github.zly2006.zhihu.viewmodel.getOrFetchContentDetail
import com.github.zly2006.zhihu.viewmodel.postSigned
import com.github.zly2006.zhihu.viewmodel.rememberPaginationEnvironment
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.math.roundToInt

const val QUESTION_SCREEN_LIST_TAG = "question_screen_list"
const val QUESTION_TITLE_TAG = "question_title"
const val QUESTION_DETAIL_TOGGLE_TAG = "question_detail_toggle"
const val QUESTION_DETAIL_CONTENT_TAG = "question_detail_content"
const val QUESTION_DETAIL_PREVIEW_TAG = "question_detail_preview"
const val QUESTION_SORT_DEFAULT_TAG = "question_sort_default"
const val QUESTION_SORT_UPDATED_TAG = "question_sort_updated"
const val QUESTION_FOLLOW_BUTTON_TAG = "question_follow_button"
const val QUESTION_VIEW_LOG_BUTTON_TAG = "question_view_log_button"
const val QUESTION_SHARE_BUTTON_TAG = "question_share_button"
const val QUESTION_WRITE_ANSWER_BUTTON_TAG = "question_write_answer_button"
const val QUESTION_COMMENTS_BUTTON_TAG = "question_comments_button"
const val QUESTION_IMMERSIVE_BACK_TAG = "question_immersive_back"
const val QUESTION_IMMERSIVE_TITLE_TAG = "question_immersive_title"
const val QUESTION_COLLAPSE_FAB_TAG = "question_collapse_fab"
const val QUESTION_STICKY_ACTIONS_TAG = "question_sticky_actions"
const val QUESTION_STATS_TAG = "question_stats"
private const val QUESTION_DETAIL_COLLAPSE_THRESHOLD = 100
private val QUESTION_DETAIL_COLLAPSED_MAX_HEIGHT: Dp = 180.dp
private val QUESTION_DETAIL_MASK_HEIGHT: Dp = 88.dp
private val QUESTION_DETAIL_TOGGLE_ZONE_HEIGHT: Dp = 56.dp

/** 进入沉浸态需要向下阅读的距离（产品设计稿 §2.3.4「下滑阅读一段距离」）。 */
private val IMMERSIVE_ENTER_DISTANCE = 320.dp

/** 沉浸态上滑时问题标题的停留时长。 */
private const val IMMERSIVE_TITLE_VISIBLE_MILLIS = 2_000L

/** 热评门槛：点赞数低于此值的评论不进入热评模块（产品设计稿 §2.2 ⑥）。 */
private const val HOT_COMMENT_MIN_LIKES = 10

/** 吸底操作栏与收起按钮的高度，正文底部要按它预留安全边距。 */
private val StickyActionBarHeight = 56.dp

private suspend fun loadQuestion(
    environment: ContentLoadEnvironment,
    question: Question,
): DataHolder.Question? {
    environment.addReadHistory(question.questionId.toString(), "question")
    val include =
        "read_count,visit_count,answer_count,voteup_count,comment_count,follower_count,detail,excerpt,author,relationship.is_following,topics"
    val jsonObject =
        environment.fetchJson("https://www.zhihu.com/api/v4/questions/${question.questionId}", include)
            ?: return null
    val questionData = decodeQuestionContentDetail(jsonObject)
    environment.postHistoryDestination(Question(question.questionId, questionData.title))
    environment.recordContentOpenEvent(destination = question, questionId = question.questionId)
    return questionData
}

/**
 * 回答页（产品设计稿 §二）：问题下的回答瀑布流。
 *
 * 页面三层架构：
 * - 悬浮控件层：沉浸态的左上角返回按钮、上滑时浮现的问题标题；
 * - 内容层：唯一运动层，回答按「分割线 + 作者行 + 正文 + 尾注 + 操作栏 + 热评」连续排列；
 * - 固定操作层：展开某条长回答时吸附在底部的操作栏与右下角「收起」按钮。
 *
 * 长回答的内容区最高为屏幕高度的 0.618（黄金比），截断处渐隐并给出蓝色「展开 ▾」；
 * 展开后操作栏吸底、右下角出现「收起」，收起时回到截断位置。
 *
 * 页面会记录内容打开来源和历史记录，并复用问题的关注、排序、评论与分享入口。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    question: Question,
) {
    val colors = contentFirstColors
    val readingPlayerOverlayPadding = LocalReadingPlayerOverlayPadding.current
    val settings = rememberSettingsStore()
    val executeShareAction = rememberShareActionExecutor()
    val openZhihuWebUrl = rememberZhihuWebUrlOpener()
    val navigator = LocalNavigator.current
    val viewModel: QuestionFeedViewModel = viewModel(key = "question_${question.questionId}") {
        QuestionFeedViewModel(question.questionId)
    }
    val answerReadingQueueSourceId = "question:${question.questionId}:answers:${viewModel.sortOrder}"
    RegisterReadingQueueSource(
        sourceId = answerReadingQueueSourceId,
        items = viewModel.displayItems,
    )
    val paginationEnvironment = rememberPaginationEnvironment(allowGuestAccess = false)
    val listState = rememberLazyListState()
    var followerCount by remember(viewModel.questionDetail) { mutableIntStateOf(viewModel.questionDetail?.followerCount ?: 0) }
    var isFollowing by remember(viewModel.questionDetail) {
        mutableStateOf(viewModel.questionDetail?.relationship?.isFollowing ?: false)
    }
    var showComments by rememberSaveable(question.questionId) { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var commentsTarget by remember { mutableStateOf<NavDestination?>(null) }
    val userMessages = rememberUserMessageSink()
    val haptics = rememberHapticController()
    var isQuestionDetailExpanded by rememberSaveable(question.questionId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val questionContentPlainText = remember(viewModel.questionDetail?.detail.orEmpty()) {
        Ksoup.parse(viewModel.questionDetail?.detail.orEmpty()).text().trim()
    }
    val shareText = getShareText(question, viewModel.questionDetail?.title ?: question.title)
    val topBarTitleThresholdPx = with(LocalDensity.current) { 160.dp.roundToPx() }
    val showTopBarTitle by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset >= topBarTitleThresholdPx
        }
    }
    val pageTurnTarget = rememberPageTurnTarget(
        listState = listState,
        enabled = !showComments && !showShareDialog,
    )

    // ── 回答瀑布流的页面级状态 ────────────────────────────────────────────────
    var expandedAnswerKey by rememberSaveable(question.questionId) { mutableStateOf<String?>(null) }
    var immersiveTitleVisible by remember { mutableStateOf(false) }
    var immersiveTitleToken by remember { mutableIntStateOf(0) }
    val answerDetails = remember { mutableStateMapOf<Long, DataHolder.Answer>() }
    val loadedAnswerIds = remember { mutableStateMapOf<Long, Boolean>() }
    val hotComments = remember { mutableStateMapOf<Long, DataHolder.Comment>() }
    val queriedHotCommentIds = remember { mutableStateMapOf<Long, Boolean>() }
    val voteStates = remember { mutableStateMapOf<Long, VoteUpState>() }
    val voteCounts = remember { mutableStateMapOf<Long, Int>() }
    val favoritedAnswers = remember { mutableStateMapOf<Long, Boolean>() }
    var collectionTargetAnswerId by remember { mutableStateOf<Long?>(null) }
    val answerCollections = remember { mutableStateMapOf<String, Collection>() }
    var collectionsLoadedForAnswer by remember { mutableStateOf<Long?>(null) }

    val density = LocalDensity.current
    val immersiveEnterPx = with(density) { IMMERSIVE_ENTER_DISTANCE.toPx() }
    val immersiveScrollState = remember { ImmersiveScrollState() }
    val immersiveScrollConnection = remember(immersiveEnterPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy == 0f) return Offset.Zero
                if (dy < 0f) {
                    // 手指上滑：内容向下阅读，累计到阈值后进入沉浸态。
                    immersiveScrollState.accumulatedDownPx += -dy
                    if (!immersiveScrollState.immersive && immersiveScrollState.accumulatedDownPx > immersiveEnterPx) {
                        immersiveScrollState.immersive = true
                    }
                    immersiveScrollState.accumulatedDownPx = immersiveScrollState.accumulatedDownPx.coerceAtMost(immersiveEnterPx * 4)
                } else {
                    immersiveScrollState.accumulatedDownPx = (immersiveScrollState.accumulatedDownPx - dy).coerceAtLeast(0f)
                    if (immersiveScrollState.immersive) {
                        // 上滑是「想确认自己在哪」的动作：问题标题短暂浮现后自动淡出。
                        immersiveScrollState.titleRevealToken += 1
                    }
                }
                return Offset.Zero
            }
        }
    }
    val immersive = immersiveScrollState.immersive
    LaunchedEffect(immersiveScrollState.titleRevealToken, immersive) {
        if (!immersive || immersiveScrollState.titleRevealToken == 0) {
            immersiveTitleVisible = false
            return@LaunchedEffect
        }
        immersiveTitleVisible = true
        immersiveTitleToken = immersiveScrollState.titleRevealToken
        kotlinx.coroutines.delay(IMMERSIVE_TITLE_VISIBLE_MILLIS)
        if (immersiveTitleToken == immersiveScrollState.titleRevealToken) {
            immersiveTitleVisible = false
        }
    }
    // 回到顶部时自动退出沉浸态，让用户重新看到问题头部。
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
            .collect { atTop ->
                if (atTop && immersiveScrollState.immersive) {
                    immersiveScrollState.immersive = false
                    immersiveScrollState.accumulatedDownPx = 0f
                }
            }
    }

    // 展开态属于「当前正在读的这一条」：这条回答被划出视口（用户已经在读下一条）时自动收起，
    // 吸底操作栏随之消失，避免出现「内容早划走了，底部还停着上一条的点赞点踩」。
    LaunchedEffect(listState, expandedAnswerKey, viewModel.displayItems.size) {
        val key = expandedAnswerKey ?: return@LaunchedEffect
        val index = viewModel.displayItems.indexOfFirst { it.stableKey == key }
        if (index < 0) {
            expandedAnswerKey = null
            return@LaunchedEffect
        }
        // 列表第 0 项是问题头部，回答从第 1 项开始。
        val listIndex = index + 1
        snapshotFlow {
            val visible = listState.layoutInfo.visibleItemsInfo
            visible.isNotEmpty() && visible.none { it.index == listIndex }
        }.collect { offscreen ->
            if (offscreen) expandedAnswerKey = null
        }
    }

    fun answerIdOf(item: FeedDisplayItem): Long? =
        ((item.feed?.target as? Feed.AnswerTarget)?.id)
            ?: (item.navDestination as? Article)?.takeIf { it.type == ArticleType.Answer }?.id

    fun toggleVote(answerId: Long, next: VoteUpState) {
        scope.launch {
            try {
                val response = paginationEnvironment
                    .postSigned("https://www.zhihu.com/api/v4/answers/$answerId/voters") {
                        setBody(mapOf("type" to next.key))
                        contentType(ContentType.Application.Json)
                    }.jsonObject()
                voteStates[answerId] = next
                response["voteup_count"]
                    ?.jsonPrimitive
                    ?.content
                    ?.toIntOrNull()
                    ?.let { voteCounts[answerId] = it }
            } catch (e: Exception) {
                userMessages.showShortMessage("操作失败: ${e.message}")
            }
        }
    }

    // 加载问题详情和答案
    LaunchedEffect(question.questionId, viewModel) {
        if (viewModel.displayItems.isEmpty()) {
            launch { viewModel.refresh(paginationEnvironment) }
        }
        if (viewModel.questionDetail != null) return@LaunchedEffect
        try {
            val questionData = loadQuestion(paginationEnvironment, question)
            if (questionData != null) {
                viewModel.questionDetail = questionData
            } else {
                userMessages.showShortMessage("获取问题详情失败")
            }
        } catch (e: Exception) {
            userMessages.showShortMessage("加载失败: ${e.message}")
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.page)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colors.page,
            topBar = {
                if (!immersive) {
                    QuestionTopBar(
                        title = viewModel.questionDetail?.title ?: question.title,
                        showTitle = showTopBarTitle,
                        onNavigateBack = navigator.onNavigateBack,
                        onOpenLog = {
                            try {
                                openZhihuWebUrl("https://www.zhihu.com/question/${question.questionId}/log")
                            } catch (e: Exception) {
                                userMessages.showShortMessage("打开日志失败: ${e.message}")
                            }
                        },
                        onShare = {
                            if (shareText != null) {
                                handleShareAction(question, settings, executeShareAction) { showShareDialog = true }
                            }
                        },
                        canShare = shareText != null,
                    )
                }
            },
        ) { innerPadding ->
            FeedPullToRefresh(
                viewModel,
                padding = PaddingValues(top = innerPadding.calculateTopPadding()),
            ) {
                PaginatedList(
                    items = viewModel.displayItems,
                    onLoadMore = { viewModel.loadMore(paginationEnvironment) },
                    isEnd = { viewModel.isEnd },
                    key = { it.stableKey },
                    listState = listState,
                    modifier = Modifier
                        .padding(innerPadding)
                        .nestedScroll(immersiveScrollConnection)
                        .pageTurnViewportWithGuide(pageTurnTarget)
                        .testTag(QUESTION_SCREEN_LIST_TAG),
                    contentPadding = PaddingValues(
                        bottom = readingPlayerOverlayPadding + if (expandedAnswerKey != null) StickyActionBarHeight else 0.dp,
                    ),
                    footer = ProgressIndicatorFooter,
                    topContent = {
                        item {
                            Column {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    QuestionHeaderSection(
                                        title = viewModel.questionDetail?.title ?: question.title,
                                        visitCount = viewModel.questionDetail?.visitCount ?: 0,
                                        commentCount = viewModel.questionDetail?.commentCount ?: 0,
                                        followerCount = followerCount,
                                        onShowComments = { showComments = true },
                                    )
                                    if (viewModel.questionDetail != null) {
                                        QuestionAnimatedBodyHeader(
                                            questionId = question.questionId,
                                            questionContent = viewModel.questionDetail?.detail.orEmpty(),
                                            questionContentPlainText = questionContentPlainText,
                                            topics = viewModel.questionDetail?.topics.orEmpty(),
                                            onTopicClick = { topic -> navigator.onNavigate(Topic(topic.id, topic.name)) },
                                            isExpanded = isQuestionDetailExpanded,
                                            onToggleExpanded = { isQuestionDetailExpanded = !isQuestionDetailExpanded },
                                            isFollowing = isFollowing,
                                            onFollowClick = {
                                                scope.launch {
                                                    val nextFollowing = !isFollowing
                                                    viewModel.followQuestion(paginationEnvironment, nextFollowing)
                                                    // 触觉在状态翻转的同一帧内发出：用户按下与「已关注」的视觉确认同时到达。
                                                    haptics.confirm()
                                                    isFollowing = nextFollowing
                                                    followerCount = (followerCount + if (nextFollowing) 1 else -1).coerceAtLeast(0)
                                                    userMessages.showShortMessage(if (nextFollowing) "已关注问题" else "已取消关注问题")
                                                }
                                            },
                                            onWriteAnswerClick = {
                                                navigator.onNavigate(
                                                    WriteAnswer(
                                                        questionId = question.questionId,
                                                        questionTitle = viewModel.questionDetail?.title ?: question.title,
                                                        questionDetail = viewModel.questionDetail?.detail.orEmpty(),
                                                    ),
                                                )
                                            },
                                            answerCount = viewModel.questionDetail?.answerCount ?: 0,
                                            currentSort = viewModel.sortOrder,
                                            onSortChange = { sortOrder ->
                                                viewModel.updateSortOrder(sortOrder)
                                                viewModel.refresh(paginationEnvironment)
                                            },
                                        )
                                    }
                                }
                                // 第一条回答之前的分割线（产品设计稿 §2.2 ①），与回答之间的分割线同款。
                                HorizontalDivider(thickness = 1.dp, color = colors.divider)
                            }
                        }
                    },
                ) { item ->
                    val answerId = answerIdOf(item)
                    val answer = answerId?.let { answerDetails[it] }
                    val isExpanded = expandedAnswerKey == item.stableKey
                    val fallbackMetrics = item.cardMetrics

                    // 可见的回答才拉详情与热评：LazyColumn 只组合视口内的条目，请求量随阅读推进而不是随页数增长。
                    LaunchedEffect(answerId, item.stableKey) {
                        val id = answerId ?: return@LaunchedEffect
                        if (loadedAnswerIds[id] != true) {
                            loadedAnswerIds[id] = true
                            (
                                paginationEnvironment.getOrFetchContentDetail(
                                    Article(type = ArticleType.Answer, id = id),
                                ) as? DataHolder.Answer
                            )?.let { detail ->
                                answerDetails[id] = detail
                                voteStates[id] = VoteUpState.from(detail.reaction?.relation?.vote)
                                voteCounts[id] = detail.voteupCount
                            }
                        }
                        if (queriedHotCommentIds[id] != true) {
                            queriedHotCommentIds[id] = true
                            loadHotComment(paginationEnvironment, id)?.let { hotComments[id] = it }
                        }
                    }

                    AnswerWaterfallCard(
                        itemStableKey = item.stableKey,
                        answerId = answerId,
                        answer = answer,
                        fallbackAuthorName = item.authorName,
                        fallbackAuthorAvatar = item.avatarSrc,
                        fallbackSummary = item.summary,
                        voteUpState = answerId?.let { voteStates[it] } ?: VoteUpState.Neutral,
                        voteUpCount = answerId?.let { voteCounts[it] } ?: fallbackMetrics.voteUp ?: 0,
                        isFavorited = answerId?.let { favoritedAnswers[it] } ?: false,
                        hotComment = answerId?.let { hotComments[it] },
                        onHotCommentClick = {
                            if (answerId != null) {
                                commentsTarget = Article(type = ArticleType.Answer, id = answerId)
                                showComments = true
                            }
                        },
                        isExpanded = isExpanded,
                        onToggleExpand = { expandedAnswerKey = if (isExpanded) null else item.stableKey },
                        onVoteUp = {
                            answerId?.let { id ->
                                val current = voteStates[id] ?: VoteUpState.Neutral
                                toggleVote(id, if (current == VoteUpState.Up) VoteUpState.Neutral else VoteUpState.Up)
                            }
                        },
                        onVoteDown = {
                            answerId?.let { id ->
                                val current = voteStates[id] ?: VoteUpState.Neutral
                                toggleVote(id, if (current == VoteUpState.Down) VoteUpState.Neutral else VoteUpState.Down)
                            }
                        },
                        onFavorite = {
                            answerId?.let { id ->
                                collectionTargetAnswerId = id
                                if (collectionsLoadedForAnswer != id) {
                                    scope.launch {
                                        loadAnswerCollections(paginationEnvironment, id).forEach { collection ->
                                            answerCollections[collection.id] = collection
                                        }
                                        collectionsLoadedForAnswer = id
                                    }
                                }
                            }
                        },
                        onComment = {
                            if (answerId != null) {
                                commentsTarget = Article(type = ArticleType.Answer, id = answerId)
                                showComments = true
                            }
                        },
                        onAuthorClick = {
                            val author = answer?.author
                            if (author != null) {
                                navigator.onNavigate(
                                    Person(id = author.id, urlToken = author.urlToken, name = author.name),
                                )
                            }
                        },
                        // 瀑布流里正文已经完整呈现，卡片本身不再跳转到单条回答页；
                        // 需要评论、作者主页时用卡片内的操作栏与作者行。
                        modifier = Modifier.testTag("question_feed_item_${item.stableKey}"),
                    )
                }
            }
        }

        // ── 固定操作层：展开态吸底操作栏 + 右下角「收起」 ──────────────────────
        val expandedItem = expandedAnswerKey?.let { key -> viewModel.displayItems.firstOrNull { it.stableKey == key } }
        val expandedId = expandedItem?.let { answerIdOf(it) }
        // 「固定操作层」属于阅读态而不是页面 chrome：即使处于沉浸态也要保留吸底栏与「收起」，
        // 否则展开长文后一旦进入沉浸态就再也没有收起入口（产品设计稿 §2.3.3 的收起按钮无条件出现）。
        if (expandedItem != null) {
            val answer = expandedId?.let { answerDetails[it] }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(colors.page)
                    .testTag(QUESTION_STICKY_ACTIONS_TAG),
            ) {
                HorizontalDivider(thickness = 1.dp, color = colors.divider)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ZhihuSpacing.ScreenGutter)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                        .height(StickyActionBarHeight),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StickyActionItem(
                        icon = Icons.Outlined.ThumbUp,
                        count = expandedId?.let { voteCounts[it] }
                            ?: expandedItem.cardMetrics.voteUp ?: 0,
                        tint = if (expandedId?.let { voteStates[it] } == VoteUpState.Up) colors.accent else colors.secondaryText,
                        label = "赞同",
                        onClick = {
                            expandedId?.let { id ->
                                val current = voteStates[id] ?: VoteUpState.Neutral
                                toggleVote(id, if (current == VoteUpState.Up) VoteUpState.Neutral else VoteUpState.Up)
                            }
                        },
                    )
                    StickyActionItem(
                        icon = Icons.Outlined.ThumbDown,
                        count = null,
                        tint = if (expandedId?.let { voteStates[it] } == VoteUpState.Down) colors.accent else colors.secondaryText,
                        label = "点踩",
                        onClick = {
                            expandedId?.let { id ->
                                val current = voteStates[id] ?: VoteUpState.Neutral
                                toggleVote(id, if (current == VoteUpState.Down) VoteUpState.Neutral else VoteUpState.Down)
                            }
                        },
                    )
                    StickyActionItem(
                        icon = Icons.Outlined.BookmarkBorder,
                        count = null,
                        tint = if (expandedId?.let { favoritedAnswers[it] } == true) colors.accent else colors.secondaryText,
                        label = "收藏",
                        onClick = {
                            expandedId?.let { id ->
                                collectionTargetAnswerId = id
                                if (collectionsLoadedForAnswer != id) {
                                    scope.launch {
                                        loadAnswerCollections(paginationEnvironment, id).forEach { collection ->
                                            answerCollections[collection.id] = collection
                                        }
                                        collectionsLoadedForAnswer = id
                                    }
                                }
                            }
                        },
                    )
                    StickyActionItem(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        count = answer?.commentCount ?: 0,
                        tint = colors.secondaryText,
                        label = "评论",
                        onClick = {
                            if (expandedId != null) {
                                commentsTarget = Article(type = ArticleType.Answer, id = expandedId)
                                showComments = true
                            }
                        },
                    )
                }
            }
            // 「收起」避开系统手势区，并压在吸底栏之上，避免遮挡操作按钮。
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = StickyActionBarHeight +
                            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                    ).testTag(QUESTION_COLLAPSE_FAB_TAG),
                shape = ZhihuShapeTokens.Pill,
                color = colors.floatingSurface,
                shadowElevation = 6.dp,
                onClick = {
                    val key = expandedAnswerKey
                    expandedAnswerKey = null
                    val index = viewModel.displayItems.indexOfFirst { it.stableKey == key }
                    if (index >= 0) {
                        // 列表第 0 项是问题头部，回答从第 1 项开始；收起后回到该回答的顶部，
                        // 截断位置就在一屏之内，用户顺着正文还能看到原来的「展开」处。
                        scope.launch { listState.animateScrollToItem(index + 1) }
                    }
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.primaryText,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "收起",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primaryText,
                    )
                }
            }
        }

        // ── 悬浮控件层：沉浸态返回按钮 + 上滑浮现的问题标题 ────────────────────
        if (immersive) {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = statusBarPadding + 8.dp)
                    .size(40.dp)
                    .testTag(QUESTION_IMMERSIVE_BACK_TAG),
                shape = CircleShape,
                color = colors.floatingSurface,
                shadowElevation = 4.dp,
                onClick = navigator.onNavigateBack,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = colors.primaryText,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            AnimatedVisibility(
                visible = immersiveTitleVisible,
                enter = fadeIn(tween(Motion.DURATION_SHORT)),
                exit = fadeOut(tween(Motion.DURATION_MEDIUM)),
                modifier = Modifier.align(Alignment.TopCenter),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(colors.page.copy(alpha = 0.96f), colors.page.copy(alpha = 0.86f), Color.Transparent),
                            ),
                        ).padding(top = statusBarPadding + 12.dp, bottom = 16.dp, start = 64.dp, end = 16.dp)
                        .testTag(QUESTION_IMMERSIVE_TITLE_TAG),
                ) {
                    Text(
                        text = viewModel.questionDetail?.title ?: question.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    CommentScreenComponent(
        showComments = showComments,
        onDismiss = {
            showComments = false
            commentsTarget = null
        },
        content = commentsTarget ?: question,
    )

    // 收藏夹选择：回答瀑布流里的收藏按钮需要按当前回答加载可选收藏夹。
    CollectionDialogComponent(
        showDialog = collectionTargetAnswerId != null,
        onDismiss = { collectionTargetAnswerId = null },
        collections = answerCollections.values.toList(),
        onLoadCollections = {
            collectionTargetAnswerId?.let { id ->
                scope.launch {
                    loadAnswerCollections(paginationEnvironment, id).forEach { collection ->
                        answerCollections[collection.id] = collection
                    }
                    collectionsLoadedForAnswer = id
                }
            }
        },
        onToggleFavorite = { collection ->
            val answerId = collectionTargetAnswerId ?: return@CollectionDialogComponent
            scope.launch {
                val nextFavorited = !collection.isFavorited
                val ok = toggleAnswerCollection(paginationEnvironment, answerId, collection.id, remove = !nextFavorited)
                if (ok) {
                    answerCollections[collection.id] = collection.copy(isFavorited = nextFavorited)
                    favoritedAnswers[answerId] = answerCollections.values.any { it.isFavorited }
                } else {
                    userMessages.showShortMessage("收藏操作失败")
                }
            }
        },
        onCreateCollection = { title, description, isPublic ->
            scope.launch {
                createAnswerCollection(paginationEnvironment, title, description, isPublic)
                collectionTargetAnswerId?.let { id ->
                    loadAnswerCollections(paginationEnvironment, id).forEach { collection ->
                        answerCollections[collection.id] = collection
                    }
                }
            }
        },
    )

    // 分享对话框
    if (shareText != null) {
        ShareDialog(
            content = question,
            shareText = shareText,
            showDialog = showShareDialog,
            onDismissRequest = { showShareDialog = false },
        )
    }
}

/** 沉浸态的滚动状态：跨重组持有累计阅读距离与标题浮现令牌。 */
private class ImmersiveScrollState {
    var accumulatedDownPx by mutableFloatStateOf(0f)
    var immersive by mutableStateOf(false)
    var titleRevealToken by mutableIntStateOf(0)
}

/**
 * 拉取一条回答的热评。
 *
 * 使用与评论区相同的 `root_comment?order=score` 接口取分数最高的一条，点赞数低于
 * [HOT_COMMENT_MIN_LIKES] 时返回 null —— 产品设计稿要求「不满足阈值时整体不渲染，不留空白」。
 */
private suspend fun loadHotComment(
    environment: com.github.zly2006.zhihu.viewmodel.ZhihuApiEnvironment,
    answerId: Long,
): DataHolder.Comment? {
    val json = environment.fetchJson(
        "https://www.zhihu.com/api/v4/comment_v5/answers/$answerId/root_comment?order=score&limit=1",
        "data[*].content,excerpt,headline",
    ) ?: return null
    val comment = decodeZhihuCommentData(json, 1).firstOrNull() ?: return null
    // 评论接口返回的是 HTML 富文本，热评预览只要纯文本，否则卡片里会露出 <p> 标签。
    val plain = Ksoup.parse(comment.content).text()
    return comment
        .copy(content = plain)
        .takeIf { plain.isNotBlank() && it.likeCount >= HOT_COMMENT_MIN_LIKES }
}

private fun decodeCollections(json: JsonObject): List<Collection> = json["data"]
    ?.jsonArray
    ?.mapNotNull { element ->
        runCatching {
            com.github.zly2006.zhihu.data.ZhihuJson
                .decodeJson<Collection>(element)
        }.getOrNull()
    }.orEmpty()

private suspend fun loadAnswerCollections(
    environment: com.github.zly2006.zhihu.viewmodel.ZhihuApiEnvironment,
    answerId: Long,
): List<Collection> {
    val json = environment.fetchJson(
        "https://api.zhihu.com/collections/contents/answer/$answerId?limit=50",
        "",
    ) ?: return emptyList()
    return decodeCollections(json)
}

private suspend fun toggleAnswerCollection(
    environment: com.github.zly2006.zhihu.viewmodel.ZhihuApiEnvironment,
    answerId: Long,
    collectionId: String,
    remove: Boolean,
): Boolean = runCatching {
    val response = environment.httpClient().put("https://api.zhihu.com/collections/contents/answer/$answerId") {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody("${if (remove) "remove" else "add"}_collections=$collectionId")
    }
    response.status.isSuccess()
}.getOrDefault(false)

private suspend fun createAnswerCollection(
    environment: com.github.zly2006.zhihu.viewmodel.ZhihuApiEnvironment,
    title: String,
    description: String,
    isPublic: Boolean,
) {
    environment.postSigned("https://www.zhihu.com/api/v4/collections") {
        contentType(ContentType.Application.Json)
        setBody(
            buildJsonObject {
                put("title", title)
                put("description", description)
                put("is_public", isPublic)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionTopBar(
    title: String,
    showTitle: Boolean,
    onNavigateBack: () -> Unit,
    onOpenLog: () -> Unit,
    onShare: () -> Unit,
    canShare: Boolean,
) {
    val colors = contentFirstColors
    ZhihuTopAppBar(
        colors = zhihuTopAppBarColors(
            containerColor = colors.page,
            scrolledContainerColor = colors.page,
            titleContentColor = colors.primaryText,
            navigationIconContentColor = colors.secondaryText,
            actionIconContentColor = colors.secondaryText,
        ),
        title = {
            AnimatedContent(
                targetState = showTitle,
                transitionSpec = {
                    (fadeIn(Motion.fadeTween()) + slideInVertically(Motion.fadeTween()) { it / 2 }) togetherWith
                        (fadeOut(Motion.fadeTween()) + slideOutVertically(Motion.fadeTween()) { -it / 2 })
                },
                label = "question_top_bar_title",
            ) { shouldShowTitle ->
                Text(
                    text = if (shouldShowTitle) title else "问题",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            IconButton(onClick = onOpenLog, modifier = Modifier.testTag(QUESTION_VIEW_LOG_BUTTON_TAG)) {
                Icon(Icons.Filled.History, contentDescription = "日志")
            }
            IconButton(
                onClick = onShare,
                enabled = canShare,
                modifier = Modifier.testTag(QUESTION_SHARE_BUTTON_TAG),
            ) {
                Icon(Icons.Filled.Share, contentDescription = "分享")
            }
        },
    )
}

@Composable
private fun QuestionHeaderSection(
    title: String,
    visitCount: Int,
    commentCount: Int,
    followerCount: Int,
    onShowComments: () -> Unit,
) {
    val colors = contentFirstColors
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectionContainer(modifier = Modifier.questionSelectionWorkaround()) {
            Text(
                text = title,
                style = ZhihuTypography.ContentTitle,
                color = colors.primaryText,
                modifier = Modifier.testTag(QUESTION_TITLE_TAG),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlowRow(
                modifier = Modifier.weight(1f).testTag(QUESTION_STATS_TAG),
                horizontalArrangement = Arrangement.spacedBy(16.dp), // 水平间距
                verticalArrangement = Arrangement.spacedBy(8.dp), // 垂直间距
            ) {
                StatItem(icon = Icons.Outlined.Visibility, text = "$visitCount 浏览")
                StatItem(icon = Icons.Outlined.ChatBubbleOutline, text = "$commentCount 评论")
                StatItem(icon = Icons.Outlined.FavoriteBorder, text = "$followerCount 关注")
            }
            OutlinedButton(
                onClick = onShowComments,
                modifier = Modifier.testTag(QUESTION_COMMENTS_BUTTON_TAG),
            ) {
                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "评论")
                Spacer(Modifier.width(8.dp))
                Text("评论")
            }
        }
    }
}

@Composable
private fun QuestionAnimatedBodyHeader(
    questionId: Long,
    questionContent: String,
    questionContentPlainText: String,
    topics: List<DataHolder.Topic>,
    onTopicClick: (DataHolder.Topic) -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onWriteAnswerClick: () -> Unit,
    answerCount: Int,
    currentSort: String,
    onSortChange: (String) -> Unit,
) {
    val allowDetailCollapse =
        questionContentPlainText.length >= QUESTION_DETAIL_COLLAPSE_THRESHOLD || questionContent.contains("<img")
    val density = LocalDensity.current
    val sectionSpacingPx = with(density) { 16.dp.roundToPx() }
    val collapsedViewportHeightPx = with(density) { QUESTION_DETAIL_COLLAPSED_MAX_HEIGHT.roundToPx() }
    var fullViewportHeightPx by remember(questionId) { mutableIntStateOf(0) }
    var animationInitialized by remember(questionId) { mutableStateOf(false) }
    val animatedViewportHeightPx = remember(questionId) { Animatable(collapsedViewportHeightPx.toFloat()) }
    val animationSpec = remember { tween<Float>(durationMillis = Motion.DURATION_LONG, easing = Motion.EmphasizedDecelerateEasing) }

    LaunchedEffect(questionId) {
        animationInitialized = false
        animatedViewportHeightPx.snapTo(collapsedViewportHeightPx.toFloat())
    }
    LaunchedEffect(isExpanded, fullViewportHeightPx, collapsedViewportHeightPx) {
        if (!allowDetailCollapse || fullViewportHeightPx <= 0) return@LaunchedEffect
        val collapsedTarget = collapsedViewportHeightPx.coerceAtMost(fullViewportHeightPx).toFloat()
        val targetHeight = if (isExpanded) fullViewportHeightPx.toFloat() else collapsedTarget
        if (!animationInitialized) {
            animationInitialized = true
            animatedViewportHeightPx.snapTo(targetHeight)
            return@LaunchedEffect
        }
        if ((animatedViewportHeightPx.value - targetHeight).let { if (it < 0f) -it else it } < 0.5f) {
            return@LaunchedEffect
        }
        animatedViewportHeightPx.animateTo(targetValue = targetHeight, animationSpec = animationSpec)
    }

    val collapsedTargetPx = collapsedViewportHeightPx.coerceAtMost(fullViewportHeightPx).toFloat()
    val expandedRangePx = (fullViewportHeightPx.toFloat() - collapsedTargetPx).coerceAtLeast(1f)
    val expandProgress =
        if (!allowDetailCollapse || fullViewportHeightPx <= 0) {
            1f
        } else {
            ((animatedViewportHeightPx.value - collapsedTargetPx) / expandedRangePx).coerceIn(0f, 1f)
        }
    val viewportHeightPx =
        if (allowDetailCollapse) {
            animatedViewportHeightPx.value.roundToInt()
        } else {
            fullViewportHeightPx
        }

    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val looseConstraints =
            constraints.copy(
                minWidth = 0,
                minHeight = 0,
                maxHeight = Constraints.Infinity,
            )
        val detailPlaceable =
            subcompose("detail") {
                if (questionContent.isEmpty() && topics.isEmpty()) return@subcompose
                if (allowDetailCollapse) {
                    QuestionDetailAnimatedViewport(
                        questionId = questionId,
                        questionContent = questionContent,
                        topics = topics,
                        onTopicClick = onTopicClick,
                        viewportHeightPx = viewportHeightPx,
                        isExpanded = isExpanded,
                        overlayAlpha = 1f - expandProgress,
                        onToggleExpanded = onToggleExpanded,
                        onMeasuredFullHeight = { fullViewportHeightPx = it },
                    )
                } else {
                    QuestionDetailStaticContent(
                        questionId = questionId,
                        questionContent = questionContent,
                        topics = topics,
                        onTopicClick = onTopicClick,
                        onMeasuredHeight = { fullViewportHeightPx = it },
                    )
                }
            }.singleOrNull()?.measure(looseConstraints)
        val controlsPlaceable =
            subcompose("controls") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    QuestionPrimaryActions(
                        isFollowing = isFollowing,
                        onFollowClick = onFollowClick,
                        onWriteAnswerClick = onWriteAnswerClick,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "$answerCount 回答",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.weight(1f))
                        ZhihuFilterChip(
                            selected = currentSort == "default",
                            onClick = { onSortChange("default") },
                            modifier =
                                Modifier.testTag(QUESTION_SORT_DEFAULT_TAG).semantics {
                                    selected = currentSort == "default"
                                },
                            label = { Text("默认") },
                        )
                        ZhihuFilterChip(
                            selected = currentSort == "updated",
                            onClick = { onSortChange("updated") },
                            modifier =
                                Modifier.testTag(QUESTION_SORT_UPDATED_TAG).semantics {
                                    selected = currentSort == "updated"
                                },
                            label = { Text("最新") },
                        )
                    }
                }
            }.single().measure(looseConstraints)
        val detailHeight = detailPlaceable?.height ?: 0
        val controlsOffset = detailHeight + if (detailPlaceable == null) 0 else sectionSpacingPx
        val totalHeight = controlsOffset + controlsPlaceable.height
        layout(width = constraints.maxWidth, height = totalHeight) {
            detailPlaceable?.place(0, 0)
            controlsPlaceable.place(0, controlsOffset)
        }
    }
}

@Composable
private fun QuestionDetailStaticContent(
    questionId: Long,
    questionContent: String,
    topics: List<DataHolder.Topic>,
    onTopicClick: (DataHolder.Topic) -> Unit,
    onMeasuredHeight: (Int) -> Unit,
) {
    SubcomposeLayout(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(QUESTION_DETAIL_CONTENT_TAG),
    ) { constraints ->
        val placeable =
            subcompose("static_detail") {
                QuestionDetailWithTopics(
                    questionId = questionId,
                    questionContent = questionContent,
                    topics = topics,
                    onTopicClick = onTopicClick,
                )
            }.single().measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = 0,
                    maxHeight = Constraints.Infinity,
                ),
            )
        if (placeable.height > 0) {
            onMeasuredHeight(placeable.height)
        }
        layout(width = constraints.maxWidth, height = placeable.height) {
            placeable.place(0, 0)
        }
    }
}

@Composable
private fun QuestionDetailAnimatedViewport(
    questionId: Long,
    questionContent: String,
    topics: List<DataHolder.Topic>,
    onTopicClick: (DataHolder.Topic) -> Unit,
    viewportHeightPx: Int,
    isExpanded: Boolean,
    overlayAlpha: Float,
    onToggleExpanded: () -> Unit,
    onMeasuredFullHeight: (Int) -> Unit,
) {
    val maskHeightPx = with(LocalDensity.current) { QUESTION_DETAIL_MASK_HEIGHT.roundToPx() }
    val buttonZoneHeightPx = with(LocalDensity.current) { QUESTION_DETAIL_TOGGLE_ZONE_HEIGHT.roundToPx() }
    SubcomposeLayout(
        modifier =
            Modifier
                .fillMaxWidth()
                .clipToBounds()
                .testTag(if (isExpanded) QUESTION_DETAIL_CONTENT_TAG else QUESTION_DETAIL_PREVIEW_TAG),
    ) { constraints ->
        val looseConstraints =
            constraints.copy(
                minWidth = 0,
                minHeight = 0,
                maxHeight = Constraints.Infinity,
            )
        val contentPlaceable =
            subcompose("content") {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = QUESTION_DETAIL_TOGGLE_ZONE_HEIGHT),
                ) {
                    QuestionDetailWithTopics(
                        questionId = questionId,
                        questionContent = questionContent,
                        topics = topics,
                        onTopicClick = onTopicClick,
                    )
                }
            }.single().measure(looseConstraints)
        if (contentPlaceable.height > 0) {
            onMeasuredFullHeight(contentPlaceable.height)
        }
        val layoutHeight = viewportHeightPx.coerceIn(0, contentPlaceable.height.coerceAtLeast(0))
        val buttonPlaceable =
            subcompose("button") {
                QuestionDetailToggleButton(
                    isExpanded = isExpanded,
                    onClick = onToggleExpanded,
                )
            }.single().measure(looseConstraints)
        val overlayPlaceable =
            subcompose("overlay") {
                QuestionDetailOverlayMask(
                    alpha = overlayAlpha,
                    modifier = Modifier.fillMaxWidth(),
                )
            }.single().measure(
                Constraints(
                    minWidth = constraints.maxWidth,
                    maxWidth = constraints.maxWidth,
                    minHeight = 0,
                    maxHeight = layoutHeight.coerceAtLeast(0),
                ),
            )
        layout(width = constraints.maxWidth, height = layoutHeight) {
            contentPlaceable.place(0, 0)
            if (overlayAlpha > 0f) {
                overlayPlaceable.place(0, (layoutHeight - minOf(maskHeightPx, overlayPlaceable.height)).coerceAtLeast(0))
            }
            val buttonY = (layoutHeight - buttonZoneHeightPx + (buttonZoneHeightPx - buttonPlaceable.height) / 2).coerceAtLeast(0)
            val buttonX = (constraints.maxWidth - buttonPlaceable.width).coerceAtLeast(0)
            buttonPlaceable.place(buttonX, buttonY)
        }
    }
}

@Composable
private fun QuestionDetailWithTopics(
    questionId: Long,
    questionContent: String,
    topics: List<DataHolder.Topic>,
    onTopicClick: (DataHolder.Topic) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (questionContent.isNotEmpty()) {
            QuestionDetailContent(questionId = questionId, html = questionContent)
        }
        if (topics.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                topics.forEach { topic ->
                    ZhihuFilterChip(
                        selected = false,
                        onClick = { onTopicClick(topic) },
                        label = { Text("# ${topic.name}") },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionDetailOverlayMask(alpha: Float, modifier: Modifier = Modifier) {
    val surfaceColor = contentFirstColors.page
    Box(
        modifier =
            modifier
                .height(QUESTION_DETAIL_MASK_HEIGHT)
                .graphicsLayer { this.alpha = alpha }
                // 这里原先叠了一层 blur(12dp) 去「柔化」渐变。垂直渐变本身已经是连续过渡，
                // 再叠一层实时模糊只会让这个遮罩每帧付一次 GPU 模糊开销（在低端机上直接掉帧），
                // 视觉上几乎没有差别，因此移除；需要更柔的话应该增加渐变 stop，而不是加模糊。
                .background(
                    brush =
                        Brush.verticalGradient(
                            // 四档 stop：末尾多一档 0.92 → 1.0，替代原来模糊带来的收边过渡。
                            listOf(
                                Color.Transparent,
                                surfaceColor.copy(alpha = 0.55f),
                                surfaceColor.copy(alpha = 0.92f),
                                surfaceColor,
                            ),
                        ),
                ),
    )
}

@Composable
private fun QuestionDetailToggleButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    // 展开/收起时箭头旋转 180°：位置不跳（同一个图标原地转），用户能顺着箭头判断「刚才是哪个方向的变化」。
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = Motion.springSnappy(),
        label = "question_detail_arrow",
    )
    TextButton(
        onClick = onClick,
        modifier =
            Modifier
                .offset(y = 4.dp)
                .padding(end = 4.dp, bottom = 0.dp)
                .testTag(QUESTION_DETAIL_TOGGLE_TAG),
    ) {
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = null,
            modifier = Modifier.graphicsLayer { rotationZ = arrowRotation },
        )
        Spacer(Modifier.width(4.dp))
        Text(if (isExpanded) "收起详情" else "展开详情")
    }
}

@Composable
private fun QuestionPrimaryActions(
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onWriteAnswerClick: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onWriteAnswerClick,
            modifier = Modifier.weight(1f).testTag(QUESTION_WRITE_ANSWER_BUTTON_TAG),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
        ) {
            Icon(Icons.Filled.Edit, contentDescription = "写回答")
            Spacer(Modifier.width(8.dp))
            Text("写回答")
        }
        // 关注按钮：颜色从 tonal 过渡到 tertiary 容器，图标在「已关注」瞬间回弹一次。
        // 宽度不做动画：按钮本身是 weight(1f)，宽度由行布局决定，动它只会产生一个无效动画。
        val followContainerColor by animateColorAsState(
            targetValue = if (isFollowing) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            animationSpec = Motion.fadeTween(),
            label = "follow_container",
        )
        val followContentColor by animateColorAsState(
            targetValue = if (isFollowing) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
            animationSpec = Motion.fadeTween(),
            label = "follow_content",
        )
        FilledTonalButton(
            onClick = onFollowClick,
            modifier =
                Modifier.weight(1f).testTag(QUESTION_FOLLOW_BUTTON_TAG).semantics {
                    selected = isFollowing
                },
            colors =
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = followContainerColor,
                    contentColor = followContentColor,
                ),
        ) {
            BounceOnChange(trigger = isFollowing) {
                Icon(
                    imageVector = if (isFollowing) Icons.Filled.Check else Icons.Filled.Add,
                    contentDescription = if (isFollowing) "取消关注" else "关注问题",
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(if (isFollowing) "已关注" else "关注问题")
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    val colors = contentFirstColors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null, // 装饰性图标不需要无障碍描述
            modifier = Modifier.size(16.dp), // 图标稍微小一点，匹配辅助文字
            tint = colors.secondaryText,
        )
        Spacer(Modifier.width(4.dp)) // 图标和文字的间距
        Text(
            text = text,
            // 辅助信息通常使用更小一号的字重，比如 bodySmall 或 labelMedium
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
        )
    }
}

/** 吸底操作栏里的单个操作：数字小字号在上、图标在下，与卡片内操作栏一致。 */
@Composable
private fun StickyActionItem(
    icon: ImageVector,
    count: Int?,
    tint: Color,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
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
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}
