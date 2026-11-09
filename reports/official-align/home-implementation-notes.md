# 首页对齐官方知乎 11.14：实现说明（home-align-dev）

- 分支：`refactor/md3-ui`（工作树还含 Lead 的 theme/designsystem/ZhihuMain 改动，未提交）
- 依据：`reports/official-align/target-spec.md` §1/§2.1–§2.3、`screenshots/h1.jpg`、`crop_official_top.jpg`、`apk-analysis.md` §A/§B，以及 Lead 真机首轮比对反馈（2026-11-09）
- 自检：`./gradlew :shared:compileKotlinJvm` ✅、`./gradlew :app:assembleLiteDebug` ✅、`./gradlew :shared:ktlintCommonMainSourceSetCheck` ✅（本任务文件 0 告警）

## 改动的文件

| 文件 | 内容 |
|---|---|
| `shared/.../ui/HomeScreen.kt` | 搜索区、分类 Tab、频道编辑弹层、听入口、热榜 Tab 嵌入、移除消息方框/麦克风 |
| `shared/.../ui/components/ContentFeedCard.kt` | 按官方 §2.3 重写卡片版式、横滑图、固定三组操作行 |
| `shared/.../ui/components/ZhihuSkeleton.kt` | 骨架改为与真实卡同构的扁平版式（不再用 Card 容器） |
| `shared/.../ui/components/FeedCard.kt` | **未改动**。它是搜索/话题/收藏夹/热榜等页面的通用卡片；按 Lead 指示不改全局默认外观，首页热榜 Tab 暂时内嵌 `HotListScreen`（复用 FeedCard）。 |

## 1. 搜索区（`HomeSearchArea`）

- 顶部 `statusBars` inset 后直接 40dp 行（`ZhihuSizes.HomeSearchRowHeight`），左右 16dp（`ZhihuSizes.FeedCardHorizontalGutter`）。
- 左 38dp 圆形账号头像（`ZhihuSizes.HomeAvatarSize`）：登录态 `ZhihuNetworkImage` 取当前账号头像；游客态 `functionalSurface` 圆底 + `Icons.Filled.Person` 占位。点击：登录态 `onNavigateTopLevel(Account)`，游客态 `requestLoginNavigation()`。
- 距头像 8dp 是搜索胶囊：40dp 高、20dp 全圆角、`functionalSurface` 底、**1dp** `accent` 描边（真机实测官方为 1dp）。
- 胶囊内部（左→右）：11dp 起始内边距的热词 15sp（`ZhihuTypography.HotWord`，无热词时回退「搜索知乎内容」三级色）→「热」角标（红色 1dp 描边 + 3dp 圆角 + 4×1dp 内边距，文字用 `labelSmall`）→ 8dp → 1.5×12dp `accent` 50% 竖分隔线 → 8dp → 56×30dp 品牌蓝「搜索」按钮（14sp Bold 白字，底色用 `contentFirstColors.accentFill` = 深浅色均 `#1772F6`，文字色 `colorScheme.onPrimary`）→ 右侧 5dp。
- 胶囊点击与「搜索」按钮都导航 `Search(query = "")`；按钮在胶囊内部，点击事件先被按钮消费。
- 已移除：消息方框、未读数请求与红色角标、麦克风图标。消息入口（含未读角标）由 Lead 的底栏「消息」tab 承担。

## 2. 分类 Tab（`HomeCategoryTabRow`）

