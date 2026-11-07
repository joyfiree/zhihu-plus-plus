# UI / 交互 / 动效问题清单（2026-11-07）

> 范围：`shared/src/commonMain`（Android / iOS / macOS / Desktop 共享 UI）与 `theme/`、`designsystem/`。
> 背景：`c45343cb refactor(ui): 视觉体系从 MD3 切换为仿知乎官方 App 风格` 之后，色彩、圆角、图标已经对齐，
> 但**排版尺度、间距、点击语义和一部分设置项没有同步到位**，观感与手感仍停留在 MD3 时代。
> 本清单只记录代码里可直接定位的问题，每条给出证据与目标值，按「影响使用 → 明显观感 → 打磨」排序。

---

## 一、P0：影响正常使用

### 1. 回答页 / 问题页标题字号过大，且展开态没有行数上限

这是本次最直观的问题，根因是三处叠加：

| 位置 | 现状 | 证据 |
| --- | --- | --- |
| 展开态标题 | `headlineMedium` = **28sp / 行高 38sp / Normal** | `ui/components/TwoRowsTopAppBar.kt:118`、`theme/Type.kt:61` |
| 收起态标题 | `titleLarge` = 18sp / Medium | `ui/components/TwoRowsTopAppBar.kt:121`、`theme/Type.kt:64` |
| 标题行数 | 展开态 `maxLines = Int.MAX_VALUE` | `ui/ArticleScreen.kt:507` |
| 作者行 | 展开态上下 padding 各 16dp + 40dp 头像 | `ui/ArticleScreen.kt:515`、`:533` |

28sp 是 M3 的「中等标题」尺寸，官方详情页大标题实测在 **20–22sp 加粗**。更严重的是顶栏高度按
`max(固定行高, 标题实测高)` 计算（`TwoRowsTopAppBar.kt:548` 附近），标题不限行 + 40dp 作者行会把展开态顶栏撑到
170dp 以上；长标题（如当前设备实测的问答标题折 3 行）会占到接近四分之一屏，滚动前几乎看不到正文。

**另一处**：`ui/QuestionScreen.kt:418` 用 `headlineSmall`（24sp）+ Bold 作为问题页正文标题，与回答页的 28sp 并存，
同一个 App 里两个详情页两套标准。

**建议**
- 为内容标题新增一个语义角色，例如 `ZhihuTypography.ContentTitle = 21sp / 28sp / SemiBold`，两行顶栏与问题页共用；
  不要直接改 `headlineMedium`（`ui/HomeScreen.kt:621`、`ui/BlocklistSettingsScreen.kt:280-313` 也在用它做展示级标题）。
- 展开态 `maxLines = 2`，超出省略；作者行 padding 收到 `top = 8dp / bottom = 12dp`。
- 收起态（18sp）与展开态（21sp）差值压到 3sp，折叠时不再出现「标题被劈成两半」的观感。

### 2. 隐藏的标题行仍然可点击，展开态点顶栏空白会跳进问题页

`TwoRowsTopAppBar` 里两行标题常驻组合，隐藏行只靠 `graphicsLayer { alpha = titleAlpha() }`
（`TwoRowsTopAppBar.kt:443`、`:465`）做透明；`graphicsLayer` 不影响命中测试。而小标题外层在
`ArticleScreen.kt:496-506` 绑定了 `clickable { navigator.onNavigate(Question(...)) }`。

结果：展开态下顶栏有一块**不可见但可点**的区域，误触会静默跳转；折叠过程中 28sp 与 18sp 两行同时参与布局，
还能看到被 `clipToBounds` 裁切的残影。

**建议**：按 `collapsedFraction` 只组合当前可见的那一行（`if (collapsedFraction < 0.5f)`），而不是两层都组合后调 alpha。

### 3. 信息流的「已经到底啦」永远不会出现，且触底后每 50ms 重发一次 loadMore

`PaginatedList` 的 `isEnd` 默认值是 `{ false }`（`ui/components/PaginatedList.kt:182`），首页调用时**没有传**
（`ui/HomeScreen.kt:714-726`，只有 `footer = ProgressIndicatorFooter`）：

- 列表 footer 的 `AnimatedContent(targetState = isEnd())` 永远停在加载态，`ZhihuListFooter("已经到底啦")` 不会显示（`:263-275`）；
- `LaunchedEffect` 里的 `while (shouldLoadMore && items.isNotEmpty() && !isEnd())` 到列表底部后会以 50ms 间隔反复调用
  `onLoadMore()`。`BaseFeedViewModel` 有 `isLoading` 防重入（`viewmodel/feed/BaseFeedViewModel.kt:66`），所以不会发爆请求，
  但协程会一直空转，且「到底」状态永远无法收敛。

