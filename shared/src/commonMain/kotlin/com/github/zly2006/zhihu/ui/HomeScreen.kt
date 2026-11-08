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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.zly2006.zhihu.account.SKIP_LOGIN_PROMPT_PREFERENCE_KEY
import com.github.zly2006.zhihu.data.DataHolder
import com.github.zly2006.zhihu.data.Feed
import com.github.zly2006.zhihu.data.FeedDisplayItem
import com.github.zly2006.zhihu.data.MOBILE_NOTIFICATION_MESSAGE_URL
import com.github.zly2006.zhihu.data.MobileNotificationMessageOverview
import com.github.zly2006.zhihu.data.RecommendationMode
import com.github.zly2006.zhihu.data.navDestination
import com.github.zly2006.zhihu.data.target
import com.github.zly2006.zhihu.filter.RemoteHistorySync
import com.github.zly2006.zhihu.navigation.Account
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.ArticleType
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.Notification
import com.github.zly2006.zhihu.navigation.Pin
import com.github.zly2006.zhihu.navigation.Question
import com.github.zly2006.zhihu.navigation.Search
import com.github.zly2006.zhihu.navigation.requestLoginNavigation
import com.github.zly2006.zhihu.notification.HOME_NOTIFICATION_ACTION_OPEN_ANSWER
import com.github.zly2006.zhihu.notification.HOME_NOTIFICATION_ACTION_OPEN_ARTICLE
import com.github.zly2006.zhihu.notification.HOME_NOTIFICATION_ACTION_OPEN_PIN
import com.github.zly2006.zhihu.notification.HOME_NOTIFICATION_ACTION_OPEN_UPDATE_SETTINGS
import com.github.zly2006.zhihu.notification.HOME_NOTIFICATION_ACTION_OPEN_URL
import com.github.zly2006.zhihu.notification.HOME_NOTIFICATION_ACTION_OPEN_WEBVIEW
import com.github.zly2006.zhihu.notification.HOME_NOTIFICATION_ACTION_SET_SETTING
import com.github.zly2006.zhihu.notification.HOME_NOTIFICATION_REFRESH_INTERVAL_MILLIS
import com.github.zly2006.zhihu.notification.OnlineHomeNotification
import com.github.zly2006.zhihu.notification.OnlineHomeNotificationRepository
import com.github.zly2006.zhihu.notification.rememberNotificationSettingsStore
import com.github.zly2006.zhihu.platform.SettingsStore
import com.github.zly2006.zhihu.platform.UserMessageDuration
import com.github.zly2006.zhihu.platform.rememberAppPrivateDirectory
import com.github.zly2006.zhihu.platform.rememberExternalUrlOpener
import com.github.zly2006.zhihu.platform.rememberSettingsStore
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.platform.rememberWebViewUrlOpener
import com.github.zly2006.zhihu.reading.RegisterReadingQueueSource
import com.github.zly2006.zhihu.ui.components.AnnouncementCard
import com.github.zly2006.zhihu.ui.components.AnnouncementCardDefaults
import com.github.zly2006.zhihu.ui.components.BlockByKeywordsDialog
import com.github.zly2006.zhihu.ui.components.ContentFeedCard
import com.github.zly2006.zhihu.ui.components.DraggableRefreshButton
import com.github.zly2006.zhihu.ui.components.FeedAuthorBlockConfirmDialog
import com.github.zly2006.zhihu.ui.components.FeedAuthorBlockRequest
import com.github.zly2006.zhihu.ui.components.FeedAuthorBlockType
import com.github.zly2006.zhihu.ui.components.FeedPullToRefresh
import com.github.zly2006.zhihu.ui.components.PaginatedList
import com.github.zly2006.zhihu.ui.components.ProgressIndicatorFooter
import com.github.zly2006.zhihu.ui.components.ZhihuAlertDialog
import com.github.zly2006.zhihu.ui.components.feedKeywordExtractionAvailable
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.designsystem.Motion
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.ui.subscreens.SystemUpdateState
import com.github.zly2006.zhihu.ui.subscreens.rememberSystemUpdateState
import com.github.zly2006.zhihu.util.Log
import com.github.zly2006.zhihu.util.json
import com.github.zly2006.zhihu.viewmodel.QUALITY_FILTER_MODE_PREFERENCE_KEY
import com.github.zly2006.zhihu.viewmodel.QualityFilterMode
import com.github.zly2006.zhihu.viewmodel.feed.BaseFeedViewModel
import com.github.zly2006.zhihu.viewmodel.feed.FollowViewModel
import com.github.zly2006.zhihu.viewmodel.feed.HomeFeedInteractionViewModel
import com.github.zly2006.zhihu.viewmodel.feed.HomeFeedViewModel
import com.github.zly2006.zhihu.viewmodel.feed.ZHIHU_HOT_SEARCH_URL
import com.github.zly2006.zhihu.viewmodel.getOrFetchContentDetail
import com.github.zly2006.zhihu.viewmodel.local.LocalHomeFeedViewModel
import com.github.zly2006.zhihu.viewmodel.rememberPaginationEnvironment
import com.github.zly2006.zhihu.viewmodel.za.AndroidHomeFeedViewModel
import com.github.zly2006.zhihu.viewmodel.za.MixedHomeFeedViewModel
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock

