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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.theme.ZhihuTypography
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSizes
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.util.Log
import com.github.zly2006.zhihu.util.formatCompactCount
import com.github.zly2006.zhihu.viewmodel.rememberPaginationEnvironment
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

const val ACCOUNT_SETTINGS_SCROLL_TAG = "accountSettings.scroll"
const val ACCOUNT_SETTINGS_LOGIN_ITEM_TAG = "accountSettings.loginItem"
const val ACCOUNT_SETTINGS_PROFILE_HEADER_TAG = "accountSettings.profileHeader"
const val ACCOUNT_SETTINGS_PROFILE_NAME_TAG = "accountSettings.profileName"
const val ACCOUNT_SETTINGS_SCAN_TAG = "accountSettings.scan"
const val ACCOUNT_SETTINGS_OPEN_SETTINGS_TAG = "accountSettings.openSettings"
const val ACCOUNT_SETTINGS_WRITE_PIN_TAG = "accountSettings.writePin"
const val ACCOUNT_SETTINGS_SHORTCUT_COLLECTIONS_TAG = "accountSettings.shortcutCollections"
const val ACCOUNT_SETTINGS_SHORTCUT_SUBSCRIPTIONS_TAG = "accountSettings.shortcutSubscriptions"
const val ACCOUNT_SETTINGS_SHORTCUT_HISTORY_TAG = "accountSettings.shortcutHistory"

private val heroAvatarSize = 72.dp
private val shortcutCardHeight = 80.dp
private val contentRowMinHeight = 52.dp

/**
 * 「我的」页。
 *
 * 只承担个人相关内容：资料头部（头像 / 昵称 / 知乎号）、创作数据统计、创作入口和我自己内容的快捷入口。
 * 主题、阅读、推荐过滤、身份管理等所有配置项都收敛到 [SettingsScreen]，本页右上角只留一个齿轮入口，
 * 避免个人页被设置项淹成一张配置表。
 *
 * 这个页面既作为底部栏 tab 展示，也会被独立路由复用，[innerPadding]、[onDismissRequest]、
 * [refreshAccountProfileOnEnter] 与 [isActive] 必须保留。
 */
@Composable
fun AccountSettingScreen(
    innerPadding: PaddingValues,
    onDismissRequest: () -> Unit = {},
    refreshAccountProfileOnEnter: Boolean = true,
    isActive: Boolean = true,
) {
    val navigator = LocalNavigator.current
    val accountStore = rememberZhihuAccountStore()
    val data by rememberAccountSettingsAccountState()
    val userMessages = rememberUserMessageSink()
    val launchQrScanner = rememberAccountQrScanAction()
    val requestLogin = ::requestLoginNavigation

    val scrollState = rememberScrollState()
    val pageTurnTarget = rememberPageTurnTarget(
        scrollState = scrollState,
        enabled = isActive,
    )

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

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    fun openProfileSection(section: String? = null) {
        navigator.onNavigate(
            Person(
                id = data.id,
                urlToken = data.urlToken ?: "",
                name = data.username,
                jumpTo = section.orEmpty(),
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(contentFirstColors.page)
            .testTag(ACCOUNT_SETTINGS_SCROLL_TAG)
            .pageTurnViewportWithGuide(pageTurnTarget)
            .verticalScroll(scrollState)
            .padding(bottom = innerPadding.calculateBottomPadding() + ZhihuSpacing.ExtraLarge),
    ) {
        AccountHeader(
            data = data,
            counts = profileCounts,
            topInset = topInset,
            onOpenProfile = { openProfileSection() },
            onOpenSection = { openProfileSection(it) },
            onLogin = requestLogin,
            onScan = launchQrScanner,
            onOpenSettings = {
                onDismissRequest()
                navigator.onNavigate(Account.Settings)
            },
        )

        if (data.login) {
            AccountShortcutCard(
                onWritePin = { navigator.onNavigate(WritePin()) },
                onCollections = { data.urlToken?.let { navigator.onNavigate(Collections(it)) } },
                onSubscriptions = { openProfileSection("关注订阅") },
                onHistory = {
                    onDismissRequest()
                    navigator.onNavigateTopLevel(OnlineHistory)
                },
            )
            AccountContentSection(onOpenProfileSection = { openProfileSection(it) })
        } else {
            AccountGuestHint(
                onLogin = requestLogin,
                onOpenHistory = {
                    onDismissRequest()
                    navigator.onNavigateTopLevel(OnlineHistory)
                },
                onOpenSettings = {
                    onDismissRequest()
                    navigator.onNavigate(Account.Settings)
                },
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

/**
 * 资料头部：头像、昵称、知乎号同排对齐（官方同款），右上角只有扫码与设置入口。
 *
 * 昵称与知乎号和头像垂直居中对齐，避免旧版「头像靠左、昵称靠上、@ID 再占一行」的错位感；
 * 统计数字紧跟在资料行下面，点击直接进自己主页的对应 tab。
 */
@Composable
private fun AccountHeader(
    data: AccountSettingsAccountState,
    counts: AccountProfileCounts?,
    topInset: Dp,
    onOpenProfile: () -> Unit,
    onOpenSection: (String) -> Unit,
    onLogin: () -> Unit,
    onScan: (() -> Unit)?,
    onOpenSettings: () -> Unit,
) {
    val colors = contentFirstColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ZhihuSpacing.ScreenGutter)
            .padding(top = topInset + ZhihuSpacing.Medium),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    val subtitle = if (data.login) {
                        urlToken?.takeIf { it.isNotBlank() }?.let { "知乎号 @$it" } ?: "查看并编辑个人主页"
                    } else {
                        "登录后同步你的创作与收藏"
                    }
                    Text(
                        text = subtitle,
                        style = ZhihuTypography.ContentSignature,
                        color = colors.tertiaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            if (data.login && onScan != null) {
                Spacer(Modifier.width(ZhihuSpacing.Small))
                AccountHeaderAction(
                    icon = Icons.Default.QrCodeScanner,
                    contentDescription = "扫码",
                    modifier = Modifier.testTag(ACCOUNT_SETTINGS_SCAN_TAG),
                    onClick = onScan,
                )
            }
            Spacer(Modifier.width(ZhihuSpacing.Small))
            AccountHeaderAction(
                icon = Icons.Default.Settings,
                contentDescription = "设置",
                modifier = Modifier.testTag(ACCOUNT_SETTINGS_OPEN_SETTINGS_TAG),
                onClick = onOpenSettings,
            )
        }

        if (data.login) {
            Spacer(Modifier.height(ZhihuSpacing.Large))
            Row(modifier = Modifier.fillMaxWidth()) {
                HeroStat(formatCompactCount(counts?.answers ?: data.answerCount), "回答", Modifier.weight(1f)) {
                    onOpenSection("回答")
                }
                HeroStat(formatCompactCount(counts?.articles ?: data.articleCount), "文章", Modifier.weight(1f)) {
                    onOpenSection("文章")
                }
                HeroStat(formatCompactCount(counts?.followers ?: data.followerCount), "粉丝", Modifier.weight(1f)) {
                    onOpenSection("粉丝")
                }
                HeroStat(formatCompactCount(counts?.following ?: data.followingCount), "关注", Modifier.weight(1f)) {
                    onOpenSection("关注")
                }
            }
        }
    }
}

@Composable
private fun AccountHeaderAction(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(contentFirstColors.functionalSurface)
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
            modifier = baseModifier.background(MaterialTheme.colorScheme.primaryContainer),
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
                modifier = Modifier.size(ZhihuSizes.AvatarLarge),
            )
        }
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
            .padding(top = ZhihuSpacing.Large)
            .clip(ZhihuShapeTokens.Card)
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

/** 我的内容：进入自己主页的各个 tab，避免「我的」页除了设置入口之外没有可看的东西。 */
@Composable
private fun AccountContentSection(onOpenProfileSection: (String) -> Unit) {
    val colors = contentFirstColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ZhihuSpacing.ScreenGutter)
            .padding(top = ZhihuSpacing.Large),
    ) {
        Text(
            text = "我的内容",
            style = ZhihuTypography.FeedMeta,
            color = colors.tertiaryText,
            modifier = Modifier.padding(start = ZhihuSpacing.ExtraSmall, bottom = ZhihuSpacing.Small),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ZhihuShapeTokens.Card,
            color = colors.functionalSurface,
        ) {
            Column {
                AccountContentRow("我的回答", "回答过的问题都收在这里") { onOpenProfileSection("回答") }
                AccountContentDivider()
                AccountContentRow("我的文章", "专栏与长文创作") { onOpenProfileSection("文章") }
                AccountContentDivider()
                AccountContentRow("我的想法", "随手记录的短内容") { onOpenProfileSection("动态") }
                AccountContentDivider()
                AccountContentRow("我的收藏", "收藏夹里的内容") { onOpenProfileSection("收藏") }
                AccountContentDivider()
                AccountContentRow("我的提问", "提过的问题") { onOpenProfileSection("提问") }
            }
        }
    }
}

@Composable
private fun AccountContentDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = ZhihuSpacing.Large),
        color = contentFirstColors.divider,
    )
}

