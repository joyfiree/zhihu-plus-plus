# 「我的」页现代 UI 重构规格（v1）

> 现状：`shared/src/commonMain/kotlin/com/github/zly2006/zhihu/ui/AccountSettingScreen.kt`（695 行）是 M3 卡片堆叠：大块账号卡 + 独立「发想法」卡 + 收藏夹/关注订阅分组 + 搜索框 + 多组设置卡。
> 用户反馈：「太丑了，按新时代的 UI 设计标准，重新调整这个页面的逻辑交互和布局」。
> 目标：保持全部现有能力与 testTag 契约，重做信息架构、层级、间距与视觉，做出干净、现代、有主次的「我的」页。
> 参考当前截图：`reports/ui-redesign/screenshots/account_before.jpg`（深色）。设计令牌见 `reports/official-align/target-spec.md` §1；官方 App 的「我的」页当前无法截取（设备侧有安全验证），按现代客户端设计规范自行设计即可。

## 1. 页面结构（从上到下）

1. **可折叠 Hero 区**（LazyColumn 首项 / 或滚动联动的头部）
   - 背景：`contentFirstColors.functionalSurface` → `page` 的竖向轻渐变（深色 #212429→#191B1F，浅色 #F8F8FA→#FFFFFF），高度约 172dp + statusBars inset。
   - 右上角：40dp 图标按钮「扫码」（仅登录且有扫码能力时显示，保留 `ACCOUNT_SETTINGS_SCAN_TAG`）+ 40dp「更多」（⋮，点击弹 DropdownMenu：`身份管理`/`退出登录`（error 色）；退出登录保留现有确认弹窗逻辑）。
   - 主体：64dp 圆形头像（2dp 页面色描边）、昵称 22sp Bold（`ACCOUNT_SETTINGS_PROFILE_NAME_TAG`，1 行省略）、昵称下一行 `@urlToken` 13sp 三级色（为空不显示）。
   - 头像/昵称行整体点击进个人主页（保留现有 `Person(...)` 导航与 `ACCOUNT_SETTINGS_PROFILE_HEADER_TAG`）。
   - 昵称下方一行：主 pill 按钮「查看主页 ›」（accent 12% 底 + accent 文字，点击同上进主页）。
   - **滚动行为**：页面滚动后 Hero 收起，顶部浮出 48dp 小标题栏（左「我的」17sp Medium；右保留扫码/更多按钮，背景 `page` + 底部分割线）。用 scrollState/`derivedStateOf` + AnimatedVisibility 实现，不要固定占位。
   - 未登录态：Hero 显示默认头像 + 昵称「未登录」+ 主按钮「登录知乎」（保留 `ACCOUNT_SETTINGS_LOGIN_ITEM_TAG`，点击 `requestLoginNavigation()`），不显示扫码/更多/退出。
2. **快捷入口卡**（Hero 下方，16dp 外边距、`surfaceContainer` 底、16dp 圆角，高约 84dp）
   - 一行 4 列等分：发想法（`ACCOUNT_SETTINGS_WRITE_PIN_TAG` → WritePin）、收藏夹（`ACCOUNT_SETTINGS_SHORTCUT_COLLECTIONS_TAG` → Collections(urlToken)）、关注订阅（`ACCOUNT_SETTINGS_SHORTCUT_SUBSCRIPTIONS_TAG` → Person jumpTo "关注订阅"）、浏览历史（`ACCOUNT_SETTINGS_SHORTCUT_HISTORY_TAG` → OnlineHistory）。
   - 每项：24dp 图标（accent）+ 12sp 文字，垂直居中；点击水波纹限制在卡片圆角内。
   - 未登录态：整卡不显示（登录相关行由 Hero 承担）。
3. **设置搜索**（保留 `ACCOUNT_SETTINGS_SEARCH_TAG`，点击进 `Account.SettingsSearch`）
   - 胶囊搜索行：高 44dp、`surfaceContainer` 底、圆角 22dp、左搜索图标 +「搜索设置项」15sp 三级色。
4. **设置分组**（保留全部现有入口/testTag/点击行为，重新分组与视觉）
   - 「内容与推荐」：推荐系统与内容过滤（`ACCOUNT_SETTINGS_RECOMMEND_TAG`）
   - 「外观与阅读」：外观与阅读体验（`ACCOUNT_SETTINGS_APPEARANCE_TAG`）、朗读与播放（`ACCOUNT_SETTINGS_READING_TAG`）
   - 「账号与安全」：身份管理（`ACCOUNT_SETTINGS_IDENTITY_MANAGEMENT_TAG`）
   - 「系统」：系统与更新（`ACCOUNT_SETTINGS_SYSTEM_TAG`）、开发者选项（`ACCOUNT_SETTINGS_DEVELOPER_TAG`，保留「连续点击版本号 5 次开启」逻辑与权限）、开源许可（`ACCOUNT_SETTINGS_LICENSES_TAG`）
   - 行样式（新）：整块白/暗卡（16dp 圆角），行高 60dp，左 36dp 图标底座（28dp 图标，`functionalSurface`/`primary` 12% 底、8dp 圆角）+ 标题 15sp Medium + 11sp 三级色副标题（如需）+ 右 chevron；行间 1px 发丝线、左缩进 16dp；分组标题 13sp 三级色、8dp 上间距。
   - 分组顺序与是否合并由实现者按信息架构决定，但所有入口必须可达。
5. **页脚**：版本号行（`版本 0.31.1 (750)` 形式，保留连续点击开发者模式逻辑，可复用保留 `ACCOUNT_SETTINGS_DEVELOPER_TAG` 所在行）、检查更新入口（走 `rememberSystemUpdateState` 现有逻辑），底部 32dp 留白 + navigationBarsPadding。

## 2. 交互与状态

- 保留函数签名参数：`innerPadding`、`unreadCount`、`showUnreadBadge`、`onDismissRequest`、`refreshAccountProfileOnEnter`、`isActive`（桌面/账号面板复用，不能删）。
- 保留进入页面时刷新 profile 的逻辑（`refreshAccountProfileOnEnter`）与错误提示。
- 保留 `pageTurnViewportWithGuide` 翻页手势（滚动容器上挂载，不破坏）。
- 保留退出登录确认弹窗文案与行为；扫码行为不变。
- 未登录：Hero 登录入口 + 设置项仍可用（现有行为）。
- 深/浅色、桌面/Android 共用；颜色全部用 `contentFirstColors` / `MaterialTheme` / `zhihuSurfaces` token，字号用 `ZhihuTypography`（ContentTitle/FeedTitle/FeedAuthorName/FeedMeta/AnswerBody 等），尺寸用 `ZhihuSpacing/ZhihuSizes`。禁止裸色值。
- 删除仅 duo3 分支才用的旧快捷块实现（`useDuo3HomeAccount` 分支）与不再需要的私有 composable/常量；保底：如果某 tag/行为在新版无位置，保留该入口在合适分组，不要静默删除能力。
- 代码越短越好：不留两套布局。

## 3. 验证

- `:app:assembleLiteDebug` 通过、`:shared:compileKotlinJvm` 通过、ktlint 本文件 0 告警。
- 不修改 `theme/`、`ui/designsystem/`、`HomeScreen.kt`、`ZhihuMain.kt`、其余页面文件。
- 输出 `reports/ui-redesign/account-page-notes.md`：新结构、保留的入口与 tag、取舍、需 Lead 真机验证点。
