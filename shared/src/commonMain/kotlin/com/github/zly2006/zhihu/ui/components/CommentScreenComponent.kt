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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.data.DataHolder
import com.github.zly2006.zhihu.data.ZhihuJson
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.navigation.CommentHolder
import com.github.zly2006.zhihu.navigation.NavDestination
import com.github.zly2006.zhihu.navigation.Pin
import com.github.zly2006.zhihu.platform.rememberSettingsStore
import com.github.zly2006.zhihu.ui.CommentScreen
import com.github.zly2006.zhihu.ui.commentThreadKey
import com.github.zly2006.zhihu.ui.consumePendingCommentId
import com.github.zly2006.zhihu.viewmodel.CommentItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * 最好不要在 if 或者其他条件语句中使用，这会导致本组件内部状态丢失。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreenComponent(
    showComments: Boolean,
    onDismiss: () -> Unit,
    content: NavDestination,
    isZhPlusAuthorContent: Boolean = false,
) {
    val settings = rememberSettingsStore()
    val initialPendingCommentId = consumePendingCommentId(content)
    var pendingCommentId by remember(content) {
        mutableStateOf(initialPendingCommentId)
    }
    val commentsVisible = showComments || pendingCommentId != null
    var authorCommentPolicyAcknowledged by remember {
        mutableStateOf(settings.getBoolean(ZH_PLUS_AUTHOR_COMMENT_POLICY_ACKNOWLEDGED_KEY, false))
    }
    val contentStateKey = content.commentThreadKey()
    var activeChildComment by rememberSaveable(contentStateKey, saver = activeChildCommentSaver) {
        mutableStateOf<CommentItem?>(null)
    }
    var pendingChildComment by remember(contentStateKey) {
        mutableStateOf<DataHolder.Comment?>(null)
    }
    var commentDrafts by rememberSaveable(contentStateKey) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    var rootListResetToken by rememberSaveable(contentStateKey) { mutableIntStateOf(0) }
    val rootListState = rememberSaveable(
        contentStateKey,
        rootListResetToken,
        saver = LazyListState.Saver,
    ) {
        LazyListState()
    }
    val rootSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val childSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val childTarget = activeChildComment?.clickTarget
    val childDraftKey = childTarget?.commentThreadKey()
    var childListResetToken by rememberSaveable(contentStateKey) { mutableIntStateOf(0) }
    val childListState = rememberSaveable(
        contentStateKey,
        childDraftKey,
        childListResetToken,
        saver = LazyListState.Saver,
    ) {
        LazyListState()
    }

    @Composable
    fun DragHandleTitle(text: String) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 明确的拖拽指示条：全屏高度的弹层也能一眼看出可以下滑关闭。
            BottomSheetDefaults.DragHandle()
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
        }
    }

    fun dismissRootComments() {
        pendingCommentId = null
        pendingChildComment = null
        activeChildComment = null
        rootListResetToken += 1
        childListResetToken += 1
        onDismiss()
    }

    fun updateCommentDraft(key: String, value: String) {
        commentDrafts = if (value.isEmpty()) {
            commentDrafts - key
        } else {
            commentDrafts + (key to value)
        }
    }

    if (commentsVisible) {
        MyModalBottomSheet(
            onDismissRequest = { dismissRootComments() },
            sheetState = rootSheetState,
            // 官方 target-spec §3：评论卡用 #212429（深色）/ #F8F8FA（浅色），对应 surfaceContainerLow。
            // 遮罩显式指定 60%：M3 默认 ScrimTokens.ContainerOpacity 只有 0.32，
            // 深色下几乎看不出层次，就是「蒙版没了」的观感来源。
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = true,
                shouldDismissOnClickOutside = true,
            ),
            dragHandle = { DragHandleTitle("评论") },
            usePlatformWindow = content !is Article,
        ) {
            // 「回复」子层打开时，根层的评论列表仍在原地合成；不清掉语义会让无障碍树和 UI 自动化
            // 同时看到两份 COMMENT_SCREEN_LIST_TAG 列表（子层那份才是用户此刻真正能交互到的）。
            Box(
                modifier = if (activeChildComment != null) {
                    Modifier.clearAndSetSemantics {}
                } else {
                    Modifier
                },
            ) {
                CommentScreen(
                    content = { content },
                    initialCommentId = pendingCommentId,
                    onChildCommentClick = { commentItem ->
                        if (commentItem.clickTarget != null) {
                            pendingChildComment = null
                            activeChildComment = commentItem
                        }
                    },
                    commentInput = commentDrafts[contentStateKey].orEmpty(),
                    onCommentInputChange = { updateCommentDraft(contentStateKey, it) },
                    listState = rootListState,
                    pageTurnEnabled =
                        (content is Article || content is Pin) &&
                            activeChildComment == null &&
                            (authorCommentPolicyAcknowledged || !isZhPlusAuthorContent),
                    showPageTurnFab = content is Pin,
                    onInitialChildCommentResolved = { rootComment, childComment ->
                        pendingChildComment = childComment
                        activeChildComment = rootComment
                    },
                )
            }
        }
    }

    if (commentsVisible && activeChildComment != null && childTarget != null && childDraftKey != null) {
        MyModalBottomSheet(
            onDismissRequest = {
                pendingChildComment = null
                activeChildComment = null
                childListResetToken += 1
            },
            sheetState = childSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            // 回复子层也要有遮罩：否则弹出时下面的评论列表与页面一起透出来，看着像一块没浮起来的内容。
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = true,
                shouldDismissOnClickOutside = true,
            ),
            dragHandle = { DragHandleTitle("回复") },
            usePlatformWindow = childTarget.article !is Article,
        ) {
            CommentScreen(
                content = { childTarget },
                activeCommentItem = activeChildComment,
                onChildCommentClick = { },
                commentInput = commentDrafts[childDraftKey].orEmpty(),
                onCommentInputChange = { updateCommentDraft(childDraftKey, it) },
                listState = childListState,
                initialComment = pendingChildComment,
                pageTurnEnabled = childTarget.article is Article || childTarget.article is Pin,
                showPageTurnFab = childTarget.article is Pin,
            )
        }
    }

    if (commentsVisible && isZhPlusAuthorContent && !authorCommentPolicyAcknowledged) {
        ZhihuAlertDialog(
            modifier = Modifier.testTag(ZH_PLUS_AUTHOR_COMMENT_POLICY_DIALOG_TAG),
            onDismissRequest = {},
            title = { Text("评论区使用须知") },
            text = {
                Column {
                    Text(
                        "请勿通过知乎提交任何 Bug 反馈或功能建议。所有反馈仅在 GitHub Issues 处理；" +
                            "不要发送到任何交流群，也不要发送到知乎++作者的回答或想法评论区。",
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "评论区只可发布与当前回答或想法相关的内容。无关内容将被无视，严重时会被直接拉黑。",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag(ZH_PLUS_AUTHOR_COMMENT_POLICY_CONFIRM_TAG),
                    onClick = {
                        settings.putBoolean(ZH_PLUS_AUTHOR_COMMENT_POLICY_ACKNOWLEDGED_KEY, true)
                        authorCommentPolicyAcknowledged = true
                    },
                ) {
                    Text("我已知晓并确认")
                }
            },
        )
    }
}

const val ZH_PLUS_AUTHOR_COMMENT_POLICY_ACKNOWLEDGED_KEY = "zhPlusAuthorCommentPolicyAcknowledged"
const val ZH_PLUS_AUTHOR_COMMENT_POLICY_DIALOG_TAG = "zh_plus_author_comment_policy_dialog"
const val ZH_PLUS_AUTHOR_COMMENT_POLICY_CONFIRM_TAG = "zh_plus_author_comment_policy_confirm"

private val activeChildCommentSaver = Saver<MutableState<CommentItem?>, List<String>>(
    save = { state ->
        val commentItem = state.value
        val target = commentItem?.clickTarget
        if (commentItem == null || target == null) {
            emptyList()
        } else {
            listOf(
                ZhihuJson.json.encodeToString(commentItem.item),
                ZhihuJson.json.encodeToString(target),
            )
        }
    },
    restore = { saved ->
        mutableStateOf(
            if (saved.size != 2) {
                null
            } else {
                runCatching {
                    CommentItem(
                        item = ZhihuJson.json.decodeFromString<DataHolder.Comment>(saved[0]),
                        clickTarget = ZhihuJson.json.decodeFromString<CommentHolder>(saved[1]),
                    )
                }.getOrNull()
            },
        )
    },
)