- 4 个 Tab：关注 / 推荐 / 热榜 / 日报（`HomeCategoryTab.entries`，顺序即官方顺序），默认「推荐」。
- 行高 40dp（`ZhihuSizes.HomeTabRowHeight`）；Tab 区 `horizontalScroll` + 左对齐，第一项起始 8dp；每项固定 50dp（`HomeTabItemWidth`），文字 16sp（`ZhihuTypography.TabTitle`，选中 Bold；Lead 随后会把 token 调到官方实测 17sp/24）。
- 选中下划线：30×3dp（`HomeTabIndicatorWidth/Height`）、2dp 圆角、`accent` 底、距行底 1dp。
- 右侧固定两个 24dp 图标（点击区 40dp）：`Menu`=频道编辑、`Headphones`=听；听在朗读进行中用 `accent` 着色。
- Tab 左右各 10dp 渐变遮罩（`Brush.horizontalGradient` 到透明），只在可滚动方向显示，避免文字压右侧图标。
- 频道编辑：`ModalBottomSheet` 弹层，4 行「Tab 名 + Switch」；至少保留 1 个可见 Tab（最后一个不可关）。显隐存到应用偏好 `homeCategoryHiddenTabs`（StringSet，键暂时定义在 HomeScreen.kt，因为设置页不在本任务文件范围）。排序未做（spec 标注排序可选）。
- 听：接入项目阅读播放器：无会话时从当前推荐流第一条可朗读内容起 `ReadingStartRequest`（队列走 `ReadingQueueSourceRegistry.queueStartingAt`，播放偏好/倍速读设置）；已有会话时切播放/暂停；桌面/native 等不支持平台提示「当前平台暂不支持朗读」；空流提示「暂无可朗读内容」；启动异常提示「朗读启动失败」。
- Tab 行下不再有分割线（官方无）。

## 3. 信息流卡片（`ContentFeedCard`）

扁平无底色/描边，与页面同色，左右 16dp（`FeedCardHorizontalGutter`）。**卡片之间不画分割线**（官方靠间距分块；首轮真机反馈后已移除 `ZhihuListDivider`，同时删掉 `showDivider` 参数）。自上而下：

1. 可选关注来源行：高 22dp、13sp 次要色（`Feed.sourceLabel`，项目已有字段，无字段不渲染）。
2. 标题：18sp Bold 2 行（`ZhihuTypography.FeedTitle`，行高 25sp）。
3. 6dp → 作者行：20dp 头像（`FeedAvatarSize`）+ 8dp + 作者名 15sp Medium（`FeedAuthorName`，主色）+ 8dp + 关注数 chip（**1dp `divider` 描边、6dp 圆角、左右 6dp、13sp 次要色、无底色**；仅 `followersCount > 0` 时显示）。
4. 4dp → 摘要：15sp/22sp 3 行（`FeedSummary`，次要色，HTML 强调沿用 `parseEmphasizedHtmlTextWithTheme`）。
5. 8dp → 横滑图（有图时）：高 138dp（`FeedImageHeight`）、单页宽 184dp（4:3，`PageSize.Fixed`）、圆角 6dp（`ZhihuShapeTokens.Thumbnail`）、间距 4dp（`ZhihuSpacing.ExtraSmall`），右下角保留页圆点指示。
6. 8dp → 操作行：24dp 高（`FeedActionRowHeight`）；**固定三组（计数 0 也显示）**：
   - 赞同：官方轮廓圆角三角，用项目已有 Compose 资源 `ic_vote_up_24dp`（`painterResource`）；
   - 收藏：`Icons.Outlined.StarBorder`（星形轮廓）；
   - 评论：`Icons.Outlined.ChatBubbleOutline`（气泡轮廓）；
   - 图标 20dp（`FeedActionIconSize`）+ 4dp + 13sp 计数（`FeedMeta`、三级色）；三组图标起点对齐官方屏幕 x=16/86/160dp（前两组固定 70dp / 74dp 槽位）；右端 24dp ✕ 不感兴趣（16dp Close 图标，`content_feed_card_dismiss:<key>`）。
7. 「N 万人正在热议」banner：**未实现**（项目 `Feed`/`FeedDisplayItem` 没有任何热议话题字段，不能伪造数据，见「未完成项」）。
8. 横滑图取图顺序沿用原逻辑：回答多图 > 回答单图 > 想法图集。