同族页面（`QuestionScreen.kt:267`、`NotificationScreen.kt:166`、`CollectionScreen.kt:150`、`OnlineHistoryScreen.kt:199`）
都正确传了 `isEnd = { viewModel.isEnd }`，首页和搜索页（`SearchScreen.kt:768-791`）是遗漏。

**建议**：两处补 `isEnd = { viewModel.isEnd }`；顺手把 `LaunchedEffect` 的 key 改成读快照值，避免在 key 里调用 lambda。

### 4. 卡片作者行是「死点击区」：点击被消费但没有动作

```kotlin
// ui/components/FeedCard.kt:549-554（duo3 排版）与 :619-622（原始排版）
Row(
    modifier = Modifier.weight(1f, fill = false).clickable {},
) { /* 头像 + 昵称 + 认证 */ }
```

空 lambda 会消费点击且无任何反馈，也不冒泡到外层的 `combinedClickable`。用户点头像或昵称，既进不了作者页，
也进不了详情页，表现为「点了没反应」。

**建议**：删除内层 `clickable`（让事件冒泡到卡片），或接上 `navigator.onNavigate(Person(...))`。

### 5. 没有互动数据的卡片没有「更多」菜单

菜单入口被塞在 `if (item.details.isNotEmpty())` 里（`ui/components/FeedCard.kt:583-593`、原始排版 `:666-681`）。
`details` 是「赞同 · 评论」计数，新回答、零互动内容、部分想法为空，这些卡片因此无法屏蔽作者/关键词。

**建议**：菜单入口常驻行尾，`details` 为空时只隐藏文本。

---

## 二、P1：明显观感问题

### 6. 卡片内容左右缩进实际是 32dp，是官方与自身设计令牌的两倍

外层 `Box` 已经吃掉 16dp（`FeedCard.kt:212` `.padding(horizontal = horizontalPadding, vertical = 5.dp)`，
`horizontalPadding` 默认 `ZhihuSpacing.ScreenGutter` = 16dp，见 `:117`），内层 `Column` 又加了 16dp
（`FeedCard.kt:280` `.padding(ZhihuSpacing.Large)`）。标题与摘要因此从屏幕边缘缩进 32dp，可读宽度减少约 32dp。
卡片样式（`FeedCardCardContent`）与原始排版同样有这个问题。

**建议**：只保留一层 gutter——内层 `Column` 改为 `padding(vertical = 12.dp)`，水平方向不再加。

### 7. 缩略图三套尺寸，骨架屏只对齐其中一套

- duo3：`112 × 84`（`FeedCard.kt:525`）
- 卡片样式：`104 × 78`（`FeedCard.kt:327-328`）
- 原始排版：`88 × 66`（`FeedCard.kt:690`）
- 骨架屏：固定 `112 × 84`（`ui/components/ZhihuSkeleton.kt:143` 附近）

duo3 是默认排版，所以当前勉强对得上；一旦用户关掉 Duo3 或切到卡片样式，骨架 → 真实卡片会横向/纵向跳 8–24dp。

**建议**：把尺寸提为 `ZhihuSizes.FeedThumbnail`（112 × 84，4:3），三种排版与骨架共用。

### 8. 默认排版标题字重比卡片样式轻

卡片样式显式 `fontWeight = FontWeight.SemiBold`（`FeedCard.kt:295-299`），duo3 排版直接取 `titleMedium`
（`Type.kt:65` = 16sp / 24sp / **Medium**，`FeedCard.kt:484-495`），标题因此比摘要只重一档，与官方「标题明显压过正文」的层级不符。

**建议**：duo3 标题同样 `copy(fontWeight = FontWeight.SemiBold)`；不要在 `Type.kt` 里全局提权，以免影响设置页。

### 9. 卡片标题与缩略图使用裸 `AsyncImage`，与其它图片加载路径不一致

卡片样式用 `ZhihuNetworkImage`（占位底色 + 淡入），duo3 的缩略图和头像用裸 `AsyncImage`
（`FeedCard.kt:520`、`:555`）。同一列表里一半图片「灰块 → 淡入」，一半「白洞 → 硬跳」；弱网下头像位置是纯白方块。

**建议**：duo3 两处统一改用 `ZhihuNetworkImage`。

### 10. 卡片摘要 14sp + `#76808F` 的对比度不足

