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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.github.zly2006.zhihu.account.GuestSessionState
import com.github.zly2006.zhihu.account.rememberZhihuAccountStore
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
import com.github.zly2006.zhihu.navigation.Collections
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.Notification
import com.github.zly2006.zhihu.navigation.Pin
import com.github.zly2006.zhihu.navigation.Question
import com.github.zly2006.zhihu.navigation.Search
import com.github.zly2006.zhihu.navigation.WritePin
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
import com.github.zly2006.zhihu.theme.ZhihuTypography
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
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSizes
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
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
const val HOME_TOP_MENU_TAG = "home_top_menu"
const val HOME_TOP_MENU_PUBLISH_TAG = "home_top_menu_publish"
const val HOME_TOP_MENU_MESSAGES_TAG = "home_top_menu_messages"
const val HOME_TOP_MENU_COLLECTIONS_TAG = "home_top_menu_collections"
const val HOME_TOP_MENU_SETTINGS_TAG = "home_top_menu_settings"
const val HOME_TOP_MENU_LOGIN_TAG = "home_top_menu_login"
const val HOME_TAB_EDIT_TAG = "home_tab_edit"
const val HOME_CHANNEL_SHEET_TAG = "home_channel_sheet"
const val HOME_CHANNEL_TAB_TAG_PREFIX = "home_channel_tab"

private const val MAX_HOME_PIN_ANNOUNCEMENTS = 3
private const val HOME_TAB_HIDDEN_PREFERENCE_KEY = "homeCategoryHiddenTabs"

/** 搜索胶囊的尺寸细节（官方 11.14 实测，见 target-spec §2.1）。 */
private val SearchPillBorderWidth = 1.5.dp
private val SearchPillStartPadding = 11.dp
private val SearchPillEndPadding = 5.dp
private val HotBadgeShape = RoundedCornerShape(3.dp)
private val HotBadgeHorizontalPadding = 4.dp
private val HotBadgeVerticalPadding = 1.dp
private val SearchDividerWidth = 1.5.dp
private val SearchDividerHeight = 12.dp

/** 分类 Tab 的尺寸细节（官方 11.14 实测，见 target-spec §2.2）。 */
private val TabIndicatorShape = RoundedCornerShape(2.dp)
private val TabScrollFadeWidth = 10.dp
private val TabIconSlotSize = 40.dp

fun homeAuthorPollAnnouncementTag(pinId: Long): String = "$HOME_AUTHOR_POLL_ANNOUNCEMENT_TAG:$pinId"

fun homeOnlineNotificationTag(uuid: String): String = "$HOME_ONLINE_NOTIFICATION_TAG:$uuid"

fun homePinAnnouncementReadKey(pinId: Long): String = "$HOME_PIN_ANNOUNCEMENT_READ_KEY_PREFIX$pinId"

fun homeCategoryTabTag(label: String): String = "$HOME_CATEGORY_TAB_TAG_PREFIX:$label"

fun homeChannelTabTag(label: String): String = "$HOME_CHANNEL_TAB_TAG_PREFIX:$label"

/**
 * 首页分类 Tab（官方 11.14：关注 / 推荐 / 热榜 / 日报；频道编辑可显隐，默认停在推荐）。
 */
private enum class HomeCategoryTab(
    val label: String,
) {
    Following("关注"),
    Recommend("推荐"),
    HotList("热榜"),
    Daily("日报"),
}

