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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.github.zly2006.zhihu.ui.designsystem.Motion
import com.github.zly2006.zhihu.ui.designsystem.ZhihuShapeTokens
import com.github.zly2006.zhihu.ui.designsystem.ZhihuSpacing
import com.github.zly2006.zhihu.ui.designsystem.rememberHapticController
import com.github.zly2006.zhihu.ui.designsystem.zhihuSurfaces
import com.github.zly2006.zhihu.util.ProvideContentColorTextStyle
import kotlinx.coroutines.delay

/**
 * 为设置项提供跳转高亮和滚动定位能力。
 *
 * 账号页、分享弹窗等入口会带着具体 `settingKey` 打开设置页；目标设置项通过这个 modifier 注册位置并播放短暂高亮，
 * 让用户知道刚才跳到的是哪一项。新增可被外部入口直达的设置时，应复用这个机制，而不是手写一次性滚动逻辑。
 */
fun Modifier.highlightSetting(
    settingKey: String?,
    highlightedKey: String,
    onPositioned: ((Int) -> Unit)? = null,
    bringIntoViewRequester: BringIntoViewRequester? = null,
    shape: Shape = RectangleShape,
    color: Color? = null,
): Modifier = composed {
    var modifier = this
    val actualBringIntoViewRequester = remember(settingKey, bringIntoViewRequester) {
        bringIntoViewRequester ?: if (settingKey != null) BringIntoViewRequester() else null
    }

    if (actualBringIntoViewRequester != null) {
        modifier = modifier.bringIntoViewRequester(actualBringIntoViewRequester)
    }

    if (onPositioned != null) {
        modifier = modifier.onGloballyPositioned { coords ->
            onPositioned(coords.positionInRoot().y.toInt())
        }
    }

    if (settingKey != null) {
        val isTarget = settingKey.isNotEmpty() && settingKey == highlightedKey
        val highlightAlpha = remember { Animatable(0f) }
        val highlightColor = color ?: MaterialTheme.colorScheme.primaryContainer

        LaunchedEffect(isTarget) {
            if (isTarget) {
                actualBringIntoViewRequester?.let {
                    delay(200)
                    it.bringIntoView()
                }
                // 闪烁两次：亮、暗、亮、暗、亮。
                repeat(2) {
                    highlightAlpha.animateTo(0.4f, tween(Motion.DURATION_SHORT, easing = LinearEasing))
                    highlightAlpha.animateTo(0.1f, tween(Motion.DURATION_SHORT, easing = LinearEasing))
                }
                highlightAlpha.animateTo(0.4f, tween(Motion.DURATION_SHORT))
                delay(2000)
                highlightAlpha.animateTo(0f, tween(Motion.DURATION_LONG, easing = FastOutSlowInEasing))
            } else {
                highlightAlpha.animateTo(0f, tween(Motion.DURATION_LONG))
            }
        }

        modifier = modifier.drawWithContent {
            drawContent()
            if (highlightAlpha.value > 0f) {
                drawRect(highlightColor.copy(alpha = highlightAlpha.value))
            }
        }
    }

    modifier
}

/**
 * 设置页的分组容器。
 *
 * 一组设置 = 一个分组标题 + 若干等高的圆角行 + 可选的说明文字。组内行之间用 6dp 间隙分隔，
 * 行本身带 12dp 圆角和 `surfaceContainerLow` 底色，形成 M3 的「分组列表」观感；
 * 间隙会在展开/收起动画中随行高一起收缩，不会留下突兀空白。
 *
 * 视觉上它是设置页的主要节奏单位，新增设置页时优先用它组织内容。
 */
@Composable
fun SettingItemGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    settingKey: String? = null,
    highlightedKey: String = "",
    onPositioned: ((rootY: Int) -> Unit)? = null,
    bringIntoViewRequester: BringIntoViewRequester? = null,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .highlightSetting(
                settingKey = settingKey,
                highlightedKey = highlightedKey,
                onPositioned = onPositioned,
                bringIntoViewRequester = bringIntoViewRequester,
            ).padding(horizontal = ZhihuSpacing.ScreenGutter)
            .padding(bottom = ZhihuSpacing.ExtraLarge)
            .then(modifier),
    ) {
        title?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 10.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        header?.let { it() }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ZhihuShapeTokens.Section,
            color = zhihuSurfaces.cardStrong,
        ) {
            Layout(
                content = content,
                modifier = Modifier.fillMaxWidth(),
            ) { measurables, constraints ->
                val placeables = measurables.map { it.measure(constraints) }
                val spacing = 6.dp.roundToPx()
                val baseItemHeight = 56.dp.toPx()

                var yPosition = 0
                val positions = mutableListOf<Int>()
                var hasVisibleBefore = false

                for (i in placeables.indices) {
                    val placeable = placeables[i]
                    val scale = (placeable.height / baseItemHeight).coerceIn(0f, 1f)

                    // 非首个可见元素前按比例补间距；元素缩小时，它前面的间距也同步收缩。
                    if (hasVisibleBefore && scale > 0f) {
                        yPosition += (spacing * scale).toInt()
                    }

                    positions.add(yPosition)
                    yPosition += placeable.height

                    if (scale > 0f) {
                        hasVisibleBefore = true
                    }
                }

                layout(constraints.maxWidth, yPosition) {
                    for (i in placeables.indices) {
                        placeables[i].placeRelative(0, positions[i])
                    }
                }
            }
        }

        footer?.let {
            Column(
                modifier = Modifier.padding(start = 4.dp, top = ZhihuSpacing.Small, end = 4.dp),
            ) {
                ProvideContentColorTextStyle(
                    textStyle = MaterialTheme.typography.bodySmall,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    it()
                }
            }
        }
    }
}

