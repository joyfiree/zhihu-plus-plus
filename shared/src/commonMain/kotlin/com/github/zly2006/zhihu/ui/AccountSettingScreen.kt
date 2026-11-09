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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.zly2006.zhihu.account.rememberZhihuAccountStore
import com.github.zly2006.zhihu.navigation.Account
import com.github.zly2006.zhihu.navigation.Collections
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.navigation.OnlineHistory
import com.github.zly2006.zhihu.navigation.Person
import com.github.zly2006.zhihu.navigation.WritePin
import com.github.zly2006.zhihu.navigation.requestLoginNavigation
import com.github.zly2006.zhihu.platform.rememberPlainTextClipboard
import com.github.zly2006.zhihu.platform.rememberSettingsStore
import com.github.zly2006.zhihu.platform.rememberSystemUrlOpener
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.reading.isReadingPlayerSupported
import com.github.zly2006.zhihu.theme.ZhihuTypography
import com.github.zly2006.zhihu.ui.components.ZhihuAlertDialog
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.designsystem.Motion
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSizes
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.ui.subscreens.SystemUpdateState
import com.github.zly2006.zhihu.ui.subscreens.rememberSystemUpdateChecker
import com.github.zly2006.zhihu.ui.subscreens.rememberSystemUpdateState
import com.github.zly2006.zhihu.util.Log
import com.github.zly2006.zhihu.util.formatCompactCount
import com.github.zly2006.zhihu.viewmodel.rememberPaginationEnvironment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.painterResource
import zhihu.shared.generated.resources.Res
import zhihu.shared.generated.resources.ic_license_24dp

const val ACCOUNT_SETTINGS_SCROLL_TAG = "accountSettings.scroll"
const val ACCOUNT_SETTINGS_LOGIN_ITEM_TAG = "accountSettings.loginItem"
const val ACCOUNT_SETTINGS_PROFILE_HEADER_TAG = "accountSettings.profileHeader"
const val ACCOUNT_SETTINGS_PROFILE_NAME_TAG = "accountSettings.profileName"
const val ACCOUNT_SETTINGS_SCAN_TAG = "accountSettings.scan"
const val ACCOUNT_SETTINGS_WRITE_PIN_TAG = "accountSettings.writePin"
const val ACCOUNT_SETTINGS_SHORTCUT_COLLECTIONS_TAG = "accountSettings.shortcutCollections"
const val ACCOUNT_SETTINGS_SHORTCUT_SUBSCRIPTIONS_TAG = "accountSettings.shortcutSubscriptions"
const val ACCOUNT_SETTINGS_SHORTCUT_NOTIFICATION_TAG = "accountSettings.shortcutNotification"
const val ACCOUNT_SETTINGS_SHORTCUT_HISTORY_TAG = "accountSettings.shortcutHistory"
const val ACCOUNT_SETTINGS_APPEARANCE_TAG = "accountSettings.appearance"
const val ACCOUNT_SETTINGS_READING_TAG = "accountSettings.reading"
const val ACCOUNT_SETTINGS_RECOMMEND_TAG = "accountSettings.recommend"
const val ACCOUNT_SETTINGS_SEARCH_TAG = "accountSettings.search"
const val ACCOUNT_SETTINGS_SYSTEM_TAG = "accountSettings.system"
const val ACCOUNT_SETTINGS_DEVELOPER_TAG = "accountSettings.developer"
const val ACCOUNT_SETTINGS_LICENSES_TAG = "accountSettings.licenses"
const val ACCOUNT_SETTINGS_IDENTITY_MANAGEMENT_TAG = "accountSettings.identityManagement"

private val heroAvatarSize = 64.dp
private val collapsedBarHeight = 48.dp
private val collapsedBarTrigger = 48.dp
private val shortcutCardHeight = 80.dp
private val settingsRowMinHeight = 56.dp
private val settingsRowIconSize = 28.dp

/**
 * 账号与设置入口页。
 *
 * 已登录时顶部是可折叠 Hero（头像、昵称、@ID、回答/文章/粉丝/关注统计、进主页 pill、扫码与更多菜单），Hero 滚出后浮出 48dp 小标题栏；
 * 下方依次是快捷入口卡、设置搜索、分组设置和页脚。未登录时 Hero 只保留登录入口，快捷入口卡与统计隐藏。
 * 开发者选项通过连续点击页脚版本号 5 次开启，退出登录走确认弹窗。
 *
 * 这个页面既作为底部栏 tab 展示，也被单独的路由/账号面板复用，所以 [innerPadding]、[onDismissRequest]、
 * [refreshAccountProfileOnEnter] 与 [isActive] 都必须保留，不能改成只在 tab 场景成立的默认值。
 */