private val HomeCategoryTabs = HomeCategoryTab.entries

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
 * 首页信息流页面（官方知乎 11.14 版式，见 `reports/official-align/target-spec.md` §2）。
 *
 * 自上而下：状态栏 → 搜索区（账号头像 + 蓝描边搜索胶囊）→ 分类 Tab（关注 / 推荐 / 热榜 / 日报）
 * → 内容卡片流 → 底部 Tab（由主壳承载）。消息入口由底栏承担，因此搜索区不再有消息方框。
 *
 * 改动时同时检查 `recommendationMode`、`showRefreshFab`、频道显隐偏好与四类内容
 * （推荐 / 关注 / 热榜 / 日报）的加载与回到顶部路径。
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
    val userMessages = rememberUserMessageSink()
    val openExternalUrl = rememberExternalUrlOpener()
    val openWebViewUrl = rememberWebViewUrlOpener()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val showRefreshFab = settings.getBoolean("showRefreshFab", false)
    val showFeedThumbnail by rememberObservedSetting(settings, "showFeedThumbnail") {
        getBoolean("showFeedThumbnail", true)
    }
    val autoRefreshOnStartup = settings.getBoolean(AUTO_REFRESH_HOME_ON_STARTUP_PREFERENCE_KEY, true)
    val hiddenTabKeys by rememberObservedSetting(settings, HOME_TAB_HIDDEN_PREFERENCE_KEY) {
        getStringSet(HOME_TAB_HIDDEN_PREFERENCE_KEY, emptySet())
    }
    val visibleTabs = HomeCategoryTabs.filterNot { it.name in hiddenTabKeys }.ifEmpty { listOf(HomeCategoryTab.Recommend) }
    var selectedCategoryName by rememberSaveable { mutableStateOf(HomeCategoryTab.Recommend.name) }
    val selectedCategory = visibleTabs.firstOrNull { it.name == selectedCategoryName } ?: visibleTabs.first()
    var showChannelSheet by remember { mutableStateOf(false) }

    // 获取当前推荐算法设置
    val currentRecommendationMode =
        RecommendationMode.entries.find {
            it.key == settings.getString("recommendationMode", RecommendationMode.MIXED.key)
        } ?: RecommendationMode.MIXED
    val startupCacheFile = remember(appPrivateDirectory, currentRecommendationMode) {
        Path(appPrivateDirectory, homeFeedStartupCacheFileName(currentRecommendationMode))
    }

    val account = rememberAccountSettingsAccountState().value
    val accountStore = rememberZhihuAccountStore()
    val guestSessionState by accountStore.guestSessionState.collectAsState()
    // 消息未读角标现在挂在搜索框右侧的更多入口上；仅登录态且用户开启角标时请求。
    val notificationSettings = rememberNotificationSettingsStore()
    val unreadBadgeEnabled = notificationSettings.getUnreadBadgeEnabled()
    var unreadNotificationCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(lifecycleOwner, account.login, unreadBadgeEnabled, baseEnvironment) {
        if (!account.login || !unreadBadgeEnabled) {
            unreadNotificationCount = 0
            return@LaunchedEffect
        }
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            unreadNotificationCount = runCatching {
                baseEnvironment
                    .mobileHomeFeedHttpClient()
                    .get("$MOBILE_NOTIFICATION_MESSAGE_URL?limit=20")
                    .json<MobileNotificationMessageOverview>()
                    .totalUnreadCount
            }.getOrDefault(0)
        }
    }
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
                is Feed.AnswerTarget ->
                    Question(
                        questionId = target.question.id,
                        title = target.question.title,
                        initialAnswerId = target.id,
                    )
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
                        navigator.onNavigate(
                            Question(
                                questionId = question.id,
                                title = question.title,
                                initialAnswerId = answerId,
                            ),
                        )
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

    LaunchedEffect(
        lifecycleOwner,
        currentRecommendationMode,
        account.login,
        account.id,
        autoRefreshOnStartup,
        isActive,
        guestSessionState,
    ) {
        if (!isActive) return@LaunchedEffect
        // 游客首屏等官方游客凭证初始化完成（或明确失败）再发首个请求，避免无 d_c0 的首屏 403。
        if (!account.login &&
            guestSessionState != GuestSessionState.READY &&
            guestSessionState != GuestSessionState.FAILED
        ) {
            return@LaunchedEffect
        }
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (account.login &&
                account.hasRequiredCookie &&
                settings.getBoolean("enableContentFilter", true) &&
                !settings.getBoolean("reverseBlock", false)
            ) {
                remoteHistory.start(homeState.viewModelScope, baseEnvironment)
            }
            if (viewModel.displayItems.isEmpty()) {
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
            if (showTopActions) {
                // Scaffold 的 topBar 槽位会把多个子项叠在同一位置，因此这里显式竖排。
                Column(modifier = Modifier.fillMaxWidth()) {
                    HomeSearchArea(
                        hotSearchWord = hotSearchWord,
                        account = account,
                        unreadCount = unreadNotificationCount,
                        onOpenSearch = { navigator.onNavigate(Search(query = "")) },
                    )
                    HomeCategoryTabRow(
                        tabs = visibleTabs,
                        selected = selectedCategory,
                        onSelect = { tab -> selectedCategoryName = tab.name },
                        onEditChannels = { showChannelSheet = true },
                    )
                }
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
            HomeCategoryTab.HotList -> Box(
                // 热榜复用独立热榜页的分页/刷新能力，只让出搜索区与底栏高度。
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = contentTopPadding,
                        bottom = innerPadding.calculateBottomPadding() + readingPlayerOverlayPadding,
                    ),
            ) {
                HotListScreen(
                    scrollToTopTrigger = scrollToTopTrigger,
                    isActive = isActive && selectedCategory == HomeCategoryTab.HotList,
                )
            }
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

    if (showChannelSheet) {
        HomeChannelSheet(
            tabs = HomeCategoryTabs,
            hiddenTabKeys = hiddenTabKeys,
            onDismiss = { showChannelSheet = false },
            onToggle = { tab, visible ->
                val updated = if (visible) hiddenTabKeys - tab.name else hiddenTabKeys + tab.name
                if (HomeCategoryTabs.any { it.name !in updated }) {
                    settings.putStringSet(HOME_TAB_HIDDEN_PREFERENCE_KEY, updated)
                }
            },
        )
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

/** 搜索区：40dp 蓝描边搜索胶囊 + 右侧更多菜单入口（发布 / 消息 / 收藏夹 / 历史 / 设置）。 */
@Composable
private fun HomeSearchArea(
    hotSearchWord: String,
    account: AccountSettingsAccountState,
    unreadCount: Int,
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
                .height(ZhihuSizes.HomeSearchRowHeight)
                .padding(horizontal = ZhihuSizes.FeedCardHorizontalGutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeSearchPill(
                hotSearchWord = hotSearchWord,
                onOpenSearch = onOpenSearch,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(ZhihuSpacing.Small))
            HomeTopMenu(account = account, unreadCount = unreadCount)
        }
    }
}

/**
 * 搜索框右侧的「更多」入口。
 *
 * 底栏只保留「首页 / 我的」两个高频入口，发布、消息、收藏夹、历史与设置收进这个小弹层；
 * 消息未读数以角标形式显示在入口按钮上（仅登录态请求）。
 */
@Composable
private fun HomeTopMenu(
    account: AccountSettingsAccountState,
    unreadCount: Int,
) {
    val colors = contentFirstColors
    val navigator = LocalNavigator.current
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.size(ZhihuSizes.HomeAvatarSize)) {
        // 外圈不裁剪：未读角标需要压在按钮右上角之外，不能被圆形 clip 切掉。
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(colors.functionalSurface)
                .clickable(role = Role.Button) { expanded = true }
                .testTag(HOME_TOP_MENU_TAG)
                .semantics { contentDescription = "更多" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.GridView,
                contentDescription = null,
                tint = colors.primaryText,
                modifier = Modifier.size(ZhihuSizes.IconSmall),
            )
        }
        if (unreadCount > 0) {
            Badge(
                containerColor = colors.badge,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-6).dp),
            ) {
                Text(
                    text = if (unreadCount > 99) "99+" else "$unreadCount",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (account.login) {
                HomeTopMenuItem(Icons.Outlined.Edit, "发布想法", HOME_TOP_MENU_PUBLISH_TAG) {
                    expanded = false
                    navigator.onNavigate(WritePin())
                }
                HomeTopMenuItem(Icons.Outlined.ChatBubbleOutline, "消息", HOME_TOP_MENU_MESSAGES_TAG, unreadCount) {
                    expanded = false
                    navigator.onNavigate(Notification)
                }
                HomeTopMenuItem(Icons.Outlined.BookmarkBorder, "收藏夹", HOME_TOP_MENU_COLLECTIONS_TAG) {
                    expanded = false
                    account.urlToken?.takeIf(String::isNotBlank)?.let {
                        navigator.onNavigate(Collections(it))
                    }
                }
                HorizontalDivider(color = colors.divider)
                HomeTopMenuItem(Icons.Outlined.Settings, "设置", HOME_TOP_MENU_SETTINGS_TAG) {
                    expanded = false
                    navigator.onNavigateTopLevel(Account)
                }
            } else {
                HomeTopMenuItem(Icons.AutoMirrored.Filled.Login, "登录知乎", HOME_TOP_MENU_LOGIN_TAG) {
                    expanded = false
                    requestLoginNavigation()
                }
                HomeTopMenuItem(Icons.Outlined.Settings, "设置", HOME_TOP_MENU_SETTINGS_TAG) {
                    expanded = false
                    navigator.onNavigateTopLevel(Account)
                }
            }
        }
    }
}

