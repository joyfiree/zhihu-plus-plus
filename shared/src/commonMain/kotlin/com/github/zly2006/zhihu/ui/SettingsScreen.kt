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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwitchAccount
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.account.rememberZhihuAccountStore
import com.github.zly2006.zhihu.navigation.Account
import com.github.zly2006.zhihu.navigation.LocalNavigator
import com.github.zly2006.zhihu.platform.rememberPlainTextClipboard
import com.github.zly2006.zhihu.platform.rememberSettingsStore
import com.github.zly2006.zhihu.platform.rememberSystemUrlOpener
import com.github.zly2006.zhihu.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.reading.isReadingPlayerSupported
import com.github.zly2006.zhihu.theme.ZhihuTypography
import com.github.zly2006.zhihu.ui.components.ZhihuAlertDialog
import com.github.zly2006.zhihu.ui.components.pageTurnViewportWithGuide
import com.github.zly2006.zhihu.ui.components.rememberPageTurnTarget
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSizes
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.contentFirstColors
import com.github.zly2006.zhihu.ui.subscreens.SystemUpdateState
import com.github.zly2006.zhihu.ui.subscreens.rememberSystemUpdateChecker
import com.github.zly2006.zhihu.ui.subscreens.rememberSystemUpdateState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import zhihu.shared.generated.resources.Res
import zhihu.shared.generated.resources.ic_license_24dp

const val SETTINGS_SCREEN_SCROLL_TAG = "settingsScreen.scroll"
const val SETTINGS_SCREEN_BACK_TAG = "settingsScreen.back"
const val SETTINGS_SCREEN_SEARCH_TAG = "settingsScreen.search"
const val SETTINGS_SCREEN_APPEARANCE_TAG = "settingsScreen.appearance"
const val SETTINGS_SCREEN_READING_TAG = "settingsScreen.reading"
const val SETTINGS_SCREEN_RECOMMEND_TAG = "settingsScreen.recommend"
const val SETTINGS_SCREEN_SYSTEM_TAG = "settingsScreen.system"
const val SETTINGS_SCREEN_DEVELOPER_TAG = "settingsScreen.developer"
const val SETTINGS_SCREEN_LICENSES_TAG = "settingsScreen.licenses"
const val SETTINGS_SCREEN_IDENTITY_MANAGEMENT_TAG = "settingsScreen.identityManagement"
const val SETTINGS_SCREEN_VERSION_TAG = "settingsScreen.version"
const val SETTINGS_SCREEN_LOGOUT_TAG = "settingsScreen.logout"

private val settingsRowMinHeight = 56.dp
private val settingsRowIconSize = 28.dp

/**
 * 设置页：应用所有设置项的唯一入口。
 *
 * 「我的」页只负责个人资料与创作内容，主题/阅读/推荐/系统等配置全部收敛到这里，避免个人页被设置项淹成一张配置表。
 * 页面既可被「我的」页的齿轮按钮 push 进来，也可由设置搜索、分享弹窗等深链直接打开指定设置项。
 * 开发者选项通过连续点击版本号 5 次开启。
 */