@Composable
fun AccountSettingScreen(
    innerPadding: PaddingValues,
    unreadCount: Int = 0,
    showUnreadBadge: Boolean = true,
    onDismissRequest: () -> Unit = {},
    refreshAccountProfileOnEnter: Boolean = true,
    isActive: Boolean = true,
) {
    val navigator = LocalNavigator.current
    val accountStore = rememberZhihuAccountStore()
    val data by rememberAccountSettingsAccountState()
    val settings = rememberSettingsStore()
    val copyPlainText = rememberPlainTextClipboard()
    val openSystemUrl = rememberSystemUrlOpener()
    val userMessages = rememberUserMessageSink()
    val systemUpdateState = rememberSystemUpdateState()
    val checkForUpdate = rememberSystemUpdateChecker()
    val versionInfo = rememberAppVersionInfo()
    val readingPlayerSupported = isReadingPlayerSupported
    val launchQrScanner = rememberAccountQrScanAction()
    val requestLogin = ::requestLoginNavigation
    val scope = rememberCoroutineScope()

    var isDeveloper by remember { mutableStateOf(settings.getBoolean("developer", false)) }
    var clickTimes by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val pageTurnTarget = rememberPageTurnTarget(
        scrollState = scrollState,
        enabled = isActive && !showLogoutDialog,
    )
    LaunchedEffect(isDeveloper) {
        settings.putBoolean("developer", isDeveloper)
    }

    LaunchedEffect(data.login, refreshAccountProfileOnEnter) {
        if (refreshAccountProfileOnEnter && data.login) {
            try {
                accountStore.client.refreshAndSaveProfile()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AccountSettingScreen", "Failed to refresh account profile", e)
                userMessages.showShortMessage("获取用户信息失败")
            }
        }
    }

    // /api/v4/me 的 self 只保证资料字段；回答/文章/粉丝/关注计数从成员资料接口补齐（PeopleScreen 同源）。
    val statsEnvironment = rememberPaginationEnvironment(allowGuestAccess = false)
    var profileCounts by remember { mutableStateOf<AccountProfileCounts?>(null) }
    LaunchedEffect(data.login, data.urlToken, data.id, statsEnvironment) {
        if (!data.login) {
            profileCounts = null
            return@LaunchedEffect
        }
        val token = data.urlToken?.takeIf { it.isNotBlank() } ?: data.id
        if (token.isBlank()) return@LaunchedEffect
        try {
            val json = statsEnvironment.fetchJson(
                "https://www.zhihu.com/api/v4/members/$token",
                "answer_count,articles_count,follower_count,following_count",
            )
            profileCounts = json?.let { profile ->
                AccountProfileCounts(
                    answers = profile["answer_count"]?.jsonPrimitive?.intOrNull,
                    articles = profile["articles_count"]?.jsonPrimitive?.intOrNull,
                    followers = profile["follower_count"]?.jsonPrimitive?.intOrNull,
                    following = profile["following_count"]?.jsonPrimitive?.intOrNull,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("AccountSettingScreen", "Failed to load profile counts", e)
        }
    }

    val updateState by systemUpdateState.collectAsState()
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is SystemUpdateState.UpdateAvailable -> {
                val versionType = if (state.isNightly) "Nightly版本" else "正式版本"
                userMessages.showShortMessage("发现新$versionType ${state.version}")
            }
            is SystemUpdateState.Error -> userMessages.showLongMessage("检查更新失败: ${state.message}")
            else -> Unit
        }
    }
    val updateLabel = when (updateState) {
        SystemUpdateState.Checking -> "检查中…"
        SystemUpdateState.Latest -> "已是最新版本"
        is SystemUpdateState.UpdateAvailable -> "发现新版本"
        is SystemUpdateState.Error -> "检查更新失败"
        else -> "检查更新"
    }

    val topInset = innerPadding.calculateTopPadding()
    val collapseTriggerPx = with(LocalDensity.current) { collapsedBarTrigger.roundToPx() }
    val showCollapsedBar by remember(collapseTriggerPx) {
        derivedStateOf { scrollState.value > collapseTriggerPx }
    }

    val openProfile = {
        navigator.onNavigate(
            Person(id = data.id, urlToken = data.urlToken ?: "", name = data.username),
        )
    }
    val openProfileSection = { section: String ->
        navigator.onNavigate(
            Person(id = data.id, urlToken = data.urlToken ?: "", name = data.username, jumpTo = section),
        )
    }
    val openIdentityManagement = { navigator.onNavigate(Account.IdentityManagement) }
    val requestLogout = { showLogoutDialog = true }

    Box(
        modifier = Modifier.fillMaxSize().background(contentFirstColors.page),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag(ACCOUNT_SETTINGS_SCROLL_TAG)
                .pageTurnViewportWithGuide(pageTurnTarget)
                .verticalScroll(scrollState)
                .padding(bottom = innerPadding.calculateBottomPadding() + ZhihuSpacing.ExtraLarge),
        ) {
            AccountHero(
                data = data,
                counts = profileCounts,
                topInset = topInset,
                scan = launchQrScanner,
                onOpenProfile = openProfile,
                onOpenSection = openProfileSection,
                onLogin = requestLogin,
                onIdentityManagement = openIdentityManagement,
                onLogout = requestLogout,
            )

            if (data.login) {
                AccountShortcutCard(
                    onWritePin = { navigator.onNavigate(WritePin()) },
                    onCollections = { data.urlToken?.let { navigator.onNavigate(Collections(it)) } },
                    onSubscriptions = {
                        navigator.onNavigate(
                            Person(
                                id = data.id,
                                urlToken = data.urlToken ?: "",
                                name = data.username,
                                jumpTo = "关注订阅",
                            ),
                        )
                    },
                    onHistory = {
                        onDismissRequest()
                        navigator.onNavigateTopLevel(OnlineHistory)
                    },
                )
            }

            SettingsSearchPill(onClick = { navigator.onNavigate(Account.SettingsSearch) })

            SettingsSection(title = "内容与推荐") {
                SettingsRow(
                    title = "推荐系统与内容过滤",
                    subtitle = "推荐、智能过滤、关键词屏蔽等",
                    icon = { SettingsRowIcon(Icons.Default.FilterAlt) },
                    modifier = Modifier.testTag(ACCOUNT_SETTINGS_RECOMMEND_TAG),
                    onClick = { navigator.onNavigate(Account.RecommendSettings()) },
                )
            }

            SettingsSection(title = "外观与阅读") {
                SettingsRow(
                    title = "外观与阅读体验",
                    subtitle = "主题颜色、字体大小等",
                    icon = { SettingsRowIcon(Icons.Default.Palette) },
                    modifier = Modifier.testTag(ACCOUNT_SETTINGS_APPEARANCE_TAG),
                    onClick = { navigator.onNavigate(Account.AppearanceSettings()) },
                )
                if (readingPlayerSupported) {
                    SettingsDivider()
                    SettingsRow(
                        title = "朗读与播放",
                        subtitle = "朗读内容、播放队列与条目过渡",
                        icon = { SettingsRowIcon(Icons.AutoMirrored.Filled.VolumeUp) },
                        modifier = Modifier.testTag(ACCOUNT_SETTINGS_READING_TAG),
                        onClick = { navigator.onNavigate(Account.ReadingSettings) },
                    )
                }
            }

            if (data.login) {
                SettingsSection(title = "账号与安全") {
                    SettingsRow(
                        title = "身份管理",
                        subtitle = "创建马甲号或切换当前账号",
                        icon = { SettingsRowIcon(Icons.Default.SwitchAccount) },
                        modifier = Modifier.testTag(ACCOUNT_SETTINGS_IDENTITY_MANAGEMENT_TAG),
                        onClick = openIdentityManagement,
                    )
                }
            }

            SettingsSection(title = "系统") {
                SettingsRow(
                    title = "系统与更新",
                    subtitle = "GitHub、更新设置等",
                    icon = { SettingsRowIcon(Icons.Default.Settings) },
                    modifier = Modifier.testTag(ACCOUNT_SETTINGS_SYSTEM_TAG),
                    onClick = { navigator.onNavigate(Account.SystemAndUpdateSettings()) },
                )
                SettingsDivider()
                AnimatedVisibility(isDeveloper) {
                    Column {
                        SettingsRow(
                            title = "开发者选项",
                            icon = { SettingsRowIcon(Icons.Default.Code) },
                            modifier = Modifier.testTag(ACCOUNT_SETTINGS_DEVELOPER_TAG),
                            onClick = { navigator.onNavigate(Account.DeveloperSettings) },
                        )
                        SettingsDivider()
                    }
                }
                SettingsRow(
                    title = "开源许可",
                    subtitle = "查看第三方组件许可证",
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_license_24dp),
                            contentDescription = null,
                            modifier = Modifier.size(settingsRowIconSize),
                        )
                    },
                    modifier = Modifier.testTag(ACCOUNT_SETTINGS_LICENSES_TAG),
                    onClick = { navigator.onNavigate(Account.OpenSourceLicenses) },
                )
            }

            AccountFooter(
                versionInfo = versionInfo,
                updateLabel = updateLabel,
                onVersionClick = {
                    clickTimes++
                    if (clickTimes == 5) {
                        clickTimes = 0
                        isDeveloper = true
                        userMessages.showShortMessage("You are now a developer")
                    }
                },
                onVersionLongClick = {
                    copyPlainText("version", versionInfo)
                    userMessages.showShortMessage("已复制版本号")
                },
                onCheckUpdate = { scope.launch { checkForUpdate.check() } },
                onGithub = { openSystemUrl("https://github.com/zly2006/zhihu-plus-plus") },
                onAgpl = { openSystemUrl("https://github.com/zly2006/zhihu-plus-plus/blob/master/LICENSE") },
            )
        }

        AnimatedVisibility(
            visible = showCollapsedBar,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(Motion.fadeTween()),
            exit = fadeOut(Motion.fadeTween()),
        ) {
            AccountCollapsedBar(
                topInset = topInset,
                scan = launchQrScanner,
                onIdentityManagement = openIdentityManagement,
                onLogout = requestLogout,
            )
        }
    }

    if (showLogoutDialog) {
        ZhihuAlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = {
                Text(
                    if (accountStore.accounts.size > 1) {
                        "确定退出并移除当前登录账号吗？退出后会自动切换到另一个已保存账号。"
                    } else {
                        "确定要退出登录吗？"
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        accountStore.clear()
                        showLogoutDialog = false
                    },
                ) {
                    Text("退出")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun AccountHero(
    data: AccountSettingsAccountState,
    counts: AccountProfileCounts?,
    topInset: Dp,
    scan: (() -> Unit)?,
    onOpenProfile: () -> Unit,
    onOpenSection: (String) -> Unit,
    onLogin: () -> Unit,
    onIdentityManagement: () -> Unit,
    onLogout: () -> Unit,
) {
    val colors = contentFirstColors
    val gradient = Brush.verticalGradient(listOf(colors.functionalSurface, colors.page))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ZhihuSpacing.ScreenGutter)
                .padding(top = topInset + ZhihuSpacing.Medium, bottom = ZhihuSpacing.Medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .testTag(ACCOUNT_SETTINGS_PROFILE_HEADER_TAG)
                        .clickable(onClick = if (data.login) onOpenProfile else onLogin),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AccountAvatar(data)
                    Spacer(Modifier.width(ZhihuSpacing.Large))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (data.login) data.username else "未登录",
                            style = ZhihuTypography.ContentTitle,
                            color = colors.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag(ACCOUNT_SETTINGS_PROFILE_NAME_TAG),
                        )
                        val urlToken = data.urlToken
                        if (data.login && !urlToken.isNullOrBlank()) {
                            Text(
                                text = "@$urlToken",
                                style = ZhihuTypography.ContentSignature,
                                color = colors.tertiaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (data.login) {
                    Spacer(Modifier.width(ZhihuSpacing.Small))
                    AccountTopActions(
                        scan = scan,
                        scanTestTag = ACCOUNT_SETTINGS_SCAN_TAG,
                        onIdentityManagement = onIdentityManagement,
                        onLogout = onLogout,
                    )
                }
            }
            if (data.login) {
                Spacer(Modifier.height(ZhihuSpacing.Small))
                HeroStatRow(data = data, counts = counts, onOpenSection = onOpenSection)
            }
            Spacer(Modifier.height(ZhihuSpacing.Small))
            HeroPillButton(
                label = if (data.login) "查看主页" else "登录知乎",
                modifier = if (data.login) Modifier else Modifier.testTag(ACCOUNT_SETTINGS_LOGIN_ITEM_TAG),
                filled = !data.login,
                showChevron = data.login,
                onClick = if (data.login) onOpenProfile else onLogin,
            )
        }
    }
}

private data class AccountProfileCounts(
    val answers: Int? = null,
    val articles: Int? = null,
    val followers: Int? = null,
    val following: Int? = null,
)

@Composable
private fun HeroStatRow(
    data: AccountSettingsAccountState,
    counts: AccountProfileCounts?,
    onOpenSection: (String) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        HeroStat(formatCompactCount(counts?.answers ?: data.answerCount), "回答", Modifier.weight(1f)) { onOpenSection("回答") }
        HeroStat(formatCompactCount(counts?.articles ?: data.articleCount), "文章", Modifier.weight(1f)) { onOpenSection("文章") }
        HeroStat(formatCompactCount(counts?.followers ?: data.followerCount), "粉丝", Modifier.weight(1f)) { onOpenSection("粉丝") }
        HeroStat(formatCompactCount(counts?.following ?: data.followingCount), "关注", Modifier.weight(1f)) { onOpenSection("关注") }
    }
}