/**
 * 分组顶部的强调型总开关。
 *
 * 适合“启用全部”“开发者模式”这类会影响一组子设置的主控制。它用 `primaryContainer` 药丸形态
 * 区别于普通行，点击整行即可切换，具体批量写入哪些 preference key 由调用方负责。
 */
@Composable
fun SettingItemOverall(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    settingKey: String? = null,
    highlightedKey: String = "",
    onPositioned: ((rootY: Int) -> Unit)? = null,
    bringIntoViewRequester: BringIntoViewRequester? = null,
) {
    val haptics = rememberHapticController()
    SettingItem(
        modifier = modifier.padding(bottom = ZhihuSpacing.Medium),
        contentModifier = contentModifier.padding(horizontal = 8.dp),
        title = title,
        description = description,
        icon = icon,
        onClick = {
            haptics.confirm()
            onCheckedChange(!checked)
        },
        endAction = {
            SwitchWithIcon(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        },
        shape = ZhihuShapeTokens.Pill,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        enabled = enabled,
        settingKey = settingKey,
        highlightedKey = highlightedKey,
        onPositioned = onPositioned,
        bringIntoViewRequester = bringIntoViewRequester,
    )
}

/**
 * 标准开关设置行。
 *
 * 标题、说明、可选图标和右侧 Switch 组成一行，整行点击与直接点开关等价。大多数布尔设置都应使用这个组件，
 * 以保持触控区域、禁用态、高亮跳转和测试定位行为一致。
 */
@Composable
fun SettingItemWithSwitch(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    settingKey: String? = null,
    highlightedKey: String = "",
    onPositioned: ((rootY: Int) -> Unit)? = null,
    bringIntoViewRequester: BringIntoViewRequester? = null,
) {
    val haptics = rememberHapticController()
    SettingItem(
        modifier = modifier,
        title = title,
        description = description,
        icon = icon,
        onClick = {
            haptics.confirm()
            onCheckedChange(!checked)
        },
        endAction = {
            SwitchWithIcon(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        },
        enabled = enabled,
        settingKey = settingKey,
        highlightedKey = highlightedKey,
        onPositioned = onPositioned,
        bringIntoViewRequester = bringIntoViewRequester,
    )
}

/**
 * 带勾选图标的开关。
 *
 * 开启时拇指上显示对勾（M3 允许的可选拇指图标），关闭时保持空白——
 * 旧实现还会在关闭态画一个叉号，视觉上过于嘈杂。
 */
@Composable
fun SwitchWithIcon(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        thumbContent = {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        },
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    )
}

/**
 * 设置页的基础行组件。
 *
 * 一行由左侧可选图标、标题/说明、右侧动作和底部扩展内容组成，可表示普通导航入口、数值配置、下拉选择或开关行的底层布局。
 * 它同时接入高亮跳转、禁用态和统一圆角/背景色，是所有设置项视觉一致性的基础。
 *
 * 排版遵循 M3 list item：标题 `bodyLarge`，说明 `bodyMedium` + `onSurfaceVariant`，
 * 图标 24dp 并继承 `onSurfaceVariant` 内容色，右侧动作与文字垂直居中。
 */
@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    endAction: (@Composable () -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    shape: Shape = ZhihuShapeTokens.Thumbnail,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = zhihuSurfaces.card,
        contentColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = zhihuSurfaces.card,
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ),
    enabled: Boolean = true,
    settingKey: String? = null,
    highlightedKey: String = "",
    onPositioned: ((rootY: Int) -> Unit)? = null,
    bringIntoViewRequester: BringIntoViewRequester? = null,
) {
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .highlightSetting(
                settingKey = settingKey,
                highlightedKey = highlightedKey,
                onPositioned = onPositioned,
                bringIntoViewRequester = bringIntoViewRequester,
                shape = shape,
            ).then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, onClick = onClick)
                } else {
                    Modifier
                },
            ),
        shape = shape,
        color = if (enabled) colors.containerColor else colors.disabledContainerColor,
        contentColor = contentColor,
    ) {
        Column(
            contentModifier.padding(
                horizontal = ZhihuSpacing.Large,
                vertical = ZhihuSpacing.Medium,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ZhihuSpacing.Large),
            ) {
                icon?.let {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (enabled) {
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        CompositionLocalProvider(
                            LocalContentColor provides if (enabled) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                colors.disabledContentColor
                            },
                        ) {
                            it()
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .heightIn(min = 28.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    ProvideContentColorTextStyle(
                        textStyle = MaterialTheme.typography.bodyLarge,
                        contentColor = contentColor,
                    ) {
                        title()
                    }
                    description?.let {
                        ProvideContentColorTextStyle(
                            textStyle = MaterialTheme.typography.bodyMedium,
                            contentColor = if (enabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                colors.disabledContentColor
                            },
                        ) {
                            it()
                        }
                    }
                }
                endAction?.let { it() }
                if (endAction == null && bottomAction == null && onClick != null) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            colors.disabledContentColor
                        },
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            bottomAction?.let { it() }
        }
    }
}
