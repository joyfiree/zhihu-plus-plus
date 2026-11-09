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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.github.zly2006.zhihu.data.toFeedDisplayItemNavDestinationJson
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.ArticleType
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.Login
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
import com.github.zly2006.zhihu.ui.components.AnswerActionGroup
import com.github.zly2006.zhihu.ui.components.AnswerWaterfallCard
import com.github.zly2006.zhihu.ui.components.CollectionDialogComponent
import com.github.zly2006.zhihu.ui.components.CommentScreenComponent
import com.github.zly2006.zhihu.ui.components.FeedPullToRefresh
import com.github.zly2006.zhihu.ui.components.PaginatedList
import com.github.zly2006.zhihu.ui.components.ProgressIndicatorFooter
import com.github.zly2006.zhihu.ui.components.ShareAction
import com.github.zly2006.zhihu.ui.components.ShareDialog
import com.github.zly2006.zhihu.ui.components.VoteIcon
import com.github.zly2006.zhihu.ui.components.ZhihuFilterChip
import com.github.zly2006.zhihu.ui.components.ZhihuNetworkImage
import com.github.zly2006.zhihu.ui.components.cardMetrics
import com.github.zly2006.zhihu.ui.components.getShareText
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.components.rememberShareActionExecutor
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSizes
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.util.jsonObject
import com.github.zly2006.zhihu.util.raiseForStatus
import com.github.zly2006.zhihu.viewmodel.ContentLoadEnvironment
import com.github.zly2006.zhihu.viewmodel.addReadHistory
import com.github.zly2006.zhihu.viewmodel.deleteSigned
import com.github.zly2006.zhihu.viewmodel.feed.QuestionFeedViewModel
import com.github.zly2006.zhihu.viewmodel.getOrFetchContentDetail
import com.github.zly2006.zhihu.viewmodel.postSigned
import com.github.zly2006.zhihu.viewmodel.rememberPaginationEnvironment
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.math.abs
import kotlin.math.roundToInt

const val QUESTION_SCREEN_LIST_TAG = "question_screen_list"
const val QUESTION_TITLE_TAG = "question_title"
const val QUESTION_DETAIL_TOGGLE_TAG = "question_detail_toggle"
const val QUESTION_DETAIL_CONTENT_TAG = "question_detail_content"
const val QUESTION_SORT_DEFAULT_TAG = "question_sort_default"
const val QUESTION_SORT_UPDATED_TAG = "question_sort_updated"
const val QUESTION_FOLLOW_BUTTON_TAG = "question_follow_button"
const val QUESTION_VIEW_LOG_BUTTON_TAG = "question_view_log_button"
const val QUESTION_SHARE_BUTTON_TAG = "question_share_button"
const val QUESTION_WRITE_ANSWER_BUTTON_TAG = "question_write_answer_button"
const val QUESTION_COMMENTS_BUTTON_TAG = "question_comments_button"
const val QUESTION_STICKY_ACTIONS_TAG = "question_sticky_actions"

/** 热评门槛：点赞数低于此值的评论不进入热评模块。 */
private const val HOT_COMMENT_MIN_LIKES = 10

/** 顶栏从「无标题」切到「问题标题」的滚动距离。 */
private val ToolbarTitleThreshold = 160.dp

/** 吸底条左侧作者区宽度（官方实测 132dp）。 */
private val StickyAuthorAreaWidth = 132.dp

/** 吸底条头像尺寸（官方实测 32dp）。 */
private val StickyAvatarSize = 32.dp

/** 进入问题时目标回答不在已加载页时，最多再补 3 页（首批 20 + 60 = 80 条）。 */
private const val INITIAL_ANSWER_MAX_EXTRA_PAGES = 3
private const val INITIAL_ANSWER_LOCATE_MAX_ITEMS = 80

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
 * 问题回答页（target-spec §3）：问题头 + 官方连续列表形态的回答卡 + 吸底操作条。
 *
 * 页面是内容优先的单层结构：
 * - 列表层：问题头 → 回答卡（含作者行、赞同行、正文、元信息、48dp 操作行、热评）连续排列；
 * - 悬浮层：透明工具栏（返回 / 滚动后问题标题 / 评论 / 日志 / 分享，问题头滚出后出现写回答）；
 * - 吸底层：当前正在阅读的回答其操作行滚出屏幕后显示 48dp 吸底操作条，
 *   右下角 36dp「下一篇内容」按钮滚动到下一条回答开头。
 *
 * 问题详情在副信息行点击后内联展开；游客阅读上限块只在 [guestReadLimitReached] 为 true 时渲染（业务状态由游客任务提供）。
 */