摘要使用 `bodyMedium`（14sp）+ `onSurfaceVariant`（浅色 `#76808F`，`theme/OfficialZhihuScheme.kt`）。
相对亮度下对纯白卡片约 4.0:1，14sp 不属于大字号，AA 需要 4.5:1。`docs/zhihu-official-style.md` 的 3:1 承诺是给
小字/辅助文字的，而摘要是阅读主体。

**建议**：浅色下摘要单独用 `onSurface.copy(alpha = 0.82f)`，或把 `onSurfaceVariant` 调到 `#6A7380`（约 4.6:1）；
深色 `#9AA3B2` 对 `#1E1E1E` 约 6.5:1，无需调整。

### 11. 卡片元信息行三档字号混杂

作者名 `labelLarge`（**15sp**，`FeedCard.kt:563-570`），duo3 的计数 `labelMedium`（12sp，`:579-587`），
卡片样式的计数 `labelSmall`（11sp，`:388`）。官方作者名 13–14sp、计数 12sp，且同为灰色。

**建议**：作者名统一 13sp、计数统一 12sp，同一卡片内不再出现第三种 meta 字号。

### 12. 首页顶栏（非 duo3 路径）品牌标题 28sp

`HomeScreen.kt:619-625` 用 `headlineMedium`（28sp）+ Bold 渲染「知乎」。官方顶部词标约 20–22sp，
28sp Bold 在 360dp 宽屏上已经接近标题的视觉重量上限。

**建议**：改 20sp SemiBold（可与第 1 条的 `ContentTitle` 共用），或直接 `titleLarge`。

### 13. duo3 首页顶栏：搜索胶囊溢出白色容器，账号按钮比胶囊还高

容器高度是 `statusBars + 8dp + 32dp`（`HomeScreen.kt:520-525`），而内部 Row 是 `top = statusBars` + `8dp` padding，
胶囊 `height(56.dp)`（`:537`），胶囊下沿比白色容器底边低 24dp；账号按钮 `size(64.dp)`（`:566-570`）又比胶囊高 8dp。

**建议**：容器高度改为 `statusBars + 8 + 56 + 8`，账号按钮收到 48dp 触控盒 / 32dp 头像。

### 14. 设置页分组是「灰底里塞一堆 6dp 圆角白卡」，不是官方整块白卡

容器用 `ZhihuShapeTokens.Section`（12dp）+ `zhihuSurfaces.cardStrong`（灰），行用
`ZhihuShapeTokens.Thumbnail`（**6dp**，`ui/components/SettingItem.kt:391`）+ `card`（白），行间露 6dp 灰缝
（`SettingItem.kt:192` 的 `spacing = 6.dp`）。官方「我 - 设置」是 12dp 圆角**整块白卡**、行间 0.5dp 发丝线、
线左缩进 16dp。同文件 `:143-144` 的 KDoc 还写着「行本身带 12dp 圆角」，与实现已经脱节。

**建议**：行改 `RectangleShape`（首尾由容器裁剪），容器保留 `Section + card`，行间插 `HorizontalDivider(0.5.dp, outlineVariant)`
并按有无图标缩进，删除自定义 `Layout` 的 `spacing` 计算。

### 15. 设置分组标题全部是品牌蓝

`SettingItem.kt:171-178` 用 `titleSmall` + `colorScheme.primary`。官方分组标题是灰色小字，蓝色只留给可点链接；
现在整页看下去是一片蓝色小标题。

**建议**：改 `onSurfaceVariant`，间距从写死的 `10.dp` 换成 `ZhihuSpacing.Small`。

### 16. 外观设置是一整屏 11 个分组、50+ 行，没有二级页

`ui/subscreens/AppearanceSettingsScreen.kt` 全文 1789 行，单个 `Column + verticalScroll` 从 `:382` 铺到 `:1786`，
其中「信息流样式 / 分享操作 / 双击动作」是同一个 `ExposedDropdownMenuBox` 模板复制了 5 遍。
项目已有设置搜索入口（`AccountSettingScreen.kt:482`），但一级页仍是平铺。

**建议**：一级页收敛为 7 行左右（主题 / 阅读排版 / 回答页 / 信息流与底栏 / 翻页 / 交互与反馈 / 实验特性），
`settingKey` 深链继续指向二级页内对应行。

### 17. 底部操作栏固定 36dp，把按钮压成小于 48dp 的触控目标

`ArticleScreen.kt:591-598` 的整行 `Row` 有 `.height(36.dp)`，M3 `Button` 的 `defaultMinSize` 被父级收敛；
`ArticleScreen.kt:630` 的反对按钮 `height(ButtonDefaults.MinHeight).width(ButtonDefaults.MinHeight)` 同样变成 36dp。