const val PREFERENCE_NAME = "com.github.zly2006.zhihu_preferences"
const val ARTICLE_USE_WEBVIEW_PREFERENCE_KEY = "webviewRenderLegacy"
const val HOME_TOP_ACTIONS_TAG = "home_top_actions"
const val HOME_SEARCH_BUTTON_TAG = "home_search_button"
const val HOME_SEARCH_ACTION_TAG = "home_search_action"
const val HOME_SEARCH_PLACEHOLDER_TAG = "home_search_placeholder"
const val HOME_CATEGORY_TAB_TAG_PREFIX = "home_category_tab"
const val HOME_FEED_LIST_TAG = "home_feed_list"
const val HOME_REFRESH_BUTTON_TAG = "home_refresh_button"
const val HOME_AUTHOR_POLL_ANNOUNCEMENT_TAG = "home_author_poll_announcement"
const val HOME_ONLINE_NOTIFICATION_TAG = "home_online_notification"
const val HOME_PIN_ANNOUNCEMENT_READ_KEY_PREFIX = "readHomePinAnnouncement_"
const val HOME_NOTIFICATION_BUTTON_TAG = "home_notification_button"
const val HOME_NOTIFICATION_BUTTON_CONTENT_TAG = "home_notification_button_content"
const val HOME_NOTIFICATION_BADGE_TAG = "home_notification_badge"

private const val MAX_HOME_PIN_ANNOUNCEMENTS = 3

/** 搜索区的视觉与触控尺寸，集中在这里是为了让「24×26 的消息方框」和 52dp 的搜索区高度对得上。 */
private val SearchAreaHeight = 52.dp
private val MessageEntryVisualWidth = 24.dp
private val MessageEntryVisualHeight = 26.dp
private val MessageEntryShape = RoundedCornerShape(4.dp)

fun homeAuthorPollAnnouncementTag(pinId: Long): String = "$HOME_AUTHOR_POLL_ANNOUNCEMENT_TAG:$pinId"

fun homeOnlineNotificationTag(uuid: String): String = "$HOME_ONLINE_NOTIFICATION_TAG:$uuid"

fun homePinAnnouncementReadKey(pinId: Long): String = "$HOME_PIN_ANNOUNCEMENT_READ_KEY_PREFIX$pinId"

fun homeCategoryTabTag(label: String): String = "$HOME_CATEGORY_TAB_TAG_PREFIX:$label"

/**
 * 首页分类 Tab（产品设计稿 §1.3.2）。
 *
 * 只有关注 / 推荐 / 日报三个，点击切换、不支持横滑；默认停在推荐。
 */
private enum class HomeCategoryTab(
    val label: String,
) {
    Following("关注"),
    Recommend("推荐"),
    Daily("日报"),
}

private val HomeCategoryTabs = listOf(
    HomeCategoryTab.Following,
    HomeCategoryTab.Recommend,
    HomeCategoryTab.Daily,
)

private const val DEFAULT_HOME_CATEGORY_TAB_INDEX = 1

// Pager can dispose this page while its feed ViewModels survive. Keep the header rows and
// synchronization owner alive too, so restoring a saved list index does not shift the visible card.
private class HomeScreenState(
    settings: SettingsStore,
) : ViewModel() {
    val remoteHistory = RemoteHistorySync(settings)
    val onlineNotifications = mutableStateOf(emptyList<OnlineHomeNotification>())
    val authorPinAnnouncements = mutableStateOf(emptyList<HomePinAnnouncement>())
    val dismissedUpdateVersion = mutableStateOf<String?>(null)
}