保留的能力：整卡点击/长按（长按弹既有屏蔽菜单：按关键词/屏蔽用户/屏蔽提问者/屏蔽话题）、✕ 不感兴趣、`showFeedThumbnail` 开关、下拉刷新、置顶刷新 FAB、骨架屏。

## 4. 热榜 Tab

内嵌 `HotListScreen`（`innerPadding=0`，顶部让出搜索区、底部让出底栏与朗读悬浮条，`isActive` 只在选中时 true，透传 `scrollToTopTrigger` 支持「再点一次回到顶部/刷新」）。这一版复用其既有 `FeedCard` 卡片与刷新 FAB；按 Lead 指示不修改 `FeedCard` 全局默认外观。

## 5. 骨架屏

`ZhihuSkeletonCard` 去掉 `Card` 容器，改为与 `ContentFeedCard` 逐段同构的扁平占位：标题 2 条（18dp 高）→ 20dp 圆头像 + 作者条 → 摘要 3 条 → 138dp 图片块 → 3 组操作条；内部只负责垂直节奏，左右 16dp 由 `PaginatedList` 统一给出，避免双内边距。

## 6. Lead 真机首轮反馈的落实（2026-11-09 第二轮）

1. **搜索行 48dp → 官方应为 40dp**：代码本来就是 40dp（`ZhihuSizes.HomeSearchRowHeight`）。真机 126px=48dp 的来源是 **Compose 无障碍触摸扩展**：`SemanticsNode.touchBoundsInRoot` 对可点击节点按最小 48dp 触摸目标扩展（`getUseMinimumTouchTarget`），uiautomator 报的是扩展后的 bounds；官方 App 是 View 体系，报的是真实布局 bounds。数值可自证：我们的搜索胶囊真实布局 = 状态栏 28dp + 40dp（74–179px），触摸扩展后 [64,190]（48dp）；Tab 行真实布局 179–284px，单 Tab 触摸扩展后 169–295px。像素测量截图里应为 105px。**无需改高度**，请用像素测量复核（不要用 uiautomator bounds 比官方截图）。
2. **Tab 字号 16sp → 官方约 17sp**：`TabTitle` token 由 Lead 调整为 17sp/24 后自动生效（代码已只用 token，无裸字号）。
3. **操作行固定三组 + 官方图标**：已实现（见 §3-6），计数为 0 也显示；赞同改用 `ic_vote_up_24dp`。
4. **搜索按钮填充色**：已改用新 token `contentFirstColors.accentFill`（深浅均 `#1772F6`）。
5. **胶囊描边**：由 1.5dp 改为 1dp。
6. **卡片间分割线**：已移除（`ContentFeedCard` 不再画 `ZhihuListDivider`；`PaginatedList` 本身不画卡片分割线）。
7. **关注数 chip**：`Feed.Person.followersCount` 字段存在（`@JsonNames("followerCount")`），已接上；接口不返回时按 0 不显示，不伪造。

## 7. 未完成项 / 取舍

1. **「N 万人正在热议」banner 未实现**：`FeedDisplayItem`/`Feed` 没有热议话题字段（`grep 热议|debate|hotTopic` 无结果），需要新建数据/请求，超出「不伪造数据、不新增请求」边界。若后续拿到字段，可在操作行下加 10dp 间距 + 30.5dp 高 banner。
2. **关注来源行无图标**：spec §2.3-8 提到 14dp 图标，但官方图标随来源类型变化且模型无来源类型字段，先只渲染 13sp 文本（22dp 行高），避免用错图标。
3. **搜索按钮「聚焦输入」未实现**：`Search` 目的地没有 focus 参数，`SearchScreen` 不在本任务文件范围；胶囊与按钮目前都只导航到搜索页。
4. **游客态热词为空**：沿用原逻辑——仅登录态拉取热搜词（`ZHIHU_HOT_SEARCH_URL`），游客显示「搜索知乎内容」占位；官方游客也显示热词，如需对齐可放开该判断（接口失败已被 catch）。
5. **热榜 Tab 卡片样式**：复用 `HotListScreen` 的 `FeedCard`（项目既有卡片，页面/卡片同色后不会再出现「浮层卡」观感），与推荐流的 `ContentFeedCard` 版式不同；后续若需统一再单独排期。
6. **频道排序未做**（spec 标可选）；显隐偏好键 `homeCategoryHiddenTabs` 暂放 HomeScreen.kt，待设置页纳入范围后可移到设置模块。
7. **`HOME_NOTIFICATION_*` tag 常量保留未用**：等 Lead 更新 instrument test 后删除（消息入口已移除，对应 UI 不存在）。