**建议**：行高改 48dp，视觉药丸仍 36dp（用 `padding(vertical = 6.dp)` 表达）。

### 18. 底部操作栏没有背景层，正文从按钮下方穿过

`ArticleScreen.kt:586-702` 的 `bottomBar` 直接是一个 `Row`，没有 `Surface` / `background`；
只有状态栏有渐变遮罩。与官方的「白色底栏 + 发丝线」不符。

**建议**：用 `Surface(color = MaterialTheme.colorScheme.surface)` 包住整条 Column，顶部可加 1dp 发丝线。

### 19. 状态栏渐变遮罩无条件绘制，与注释矛盾

`ArticleScreen.kt:1123-1133` 的注释写「仅 duo3 路径需要；主视觉路径不绘制」，但代码里没有
`if (useDuo3ArticleActions)`，渐变恒压 `statusBar + 16dp`。

**建议**：补上分支，或主视觉路径改用 `surface` 同色。

### 20. 正文默认 16sp / 160% 行高，低于官方 17–18sp / 1.7

`theme/Type.kt:68` 的 `bodyLarge` 是 16sp / 26sp，Markdown 正文按 `16.sp * fontSize / 100` 与
`PREF_LINE_HEIGHT` 默认 160（`markdown/RenderMarkdown.kt:363`、`:323`）渲染。官方正文 17–18sp、行高约 1.7。

**建议**：base 改 17sp、`PREF_LINE_HEIGHT` 默认改 170。这是用户可调项，改**默认值**，不要写死。

### 21. 正文图片只占 80% 宽并居中

`markdown/RenderMarkdown.kt:124-125` `.fillMaxWidth(0.8f)`。官方正文图满宽。

**建议**：`fillMaxWidth()`，圆角维持 `ZhihuShapeTokens.Thumbnail`（6dp）。

### 22. BottomSheet 圆角从来不是 16dp

`docs/zhihu-official-style.md` 把「BottomSheet 顶部 28dp → 16dp」列为本次重构项，但
`MyModalBottomSheet` 的默认值仍是 `BottomSheetDefaults.ExpandedShape`（`ui/components/MyModalBottomSheet.kt:110`、`:271`），
`ZhihuShapeTokens.BottomSheet`（16dp）在 `shared/src` 里 **0 次使用**。

**建议**：默认值改为 `ZhihuShapeTokens.BottomSheet`。

### 23. BottomSheet 内容不被底部导航栏 inset 保护

`MyModalBottomSheet.kt:116` 只取 `BottomSheetDefaults.windowInsets`（仅 Top），`:372` 只做 `windowInsetsPadding(contentWindowInsets())`。
评论列表、播放队列等滚到底时，最后一条会落在手势条 / 三键导航下面。

**建议**：内层 Column 追加 `.navigationBarsPadding()`，并把 `consumeWindowInsets` 的 bottom 一起算进去。

### 24. 评论输入框行高 18sp 配 16sp 字，打字时上下贴死

`ui/CommentScreen.kt:1146-1149` `MaterialTheme.typography.bodyLarge.copy(lineHeight = 18.sp, ...)`。
多行输入时明显拥挤。

**建议**：沿用 `bodyLarge` 的 26sp。

### 25. 评论占位符与正文同字号同颜色

`CommentScreen.kt:1133-1142` 占位 `Text` 用 `bodyLarge` 且没有指定 `color`，落到 `LocalContentColor`（近黑），
与真实输入无法区分。

**建议**：`bodyMedium` + `onSurfaceVariant`。

### 26. 评论区大量 24dp 触控目标

`CommentScreen.kt:1449` 更多、`:1155` 发送、`:1061` 取消回复都是 `IconButton(... size(24.dp))`；
`:1492-1499` 的回复按钮用 `Column(Modifier.height(24.dp))` 承载 16dp 图标；`:1519-1527` 点赞整行 clickable 视觉高度也只有 24dp。
项目规范是 48dp（`ui/designsystem/Dimens.kt:48`）。

**建议**：`IconButton` 不要显式 `size(24.dp)`（会把 `minimumInteractiveComponentSize` 一起收敛）；
回复/点赞行改 `heightIn(min = 40.dp)` 或 `minimumInteractiveComponentSize()`，视觉 24dp 用内边距实现。

### 27. 二级评论三套缩进值互不相同

子评论容器 `padding(start = 40.dp)`（`CommentScreen.kt:764`），父评论正文起点 = 40dp 头像 + 12dp（`:1290-1296`）= 52dp，
底部信息栏又是 44dp（`:1414`）。