@Composable
fun SettingsScreen() {
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
    val scope = rememberCoroutineScope()

    var isDeveloper by remember { mutableStateOf(settings.getBoolean("developer", false)) }
    var clickTimes by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val pageTurnTarget = rememberPageTurnTarget(
        scrollState = scrollState,
        enabled = !showLogoutDialog,
    )
    LaunchedEffect(isDeveloper) {
        settings.putBoolean("developer", isDeveloper)
    }

    val updateState by systemUpdateState.collectAsState()
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is SystemUpdateState.UpdateAvailable -> {
                val versionType = if (state.isNightly) "Nightly版本" else "正式版本"
                userMessages.showShortMessage("发现新$versionType ${state.version}")
            }
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

    Column(modifier = Modifier.fillMaxSize().background(contentFirstColors.page)) {
        SettingsTopBar(
            statusBarsPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            onNavigateBack = { navigator.onNavigateBack() },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag(SETTINGS_SCREEN_SCROLL_TAG)
                .pageTurnViewportWithGuide(pageTurnTarget)
                .verticalScroll(scrollState)
                .padding(bottom = ZhihuSpacing.ExtraLarge),
        ) {
            SettingsSearchPill(onClick = { navigator.onNavigate(Account.SettingsSearch) })

            SettingsSection(title = "内容与推荐") {
                SettingsRow(
                    title = "推荐系统与内容过滤",
                    subtitle = "推荐、智能过滤、关键词屏蔽等",
                    icon = { SettingsRowIcon(Icons.Default.FilterAlt) },
                    modifier = Modifier.testTag(SETTINGS_SCREEN_RECOMMEND_TAG),
                    onClick = { navigator.onNavigate(Account.RecommendSettings()) },
                )
            }

            SettingsSection(title = "外观与阅读") {
                SettingsRow(
                    title = "外观与阅读体验",
                    subtitle = "主题颜色、字体大小等",
                    icon = { SettingsRowIcon(Icons.Default.Palette) },
                    modifier = Modifier.testTag(SETTINGS_SCREEN_APPEARANCE_TAG),
                    onClick = { navigator.onNavigate(Account.AppearanceSettings()) },
                )
                if (readingPlayerSupported) {
                    SettingsDivider()
                    SettingsRow(
                        title = "朗读与播放",
                        subtitle = "朗读内容、播放队列与条目过渡",
                        icon = { SettingsRowIcon(Icons.AutoMirrored.Filled.VolumeUp) },
                        modifier = Modifier.testTag(SETTINGS_SCREEN_READING_TAG),
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
                        modifier = Modifier.testTag(SETTINGS_SCREEN_IDENTITY_MANAGEMENT_TAG),
                        onClick = { navigator.onNavigate(Account.IdentityManagement) },
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "退出登录",
                        subtitle = "退出后用另一个账号登录",
                        icon = {
                            SettingsRowIcon(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        modifier = Modifier.testTag(SETTINGS_SCREEN_LOGOUT_TAG),
                        onClick = { showLogoutDialog = true },
                    )
                }
            }

            SettingsSection(title = "系统") {
                SettingsRow(
                    title = "系统与更新",
                    subtitle = "GitHub、更新设置等",
                    icon = { SettingsRowIcon(Icons.Default.Settings) },
                    modifier = Modifier.testTag(SETTINGS_SCREEN_SYSTEM_TAG),
                    onClick = { navigator.onNavigate(Account.SystemAndUpdateSettings()) },
                )
                SettingsDivider()
                AnimatedVisibility(isDeveloper) {
                    Column {
                        SettingsRow(
                            title = "开发者选项",
                            icon = { SettingsRowIcon(Icons.Default.Code) },
                            modifier = Modifier.testTag(SETTINGS_SCREEN_DEVELOPER_TAG),
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
                    modifier = Modifier.testTag(SETTINGS_SCREEN_LICENSES_TAG),
                    onClick = { navigator.onNavigate(Account.OpenSourceLicenses) },
                )
            }

            SettingsFooter(
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
private fun SettingsTopBar(
    statusBarsPadding: Dp,
    onNavigateBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(contentFirstColors.page)) {
        Spacer(Modifier.height(statusBarsPadding))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(start = ZhihuSpacing.Small, end = ZhihuSpacing.ScreenGutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onNavigateBack)
                    .testTag(SETTINGS_SCREEN_BACK_TAG),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = contentFirstColors.primaryText,
                    modifier = Modifier.size(ZhihuSizes.Icon),
                )
            }
            Spacer(Modifier.width(ZhihuSpacing.ExtraSmall))
            Text(
                text = "设置",
                style = ZhihuTypography.TabTitle,
                color = contentFirstColors.primaryText,
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = contentFirstColors.divider)
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
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag(SETTINGS_SCREEN_SEARCH_TAG),
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
private fun SettingsRowIcon(
    icon: ImageVector,
    tint: Color? = null,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(settingsRowIconSize),
        tint = tint ?: Color.Unspecified,
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
private fun SettingsFooter(
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
                .testTag(SETTINGS_SCREEN_VERSION_TAG)
                .clickable(onClick = onVersionClick)
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
        Text(
            text = "长按版本号复制",
            style = ZhihuTypography.BottomNavLabel,
            color = contentFirstColors.tertiaryText,
            modifier = Modifier
                .padding(top = ZhihuSpacing.Small)
                .clickable(onClick = onVersionLongClick),
        )
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