@Composable
private fun HeroStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = value,
            style = ZhihuTypography.ContentAuthor.copy(fontWeight = FontWeight.Bold),
            color = contentFirstColors.primaryText,
            maxLines = 1,
        )
        Text(
            text = label,
            style = ZhihuTypography.BottomNavLabel,
            color = contentFirstColors.tertiaryText,
        )
    }
}

@Composable
private fun AccountAvatar(data: AccountSettingsAccountState) {
    val shape = CircleShape
    val baseModifier = Modifier.size(heroAvatarSize).clip(shape)
    if (data.login) {
        AsyncImage(
            model = data.avatarUrl,
            contentDescription = null,
            modifier = baseModifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(2.dp, contentFirstColors.page, shape),
        )
    } else {
        Box(
            modifier = baseModifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = contentFirstColors.tertiaryText,
                modifier = Modifier.size(ZhihuSizes.AvatarMedium),
            )
        }
    }
}

@Composable
private fun HeroPillButton(
    label: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    showChevron: Boolean = false,
    onClick: () -> Unit,
) {
    val containerColor = if (filled) contentFirstColors.accentFill else contentFirstColors.accent.copy(alpha = 0.12f)
    Surface(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        shape = ZhihuShapeTokens.Pill,
        color = containerColor,
        contentColor = if (filled) MaterialTheme.colorScheme.onPrimary else contentFirstColors.accent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AccountTopActions(
    scan: (() -> Unit)?,
    scanTestTag: String? = null,
    onIdentityManagement: () -> Unit,
    onLogout: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(ZhihuSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        scan?.let { onScan ->
            AccountIconButton(
                icon = Icons.Default.QrCodeScanner,
                contentDescription = "扫码",
                modifier = if (scanTestTag != null) Modifier.testTag(scanTestTag) else Modifier,
                onClick = onScan,
            )
        }
        Box {
            AccountIconButton(
                icon = Icons.Default.MoreVert,
                contentDescription = "更多",
                onClick = { menuExpanded = true },
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("身份管理") },
                    leadingIcon = { Icon(Icons.Default.SwitchAccount, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onIdentityManagement()
                    },
                )
                DropdownMenuItem(
                    text = { Text("退出登录", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onLogout()
                    },
                )
            }
        }
    }
}

@Composable
private fun AccountIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentFirstColors.secondaryText,
            modifier = Modifier.size(ZhihuSizes.IconSmall),
        )
    }
}