**建议**：统一定义 `CommentIndent = 52.dp`，子评论与时间/点赞行共用。

### 28. 评论区根层与「回复」子层是两个并存的 Sheet，语义树里出现两份评论列表

`ui/components/CommentScreenComponent.kt:150-215` 两个 `MyModalBottomSheet` 同时 `if (commentsVisible ...)`，
各自渲染一个带 `COMMENT_SCREEN_LIST_TAG` 的 `LazyColumn`。当前设备 dump 已经能看到重复节点。

**建议**：子层打开时给根层加 `Modifier.clearAndSetSemantics {}`，或改成单 Sheet 内部导航栈。

### 29. 问题页评论数出现两次

`ui/QuestionScreen.kt:433-444`：左侧 `StatItem("$commentCount 评论")`，右侧 `OutlinedButton { Icon(Comment); Text("$commentCount") }` 同一行重复展示。

**建议**：按钮只保留图标 +「评论」，或右侧换成官方的主行动作（写回答 / 关注）。

### 30. 用户页顶栏右上角有一个 96×56 的透明热区，会吞掉 Initial pass 的全部触摸

`ui/PeopleScreen.kt:818-843` 的 `Box` 挂了一个 `pointerInput`，在 `PointerEventPass.Initial` 里无条件 `consume()`。
它只是给折叠中的搜索按钮垫背景，却会吃掉该矩形内所有手势。

**建议**：删除该 Box 与 `pointerInput`，把同样的渐变挂到 `IconButton` 自己的 `.background(..., CircleShape)` 上。

### 31. 用户页关注按钮两个状态视觉相同

`PeopleScreen.kt:1767-1772` 两种状态都是 `OutlinedButton`，只有「关注 / 取消关注」四个字不同。
官方是「未关注 = 实心蓝药丸，已关注 = 灰底灰字」。

**建议**：按状态切换 `Button` / `Surface(Pill, surfaceContainerHighest)`。

### 32. 用户页切换 Tab 再切回来会丢滚动位置

`PeopleScreen.kt:878` 在 pager 的 page lambda 里 `rememberLazyListState()`，而 `HorizontalPager`（`:872-876`）
没有设 `beyondViewportPageCount`（默认 0）。离开可视窗口的页会退出组合，状态随之销毁。

**建议**：`beyondViewportPageCount = 2`，或把各页 `LazyListState` 提升到 `rememberSaveable` 列表中按 index 取。

### 33. 用户页统计数字与标签同字号同颜色

`PeopleScreen.kt:1535-1536` 数字 `titleMedium`、标签 `labelMedium`，都没有颜色与字重区分。

**建议**：数字 `titleMedium + SemiBold + onSurface`，标签 `labelMedium + onSurfaceVariant`。

### 34. 设置行 / 顶栏图标按钮触控目标不足 48dp

- 账号页扫码、退出登录：`size(44.dp)`（`ui/AccountSettingScreen.kt:279-291`）
- 设置搜索框：`height(36.dp)`（`AccountSettingScreen.kt:475-483`）
- 朗读播放器主按钮：40dp，同排其它按钮却是 48dp（`ui/components/ReadingPlayerBar.kt:191-198`）
- 搜索页清空 32dp、更多/刷新 40dp（`ui/SearchScreen.kt:277`、`:395-399`、`:573-584`）
- Markdown 工具栏 40dp（`ui/components/MarkdownShotcutToolbar.kt:100-111`）

**建议**：统一 `ZhihuSizes.MinTouchTarget`（48dp），视觉尺寸靠内层 padding 或 `minimumInteractiveComponentSize()` 表达。

### 35. 搜索页历史/热搜前导图标被压成 18dp，与序号列不齐

`SearchScreen.kt:482-485`、`:539-542` 写的是 `Modifier.width(28.dp).size(18.dp)`——`size` 会同时覆盖宽高，
最终是 18×18；而热搜序号是 `width(28.dp)`（`:618`）。两组列表左右错位 5dp。

**建议**：改 `Modifier.size(width = 28.dp, height = 18.dp)`。

### 36. 首屏可用阅读区域被顶栏和底栏一起挤到不足三分之一屏

展开态顶栏（28sp 标题 + 16dp 上下 padding + 40dp 头像行）与底部操作栏（36dp + 安全区内边距 + 正文额外的
`(16 + 36).dp` 占位，见 `ui/ArticleScreen.kt:1081`、`:1111`）同时挤压正文。在小屏或系统大字号下，
首屏正文只剩两三行。这与第 1、17 条同源，修完前两条后复测即可。

---

## 三、P2：打磨项

### 37. 底部导航栏 80dp，明显高于官方