@Composable
private fun HomeTopMenuItem(
    icon: ImageVector,
    label: String,
    tag: String,
    badgeCount: Int = 0,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text = label, style = ZhihuTypography.AnswerVoteLine) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ZhihuSizes.IconSmall),
            )
        },
        trailingIcon = if (badgeCount > 0) {
            {
                Badge(containerColor = contentFirstColors.badge) {
                    Text(if (badgeCount > 99) "99+" else "$badgeCount")
                }
            }
        } else {
            null
        },
        modifier = Modifier.testTag(tag),
        onClick = onClick,
    )
}

/**
 * 搜索胶囊：40dp 高、20dp 圆角、1.5dp 品牌蓝描边；底色与页面同色（官方只有描边可见）。
 * 内部是热词 + 热标 + 竖分隔 + 搜索按钮。
 */
@Composable
private fun HomeSearchPill(
    hotSearchWord: String,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = contentFirstColors
    val hasHotWord = hotSearchWord.isNotBlank()
    val pillShape = RoundedCornerShape(ZhihuSizes.HomeSearchPillRadius)
    Row(
        modifier = modifier
            .height(ZhihuSizes.HomeSearchRowHeight)
            .border(SearchPillBorderWidth, colors.accentFill, pillShape)
            .clickable(role = Role.Button, onClick = onOpenSearch)
            .testTag(HOME_SEARCH_BUTTON_TAG)
            .padding(start = SearchPillStartPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (hasHotWord) hotSearchWord else "搜索知乎内容",
            style = ZhihuTypography.HotWord,
            color = if (hasHotWord) colors.primaryText else colors.tertiaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .testTag(HOME_SEARCH_PLACEHOLDER_TAG),
        )
        if (hasHotWord) {
            Spacer(Modifier.width(6.dp))
            HomeHotBadge()
            Spacer(Modifier.width(ZhihuSpacing.Small))
            Box(
                modifier = Modifier
                    .width(SearchDividerWidth)
                    .height(SearchDividerHeight)
                    .background(colors.accent.copy(alpha = 0.5f)),
            )
            Spacer(Modifier.width(ZhihuSpacing.Small))
        }
        HomeSearchActionButton(onClick = onOpenSearch)
        Spacer(Modifier.width(SearchPillEndPadding))
    }
}