@Composable
private fun AccountCollapsedBar(
    topInset: Dp,
    scan: (() -> Unit)?,
    onIdentityManagement: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(contentFirstColors.page),
    ) {
        Spacer(Modifier.height(topInset))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(collapsedBarHeight)
                .padding(start = ZhihuSpacing.ScreenGutter, end = ZhihuSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "我的",
                style = ZhihuTypography.ContentAuthor.copy(fontWeight = FontWeight.Medium),
                color = contentFirstColors.primaryText,
                modifier = Modifier.weight(1f),
            )
            AccountTopActions(
                scan = scan,
                onIdentityManagement = onIdentityManagement,
                onLogout = onLogout,
            )
        }
        HorizontalDivider(color = contentFirstColors.divider)
    }
}

@Composable
private fun AccountShortcutCard(
    onWritePin: () -> Unit,
    onCollections: () -> Unit,
    onSubscriptions: () -> Unit,
    onHistory: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ZhihuSpacing.ScreenGutter)
            .padding(top = ZhihuSpacing.Medium)
            .clip(MaterialTheme.shapes.large)
            .background(contentFirstColors.functionalSurface)
            .height(shortcutCardHeight),
    ) {
        AccountShortcut(Icons.Default.Edit, "发想法", Modifier.weight(1f).testTag(ACCOUNT_SETTINGS_WRITE_PIN_TAG), onWritePin)
        AccountShortcut(Icons.Default.Bookmark, "收藏夹", Modifier.weight(1f).testTag(ACCOUNT_SETTINGS_SHORTCUT_COLLECTIONS_TAG), onCollections)
        AccountShortcut(Icons.Default.Groups, "关注订阅", Modifier.weight(1f).testTag(ACCOUNT_SETTINGS_SHORTCUT_SUBSCRIPTIONS_TAG), onSubscriptions)
        AccountShortcut(Icons.Default.History, "浏览历史", Modifier.weight(1f).testTag(ACCOUNT_SETTINGS_SHORTCUT_HISTORY_TAG), onHistory)
    }
}