`ui/designsystem/Dimens.kt:53` `NavigationBarHeight = 80.dp`，`ui/ZhihuMain.kt:675` 直接把它当高度用。
官方底栏约 56dp + inset。**建议**：改 64dp。

### 38. 遗留魔法数：卡片间距两处独立定义

`FeedCard.kt:212` 的 `vertical = 5.dp` 与 `PaginatedList.kt:231` 骨架的 `vertical = 5.dp` 是同一个值但各写一份，
任何一处改动都会让骨架与卡片跳版。**建议**：提为 `ZhihuSpacing.CardGap`。

### 39. `duo3CardAppearance` 是死分支

`FeedCard.kt:216-222` 的 `containerColor` 两个三目分支都返回 `zhihuSurfaces.card`，开关实际已失效，会误导后续维护者。
**建议**：恢复语义或删除该设置项。

### 40. 卡片设置读取没有 key，改完必须杀进程

`FeedCard.kt:131-140` 全部是 `remember { settings.getBoolean(...) }`（无 key），`duo3_card_layout` /
`duo3_card_large_title` / `duo3_card_appearance` / `feedCardStyle` 切换后不生效。
项目已有 `rememberObservedSetting`（`ui/UiSupportFiles.kt:145`）。**建议**：改用它。

### 41. 多处动画绕过 Motion 令牌

- `ui/TopicScreen.kt:665-670` 自写 `tween(Motion.DURATION_LONG, FastOutSlowInEasing)`；
  `Motion.kt:30-35` 明确禁止组件里写字面量时长/自定义曲线。
- `AppearanceSettingsScreen.kt:461/903/985/1680/1730/1752` 六处 `AnimatedVisibility` 未传 spec，落回默认 300ms 淡入 + 展开，
  与页面里 `Motion.springMedium()`（`:1209`）不同源。
- `QuestionScreen.kt:371-384` 的顶栏切换用默认 spec 的 `fadeIn + slideInVertically`。
- `SettingItem.kt:117-122` 的高亮自写 `LinearEasing` / `FastOutSlowInEasing` + 裸 `delay(2000)`，总时长 2.8s。

**建议**：统一 `Motion.enterTween()/exitTween()/fadeTween()/springMedium()`；高亮停留改 900ms。

### 42. 翻页 FAB 完全没有按压反馈，拖动却触发「长按」震感

`ui/components/DraggablePageTurnButtons.kt:316-321` `indication = null`，外层 `Surface` 也没有 `shadowElevation`；
`:262-265` 的 `onDragStart` 调用 `haptics.longPress()`，用户正常滑动一下就会收到长按语义的震感。

**建议**：保留 indication 或按 `Motion.Gesture.PRESSED_SCALE` 做按下缩放；`onDragStart` 改用轻量 tick。

### 43. 播放器/列表面板的 Sheet 常停在半屏

`HomeScreen.kt:700-703` 的账号面板没传 `sheetState`，走 `skipPartiallyExpanded = false`，必然高于半屏的内容
首次打开只到 50%，要再拖一次。项目里 `ArticleScreen.kt:1329` 已有正确范例。**建议**：全屏类 Sheet 统一 `skipPartiallyExpanded = true`。

### 44. 播放列表 Sheet 高度写死 560dp

`ReadingPlayerBar.kt:392-396` `heightIn(max = 560.dp)`，在 640dp 级屏上会把标题行顶出屏幕。**建议**：`fillMaxHeight(0.8f)`。

### 45. 页面底色不统一

`AccountSettingScreen.kt:196-199` 与 `HomeScreen.kt:702` 用 `surfaceContainer`，而深色下 `zhihuSurfaces.page` 是
`surfaceContainerLowest`（`ZhihuSurfaces.kt:56-60`）；深色模式下账号页比其它页面亮一档，切换时能看到底色跳变。
**建议**：两处统一 `zhihuSurfaces.page`。

### 46. 分享弹层是自绘 Sheet：有把手但不可拖，且没有导航栏 inset

`ui/components/ShareDialogComponent.kt:116-131` 画了 40×4dp 的把手条，`:104-157` 的 `Surface` 没有任何拖拽手势；
`:111-115` 也没有 `navigationBarsPadding()`，最后一个动作在全面屏上会压到系统条。**建议**：直接用 `MyModalBottomSheet` 承载。

### 47. 导出弹窗超长不可滚动，圆角还不一致

`ui/components/ExportDialogComponent.kt:88-93` 无滚动，内容约 600dp 量级；`:86` 用 `ZhihuShapeTokens.Card`（12dp），
而全站弹窗统一 `ZhihuShapeTokens.Dialog`（16dp）。**建议**：加 `heightIn(max = 560.dp).verticalScroll(...)`，shape 改 Dialog。

