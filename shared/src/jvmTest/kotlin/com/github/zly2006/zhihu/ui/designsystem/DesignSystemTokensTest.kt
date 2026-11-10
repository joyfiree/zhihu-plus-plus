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

package com.github.zly2006.zhihu.ui.designsystem

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zly2006.zhihu.theme.ZhihuTypography
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * design system token 回归（计划文档 T0.2）。
 *
 * token 是全局视觉一致性的唯一来源，数值改动应当是有意识的；用测试把当前档位锁住，
 * 避免后续顺手调参造成「设置页和导航栏节奏对不上」这类难以定位的漂移。
 */
class DesignSystemTokensTest {
    @Test
    fun motionDurationsKeepTheirTiers() {
        assertEquals(200, Motion.DURATION_SHORT)
        assertEquals(300, Motion.DURATION_MEDIUM)
        assertEquals(450, Motion.DURATION_LONG)
    }

    @Test
    fun spacingLadderStaysOnTheFourDpGrid() {
        assertEquals(4.dp, ZhihuSpacing.ExtraSmall)
        assertEquals(8.dp, ZhihuSpacing.Small)
        assertEquals(12.dp, ZhihuSpacing.Medium)
        assertEquals(16.dp, ZhihuSpacing.Large)
        assertEquals(24.dp, ZhihuSpacing.ExtraLarge)
        assertEquals(32.dp, ZhihuSpacing.Huge)
        assertEquals(16.dp, ZhihuSpacing.ScreenGutter)
    }

    /** 信息流卡片元信息行的官方档位：作者名 13sp、计数 12sp，不再出现第三种字号。 */
    @Test
    fun feedCardMetaKeepsOfficialTwoTierSizes() {
        assertEquals(13.sp, ZhihuTypography.FeedCardAuthorName.fontSize)
        assertEquals(12.sp, ZhihuTypography.FeedCardCount.fontSize)
        assertTrue(ZhihuTypography.FeedCardCount.fontSize < ZhihuTypography.FeedCardAuthorName.fontSize)
    }

    @Test
    fun componentMinimumsStayTouchable() {
        assertEquals(48.dp, ZhihuSizes.MinTouchTarget)
        assertEquals(56.dp, ZhihuSizes.ListItemMinHeight)
        assertEquals(60.dp, ZhihuSizes.NavigationBarHeight)
        assertEquals(24.dp, ZhihuSizes.Icon)
    }

    /**
     * 嵌套子评论的字号必须有语义角色。
     *
     * 之前是在调用点对 `FeedAuthorName` 做 `copy(fontSize = 13.sp)`，既绕过 token 又与
     * `audit.md` 的「无裸字号」结论冲突。这里把三档值锁住，避免以后再退回裸值。
     */
    @Test
    fun commentChildRolesStayOneStepBelowTheirParents() {
        assertEquals(15.sp, ZhihuTypography.CommentBody.fontSize)
        assertEquals(13.sp, ZhihuTypography.CommentChildAuthorName.fontSize)
        assertEquals(18.sp, ZhihuTypography.CommentChildAuthorName.lineHeight)
        assertEquals(12.sp, ZhihuTypography.CommentChildMeta.fontSize)
        assertEquals(14.sp, ZhihuTypography.CommentChildBody.fontSize)
        // 子层必须真的比一层小一档，否则加 token 就失去了分级意义。
        assertTrue(ZhihuTypography.CommentChildAuthorName.fontSize < ZhihuTypography.FeedAuthorName.fontSize)
        assertTrue(ZhihuTypography.CommentChildMeta.fontSize < ZhihuTypography.FeedMeta.fontSize)
    }

    /**
     * `docs/md3-refactor/audit.md` 宣称「裸 `fontSize = N` 只剩 2 处内容渲染豁免」。
     *
     * 该结论过去随提交漂移过：子评论曾经悄悄多出两处 `copy(fontSize = 13.sp)`，文档没跟上。
     * 这里直接扫 `commonMain` 源码，把 `fontSize = 数字.sp` 的真实命中数与白名单对齐，
     * 防止「文档与代码各说各话」再次发生。允许列表只登记确实无法走 Typography 的内容渲染尺寸。
     */
    @Test
    fun nakedFontSizesStayOnTheAuditWhitelist() {
        val commonMain = listOf(
            File("shared/src/commonMain"),
            File("../shared/src/commonMain"),
            File("src/commonMain"),
        ).firstOrNull { it.isDirectory }
        if (commonMain == null) return // 源码不在测试工作目录时跳过，避免误报为达标

        val nakedSize = Regex("""fontSize\s*=\s*\d+\.sp""")
        val allowed = setOf("CommentScreen.kt", "WriteContentEditor.kt")
        val hits = commonMain
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    if (nakedSize.containsMatchIn(line) &&
                        !line.trimStart().startsWith("//") &&
                        !line.trimStart().startsWith("*")
                    ) {
                        "${file.name}:${index + 1}: ${line.trim()}"
                    } else {
                        null
                    }
                }
            }.toList()

        val unexpected = hits.filter { hit -> allowed.none { hit.startsWith("$it:") } }
        assertTrue(
            unexpected.isEmpty(),
            "audit.md 白名单外出现裸字号（应提成 ZhihuTypography 语义角色）：\n${unexpected.joinToString("\n")}",
        )
        assertEquals(2, hits.size, "audit.md 登记的是 2 处内容渲染豁免，实际命中 ${hits.size} 处，请同步更新白名单")
    }
}