/**
 * 首页信息流页面（产品设计稿 §一）。
 *
 * 自上而下五段式：状态栏 → 搜索区（消息方框 + 搜索框 + 搜索按钮）→ 分类 Tab（关注 / 推荐 / 日报）
 * → 内容卡片流 → 底部 Tab（由主壳承载）。
 *
 * 页面本身只负责「搜索区 + 分类 Tab + 当前分类的内容」，消息入口从底栏移到了搜索区，
 * 创作入口按产品策略移到「我的」页，因此这里不再有悬浮发布按钮。
 *
 * 改动时同时检查 `recommendationMode`、`showRefreshFab`、`showUnreadBadge` 与三类内容
 * （推荐流 / 关注流 / 日报）的加载与回到顶部路径。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    scrollToTopTrigger: Int,
    innerPadding: PaddingValues,
    showTopActions: Boolean = true,
    isActive: Boolean = true,
) {
    val colors = contentFirstColors
    val readingPlayerOverlayPadding = LocalReadingPlayerOverlayPadding.current
    val navigator = LocalNavigator.current
    val baseEnvironment = rememberPaginationEnvironment(allowGuestAccess = true)
    val settings = rememberSettingsStore()
    val homeState: HomeScreenState = viewModel { HomeScreenState(settings) }
    val remoteHistory = homeState.remoteHistory
    val paginationEnvironment = remember(baseEnvironment, remoteHistory) { remoteHistory.feedEnvironment(baseEnvironment) }
    val appPrivateDirectory = rememberAppPrivateDirectory()
    val notificationSettings = rememberNotificationSettingsStore()
    val userMessages = rememberUserMessageSink()
    val openExternalUrl = rememberExternalUrlOpener()
    val openWebViewUrl = rememberWebViewUrlOpener()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val showRefreshFab = settings.getBoolean("showRefreshFab", true)
    val showFeedThumbnail by rememberObservedSetting(settings, "showFeedThumbnail") {
        getBoolean("showFeedThumbnail", true)
    }
    val autoRefreshOnStartup = settings.getBoolean(AUTO_REFRESH_HOME_ON_STARTUP_PREFERENCE_KEY, true)
    val showUnreadBadge = notificationSettings.getUnreadBadgeEnabled()
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(DEFAULT_HOME_CATEGORY_TAB_INDEX) }
    val selectedCategory = HomeCategoryTabs.getOrElse(selectedCategoryIndex) { HomeCategoryTab.Recommend }

    // 获取当前推荐算法设置
    val currentRecommendationMode =
        RecommendationMode.entries.find {
            it.key == settings.getString("recommendationMode", RecommendationMode.MIXED.key)
        } ?: RecommendationMode.MIXED
    val startupCacheFile = remember(appPrivateDirectory, currentRecommendationMode) {
        Path(appPrivateDirectory, homeFeedStartupCacheFileName(currentRecommendationMode))
    }

    val account = rememberAccountSettingsAccountState().value
    if (account.login && !account.hasRequiredCookie) {
        ZhihuAlertDialog(
            onDismissRequest = {},
            title = { Text("Cookie 不完整") },
            text = { Text("当前登录信息缺少必要的 Cookie d_c0，请重新登录。") },
            confirmButton = {
                TextButton(onClick = ::requestLoginNavigation) {
                    Text("重新登录")
                }
            },
        )
    }
    val updateState by rememberSystemUpdateState().collectAsState()
    val updateAnnouncement = updateState as? SystemUpdateState.UpdateAvailable
    val versionName = rememberAppVersionInfo().substringBefore(' ').takeIf { it.firstOrNull()?.isDigit() == true }
    val onlineNotificationRepository = remember(settings) {
        OnlineHomeNotificationRepository(settings)
    }
    var onlineNotifications by homeState.onlineNotifications
    val isDebuggable = rememberHomeIsDebuggable()
    val viewModel: BaseFeedViewModel = when (currentRecommendationMode) {
        RecommendationMode.WEB -> viewModel { HomeFeedViewModel() }
        RecommendationMode.ANDROID -> viewModel { AndroidHomeFeedViewModel() }
        RecommendationMode.LOCAL -> viewModel { LocalHomeFeedViewModel() }
        RecommendationMode.MIXED -> viewModel { MixedHomeFeedViewModel() }
    }
    val localHomeViewModel = viewModel as? LocalHomeFeedViewModel
    // 卡片点击：能确定所属问题就直接进「该问题下的回答瀑布流」；手机版推荐卡片只带回答 id，
    // 先取一次回答详情拿到 question.id 再跳，取不到才退回单条回答详情页。
    val openItem: (FeedDisplayItem) -> Unit = { item ->
        val feed = item.feed
        if (feed != null) {
            (viewModel as? HomeFeedInteractionViewModel)?.onUiContentClick(paginationEnvironment, feed, item)
        } else {
            localHomeViewModel?.onLocalItemOpened(item)
        }
        val questionTarget = item.feed?.target.let { target ->
            when (target) {
                is Feed.AnswerTarget -> Question(target.question.id, target.question.title)
                is Feed.QuestionTarget -> Question(target.id, target.title)
                else -> null
            }
        }
        val fallback = item.navDestination
        if (questionTarget != null) {
            navigator.onNavigate(questionTarget)
        } else {
            val answerId = (fallback as? Article)?.takeIf { it.type == ArticleType.Answer }?.id
            if (answerId == null) {
                fallback?.let { navigator.onNavigate(it) }
            } else {
                scope.launch {
                    val detail = paginationEnvironment.getOrFetchContentDetail(
                        Article(type = ArticleType.Answer, id = answerId),
                    ) as? DataHolder.Answer
                    val question = detail?.question
                    if (question != null) {
                        navigator.onNavigate(Question(question.id, question.title))
                    } else {
                        // 匿名访问 answers 接口会被知乎要求登录，拿不到 question.id 时跳单条回答页
                        // 只会看到「没有知识存在的荒原」，不如留在原地给出明确提示。
                        userMessages.showShortMessage("暂时无法打开这条回答，登录后可查看所属问题")
                    }
                }
            }
        }
    }
    val readingQueueSourceId = "home:${currentRecommendationMode.name}"
    RegisterReadingQueueSource(
        sourceId = readingQueueSourceId,
        items = viewModel.displayItems,
    )

    var dismissedUpdateVersion by homeState.dismissedUpdateVersion
    var authorPinAnnouncements by homeState.authorPinAnnouncements
    var dismissedFeedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var hotSearchWord by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    var cachedScrollToTopTrigger by remember { mutableIntStateOf(scrollToTopTrigger) }
    LaunchedEffect(scrollToTopTrigger) {
        when (
            topLevelReselectAction(
                triggerDelta = scrollToTopTrigger - cachedScrollToTopTrigger,
                isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0,
            )
        ) {
            TopLevelReselectAction.Refresh -> viewModel.refresh(paginationEnvironment)
            TopLevelReselectAction.ScrollToTop -> listState.animateScrollToItem(0)
            null -> {}
        }
        cachedScrollToTopTrigger = scrollToTopTrigger
    }

    var unreadCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        try {
            unreadCount = paginationEnvironment
                .mobileHomeFeedHttpClient()
                .get("$MOBILE_NOTIFICATION_MESSAGE_URL?limit=20")
                .json<MobileNotificationMessageOverview>()
                .totalUnreadCount
        } catch (_: Exception) {
            // 忽略错误
        }
    }

    // 搜索框占位用当前热搜第一条（产品设计稿 §1.3.1）。取不到时退回固定文案，不阻塞首屏。
    LaunchedEffect(account.login) {
        if (!account.login) return@LaunchedEffect
        val word = try {
            paginationEnvironment
                .fetchJson(ZHIHU_HOT_SEARCH_URL, "")
                ?.get("hot_search_queries")
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("query")
                ?.jsonPrimitive
                ?.contentOrNull
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("HomeScreen", "Failed to load hot search word", e)
            null
        }
        if (!word.isNullOrBlank()) hotSearchWord = word
    }

    val latestLoadedDisplayItems = viewModel.latestLoadedDisplayItems.value
    LaunchedEffect(latestLoadedDisplayItems) {
        if (latestLoadedDisplayItems.isNotEmpty()) {
            encodeHomeFeedStartupSnapshot(latestLoadedDisplayItems)?.let { serialized ->
                withContext(Dispatchers.Default) {
                    runCatching {
                        SystemFileSystem.sink(startupCacheFile).buffered().use { it.writeString(serialized) }
                    }
                }
            }
        }
    }

    val completedPageCount = viewModel.completedPageCount
    LaunchedEffect(completedPageCount) {
        if (completedPageCount > 0 &&
            currentRecommendationMode == RecommendationMode.WEB &&
            settings.getString(QUALITY_FILTER_MODE_PREFERENCE_KEY, QualityFilterMode.RULES.name) == QualityFilterMode.HIDE.name &&
            viewModel.displayItems.isEmpty() &&
            !viewModel.isEnd
        ) {
            viewModel.loadMore(paginationEnvironment)
        }
    }

    LaunchedEffect(lifecycleOwner, currentRecommendationMode, account.login, account.id, autoRefreshOnStartup, isActive) {
        if (!isActive) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (account.login &&
                account.hasRequiredCookie &&
                settings.getBoolean("enableContentFilter", true) &&
                !settings.getBoolean("reverseBlock", false)
            ) {
                remoteHistory.start(homeState.viewModelScope, baseEnvironment)
            }
            if (!account.login &&
                !settings.getBoolean(SKIP_LOGIN_PROMPT_PREFERENCE_KEY, false) &&
                settings.getBoolean("loginForRecommendation", true)
            ) {
                requestLoginNavigation()
            } else if (viewModel.displayItems.isEmpty()) {
                val cachedItems = if (autoRefreshOnStartup) {
                    emptyList()
                } else {
                    withContext(Dispatchers.Default) {
                        runCatching {
                            if (SystemFileSystem.exists(startupCacheFile)) {
                                SystemFileSystem.source(startupCacheFile).buffered().use { source ->
                                    decodeHomeFeedStartupSnapshot(source.readString())
                                }
                            } else {
                                emptyList()
                            }
                        }.getOrDefault(emptyList())
                    }
                }
                if (cachedItems.isNotEmpty()) {
                    remoteHistory.awaitRecentPage()
                    val unreadCachedItems = remoteHistory.filterRemoteReads(cachedItems)
                    if (unreadCachedItems.isEmpty()) {
                        viewModel.refresh(paginationEnvironment)
                    } else {
                        viewModel.addDisplayItems(unreadCachedItems)
                    }
                } else {
                    viewModel.refresh(paginationEnvironment)
                }
            }
        }
    }

    val historyRevision by remoteHistory.importedRevision.collectAsState()
    LaunchedEffect(remoteHistory, viewModel, historyRevision, isActive) {
        if (!isActive) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.isNotEmpty() }.first { it }
        snapshotFlow { viewModel.displayItems.toList() }.collectLatest { items ->
            // Keep cards already reached by the user stable; only prune the unseen tail in place.
            val visibleKeys = listState.layoutInfo.visibleItemsInfo
                .map { it.key }
                .toSet()
            val lastVisible = items.indexOfLast { it.stableKey in visibleKeys }
            val candidates = items.drop(lastVisible + 1)
            val keptKeys = remoteHistory.filterRemoteReads(candidates).map { it.stableKey }.toSet()
            val removedKeys = candidates.filterNot { it.stableKey in keptKeys }.map { it.stableKey }.toSet()
            // The user may have scrolled while the database query was suspended.
            val currentVisibleKeys = listState.layoutInfo.visibleItemsInfo
                .map { it.key }
                .toSet()
            val protectedEnd = viewModel.displayItems.indexOfLast { it.stableKey in currentVisibleKeys }
            for (index in viewModel.displayItems.lastIndex downTo protectedEnd + 1) {
                if (viewModel.displayItems[index].stableKey in removedKeys) viewModel.displayItems.removeAt(index)
            }
            viewModel.latestLoadedDisplayItems.value = remoteHistory.filterRemoteReads(viewModel.latestLoadedDisplayItems.value)
        }
    }

    LaunchedEffect(lifecycleOwner, scrollToTopTrigger) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val loadedAnnouncements = try {
                paginationEnvironment
                    .fetchJson(ZHIHU_PLUS_AUTHOR_PINS_URL, "")
                    ?.let(::decodeHomePinAnnouncements)
                    ?.filter {
                        it.kind == HomePinAnnouncementKind.Poll ||
                            it.createdAtEpochSeconds >= Clock.System.now().epochSeconds - 5 * 24 * 60 * 60
                    }?.filterNot { settings.getBoolean(homePinAnnouncementReadKey(it.pinId), false) }
                    ?.take(MAX_HOME_PIN_ANNOUNCEMENTS)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HomeScreen", "Failed to load home pin announcements", e)
                null
            }
            if (loadedAnnouncements != null) {
                authorPinAnnouncements = loadedAnnouncements
            }
        }
    }

    LaunchedEffect(lifecycleOwner, paginationEnvironment, versionName) {
        if (versionName != null) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    try {
                        onlineNotifications = onlineNotificationRepository.load(
                            versionName = versionName,
                            httpClient = paginationEnvironment.httpClient(),
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "Failed to load online notifications", e)
                    }
                    delay(HOME_NOTIFICATION_REFRESH_INTERVAL_MILLIS)
                }
            }
        }
    }

    // 显示错误信息
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            userMessages.showMessage(it, UserMessageDuration.Long)
        }
    }

    var feedAuthorBlockRequest by remember { mutableStateOf<FeedAuthorBlockRequest?>(null) }

    // 按关键词屏蔽对话框
    var showBlockByKeywordsDialog by remember { mutableStateOf(false) }
    var feedToBlockByKeywords by remember { mutableStateOf<Pair<String, String?>?>(null) } // 二元组内容为标题和摘要。
    val pageTurnTarget = rememberPageTurnTarget(
        listState = listState,
        enabled = isActive &&
            selectedCategory == HomeCategoryTab.Recommend &&
            feedAuthorBlockRequest == null &&
            !showBlockByKeywordsDialog &&
            (!account.login || account.hasRequiredCookie),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.page,
        topBar = {
            if (!showTopActions) {
                return@Scaffold
            }
            // Scaffold 的 topBar 槽位会把多个子项叠在同一位置，因此这里显式竖排。
            Column(modifier = Modifier.fillMaxWidth()) {
                HomeSearchArea(
                    hotSearchWord = hotSearchWord,
                    unreadCount = if (showUnreadBadge) unreadCount else 0,
                    onOpenNotifications = { navigator.onNavigate(Notification) },
                    onOpenSearch = { navigator.onNavigate(Search(query = "")) },
                )
                HomeCategoryTabRow(
                    selected = selectedCategory,
                    onSelect = { tab ->
                        selectedCategoryIndex = HomeCategoryTabs.indexOf(tab)
                    },
                )
            }
        },
    ) { scaffoldPadding ->
        val contentTopPadding = scaffoldPadding.calculateTopPadding()
        when (selectedCategory) {
            HomeCategoryTab.Recommend -> {
                FeedPullToRefresh(viewModel, PaddingValues(top = contentTopPadding)) {
                    PaginatedList(
                        items = viewModel.displayItems.filterNot { it.stableKey in dismissedFeedKeys },
                        listState = listState,
                        modifier = Modifier
                            .pageTurnViewportWithGuide(pageTurnTarget)
                            .testTag(HOME_FEED_LIST_TAG),
                        contentPadding = PaddingValues(
                            top = contentTopPadding,
                            bottom = innerPadding.calculateBottomPadding() + readingPlayerOverlayPadding,
                        ),
                        onLoadMore = { viewModel.loadMore(paginationEnvironment) },
                        isEnd = { viewModel.isEnd },
                        footer = ProgressIndicatorFooter,
                        key = { item -> item.stableKey },
                        topContent = {
                            item {
                                val availableUpdate = updateAnnouncement

                                AnnouncementCard(
                                    visible = availableUpdate != null && dismissedUpdateVersion != availableUpdate.version,
                                    title = "发现新版本：${availableUpdate?.version}${if (availableUpdate?.isNightly == true) " (Nightly)" else ""}",
                                    leadingIcon = { Icon(Icons.Default.ArrowCircleUp, contentDescription = null) },
                                    accept = { Text("查看更新") },
                                    onAccept = {
                                        navigator.onNavigate(Account.SystemAndUpdateSettings())
                                    },
                                    dismiss = { Text("以后") },
                                    onDismiss = {
                                        availableUpdate?.version?.let { versionStr ->
                                            dismissedUpdateVersion = versionStr
                                        }
                                    },
                                    colors = AnnouncementCardDefaults.colorsImportant(),
                                )
                            }
                            onlineNotifications.forEach { notification ->
                                val markRead = {
                                    onlineNotificationRepository.markRead(notification)
                                    onlineNotifications = onlineNotifications.filterNot { it.uuid == notification.uuid }
                                }
                                item(notification.uuid) {
                                    Box(modifier = Modifier.animateItem()) {
                                        AnnouncementCard(
                                            modifier = Modifier.testTag(homeOnlineNotificationTag(notification.uuid)),
                                            visible = true,
                                            title = notification.title,
                                            leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                                            content = notification.content,
                                            accept = notification.accept?.let { accept ->
                                                { Text(accept.text) }
                                            },
                                            onAccept = {
                                                val accept = notification.accept
                                                markRead()
                                                when (accept?.key) {
                                                    HOME_NOTIFICATION_ACTION_OPEN_URL -> {
                                                        accept.value
                                                            ?.jsonPrimitive
                                                            ?.contentOrNull
                                                            ?.let(openExternalUrl::invoke)
                                                    }
                                                    HOME_NOTIFICATION_ACTION_OPEN_WEBVIEW -> {
                                                        accept.value
                                                            ?.jsonPrimitive
                                                            ?.contentOrNull
                                                            ?.let(openWebViewUrl::invoke)
                                                    }
                                                    HOME_NOTIFICATION_ACTION_OPEN_UPDATE_SETTINGS -> {
                                                        navigator.onNavigate(Account.SystemAndUpdateSettings())
                                                    }
                                                    HOME_NOTIFICATION_ACTION_OPEN_PIN -> {
                                                        accept.value?.jsonPrimitive?.contentOrNull?.toLongOrNull()?.let {
                                                            navigator.onNavigate(Pin(it))
                                                        }
                                                    }
                                                    HOME_NOTIFICATION_ACTION_OPEN_ANSWER -> {
                                                        accept.value?.jsonPrimitive?.contentOrNull?.toLongOrNull()?.let {
                                                            navigator.onNavigate(Article(type = ArticleType.Answer, id = it))
                                                        }
                                                    }
                                                    HOME_NOTIFICATION_ACTION_OPEN_ARTICLE -> {
                                                        accept.value?.jsonPrimitive?.contentOrNull?.toLongOrNull()?.let {
                                                            navigator.onNavigate(Article(type = ArticleType.Article, id = it))
                                                        }
                                                    }
                                                    HOME_NOTIFICATION_ACTION_SET_SETTING -> {
                                                        val setting = accept.value?.jsonObject
                                                        val name = setting?.get("setting_name")?.jsonPrimitive?.contentOrNull
                                                        when (setting?.get("value_type")?.jsonPrimitive?.contentOrNull) {
                                                            "boolean" -> setting["value"]?.jsonPrimitive?.booleanOrNull?.let {
                                                                settings.putBoolean(name!!, it)
                                                            }
                                                            "string" -> setting["value"]?.jsonPrimitive?.contentOrNull?.let {
                                                                settings.putString(name!!, it)
                                                            }
                                                            "int" -> setting["value"]?.jsonPrimitive?.intOrNull?.let {
                                                                settings.putInt(name!!, it)
                                                            }
                                                        }
                                                    }
                                                    else -> userMessages.showShortMessage("当前版本不支持此通知操作")
                                                }
                                            },
                                            dismiss = { Text(notification.dismiss) },
                                            onDismiss = markRead,
                                        )
                                    }
                                }
                            }
                            authorPinAnnouncements.forEach { announcement ->
                                item(announcement.pinId) {
                                    AnnouncementCard(
                                        modifier = Modifier.testTag(homeAuthorPollAnnouncementTag(announcement.pinId)),
                                        visible = true,
                                        title = if (announcement.kind == HomePinAnnouncementKind.Poll) {
                                            "请给未来的知乎++提出建议"
                                        } else {
                                            "知乎++新动态"
                                        },
                                        leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                                        content = buildString {
                                            append(announcement.title)
                                            val details = buildList {
                                                if (announcement.optionCount > 0) {
                                                    add("${announcement.optionCount} 个选项")
                                                }
                                                if (announcement.memberCount > 0) {
                                                    add("${announcement.memberCount} 人已参与")
                                                }
                                            }
                                            if (details.isNotEmpty()) {
                                                append("\n")
                                                append(details.joinToString(" · "))
                                            }
                                        },
                                        accept = {
                                            Text(if (announcement.kind == HomePinAnnouncementKind.Poll) "去投票" else "查看")
                                        },
                                        onAccept = {
                                            settings.putBoolean(homePinAnnouncementReadKey(announcement.pinId), true)
                                            authorPinAnnouncements = authorPinAnnouncements.filterNot {
                                                it.pinId == announcement.pinId
                                            }
                                            navigator.onNavigate(Pin(announcement.pinId))
                                        },
                                        dismiss = { Text("关闭") },
                                        onDismiss = {
                                            settings.putBoolean(homePinAnnouncementReadKey(announcement.pinId), true)
                                            authorPinAnnouncements = authorPinAnnouncements.filterNot {
                                                it.pinId == announcement.pinId
                                            }
                                        },
                                    )
                                }
                            }
                        },
                    ) { item ->
                        ContentFeedCard(
                            item = item,
                            showImages = showFeedThumbnail,
                            onClick = { openItem(item) },
                            onDismiss = {
                                // 不感兴趣：卡片原位移除，并把负反馈交给当前推荐链路。
                                dismissedFeedKeys = dismissedFeedKeys + item.stableKey
                                viewModel.displayItems.removeAll { it.stableKey == item.stableKey }
                                localHomeViewModel?.onLocalItemDismissed(item)
                            },
                            menuItems = { dismissMenu ->
                                if (feedKeywordExtractionAvailable) {
                                    DropdownMenuItem(
                                        text = { Text("按关键词屏蔽") },
                                        onClick = {
                                            dismissMenu()
                                            viewModel.handleBlockByKeywords(paginationEnvironment, userMessages, item) { (_, contentInfo) ->
                                                feedToBlockByKeywords = contentInfo.first to contentInfo.second
                                                showBlockByKeywordsDialog = true
                                            }
                                        },
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("屏蔽用户") },
                                    onClick = {
                                        dismissMenu()
                                        viewModel.handleBlockUser(paginationEnvironment, userMessages, item) { authorInfo ->
                                            feedAuthorBlockRequest = FeedAuthorBlockRequest(
                                                type = FeedAuthorBlockType.CONTENT_AUTHOR,
                                                userId = authorInfo.first,
                                                userName = authorInfo.second,
                                            )
                                        }
                                    },
                                )
                                val canBlockQuestionAuthor = when (item.feed?.target) {
                                    is Feed.AnswerTarget, is Feed.QuestionTarget -> true
                                    else -> item.raw is DataHolder.Answer || item.raw is DataHolder.Question
                                }
                                if (canBlockQuestionAuthor) {
                                    DropdownMenuItem(
                                        text = { Text("屏蔽提问者") },
                                        onClick = {
                                            dismissMenu()
                                            viewModel.handleBlockQuestionAuthor(paginationEnvironment, userMessages, item) { authorInfo ->
                                                feedAuthorBlockRequest = FeedAuthorBlockRequest(
                                                    type = FeedAuthorBlockType.QUESTION_AUTHOR,
                                                    userId = authorInfo.first,
                                                    userName = authorInfo.second,
                                                )
                                            }
                                        },
                                    )
                                }
                                val topics = when (val raw = item.raw) {
                                    is DataHolder.Answer -> raw.question.topics
                                    is DataHolder.Question -> raw.topics
                                    is DataHolder.Article -> raw.topics ?: emptyList()
                                    is DataHolder.Pin -> raw.topics ?: emptyList()
                                    else -> emptyList()
                                }
                                topics.forEach { topic ->
                                    DropdownMenuItem(
                                        text = { Text("屏蔽「${topic.name}」") },
                                        onClick = {
                                            dismissMenu()
                                            viewModel.handleBlockTopic(userMessages, topic.id, topic.name)
                                        },
                                    )
                                }
                            },
                        )
                    }

                    if (showRefreshFab) {
                        if (isDebuggable) {
                            DraggableRefreshButton(
                                onClick = {
                                    val data = Json.encodeToString(viewModel.debugData)
                                    paginationEnvironment.setPlainTextClipboard("data", data)
                                    userMessages.showShortMessage("已复制调试数据")
                                },
                                preferenceName = "copyAll",
                                bottomAvoidance = readingPlayerOverlayPadding,
                            ) {
                                Icon(Icons.Default.CopyAll, contentDescription = "复制")
                            }
                        }
                        DraggableRefreshButton(
                            modifier = Modifier.testTag(HOME_REFRESH_BUTTON_TAG),
                            bottomAvoidance = readingPlayerOverlayPadding,
                            onClick = { viewModel.refresh(paginationEnvironment) },
                        ) {
                            // 刷新图标 ↔ 转圈：容器尺寸固定 30dp，两者在同一格内交叉淡入淡出，
                            // 避免「转圈比图标大 10dp」导致的 FAB 内容跳动。
                            Box(
                                modifier = Modifier.size(30.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                AnimatedContent(
                                    targetState = viewModel.isLoading,
                                    transitionSpec = {
                                        fadeIn(Motion.fadeTween()) togetherWith fadeOut(Motion.fadeTween())
                                    },
                                    label = "refresh_fab_content",
                                ) { loading ->
                                    if (loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.fillMaxSize(),
                                            strokeWidth = 3.dp,
                                        )
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            HomeCategoryTab.Following -> HomeFollowingFeed(
                innerPadding = innerPadding,
                contentTopPadding = contentTopPadding,
                isActive = isActive && selectedCategory == HomeCategoryTab.Following,
                onOpenItem = openItem,
            )
            HomeCategoryTab.Daily -> Box(
                // Scaffold 的 content 自己承担内边距：日报是完整页面而不是分页列表，
                // 直接让出搜索区与底栏高度，避免列表钻到分类 Tab 底下。
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = contentTopPadding,
                        bottom = innerPadding.calculateBottomPadding(),
                    ),
            ) {
                DailyScreen(
                    scrollToTopTrigger = scrollToTopTrigger,
                    isActive = isActive && selectedCategory == HomeCategoryTab.Daily,
                    embedded = true,
                )
            }
        }
    }

    FeedAuthorBlockConfirmDialog(
        request = feedAuthorBlockRequest,
        displayItems = viewModel.displayItems,
        onDismiss = { feedAuthorBlockRequest = null },
        onConfirm = {
            viewModel.refresh(paginationEnvironment)
            feedAuthorBlockRequest = null
        },
    )

    // 按关键词屏蔽对话框
    feedToBlockByKeywords?.let { (title, excerpt) ->
        BlockByKeywordsDialog(
            showDialog = showBlockByKeywordsDialog,
            feedTitle = title,
            feedExcerpt = excerpt,
            onDismiss = {
                showBlockByKeywordsDialog = false
                feedToBlockByKeywords = null
            },
            onConfirm = {
                viewModel.refresh(paginationEnvironment)
                showBlockByKeywordsDialog = false
                feedToBlockByKeywords = null
            },
        )
    }
}

/** 搜索区（§1.3.1）：消息方框 + 胶囊搜索框 + 搜索按钮，整行高度 52dp。 */
@Composable
private fun HomeSearchArea(
    hotSearchWord: String,
    unreadCount: Int,
    onOpenNotifications: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    val colors = contentFirstColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.page)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
    ) {
        Row(
            modifier = Modifier
                .testTag(HOME_TOP_ACTIONS_TAG)
                .fillMaxWidth()
                .height(SearchAreaHeight)
                .padding(start = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeMessageEntryButton(
                unreadCount = unreadCount,
                onClick = onOpenNotifications,
            )
            Spacer(Modifier.width(4.dp))
            HomeSearchField(
                hotSearchWord = hotSearchWord,
                onClick = onOpenSearch,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            HomeSearchActionButton(onClick = onOpenSearch)
        }
    }
}

/** 消息方框：24×26 圆角方框 + 信封线性图标，右上角红色数字角标。 */
@Composable
private fun HomeMessageEntryButton(
    unreadCount: Int,
    onClick: () -> Unit,
) {
    val colors = contentFirstColors
    // 点击盒比视觉方框大一圈（40×48），保证拇指可点；方框本身贴 16dp 内容边距。
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 48.dp)
            .testTag(HOME_NOTIFICATION_BUTTON_TAG)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = if (unreadCount > 0) "消息，$unreadCount 条未读" else "消息" },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 34.dp)
                .testTag(HOME_NOTIFICATION_BUTTON_CONTENT_TAG),
            contentAlignment = Alignment.CenterStart,
        ) {
            // 外框不裁剪，角标才能压在方框右上角而不被切掉；描边画在等尺寸的内层。
            Box(modifier = Modifier.size(width = MessageEntryVisualWidth, height = MessageEntryVisualHeight)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(MessageEntryShape)
                        .border(1.dp, colors.secondaryText, MessageEntryShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MailOutline,
                        contentDescription = null,
                        tint = colors.primaryText,
                        modifier = Modifier.size(13.dp),
                    )
                }
                if (unreadCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-6).dp)
                            .testTag(HOME_NOTIFICATION_BADGE_TAG),
                        containerColor = colors.badge,
                        contentColor = Color.White,
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else "$unreadCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        )
                    }
                }
            }
        }
    }
}