### 48. 编辑器：未保存保护缺失、发布按钮可点空内容、预览条件不一致

- `WriteAnswerScreen.kt:129`、`WritePinScreen.kt:225-233` 的内容只用 `remember`，返回即丢（`WriteAnswerScreen.kt:358-362`、`WritePinScreen.kt:426-430`）。
- 发布按钮 `enabled = !isSubmitting && !isUploadingImage`（`WriteAnswerScreen.kt:370-373`），空内容只在点击后提示；
  同一屏的预览却用 `content.text.isNotBlank()`（`:393`）。
- 纯图片想法无法预览（`WritePinScreen.kt:450` 与 `:325` 的发布条件不一致）。
- 编辑器底部留白 `bottomPadding = 280.dp` 写死（`WriteAnswerScreen.kt:424`），与 FAB 列实际高度（取决于按钮个数）解耦。

**建议**：内容改 `rememberSaveable`；发布/预览共用同一谓词；留白用 `onGloballyPositioned` 测量 FAB 列高度。

### 49. Markdown 工具栏：无选区点格式后光标落在占位文字之前

`MarkdownShotcutToolbar.kt:157-162` 无选区时 `cursorOffset = prefix.length`，得到 `**加粗**` 后光标在 `**` 与「加」之间，
继续输入会变成「加粗x」夹心。插入链接同理（`:412-425` `cursorOffset = 1`）。

**建议**：插入后把 `selection` 设为占位符区间，让用户输入即替换。

### 50. 通知、通知时间线、私信失败后会永久停在骨架屏

`viewmodel/NotificationViewModel.kt:154-160` / `:249-254` / `:471` 与 `PrivateMessageScreen.kt:224-263` 的 catch 只调用
`handleFetchFailure`，不写 `errorMessage` 也不设 `lastPaging`，页面又不读 `errorMessage`。
`PaginatedList.kt:226-234` 因此持续渲染骨架，用户无从重试。

**建议**：catch 里写 `errorMessage`，页面在「空列表 + 有错误」时展示「加载失败 + 重试」，而不是骨架。

### 51. 收藏直达页在收藏夹请求失败时无限转圈

`CollectionBrowseScreen.kt:312-321` 的 `contentViewModel == null -> CircularProgressIndicator` 没有失败出口；
`:302` 的「还没有收藏夹」和 else 分支都进不去。**建议**：为该分支加失败判定与重试入口。

### 52. 私信发送期间整个输入框被禁用

`PrivateMessageScreen.kt:189` `enabled = !viewModel.isSending`，发送中会失焦、键盘收起、连续输入被打断。
发送按钮（`:212`）已单独判断。**建议**：去掉输入框的 `enabled` 条件。

### 53. 通知时间三套格式

会话列表固定 `MM-DD`（`NotificationScreen.kt:614-619`），单条通知用相对时间（`:515-520`），
邀请卡片用 `HH:mm / 昨天 / MM-DD`（`NotificationTimelineScreen.kt:339-343`）。

**建议**：抽一个 `formatNotificationTime` 统一。

### 54. 通知分类入口随列表滚走

`NotificationScreen.kt:170-181` 分类行是 `topContent` 里的第一个 `item`，滚动后无法切分类。**建议**：固定表头或 `stickyHeader`。

### 55. 列表页顶栏风格不统一

`NotificationScreen.kt:130` 用居中栏（`ZhihuCenterAlignedTopAppBar`），同性质的列表页 `CollectionScreen.kt:110` 用左对齐；
`ZhihuTopAppBar.kt:85-86` 的文档写明居中栏「用于详情页」。

**建议**：列表页统一左对齐（或统一居中，但要改文档），标题字重收进 `ZhihuTopAppBar` 内部。

### 56. 想法详情的赞/评栏在正文流里，会随内容滚走

`PinScreen.kt:740-775` 的两个 `FilledTonalButton` 位于 `Column + verticalScroll`（`:510-516`）内部。
官方的赞/评/分享固定在底部。**建议**：`Scaffold` 增加 `bottomBar`，正文里删除这段。

### 57. 收藏删除弹窗复制两份，且确认按钮没有破坏性视觉

`CollectionScreen.kt:253-303` 与 `CollectionBrowseScreen.kt:381-434` 逐字重复；确认按钮是无色 `TextButton`，
而同页删除图标用了 `colorScheme.error`。**建议**：抽 `CollectionDeleteDialog`，确认按钮用 error 色。