/** 「热」角标：红字 + 1dp 同色描边 + 3dp 圆角。 */
@Composable
private fun HomeHotBadge() {
    val colors = contentFirstColors
    Text(
        text = "热",
        style = MaterialTheme.typography.labelSmall,
        color = colors.badge,
        modifier = Modifier
            .border(1.dp, colors.badge, HotBadgeShape)
            .padding(horizontal = HotBadgeHorizontalPadding, vertical = HotBadgeVerticalPadding),
    )
}

/** 胶囊内「搜索」按钮：56×30dp 品牌蓝底、14sp Bold 白字、全圆角。 */
@Composable
private fun HomeSearchActionButton(onClick: () -> Unit) {
    val colors = contentFirstColors
    Box(
        modifier = Modifier
            .width(ZhihuSizes.HomeSearchButtonWidth)
            .height(ZhihuSizes.HomeSearchButtonHeight)
            .clip(ZhihuShapeTokens.Pill)
            .background(colors.accentFill)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(HOME_SEARCH_ACTION_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "搜索",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

/** 分类 Tab（官方 11.14，target-spec §2.2）：40dp 行、50dp 项、30×3dp 下划线，右侧频道编辑 + 听。 */
@Composable
private fun HomeCategoryTabRow(
    tabs: List<HomeCategoryTab>,
    selected: HomeCategoryTab,
    onSelect: (HomeCategoryTab) -> Unit,
    onEditChannels: () -> Unit,
) {
    val colors = contentFirstColors
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ZhihuSizes.HomeTabRowHeight)
            .background(colors.page),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .horizontalScroll(scrollState)
                    .padding(start = ZhihuSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    HomeCategoryTabItem(
                        tab = tab,
                        selected = tab == selected,
                        onClick = { onSelect(tab) },
                    )
                }
            }
            // 左右 10dp 渐变遮罩：横向滚动时 Tab 文字不会直接压在右侧固定图标上。
            if (scrollState.canScrollBackward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(TabScrollFadeWidth)
                        .background(Brush.horizontalGradient(listOf(colors.page, Color.Transparent))),
                )
            }
            if (scrollState.canScrollForward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(TabScrollFadeWidth)
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, colors.page))),
                )
            }
        }
        HomeTabIconButton(
            icon = Icons.Default.Menu,
            label = "频道编辑",
            tag = HOME_TAB_EDIT_TAG,
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = onEditChannels,
        )
        Spacer(Modifier.width(ZhihuSpacing.Small))
    }
}

