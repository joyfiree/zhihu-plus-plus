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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.github.zly2006.zhihu.platform.rememberExternalUrlOpener
import com.github.zly2006.zhihu.platform.rememberSettingsStore
import com.github.zly2006.zhihu.ui.components.ZhihuAlertDialog

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
    val settings = rememberSettingsStore()
    var noticeStep by rememberSaveable {
        mutableIntStateOf(0)
    }
    var selectedMethod by rememberSaveable(supportedLoginMethods) {
        mutableStateOf(supportedLoginMethods.first())
    }
    var loggedInUsername by remember { mutableStateOf<String?>(null) }
    val onLoginSuccess: (String) -> Unit = { username ->
        // 用户主动登录成功后恢复「未登录时自动提示登录」的默认行为。
        settings.putBoolean(SKIP_LOGIN_PROMPT_PREFERENCE_KEY, false)
        loggedInUsername = username
    }

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

            // 未登录也能浏览信息流和详情；跳过时记住选择，HomeScreen 读同一个偏好，
            // 下次启动不再把用户自动带到登录页。账号页仍保留登录入口。
            TextButton(
                onClick = {
                    settings.putBoolean(SKIP_LOGIN_PROMPT_PREFERENCE_KEY, true)
                    onLoginComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_skip"),
            ) {
                Text("跳过登录，先浏览")
            }
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
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(stepTag),
    ) {
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
            }
        }
    }
}

private data class LoginNotice(
    val message: String,
    val secondaryButtonText: String,
)

private const val LOGIN_NOTICE_COUNT = 3

/** 用户选择「跳过登录」后不再自动弹出登录页的偏好。 */
internal const val SKIP_LOGIN_PROMPT_PREFERENCE_KEY = "skipLoginPrompt"

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