@Composable
private fun AccountContentRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = contentRowMinHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = ZhihuSpacing.Large, vertical = ZhihuSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ZhihuTypography.FeedAuthorName,
                color = contentFirstColors.primaryText,
            )
            Text(
                text = subtitle,
                style = ZhihuTypography.BottomNavLabel,
                color = contentFirstColors.tertiaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = contentFirstColors.tertiaryText,
            modifier = Modifier.size(ZhihuSizes.IconSmall),
        )
    }
}

/**
 * 未登录：登录引导 + 游客也能用的本地入口，不伪造任何个人数据。
 *
 * 游客态数据（粉丝/关注/作品）本来就该是空的，所以这里不再堆「暂无内容」占位，
 * 而是把浏览历史这类本地能力和设置入口摆出来，避免整页只有一张登录卡。
 */
@Composable
private fun AccountGuestHint(
    onLogin: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = contentFirstColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ZhihuSpacing.ScreenGutter)
            .padding(top = ZhihuSpacing.Large),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ZhihuShapeTokens.Card,
            color = colors.functionalSurface,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(ZhihuSpacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "登录后可以发布想法、管理收藏，并同步阅读记录",
                    style = ZhihuTypography.FeedSummary,
                    color = colors.secondaryText,
                )
                Spacer(Modifier.height(ZhihuSpacing.Medium))
                Text(
                    text = "登录知乎",
                    style = ZhihuTypography.FeedAuthorName.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .testTag(ACCOUNT_SETTINGS_LOGIN_ITEM_TAG)
                        .clip(ZhihuShapeTokens.Pill)
                        .background(colors.accentFill)
                        .clickable(onClick = onLogin)
                        .padding(horizontal = ZhihuSpacing.ExtraLarge, vertical = ZhihuSpacing.Small),
                )
            }
        }

        Spacer(Modifier.height(ZhihuSpacing.Large))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ZhihuShapeTokens.Card,
            color = colors.functionalSurface,
        ) {
            Column {
                AccountContentRow("浏览历史", "本地阅读记录，未登录也能看", onOpenHistory)
                AccountContentDivider()
                AccountContentRow("设置", "主题、阅读、推荐与系统", onOpenSettings)
            }
        }
    }
}