### 58. 关注页两个子页几乎整段复制

`FollowScreen.kt:417-466` 与 `:572-621` 是逐字相同的 `menuItems` 块，tab 标题也定义两次（`:156`、`:217`）。
**建议**：抽 `FeedBlockMenuItems(...)` 与文件级 `TAB_TITLES`。

### 59. 私信里的表情按钮是永久禁用的死控件

`PrivateMessageScreen.kt:193-199` `IconButton(onClick = {}, enabled = false)`，长期占据 48dp 输入栏空间。
**建议**：功能落地前移除，或改为可点击并提示「暂未支持」。

### 60. 身份管理把所有失败都塞进列表错误位

`IdentityManagementScreen.kt:291-307` 用单一 `state.errorMessage` 渲染「加载失败 + 重试」，
而切换账号失败（`:462-466`）与创建失败（`:548-554`）也写这个字段；重试却只刷新列表。
**建议**：拆 `listError` / `actionError`，动作失败留在对应弹窗或走 `showLongMessage`。

### 61. 无障碍：图标与文字重复朗读、未读仅靠颜色

- `NotificationScreen.kt:254-258` 图标 `contentDescription = category.detailTitle` 与下方文字重复。
- `NotificationScreen.kt:433-439` 未读是 6dp `error` 圆点，没有 `stateDescription`。
- `AccountSettingScreen.kt:245-252`、`PeopleScreen.kt:1682-1692`、`PinScreen.kt:538-544` 的头像 description 只说「头像/用户头像」，
  不表达点击行为，且与所在 Row 的合并语义叠加。
- `AnswerVerticalOverscroll.kt:428-439` 图标 description 与相邻文字重复。

**建议**：装饰性图标 `contentDescription = null`；独立可点元素描述动作（如「查看头像大图」）；未读补 `stateDescription`。

### 62. 回答切换预览卡的摘要传的是问题标题

`ui/components/AnswerVerticalOverscroll.kt:342` `excerpt = previousAnswer.title`，预览卡标题因此永远是问题名而不是回答内容。
另外拖动时外层 Box 没有铺底色（`:374-380`），位移后上下会露出窗口底色。

**建议**：补正文首段作为 excerpt；外层加 `.background(MaterialTheme.colorScheme.surface)`。

### 63. 菜单里有开发期说明文案

`ui/article/ArticleActionsMenu.kt:425`：「在电脑中打开（我计划使用浏览器插件实现，还在写，点击后请手动前往收藏夹打开）」
会被折成两行以上。**建议**：移到副文案或移除。

### 64. 编辑器工具栏符号无可读含义

`MarkdownShotcutToolbar.kt:44-54` 的标签是 `H1 / H2 / B / I / Link / > / </> / ``` / HR / ∑ / #`，
`contentDescription` 也没有中文解释。**建议**：至少补「加粗（B）」这类中文描述。

---

## 四、建议的修复顺序

1. **P0 全部**：内容标题 21sp + 2 行 + 作者行收敛；隐藏标题行不可点；首页/搜索补 `isEnd`；卡片作者区点击；卡片菜单常驻。
2. **详情页尺度**：正文 17sp / 170%；正文图满宽；底栏 48dp + 白底 + 发丝线；状态栏遮罩分支。
3. **信息流观感**：去掉双层 gutter；缩略图 token 化；标题字重；`ZhihuNetworkImage` 统一；摘要对比度。
4. **设置与 Sheet**：设置行改整块白卡 + 发丝线；分组标题去蓝；BottomSheet 16dp + 导航栏 inset；外观设置拆二级页。
5. **触控与无障碍**：全部 24-44dp 目标提到 48dp；评论重复语义；图标描述。
6. **动效与遗留**：Motion 令牌统一；翻页 FAB 反馈；Sheet 展开策略；死分支与魔法数清理。

---

## 五、验证方式说明

当前设备（1440×3168 @ 4.0x）上应用需要登录才能进入详情页，且本机没有可用的账号备份，
因此本文档的结论全部来自**源码核对**（每个条目都带文件与行号），而不是截图比对。
落地时建议按项目既有流程：

```bash
./gradlew assembleLiteDebug
./gradlew ktlintFormat
```

涉及 UI 的改动按 `AGENTS.md` 的「UI 调试强制清单」在 AVD 上逐项复检；标题字号、缩略图尺寸、底栏高度这类
可量化的项，建议在 `shared/src/jvmTest` 里补像素/尺寸断言（现有 `ThemeContrastTest`、`DesignSystemTokensTest`
已经是这种形式），避免再次漂移。