@Composable
private fun AccountShortcut(
    icon: ImageVector,
    label: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentFirstColors.accent,
            modifier = Modifier.size(ZhihuSizes.Icon),
        )
        Spacer(Modifier.height(ZhihuSpacing.ExtraSmall))
        Text(
            text = label,
            style = ZhihuTypography.BottomNavLabel,
            color = contentFirstColors.primaryText,
        )
    }
}

@Composable
private fun SettingsSearchPill(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ZhihuSpacing.ScreenGutter)
            .padding(top = ZhihuSpacing.Medium),
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag(ACCOUNT_SETTINGS_SEARCH_TAG),
            shape = ZhihuShapeTokens.Pill,
            color = contentFirstColors.functionalSurface,
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = ZhihuSpacing.Large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = contentFirstColors.tertiaryText,
                    modifier = Modifier.size(ZhihuSizes.IconSmall),
                )
                Spacer(Modifier.width(ZhihuSpacing.Small))
                Text(
                    text = "搜索设置项",
                    style = ZhihuTypography.FeedSummary,
                    color = contentFirstColors.tertiaryText,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ZhihuSpacing.ScreenGutter),
    ) {
        Text(
            text = title,
            style = ZhihuTypography.FeedMeta,
            color = contentFirstColors.tertiaryText,
            modifier = Modifier.padding(
                start = ZhihuSpacing.ExtraSmall,
                top = ZhihuSpacing.Medium,
                bottom = ZhihuSpacing.Small,
            ),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = contentFirstColors.functionalSurface,
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRowIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(settingsRowIconSize),
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = settingsRowMinHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = ZhihuSpacing.Large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(contentFirstColors.accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentFirstColors.accent) {
                icon()
            }
        }
        Spacer(Modifier.width(ZhihuSpacing.Medium))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = ZhihuTypography.FeedAuthorName,
                color = contentFirstColors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentFirstColors.tertiaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = contentFirstColors.tertiaryText,
            modifier = Modifier.size(ZhihuSizes.IconSmall),
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = ZhihuSpacing.Large),
        color = contentFirstColors.divider,
    )
}

@Composable
private fun AccountFooter(
    versionInfo: String,
    updateLabel: String,
    onVersionClick: () -> Unit,
    onVersionLongClick: () -> Unit,
    onCheckUpdate: () -> Unit,
    onGithub: () -> Unit,
    onAgpl: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = ZhihuSpacing.ExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "知乎++ 版本 $versionInfo",
            style = ZhihuTypography.FeedMeta,
            color = contentFirstColors.tertiaryText,
            modifier = Modifier
                .combinedClickable(onLongClick = onVersionLongClick, onClick = onVersionClick)
                .padding(horizontal = ZhihuSpacing.Small, vertical = ZhihuSpacing.ExtraSmall),
        )
        Row(
            modifier = Modifier.padding(top = ZhihuSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(ZhihuSpacing.ExtraLarge),
        ) {
            FooterLink(label = updateLabel, onClick = onCheckUpdate)
            FooterLink(label = "GitHub", onClick = onGithub)
            FooterLink(label = "AGPL-3.0", onClick = onAgpl)
        }
    }
}

@Composable
private fun FooterLink(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = ZhihuTypography.FeedMeta,
        color = contentFirstColors.accent,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = ZhihuSpacing.Small),
    )
}