## 8. 需 Lead 真机验证点

1. 深色/浅色：搜索胶囊 40dp、1dp 蓝描边、热词/热标/竖线/56×30 蓝色按钮（`#1772F6` 填充）的相对位置与 `crop_official_top.jpg` 对齐；胶囊右内边距 5dp。验证高度请用截图像素行（应为 105px），不要用 uiautomator bounds（会按 48dp 触摸目标扩展）。
2. Tab 行：4 项宽度 50dp、选中下划线 30×3dp 距底 1dp；右侧两个 24dp 图标位置（`home_tab_edit` / `home_tab_listen`）；`TabTitle` 17sp 生效后对比「关注」文字宽度。
3. 卡片：标题 18sp Bold 2 行、作者行（20dp 头像 + 关注数 chip）、摘要 3 行、138dp/184dp 横滑图与 4dp 间距；操作行固定三组且计数 0 也显示、图标为官方轮廓（三角/星/气泡）、三组起点约 16/86/160dp、✕ 位置；卡片之间无分割线。
4. 点 `home_account_avatar`：登录态进账号页、游客态拉起登录；点 `home_tab_edit` 弹频道弹层，关掉「推荐」后首页默认落到第一个可见 Tab，关掉最后一个应无效。
5. 点 `home_tab_listen`（真机支持朗读）：应从推荐流起播并弹出朗读条；再点一次暂停/继续。
6. `home_category_tab:热榜` 内嵌热榜列表：滚动、下拉刷新、再点一次回顶。
7. 消息入口：搜索区不再有消息方框/未读角标（底栏消息 tab 与角标由 Lead 负责）；`ReadingPlayerUiInstrumentedTest` 点击 `home_search_button` 仍应进入搜索页。


## 6b. Lead 像素复验第二轮（2026-11-09 第三轮）

1. **搜索胶囊填充**：官方深色胶囊内部采样即页面色 `#191B1F`，只有描边可见。已移除胶囊的 `background`/`clip`（不再用 `functionalSurface`，也不再裁剪子元素），现在胶囊只有 1.5dp 描边，内部与页面同色；顺带避免胶囊圆角裁剪吃掉右侧搜索按钮的上下边缘。
2. **胶囊描边**：1dp → **1.5dp**，颜色 `contentFirstColors.accentFill`（深浅色均 `#1772F6`）。
3. **搜索按钮高度**：按钮视觉尺寸绑定 `ZhihuSizes.HomeSearchButtonHeight`（30dp），Box 内部无 padding；此前胶囊的 `clip(pillShape)` 会在右侧圆角区裁掉按钮上下边缘，已随第 1 点移除。当前树重新构建后按钮蓝色区域应为 30dp（≈79px @2.625x）。
4. **Tab 下划线颜色**：`#558EFF` → `MaterialTheme.colorScheme.onSurface`（深色白、浅色近黑）。
5. **Tab 未选中文字颜色**：`secondaryText` → `MaterialTheme.colorScheme.onSurface`，与选中态同色，只靠字重区分（选中 Bold / 未选中 Normal）。
6. **Tab 右侧频道编辑 / 听图标**：同步改为 `onSurface`（朗读进行中「听」仍用 `accent` 作为状态色）。