@Composable
private fun HomeCategoryTabItem(
    tab: HomeCategoryTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(ZhihuSizes.HomeTabItemWidth)
            .fillMaxHeight()
            .clickable(role = Role.Tab, onClick = onClick)
            .testTag(homeCategoryTabTag(tab.label))
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = tab.label,
            style = ZhihuTypography.TabTitle.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 1.dp)
                    .size(
                        width = ZhihuSizes.HomeTabIndicatorWidth,
                        height = ZhihuSizes.HomeTabIndicatorHeight,
                    ).clip(TabIndicatorShape)
                    .background(MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}

/** Tab 行右侧固定图标（视觉 24dp，点击区补到 40dp）。 */
@Composable
private fun HomeTabIconButton(
    icon: ImageVector,
    label: String,
    tag: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(TabIconSlotSize)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(ZhihuSizes.Icon),
        )
    }
}

/** 频道编辑弹层：勾选/取消分类 Tab，至少保留一个可见 Tab（复用应用偏好存储）。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeChannelSheet(
    tabs: List<HomeCategoryTab>,
    hiddenTabKeys: Set<String>,
    onDismiss: () -> Unit,
    onToggle: (HomeCategoryTab, Boolean) -> Unit,
) {
    val colors = contentFirstColors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.page,
        modifier = Modifier.testTag(HOME_CHANNEL_SHEET_TAG),
    ) {
        Text(
            text = "频道设置",
            style = ZhihuTypography.TabTitle.copy(fontWeight = FontWeight.Bold),
            color = colors.primaryText,
            modifier = Modifier.padding(horizontal = ZhihuSizes.FeedCardHorizontalGutter),
        )
        Spacer(Modifier.height(ZhihuSpacing.Small))
        tabs.forEach { tab ->
            val visible = tab.name !in hiddenTabKeys
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable(role = Role.Switch) { onToggle(tab, !visible) }
                    .testTag(homeChannelTabTag(tab.label))
                    .padding(horizontal = ZhihuSizes.FeedCardHorizontalGutter),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tab.label,
                    style = ZhihuTypography.TabTitle,
                    color = colors.primaryText,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = visible,
                    onCheckedChange = { onToggle(tab, it) },
                    enabled = visible || tabs.count { it.name !in hiddenTabKeys } > 1,
                )
            }
        }
        Spacer(Modifier.height(ZhihuSpacing.ExtraLarge))
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