/** 搜索框：胶囊形，内置热搜词占位（灰）与右侧麦克风。 */
@Composable
private fun HomeSearchField(
    hotSearchWord: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = contentFirstColors
    val placeholder = hotSearchWord.ifBlank { "搜索知乎内容" }
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(colors.functionalSurface)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(HOME_SEARCH_BUTTON_TAG)
            .padding(start = 14.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .testTag(HOME_SEARCH_PLACEHOLDER_TAG),
        )
        Icon(
            imageVector = Icons.Outlined.Mic,
            contentDescription = "语音搜索",
            tint = colors.secondaryText,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** 搜索按钮：小圆角、主题蓝底白字。 */
@Composable
private fun HomeSearchActionButton(onClick: () -> Unit) {
    val colors = contentFirstColors
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.accent)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(HOME_SEARCH_ACTION_TAG)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "搜索",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
        )
    }
}

/** 分类 Tab（§1.3.2）：三个、水平居中、等间距；选中加粗并带 2dp 主题蓝短下划线。 */
@Composable
private fun HomeCategoryTabRow(
    selected: HomeCategoryTab,
    onSelect: (HomeCategoryTab) -> Unit,
) {
    val colors = contentFirstColors
    Column(modifier = Modifier.fillMaxWidth().background(colors.page)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeCategoryTabs.forEach { tab ->
                val isSelected = tab == selected
                Column(
                    modifier = Modifier
                        .clickable(role = Role.Tab) { onSelect(tab) }
                        .testTag(homeCategoryTabTag(tab.label))
                        .semantics { this.selected = isSelected }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        ),
                        color = if (isSelected) colors.primaryText else colors.secondaryText,
                    )
                    Spacer(Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 2.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.accent else Color.Transparent),
                    )
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
    }
}