@Composable
fun QuestionScreen(
    question: Question,
    guestReadLimitReached: Boolean = false,
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
    val scope = rememberCoroutineScope()
    var isQuestionDetailExpanded by rememberSaveable(question.questionId) { mutableStateOf(false) }
    var shareContent by remember(question.questionId) { mutableStateOf<NavDestination>(question) }
    var shareDialogText by remember(question.questionId) { mutableStateOf("") }

    val density = LocalDensity.current
    val screenHeightPx = LocalWindowInfo.current.containerSize.height
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val statusBarPx = with(density) { statusBarPadding.toPx() }
    val navBarPx = with(density) { navigationBarPadding.toPx() }
    val toolbarHeightPx = with(density) { ZhihuSizes.AnswerToolbarHeight.toPx() }
    val stickyBarPx = with(density) { ZhihuSizes.StickyBarHeight.toPx() }
    val toolbarBottomPx = statusBarPx + toolbarHeightPx
    val topInsetPx = (statusBarPx + toolbarHeightPx).roundToInt()
    val toolbarTitleThresholdPx = with(density) { ToolbarTitleThreshold.roundToPx() }

    val showToolbarTitle by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset >= toolbarTitleThresholdPx
        }
    }
    val pageTurnTarget = rememberPageTurnTarget(
        listState = listState,
        enabled = !showComments && !showShareDialog,
    )

    // ── 回答列表状态 ────────────────────────────────────────────────────────
    var expandedAnswerKey by rememberSaveable(question.questionId) { mutableStateOf<String?>(null) }
    val answerDetails = remember { mutableStateMapOf<Long, DataHolder.Answer>() }
    val loadingAnswerIds = remember { mutableStateMapOf<Long, Boolean>() }
    val hotComments = remember { mutableStateMapOf<Long, DataHolder.Comment>() }
    val queriedHotCommentIds = remember { mutableStateMapOf<Long, Boolean>() }
    val voteStates = remember { mutableStateMapOf<Long, VoteUpState>() }
    val voteCounts = remember { mutableStateMapOf<Long, Int>() }
    val favoritedAnswers = remember { mutableStateMapOf<Long, Boolean>() }
    val authorFollowStates = remember { mutableStateMapOf<String, Boolean>() }
    val actionRowBounds = remember { mutableStateMapOf<String, Pair<Float, Float>>() }
    var collectionTargetAnswerId by remember { mutableStateOf<Long?>(null) }
    val answerCollections = remember { mutableStateMapOf<String, Collection>() }
    var collectionsLoadedForAnswer by remember { mutableStateOf<Long?>(null) }

    // 从首页卡片进入时，目标回答要「外露」成问题头后的第一条；找到之前先不渲染别的回答，避免先闪一条再换。
    var pinnedAnswerItem by remember(question.questionId) { mutableStateOf<FeedDisplayItem?>(null) }
    var initialAnswerResolved by remember(question.questionId) { mutableStateOf(question.initialAnswerId == null) }

    fun answerIdOf(item: FeedDisplayItem): Long? =
        ((item.feed?.target as? Feed.AnswerTarget)?.id)
            ?: (item.navDestination as? Article)?.takeIf { it.type == ArticleType.Answer }?.id

    fun authorTokenOf(item: FeedDisplayItem, answer: DataHolder.Answer?): String? =
        answer?.author?.urlToken?.takeIf { it.isNotBlank() }
            ?: (item.feed?.target as? Feed.AnswerTarget)?.author?.urlToken?.takeIf { it.isNotBlank() }

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

    fun toggleAuthorFollow(urlToken: String) {
        val nextFollowing = !(authorFollowStates[urlToken] ?: false)
        scope.launch {
            try {
                val url = "https://www.zhihu.com/api/v4/members/$urlToken/followers"
                if (nextFollowing) {
                    paginationEnvironment.postSigned(url)
                } else {
                    paginationEnvironment.deleteSigned(url)
                }.raiseForStatus()
                authorFollowStates[urlToken] = nextFollowing
            } catch (e: Exception) {
                userMessages.showShortMessage("操作失败: ${e.message}")
            }
        }
    }

    fun openCollectionPicker(answerId: Long) {
        collectionTargetAnswerId = answerId
        if (collectionsLoadedForAnswer != answerId) {
            scope.launch {
                loadAnswerCollections(paginationEnvironment, answerId).forEach { collection ->
                    answerCollections[collection.id] = collection
                }
                collectionsLoadedForAnswer = answerId
            }
        }
    }

    fun shareDestination(destination: NavDestination, text: String?) {
        if (text == null) return
        shareContent = destination
        shareDialogText = text
        when (settings.getString("shareActionMode", "ask")) {
            "copy" -> executeShareAction(ShareAction.CopyLink, destination, text)
            "share" -> executeShareAction(ShareAction.DirectShare, destination, text)
            else -> showShareDialog = true
        }
    }

    fun answerTitle() = viewModel.questionDetail?.title ?: question.title

    fun shareQuestion() {
        shareDestination(question, getShareText(question, answerTitle()))
    }

    fun shareAnswer(answerId: Long, authorName: String?) {
        val destination = Article(type = ArticleType.Answer, id = answerId)
        shareDestination(destination, getShareText(destination, answerTitle(), authorName.orEmpty()))
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

    // ── 吸底操作条：判定当前正在阅读的回答及其操作行是否可见 ──────────────────
    val currentAnswerIndex by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val viewportStart = info.viewportStartOffset
            val viewportEnd = info.viewportEndOffset
            var bestIndex = -1
            var bestVisible = 0
            for (item in info.visibleItemsInfo) {
                // 列表第 0 项是问题头部，回答从第 1 项开始。
                val displayIndex = item.index - 1
                if (displayIndex < 0) continue
                val visible = (minOf(item.offset + item.size, viewportEnd) - maxOf(item.offset, viewportStart)).coerceAtLeast(0)
                if (visible > bestVisible) {
                    bestVisible = visible
                    bestIndex = displayIndex
                }
            }
            bestIndex
        }
    }

    // 从首页卡片等入口进入时，把目标回答「外露」成问题头后的第一条，不做任何滚动跳转。
    // 首页卡片可能只带回答 id：先用回答详情确认归属（ContentDetailCache 通常已缓存），再直接置顶；
    // 详情取不到时才退回列表续载查找，避免落回默认列表顶部。
    val initialAnswerId = question.initialAnswerId
    LaunchedEffect(initialAnswerId, question.questionId) {
        val targetId = initialAnswerId ?: return@LaunchedEffect
        val cachedDetail = paginationEnvironment.getOrFetchContentDetail(
            Article(type = ArticleType.Answer, id = targetId),
        ) as? DataHolder.Answer
        if (initialAnswerResolved) return@LaunchedEffect
        if (cachedDetail != null && cachedDetail.question.id != question.questionId) {
            // 外露卡片与问题列表不一致时以回答详情为准，避免停在错误问题的列表。
            initialAnswerResolved = true
            userMessages.showShortMessage("该回答属于其他问题，已为你跳转")
            navigator.onNavigate(
                Question(
                    questionId = cachedDetail.question.id,
                    title = cachedDetail.question.title,
                    initialAnswerId = targetId,
                ),
            )
            return@LaunchedEffect
        }
        if (cachedDetail != null) {
            initialAnswerResolved = true
            val detail = cachedDetail
            answerDetails[detail.id] = detail
            voteStates[detail.id] = VoteUpState.from(detail.reaction?.relation?.vote)
            voteCounts[detail.id] = detail.voteupCount
            detail.author.urlToken.takeIf { it.isNotBlank() }?.let { token ->
                if (authorFollowStates[token] == null) authorFollowStates[token] = detail.author.isFollowing
            }
            pinnedAnswerItem = FeedDisplayItem(
                title = "",
                summary = detail.excerpt,
                details = "",
                feed = null,
                navDestinationJson = Article(type = ArticleType.Answer, id = detail.id).toFeedDisplayItemNavDestinationJson(),
                avatarSrc = detail.author.avatarUrl,
                authorName = detail.author.name,
                authorBadgeV2 = detail.author.badgeV2,
                raw = detail,
            )
            return@LaunchedEffect
        }

        // 详情不可用（例如游客态 answers 接口被要求登录）：等首页数据就绪后按 id 续载查找。
        if (viewModel.displayItems.isEmpty() && !viewModel.isEnd) {
            withTimeoutOrNull(5_000) {
                snapshotFlow { viewModel.displayItems.size }.first { it > 0 }
            }
        }
        var extraPages = 0
        while (
            !viewModel.isEnd &&
            extraPages < INITIAL_ANSWER_MAX_EXTRA_PAGES &&
            viewModel.displayItems.size < INITIAL_ANSWER_LOCATE_MAX_ITEMS &&
            viewModel.displayItems.none { answerIdOf(it) == targetId }
        ) {
            val before = viewModel.displayItems.size
            extraPages++
            viewModel.loadMore(paginationEnvironment)
            withTimeoutOrNull(5_000) {
                snapshotFlow { viewModel.displayItems.size }.first { it > before }
            }
        }
        if (initialAnswerResolved) return@LaunchedEffect
        initialAnswerResolved = true
        if (viewModel.displayItems.any { answerIdOf(it) == targetId }) return@LaunchedEffect
        userMessages.showShortMessage("未找到这条回答，已显示问题下的其他回答")
    }

    // 渲染列表：问题头之后第一条固定是目标回答（若已命中），同 id 的列表项过滤掉避免重复。
    val renderItems = remember(
        viewModel.displayItems.size,
        viewModel.sortOrder,
        initialAnswerId,
        initialAnswerResolved,
        pinnedAnswerItem,
    ) {
        val pinned = pinnedAnswerItem
        val pinnedId = pinned?.let { answerIdOf(it) }
        val located = initialAnswerId?.let { id -> viewModel.displayItems.firstOrNull { answerIdOf(it) == id } }
        when {
            pinned != null -> listOf(pinned) + viewModel.displayItems.filterNot { answerIdOf(it) == pinnedId }
            located != null -> listOf(located) + viewModel.displayItems.filterNot { it.stableKey == located.stableKey }
            initialAnswerId != null && !initialAnswerResolved -> emptyList()
            else -> viewModel.displayItems
        }
    }

    val currentAnswerItem = renderItems.getOrNull(currentAnswerIndex)
    val currentAnswerId = currentAnswerItem?.let { answerIdOf(it) }
    val currentAnswer = currentAnswerId?.let { answerDetails[it] }
    val currentAuthorToken = currentAnswerItem?.let { authorTokenOf(it, currentAnswer) }
    val stickyAuthorFollowed = currentAuthorToken?.let { authorFollowStates[it] } ?: (currentAnswer?.author?.isFollowing ?: false)
    val stickyBarVisible by remember(currentAnswerItem) {
        derivedStateOf {
            val item = currentAnswerItem ?: return@derivedStateOf false
            val bounds = actionRowBounds[item.stableKey] ?: return@derivedStateOf false
            val visibleTop = toolbarBottomPx
            val visibleBottom = screenHeightPx - navBarPx - stickyBarPx
            !(bounds.first >= visibleTop && bounds.second <= visibleBottom)
        }
    }
    val hasNextAnswer = currentAnswerIndex >= 0 && currentAnswerIndex < renderItems.lastIndex

    fun scrollToNextAnswer() {
        val index = currentAnswerIndex
        if (index < 0 || index >= renderItems.lastIndex) return
        scope.launch {
            // +1 是问题头部；负偏移让下一条回答落在悬浮工具栏下方而不是被工具栏压住。
            listState.animateScrollToItem(index + 2, scrollOffset = -topInsetPx)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.page)) {
        FeedPullToRefresh(
            viewModel = viewModel,
            padding = PaddingValues(top = statusBarPadding + ZhihuSizes.AnswerToolbarHeight),
        ) {
            PaginatedList(
                items = renderItems,
                onLoadMore = { viewModel.loadMore(paginationEnvironment) },
                isEnd = { viewModel.isEnd },
                key = { it.stableKey },
                listState = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .pageTurnViewportWithGuide(pageTurnTarget)
                    .testTag(QUESTION_SCREEN_LIST_TAG),
                contentPadding = PaddingValues(
                    top = statusBarPadding + ZhihuSizes.AnswerToolbarHeight,
                    bottom = navigationBarPadding + ZhihuSizes.StickyBarHeight + readingPlayerOverlayPadding,
                ),
                footer = { state ->
                    if (guestReadLimitReached) {
                        GuestAnswerLimitFooter(onClick = { navigator.onNavigate(Login) })
                    } else {
                        ProgressIndicatorFooter(state)
                    }
                },
                topContent = {
                    item(key = "question_header") {
                        QuestionHeader(
                            questionId = question.questionId,
                            title = answerTitle(),
                            answerCount = viewModel.questionDetail?.answerCount ?: 0,
                            visitCount = viewModel.questionDetail?.visitCount ?: 0,
                            followerCount = followerCount,
                            contentHtml = viewModel.questionDetail?.detail.orEmpty(),
                            topics = viewModel.questionDetail?.topics.orEmpty(),
                            isFollowing = isFollowing,
                            currentSort = viewModel.sortOrder,
                            isDetailExpanded = isQuestionDetailExpanded,
                            onToggleDetail = {
                                val expanding = !isQuestionDetailExpanded
                                isQuestionDetailExpanded = expanding
                                if (expanding) {
                                    // 展开后把问题头滚回顶部，保证刚展开的详情就在工具栏下方可见。
                                    scope.launch { listState.animateScrollToItem(0) }
                                }
                            },
                            onTopicClick = { topic -> navigator.onNavigate(Topic(topic.id, topic.name)) },
                            onFollowClick = {
                                scope.launch {
                                    val nextFollowing = !isFollowing
                                    viewModel.followQuestion(paginationEnvironment, nextFollowing)
                                    isFollowing = nextFollowing
                                    followerCount = (followerCount + if (nextFollowing) 1 else -1).coerceAtLeast(0)
                                    userMessages.showShortMessage(if (nextFollowing) "已关注问题" else "已取消关注问题")
                                }
                            },
                            onWriteAnswerClick = {
                                navigator.onNavigate(
                                    WriteAnswer(
                                        questionId = question.questionId,
                                        questionTitle = answerTitle(),
                                        questionDetail = viewModel.questionDetail?.detail.orEmpty(),
                                    ),
                                )
                            },
                            onSortChange = { sortOrder ->
                                // 用户主动换排序后，入口时的目标回答置顶不再保留，按新排序正常展示。
                                pinnedAnswerItem = null
                                initialAnswerResolved = true
                                viewModel.updateSortOrder(sortOrder)
                                viewModel.refresh(paginationEnvironment)
                            },
                        )
                    }
                },
            ) { item ->
                val answerId = answerIdOf(item)
                val answer = answerId?.let { answerDetails[it] }
                val authorToken = authorTokenOf(item, answer)
                val isExpanded = expandedAnswerKey == item.stableKey
                val fallbackMetrics = item.cardMetrics

                // 可见的回答才拉详情与热评：LazyColumn 只组合视口内的条目，请求量随阅读推进而不是随页数增长。
                LaunchedEffect(answerId, item.stableKey) {
                    val id = answerId ?: return@LaunchedEffect
                    if (answerDetails[id] == null && loadingAnswerIds[id] != true) {
                        loadingAnswerIds[id] = true
                        try {
                            (
                                paginationEnvironment.getOrFetchContentDetail(
                                    Article(type = ArticleType.Answer, id = id),
                                ) as? DataHolder.Answer
                            )?.let { detail ->
                                answerDetails[id] = detail
                                voteStates[id] = VoteUpState.from(detail.reaction?.relation?.vote)
                                voteCounts[id] = detail.voteupCount
                                detail.author.urlToken.takeIf { it.isNotBlank() }?.let { token ->
                                    if (authorFollowStates[token] == null) {
                                        authorFollowStates[token] = detail.author.isFollowing
                                    }
                                }
                            }
                        } finally {
                            // 失败不能标记成「已加载」：滚回来时要能重试，否则评论数会一直停在 0。
                            loadingAnswerIds[id] = false
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
                    fallbackAuthorBadge = item.authorBadgeV2,
                    fallbackSummary = item.summary,
                    voteUpState = answerId?.let { voteStates[it] } ?: VoteUpState.Neutral,
                    voteUpCount = answerId?.let { voteCounts[it] } ?: fallbackMetrics.voteUp ?: 0,
                    commentCount = answer?.commentCount ?: fallbackMetrics.comment ?: 0,
                    favoriteCount = (answer?.favlistsCount ?: 0).takeIf { it > 0 } ?: fallbackMetrics.favorite ?: 0,
                    isFavorited = answerId?.let { favoritedAnswers[it] } ?: false,
                    isAuthorFollowed = authorToken?.let { authorFollowStates[it] } ?: (answer?.author?.isFollowing ?: false),
                    hotComment = answerId?.let { hotComments[it] },
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
                    onFavorite = { answerId?.let { id -> openCollectionPicker(id) } },
                    onComment = {
                        if (answerId != null) {
                            commentsTarget = Article(type = ArticleType.Answer, id = answerId)
                            showComments = true
                        }
                    },
                    onShare = { answerId?.let { id -> shareAnswer(id, answer?.author?.name ?: item.authorName) } },
                    onFollowAuthor = { authorToken?.let { token -> toggleAuthorFollow(token) } },
                    onHotCommentClick = {
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
                    onActionRowPositionChanged = { top, bottom ->
                        val previous = actionRowBounds[item.stableKey]
                        if (previous == null || abs(previous.first - top) > 0.5f || abs(previous.second - bottom) > 0.5f) {
                            actionRowBounds[item.stableKey] = top to bottom
                        }
                    },
                    // 瀑布流里正文已经完整呈现，卡片本身不再跳转到单条回答页；
                    // 需要评论、作者主页时用卡片内的操作栏与作者行。
                    modifier = Modifier.testTag("question_feed_item_${item.stableKey}"),
                )
            }
        }

        // 顶部渐变遮罩：内容从工具栏下方穿过时保证标题/图标可读（官方滚动后同样有一层轻遮罩）。
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(statusBarPadding + ZhihuSizes.AnswerToolbarHeight + 12.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.page,
                            colors.page.copy(alpha = 0.94f),
                            colors.page.copy(alpha = 0.72f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        QuestionFloatingToolbar(
            title = answerTitle(),
            showTitle = showToolbarTitle,
            onNavigateBack = navigator.onNavigateBack,
            onScrollToTop = { scope.launch { listState.animateScrollToItem(0) } },
            onWriteAnswer = {
                navigator.onNavigate(
                    WriteAnswer(
                        questionId = question.questionId,
                        questionTitle = answerTitle(),
                        questionDetail = viewModel.questionDetail?.detail.orEmpty(),
                    ),
                )
            },
            onComments = {
                commentsTarget = null
                showComments = true
            },
            onOpenLog = {
                try {
                    openZhihuWebUrl("https://www.zhihu.com/question/${question.questionId}/log")
                } catch (e: Exception) {
                    userMessages.showShortMessage("打开日志失败: ${e.message}")
                }
            },
            onShare = { shareQuestion() },
            modifier = Modifier.align(Alignment.TopStart),
        )

        if (stickyBarVisible && currentAnswerItem != null) {
            AnswerStickyBar(
                authorName = currentAnswer?.author?.name ?: currentAnswerItem.authorName,
                avatarSrc = currentAnswer?.author?.avatarUrl ?: currentAnswerItem.avatarSrc,
                authorToken = currentAuthorToken,
                isAuthorFollowed = stickyAuthorFollowed,
                voteUpState = currentAnswerId?.let { voteStates[it] } ?: VoteUpState.Neutral,
                voteUpCount = currentAnswerId?.let { voteCounts[it] } ?: currentAnswerItem.cardMetrics.voteUp ?: 0,
                isFavorited = currentAnswerId?.let { favoritedAnswers[it] } ?: false,
                commentCount = currentAnswer?.commentCount ?: currentAnswerItem.cardMetrics.comment ?: 0,
                favoriteCount = (currentAnswer?.favlistsCount ?: 0).takeIf { it > 0 }
                    ?: currentAnswerItem.cardMetrics.favorite ?: 0,
                onFollowAuthor = { currentAuthorToken?.let { token -> toggleAuthorFollow(token) } },
                onVoteUp = {
                    currentAnswerId?.let { id ->
                        val current = voteStates[id] ?: VoteUpState.Neutral
                        toggleVote(id, if (current == VoteUpState.Up) VoteUpState.Neutral else VoteUpState.Up)
                    }
                },
                onVoteDown = {
                    currentAnswerId?.let { id ->
                        val current = voteStates[id] ?: VoteUpState.Neutral
                        toggleVote(id, if (current == VoteUpState.Down) VoteUpState.Neutral else VoteUpState.Down)
                    }
                },
                onFavorite = { currentAnswerId?.let { id -> openCollectionPicker(id) } },
                onComment = {
                    if (currentAnswerId != null) {
                        commentsTarget = Article(type = ArticleType.Answer, id = currentAnswerId)
                        showComments = true
                    }
                },
                onShare = {
                    currentAnswerId?.let { id ->
                        shareAnswer(id, currentAnswer?.author?.name ?: currentAnswerItem.authorName)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        if (hasNextAnswer) {
            NextAnswerFab(
                onClick = { scrollToNextAnswer() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = ZhihuSpacing.ScreenGutter,
                        bottom = navigationBarPadding + readingPlayerOverlayPadding +
                            if (stickyBarVisible) ZhihuSizes.StickyBarHeight + ZhihuSpacing.Large else ZhihuSpacing.Large,
                    ),
            )
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

    if (shareDialogText.isNotEmpty()) {
        ShareDialog(
            content = shareContent,
            shareText = shareDialogText,
            showDialog = showShareDialog,
            onDismissRequest = { showShareDialog = false },
        )
    }
}

/** 问题头（target-spec §3.2 + 0.618 列表首项）：标题、副信息行、问题详情、排序与问题级操作。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionHeader(
    questionId: Long,
    title: String,
    answerCount: Int,
    visitCount: Int,
    followerCount: Int,
    contentHtml: String,
    topics: List<DataHolder.Topic>,
    isFollowing: Boolean,
    currentSort: String,
    isDetailExpanded: Boolean,
    onToggleDetail: () -> Unit,
    onTopicClick: (DataHolder.Topic) -> Unit,
    onFollowClick: () -> Unit,
    onWriteAnswerClick: () -> Unit,
    onSortChange: (String) -> Unit,
) {
    val colors = contentFirstColors
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(ZhihuSpacing.ExtraSmall))
        SelectionContainer(modifier = Modifier.questionSelectionWorkaround()) {
            Text(
                text = title,
                style = ZhihuTypography.QuestionHeaderTitle,
                color = colors.primaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ZhihuSpacing.ScreenGutter)
                    .testTag(QUESTION_TITLE_TAG),
            )
        }
        Spacer(Modifier.height(ZhihuSpacing.ExtraSmall))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ZhihuSpacing.ScreenGutter)
                .heightIn(min = 18.dp)
                .clickable(role = Role.Button, onClick = onToggleDetail)
                .testTag(QUESTION_DETAIL_TOGGLE_TAG),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuestionMetaText("知乎")
            QuestionMetaDot()
            QuestionMetaText("$answerCount 个回答")
            QuestionMetaDot()
            QuestionMetaText("$visitCount 浏览")
            QuestionMetaDot()
            QuestionMetaText("$followerCount 个关注")
            Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.tertiaryText,
                modifier = Modifier.size(12.dp),
            )
        }
        if (isDetailExpanded && contentHtml.isNotBlank()) {
            Spacer(Modifier.height(ZhihuSpacing.Small))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ZhihuSpacing.ScreenGutter)
                    .testTag(QUESTION_DETAIL_CONTENT_TAG),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(ZhihuSpacing.Small)) {
                    QuestionDetailContent(questionId = questionId, html = contentHtml)
                    if (topics.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(ZhihuSpacing.Small)) {
                            topics.forEach { topic ->
                                ZhihuFilterChip(
                                    selected = false,
                                    onClick = { onTopicClick(topic) },
                                    label = { Text("# ${topic.name}") },
                                )
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Row(
                            modifier = Modifier
                                .clickable(role = Role.Button, onClick = onToggleDetail)
                                .padding(horizontal = ZhihuSpacing.Small, vertical = ZhihuSpacing.ExtraSmall),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "收起问题", style = ZhihuTypography.AnswerVoteLine, color = colors.accent)
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowUp,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(ZhihuSpacing.ExtraSmall))
        QuestionToolsRow(
            answerCount = answerCount,
            isFollowing = isFollowing,
            currentSort = currentSort,
            onSortChange = onSortChange,
            onFollowClick = onFollowClick,
            onWriteAnswerClick = onWriteAnswerClick,
        )
        Spacer(Modifier.height(ZhihuSpacing.Small))
        HorizontalDivider(thickness = ZhihuSizes.DividerThickness, color = colors.divider)
    }
}

@Composable
private fun QuestionMetaText(text: String) {
    Text(
        text = text,
        style = ZhihuTypography.AnswerMeta,
        color = contentFirstColors.tertiaryText,
        maxLines = 1,
    )
}

@Composable
private fun QuestionMetaDot() {
    Text(
        text = "·",
        style = ZhihuTypography.AnswerMeta,
        color = contentFirstColors.tertiaryText,
        modifier = Modifier.padding(horizontal = ZhihuSpacing.ExtraSmall),
    )
}

/** 回答数 + 排序 + 关注问题 + 写回答。问题头要够矮，这一行压到 24dp 高、12-13sp 小字号。 */
@Composable
private fun QuestionToolsRow(
    answerCount: Int,
    isFollowing: Boolean,
    currentSort: String,
    onSortChange: (String) -> Unit,
    onFollowClick: () -> Unit,
    onWriteAnswerClick: () -> Unit,
) {
    val colors = contentFirstColors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ZhihuSpacing.ScreenGutter),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$answerCount 回答",
            style = ZhihuTypography.AnswerMeta,
            color = colors.primaryText,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
        Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
        QuestionSortToggle(
            label = "默认",
            selected = currentSort == "default",
            tag = QUESTION_SORT_DEFAULT_TAG,
            onClick = { onSortChange("default") },
        )
        QuestionSortToggle(
            label = "最新",
            selected = currentSort == "updated",
            tag = QUESTION_SORT_UPDATED_TAG,
            onClick = { onSortChange("updated") },
        )
        Spacer(Modifier.weight(1f))
        QuestionFollowToggle(isFollowing = isFollowing, onClick = onFollowClick)
        Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
        QuestionWriteAnswerPill(onClick = onWriteAnswerClick, modifier = Modifier.testTag(QUESTION_WRITE_ANSWER_BUTTON_TAG))
    }
}

/** 紧凑「+ 关注问题」：问题头里只占 24dp 高，样式对齐小号 chip。 */
@Composable
private fun QuestionFollowToggle(isFollowing: Boolean, onClick: () -> Unit) {
    val colors = contentFirstColors
    Row(
        modifier = Modifier
            .height(24.dp)
            .clip(ZhihuShapeTokens.Pill)
            .background(if (isFollowing) colors.functionalSurface else colors.accent.copy(alpha = 0.12f))
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(QUESTION_FOLLOW_BUTTON_TAG)
            .semantics { selected = isFollowing }
            .padding(horizontal = ZhihuSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isFollowing) "已关注" else "+ 关注问题",
            style = ZhihuTypography.AnswerMeta,
            color = if (isFollowing) colors.secondaryText else colors.accent,
            maxLines = 1,
        )
    }
}

@Composable
private fun QuestionSortToggle(
    label: String,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = ZhihuTypography.AnswerMeta,
        color = if (selected) contentFirstColors.accent else contentFirstColors.tertiaryText,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .testTag(tag)
            .semantics { this.selected = selected }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 2.dp),
    )
}

@Composable
private fun QuestionWriteAnswerPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(24.dp)
            .clip(ZhihuShapeTokens.Pill)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = ZhihuSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = "写回答",
            style = ZhihuTypography.AnswerMeta,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
        )
    }
}

/** 悬浮工具栏（target-spec §3.1）：顶部时返回 + 评论/日志/分享；滚动后标题占满剩余宽度，右侧只留写回答 + 更多菜单。 */
@Composable
private fun QuestionFloatingToolbar(
    title: String,
    showTitle: Boolean,
    onNavigateBack: () -> Unit,
    onScrollToTop: () -> Unit,
    onWriteAnswer: () -> Unit,
    onComments: () -> Unit,
    onOpenLog: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = contentFirstColors
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(ZhihuSizes.AnswerToolbarHeight)
            .padding(start = 10.dp, end = ZhihuSpacing.ScreenGutter),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ZhihuSizes.MinTouchTarget)
                .clip(CircleShape)
                .then(if (showTitle) Modifier.background(colors.floatingSurface) else Modifier)
                .clickable(role = Role.Button, onClick = onNavigateBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = colors.primaryText,
                modifier = Modifier.size(ZhihuSizes.Icon),
            )
        }
        Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
        if (showTitle) {
            Text(
                text = title,
                style = ZhihuTypography.ContentAuthor,
                color = colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clip(ZhihuShapeTokens.Pill)
                    .clickable(role = Role.Button, onClick = onScrollToTop)
                    .padding(horizontal = ZhihuSpacing.Small),
            )
            Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
            QuestionWriteAnswerPill(onClick = onWriteAnswer)
            Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
            Box {
                Box(
                    modifier = Modifier
                        .size(ZhihuSizes.MinTouchTarget)
                        .clip(CircleShape)
                        .background(colors.floatingSurface)
                        .clickable(role = Role.Button, onClick = { menuExpanded = true }),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "更多",
                        tint = colors.primaryText,
                        modifier = Modifier.size(ZhihuSizes.Icon),
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    ToolbarMenuItem(
                        icon = Icons.AutoMirrored.Filled.Comment,
                        label = "评论",
                        tag = QUESTION_COMMENTS_BUTTON_TAG,
                    ) {
                        menuExpanded = false
                        onComments()
                    }
                    ToolbarMenuItem(
                        icon = Icons.Filled.History,
                        label = "问题日志",
                        tag = QUESTION_VIEW_LOG_BUTTON_TAG,
                    ) {
                        menuExpanded = false
                        onOpenLog()
                    }
                    ToolbarMenuItem(
                        icon = Icons.Filled.Share,
                        label = "分享",
                        tag = QUESTION_SHARE_BUTTON_TAG,
                    ) {
                        menuExpanded = false
                        onShare()
                    }
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
            ToolbarIconButton(
                icon = Icons.AutoMirrored.Filled.Comment,
                label = "评论",
                tag = QUESTION_COMMENTS_BUTTON_TAG,
                onClick = onComments,
            )
            ToolbarIconButton(
                icon = Icons.Filled.History,
                label = "问题日志",
                tag = QUESTION_VIEW_LOG_BUTTON_TAG,
                onClick = onOpenLog,
            )
            ToolbarIconButton(
                icon = Icons.Filled.Share,
                label = "分享",
                tag = QUESTION_SHARE_BUTTON_TAG,
                onClick = onShare,
            )
        }
    }
}

@Composable
private fun ToolbarMenuItem(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onClick,
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        modifier = Modifier.testTag(tag),
    )
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(ZhihuSizes.MinTouchTarget)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentFirstColors.secondaryText,
            modifier = Modifier.size(ZhihuSizes.Icon),
        )
    }
}

