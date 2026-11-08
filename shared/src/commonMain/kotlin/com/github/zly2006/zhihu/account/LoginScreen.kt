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

package com.github.zly2006.zhihu.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.platform.PlatformBackHandler
import com.github.zly2006.zhihu.platform.rememberExternalUrlOpener
import com.github.zly2006.zhihu.platform.rememberSettingsStore
import com.github.zly2006.zhihu.ui.components.ZhihuAlertDialog
import com.github.zly2006.zhihu.ui.rememberAppVersionInfo

/** 「跳过登录」偏好：为 true 时首页不再强制拉起登录页。 */
const val SKIP_LOGIN_PROMPT_PREFERENCE_KEY = "skipLoginPrompt"

enum class LoginMethod(
    val label: String,
    val tag: String,
) {
    Phone("手机号登录", "login_mode_phone"),
    Qr("扫码登录", "login_mode_qr"),
    Web("备用网页登录", "login_mode_web"),
}

expect val supportedLoginMethods: List<LoginMethod>

@Composable
expect fun QrLoginPane(onLoginSuccess: (String) -> Unit)

@Composable
expect fun WebLoginPane(onLoginSuccess: (String) -> Unit)

@Composable
fun LoginScreen(
    onLoginComplete: () -> Unit,
    onOpenTelemetrySettings: () -> Unit,
) {
    val openExternalUrl = rememberExternalUrlOpener()
    var noticeStep by rememberSaveable {
        mutableIntStateOf(0)
    }
    var selectedMethod by rememberSaveable(supportedLoginMethods) {
        mutableStateOf(supportedLoginMethods.first())
    }
    var loggedInUsername by remember { mutableStateOf<String?>(null) }
    val onLoginSuccess: (String) -> Unit = { username -> loggedInUsername = username }
    val settings = rememberSettingsStore()
    // 「跳过登录」只关掉「强制拉起登录页」这一件事：skipLoginPrompt=true 后首页直接以游客身份浏览。
    // 不动 loginForRecommendation，是为了让用户以后真的登录时，推荐请求仍然携带登录凭证。
    val skipLogin: () -> Unit = {
        settings.putBoolean(SKIP_LOGIN_PROMPT_PREFERENCE_KEY, true)
        onLoginComplete()
    }
    // 返回键等同「跳过登录」：否则从登录页按返回会先回到首页，首页又立刻把登录页拉起来，
    // 形成「返回 → 首页 → 又弹出登录」的死循环。
    PlatformBackHandler(enabled = true, onBack = skipLogin)

    if (noticeStep < LOGIN_NOTICE_COUNT) {
        val notice = loginNotices[noticeStep]
        LoginNoticeScreen(
            stepTag = "login_notice_step_${noticeStep + 1}",
            message = notice.message,
            secondaryButtonText = notice.secondaryButtonText,
            onSecondaryAction = {
                when (noticeStep) {
                    0 -> openExternalUrl("https://www.zhihu.com/app/")
                    1 -> openExternalUrl("https://www.zhihu.com/term/zhihu-terms")
                    else -> onOpenTelemetrySettings()
                }
            },
            onConfirm = { noticeStep++ },
            onSkipLogin = skipLogin,
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                supportedLoginMethods.forEach { method ->
                    LoginMethodButton(
                        method = method,
                        selected = method == selectedMethod,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMethod = method },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when (selectedMethod) {
                    LoginMethod.Phone -> PhoneLoginPane(onLoginSuccess)
                    LoginMethod.Qr -> QrLoginPane(onLoginSuccess)
                    LoginMethod.Web -> WebLoginPane(onLoginSuccess)
                }
            }

            TextButton(
                onClick = skipLogin,
                modifier = Modifier.fillMaxWidth().testTag("login_skip"),
            ) {
                Text("跳过登录，先随便逛逛")
            }
            LoginBuildStamp()
        }
    }

    loggedInUsername?.let { username ->
        ZhihuAlertDialog(
            onDismissRequest = onLoginComplete,
            title = { Text("登录成功") },
            text = { Text("欢迎回来，$username") },
            confirmButton = {
                TextButton(onClick = onLoginComplete) {
                    Text("确定")
                }
            },
        )
    }
}

@Composable
private fun LoginMethodButton(
    method: LoginMethod,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    // 三档登录方式等宽平分一行，默认 24dp 内容边距会让 5~6 个汉字折行，这里压缩到 12dp。
    val contentPadding = PaddingValues(horizontal = 12.dp)
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.testTag(method.tag),
            contentPadding = contentPadding,
        ) {
            Text(method.label, maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.testTag(method.tag),
            contentPadding = contentPadding,
        ) {
            Text(method.label, maxLines = 1)
        }
    }
}

@Composable
private fun LoginNoticeScreen(
    stepTag: String,
    message: String,
    secondaryButtonText: String,
    onConfirm: () -> Unit,
    onSecondaryAction: () -> Unit,
    onSkipLogin: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(stepTag),
    ) {
        // 提示页是登录流程的第一步，跳过入口必须在这里就能点到，不能逼用户先点三次「确认」。
        // 登录页本身不套 Scaffold，顶栏内边距要自己让开状态栏，否则按钮会压在时间/信号图标上。
        TextButton(
            onClick = onSkipLogin,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 4.dp,
                    end = 8.dp,
                ).testTag("login_skip"),
        ) {
            Text("跳过登录")
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onSecondaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_notice_secondary_action"),
                ) {
                    Text(secondaryButtonText)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_notice_confirm"),
                ) {
                    Text("确认")
                }
                LoginBuildStamp()
            }
        }
    }
}

/**
 * 登录页底部的构建标识。
 *
 * 用来分辨「现在跑的到底是哪个包」——同一台设备上可能同时存在 full 与 lite 两个应用，
 * 或者安装被系统拒绝后仍在跑旧版本。字号压到最小、只出现在登录页，不影响正常阅读。
 */
@Composable
private fun LoginBuildStamp() {
    Text(
        text = rememberAppVersionInfo(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("login_build_stamp"),
    )
}

private data class LoginNotice(
    val message: String,
    val secondaryButtonText: String,
)

private const val LOGIN_NOTICE_COUNT = 3

private val loginNotices = listOf(
    LoginNotice(
        message = "我清楚，本应用由开源社区开发和维护，不由知乎官方开发并运营，也不受到知乎官方的承认或支持，使用本应用的一切后果由我本人承担。我可以在 https://www.zhihu.com/app/ 下载官方应用。",
        secondaryButtonText = "下载官方 App",
    ),
    LoginNotice(
        message = "在使用本应用的过程中，我承诺遵守知乎使用协议 https://www.zhihu.com/term/zhihu-terms 。我保证在使用过程中不侵犯知乎及其他作者的著作权，使用本应用产生的一切输出仅用于个人浏览和备份，不会进行传播等其他影响作者著作权的行为。",
        secondaryButtonText = "查看协议",
    ),
    LoginNotice(
        message = "我知晓，本应用可能会收集部分匿名化的使用信息来确定使用人数，我可以在设置中随时关闭此项遥测。",
        secondaryButtonText = "查看设置",
    ),
)