/**
 * 关注分类的内容：用与推荐流相同的内容卡片呈现在关注的人的动态。
 *
 * 关注流只有一条时间线（不再有「推荐 / 动态」二级 Tab），因此这里直接复用 [FollowViewModel] 的分页能力。
 */
@Composable
private fun HomeFollowingFeed(
    innerPadding: PaddingValues,
    contentTopPadding: Dp,
    isActive: Boolean,
    onOpenItem: (FeedDisplayItem) -> Unit,
) {
    val readingPlayerOverlayPadding = LocalReadingPlayerOverlayPadding.current
    val navigator = LocalNavigator.current
    val settings = rememberSettingsStore()
    val showFeedThumbnail by rememberObservedSetting(settings, "showFeedThumbnail") {
        getBoolean("showFeedThumbnail", true)
    }
    val environment = rememberPaginationEnvironment(allowGuestAccess = true)
    val viewModel: FollowViewModel = viewModel { FollowViewModel() }
    val listState = rememberLazyListState()
    RegisterReadingQueueSource(sourceId = "home:following", items = viewModel.displayItems)
    LaunchedEffect(isActive) {
        if (isActive && viewModel.displayItems.isEmpty() && !viewModel.isLoading) {
            viewModel.refresh(environment)
        }
    }
    FeedPullToRefresh(viewModel, environment, PaddingValues(top = contentTopPadding)) {
        PaginatedList(
            items = viewModel.displayItems,
            listState = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentTopPadding,
                bottom = innerPadding.calculateBottomPadding() + readingPlayerOverlayPadding,
            ),
            onLoadMore = { viewModel.loadMore(environment) },
            isEnd = { viewModel.isEnd },
            // 关注流没有登录态时接口会失败，给重试入口比一直转骨架屏更明确。
            errorMessage = viewModel.errorMessage,
            onRetry = { viewModel.refresh(environment) },
            footer = ProgressIndicatorFooter,
            key = { item -> item.stableKey },
        ) { item ->
            ContentFeedCard(
                item = item,
                showImages = showFeedThumbnail,
                onClick = { onOpenItem(item) },
            )
        }
    }
}