/** 吸底操作条（target-spec §3.4）：当前回答的作者 + 关注 + 五组操作，高 48dp + 顶部发丝线。 */
@Composable
private fun AnswerStickyBar(
    authorName: String?,
    avatarSrc: String?,
    authorToken: String?,
    isAuthorFollowed: Boolean,
    voteUpState: VoteUpState,
    voteUpCount: Int,
    isFavorited: Boolean,
    commentCount: Int,
    favoriteCount: Int,
    onFollowAuthor: () -> Unit,
    onVoteUp: () -> Unit,
    onVoteDown: () -> Unit,
    onFavorite: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = contentFirstColors
    Column(modifier = modifier.fillMaxWidth().background(colors.page).testTag(QUESTION_STICKY_ACTIONS_TAG)) {
        HorizontalDivider(thickness = ZhihuSizes.DividerThickness, color = colors.divider)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(ZhihuSizes.StickyBarHeight)
                .padding(horizontal = ZhihuSpacing.ScreenGutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.width(StickyAuthorAreaWidth), verticalAlignment = Alignment.CenterVertically) {
                if (!avatarSrc.isNullOrBlank()) {
                    ZhihuNetworkImage(
                        model = avatarSrc,
                        contentDescription = "作者头像",
                        modifier = Modifier.size(StickyAvatarSize).clip(CircleShape),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(StickyAvatarSize)
                            .clip(CircleShape)
                            .background(colors.functionalSurface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = colors.tertiaryText,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.width(ZhihuSpacing.Small))
                Text(
                    text = authorName.orEmpty(),
                    style = ZhihuTypography.FeedAuthorName,
                    color = colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (authorToken != null) {
                    Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
                    Text(
                        text = if (isAuthorFollowed) "已关注" else "关注",
                        style = ZhihuTypography.AnswerMeta,
                        color = if (isAuthorFollowed) colors.tertiaryText else colors.accent,
                        maxLines = 1,
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = onFollowAuthor)
                            .padding(ZhihuSpacing.ExtraSmall),
                    )
                }
            }
            AnswerActionGroup(
                modifier = Modifier.weight(1f),
                count = voteUpCount,
                label = "赞同",
                tag = "sticky_answer_vote_up",
                tint = if (voteUpState == VoteUpState.Up) colors.liked else colors.tertiaryText,
                onClick = onVoteUp,
            ) {
                VoteIcon(up = true)
            }
            AnswerActionGroup(
                modifier = Modifier.weight(1f),
                count = null,
                label = "反对",
                tag = "sticky_answer_vote_down",
                tint = if (voteUpState == VoteUpState.Down) colors.liked else colors.tertiaryText,
                onClick = onVoteDown,
            ) {
                VoteIcon(up = false)
            }
            AnswerActionGroup(
                modifier = Modifier.weight(1f),
                count = favoriteCount,
                label = "收藏",
                tag = "sticky_answer_favorite",
                tint = if (isFavorited) colors.favorited else colors.tertiaryText,
                onClick = onFavorite,
            ) {
                Icon(Icons.Outlined.StarBorder, contentDescription = null, modifier = Modifier.size(ZhihuSizes.AnswerActionIconSize))
            }
            AnswerActionGroup(
                modifier = Modifier.weight(1f),
                count = commentCount,
                label = "评论",
                tag = "sticky_answer_comment",
                tint = colors.tertiaryText,
                onClick = onComment,
            ) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(ZhihuSizes.AnswerActionIconSize))
            }
            AnswerActionGroup(
                modifier = Modifier.weight(1f),
                count = null,
                label = "分享",
                tag = "sticky_answer_share",
                tint = colors.tertiaryText,
                onClick = onShare,
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(ZhihuSizes.AnswerActionIconSize))
            }
        }
    }
}

/** 「下一篇内容」悬浮按钮（target-spec §3.4）：36dp 圆形底 + 下箭头。 */
@Composable
private fun NextAnswerFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ZhihuSizes.NextAnswerFabSize)
            .clip(CircleShape)
            .background(contentFirstColors.floatingSurface)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "下一篇内容",
            tint = contentFirstColors.primaryText,
            modifier = Modifier.size(ZhihuSizes.IconSmall),
        )
    }
}

/** 游客阅读上限块（target-spec §3.5）：只在业务层确认达到上限后渲染。 */
@Composable
private fun GuestAnswerLimitFooter(onClick: () -> Unit) {
    val colors = contentFirstColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 119.dp)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "登录后查看更多精彩回答",
            style = ZhihuTypography.AnswerBody,
            color = colors.accent,
            maxLines = 1,
        )
        Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(ZhihuSizes.IconSmall),
        )
    }
}

/**
 * 拉取一条回答的热评。
 *
 * 使用与评论区相同的 `root_comment?order=score` 接口取分数最高的一条，点赞数低于
 * [HOT_COMMENT_MIN_LIKES] 时返回 null —— 不满足阈值时整体不渲染，不留空白。
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
