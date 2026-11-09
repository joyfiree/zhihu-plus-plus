# 「我的」页（AccountSettingScreen）现代 UI 重构交付说明

分支 `refactor/md3-ui`。实现按 `reports/ui-redesign/account-page-spec.md`。

## 1. 改动范围

- 只改 `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/ui/AccountSettingScreen.kt`：全量重写为单文件，删除旧 M3 卡片堆叠、旧「发想法」卡、duo3 快捷块分支与连带 import/状态；未新增实现文件。
- 未改 `theme/**`、`ui/designsystem/**`、`ui/components/SettingItem.kt`、`HomeScreen.kt`、`ZhihuMain.kt`、其它页面、`app/src/androidTest/**`。

## 2. 新结构（从上到下）

1. **可折叠 Hero**（`172dp + innerPadding.top`，`functionalSurface → page` 竖向渐变，背景一路铺到状态栏后）
   - 右上角 40dp 圆底按钮：扫码（有扫码能力时，`ACCOUNT_SETTINGS_SCAN_TAG`）、更多（⋮ → DropdownMenu：身份管理 / 退出登录（error 色））。
   - 64dp 头像（2dp `page` 色描边）+ 昵称 22sp Bold 单行省略（`PROFILE_NAME_TAG`）+ `@urlToken` 13sp 三级色（为空不显示）；整行点击进 `Person` 主页（`PROFILE_HEADER_TAG`）。
   - 行下方「查看主页 ›」pill（accent 12% 底 + accent 文字）。
   - 滚动超过 48dp 后 `derivedStateOf` + `AnimatedVisibility` 浮出 48dp 小标题栏：左「我的」17sp Medium，右扫码/更多，背景 `page` + 底部分割线（无固定占位）。
   - 未登录：默认人形头像 + 「未登录」+ 实心 accent「登录知乎」pill（`LOGIN_ITEM_TAG` → `requestLoginNavigation()`），不显示扫码/更多/退出；Hero 行点击也触发登录。
2. **快捷入口卡**：一行 4 列（发想法 / 收藏夹 / 关注订阅 / 浏览历史），16dp 圆角、约 84dp 高、`functionalSurface` 底、24dp accent 图标 + 12sp 文字，水波纹被卡片圆角裁剪；未登录整卡隐藏。
3. **设置搜索胶囊**：44dp 高、22dp 全圆角、`functionalSurface` 底、左搜索图标 + 15sp 三级色「搜索设置项」，整条点击进 `Account.SettingsSearch`（`SEARCH_TAG`）。
4. **设置分组**：每组 = 13sp 三级色组标题（上 16 / 下 8）+ 16dp 圆角整块卡；行高 ≥60dp、左 36dp 图标底座（28dp accent 图标、accent 12% 底、8dp 圆角）+ 15sp Medium 标题 + 11sp 三级色副标题 + 右 chevron；行间 1dp 发丝线（左缩进 16dp）。
   - 内容与推荐：推荐系统与内容过滤
   - 外观与阅读：外观与阅读体验、朗读与播放（平台支持时）
   - 账号与安全（仅登录）：身份管理
   - 系统：系统与更新、开发者选项（`AnimatedVisibility(isDeveloper)`）、开源许可
5. **页脚**：版本号行（`知乎++ 版本 $versionInfo`，combinedClickable：5 连击开启开发者模式、长按复制版本号）+ 链接行「检查更新 / GitHub / AGPL-3.0」；内容底部留白 32dp（含外层 bottom inset）。

滚动容器仍为 `Column + verticalScroll`，`pageTurnViewportWithGuide(pageTurnTarget)` 挂在滚动视口上，`ACCOUNT_SETTINGS_SCROLL_TAG` 位置不变，`performScrollTo` 语义可用。

## 3. 保留的入口与 testTag（18 个常量全部保留）

| 常量 | 新位置 |
|---|---|
| `ACCOUNT_SETTINGS_SCROLL_TAG` | 滚动 `Column` |
| `ACCOUNT_SETTINGS_LOGIN_ITEM_TAG` | 未登录 Hero 的「登录知乎」pill |
| `ACCOUNT_SETTINGS_PROFILE_HEADER_TAG` | Hero 头像+昵称行（点击进主页/未登录时登录） |
| `ACCOUNT_SETTINGS_PROFILE_NAME_TAG` | 昵称文本（未登录为「未登录」） |
| `ACCOUNT_SETTINGS_SCAN_TAG` | Hero 扫码按钮（小标题栏里的扫码按钮不加 tag，避免重复节点） |
| `ACCOUNT_SETTINGS_WRITE_PIN_TAG` | 快捷卡「发想法」→ `WritePin()` |
| `ACCOUNT_SETTINGS_SHORTCUT_COLLECTIONS_TAG` | 快捷卡「收藏夹」→ `Collections(urlToken)` |
| `ACCOUNT_SETTINGS_SHORTCUT_SUBSCRIPTIONS_TAG` | 快捷卡「关注订阅」→ `Person(jumpTo = "关注订阅")` |
| `ACCOUNT_SETTINGS_SHORTCUT_NOTIFICATION_TAG` | 常量保留（页面按 spec 只放 4 列，不再渲染通知快捷块，见 §5） |
| `ACCOUNT_SETTINGS_SHORTCUT_HISTORY_TAG` | 快捷卡「浏览历史」→ `onDismissRequest()` + `onNavigateTopLevel(OnlineHistory)` |
| `ACCOUNT_SETTINGS_APPEARANCE_TAG` | 外观与阅读组 |
| `ACCOUNT_SETTINGS_READING_TAG` | 外观与阅读组（朗读与播放） |
| `ACCOUNT_SETTINGS_RECOMMEND_TAG` | 内容与推荐组 |
| `ACCOUNT_SETTINGS_SEARCH_TAG` | 搜索胶囊 |
| `ACCOUNT_SETTINGS_SYSTEM_TAG` | 系统组 |
| `ACCOUNT_SETTINGS_DEVELOPER_TAG` | 系统组「开发者选项」（连续点击版本号开启后出现；androidTest `secondarySettingsBackRestoresParent` 依赖该 tag + `performScrollTo`） |
| `ACCOUNT_SETTINGS_LICENSES_TAG` | 系统组「开源许可」 |
| `ACCOUNT_SETTINGS_IDENTITY_MANAGEMENT_TAG` | 账号与安全组 |

## 4. 保留的行为/参数

- 函数签名与参数：`innerPadding`、`unreadCount`、`showUnreadBadge`、`onDismissRequest`、`refreshAccountProfileOnEnter`、`isActive` 全部保留。
- 进入页面刷新 profile（`accountStore.client.refreshAndSaveProfile()` + 失败提示）逻辑不变。
- `pageTurnViewportWithGuide` / `rememberPageTurnTarget` 保留，退出登录弹窗打开时禁用翻页。
- 退出登录确认弹窗文案、多账号提示、`accountStore.clear()` 行为不变；入口移到 Hero「更多」菜单（error 色）。
- 扫码行为、身份管理跳转、开发者模式 5 连击 + `settings.putBoolean("developer", …)`、长按复制版本号、`rememberSystemUpdateState` 的「发现新版本 / 检查更新失败」提示均保留。
- 新增「检查更新」页脚入口：点击走 `rememberSystemUpdateChecker().check()`，按钮文案跟随状态（检查更新 / 检查中… / 已是最新版本 / 发现新版本 / 检查更新失败），原有 `LaunchedEffect(updateState)` 提示逻辑未改。

## 5. 取舍与说明

- **duo3 快捷块删除**：`duo3_home_account` 分支（收藏夹/关注订阅/通知/历史 4 块）按 spec 移除，统一为一套快捷卡；`duo3_home_account` 偏好仍由 `ZhihuMain`/外观设置使用，本页不再读取。
- **通知快捷入口**：spec 的快捷卡固定 4 列，底栏已有「消息」入口；默认配置下旧页面（duo3=false）本来也不渲染该快捷块。`ACCOUNT_SETTINGS_SHORTCUT_NOTIFICATION_TAG` 常量保留，但页面不再实例化该入口；`unreadCount`/`showUnreadBadge` 参数为桌面端/账号面板复用而保留，当前页面无使用点。若需要恢复带未读角标的通知入口，建议由调用方传入并在 Hero/快捷卡外提供位置。
- **GitHub / AGPL 链接**：spec 页脚未列，但属于既有能力，放在页脚链接行保留（`openSystemUrl`）。
- **颜色**：正文/icon/分割线/卡片底色全部用 `contentFirstColors`（`page`、`primaryText`、`secondaryText`、`tertiaryText`、`accent`、`divider`、`functionalSurface`，其中 `functionalSurface` 即 spec 的 `surfaceContainer` 官方色值），字号用 `ZhihuTypography`，间距/图标尺寸用 `ZhihuSpacing/ZhihuSizes`，圆角用 `MaterialTheme.shapes.large`（16dp）与 `ZhihuShapeTokens.Pill`。与 `HomeScreen` 的内容优先页面一致，因此自定义背景色/动态取色不会改变本页基色；若要求本页跟随自定义背景，需要改用 `zhihuSurfaces`/`MaterialTheme` 色板。
- **inset**：Hero 背景铺到状态栏后，内容用 `innerPadding.calculateTopPadding()` 作为状态栏高度（Android edge-to-edge 下即 statusBar；桌面为 0）；底部用 `innerPadding.calculateBottomPadding() + 32dp` 作为滚动内容留白，未再叠加 `navigationBarsPadding()`（外层 Scaffold 的 bottom inset 已含系统导航区，避免双重留白）；水平 inset 未使用（Android/桌面返回值为 0）。
- **组件边界**：`SettingItem`/`SettingItemGroup` 的视觉（40dp 底座、M3 bodyLarge 行）与 spec 新行样式不一致，且本任务不允许改公共组件，故在页面内实现私有 `SettingsSection`/`SettingsRow`/`SettingsDivider`，未复制公共组件的 group 逻辑；未新增抽象层。
- **代码规模**：删除旧 duo3/旧分组代码后仍新增了 Hero、快捷卡、三组私有行组件的实现；全部为页面私有、无死代码。原文件 695 行 → 现 922 行（净 +227 行，主要是新的分区行组件与页脚；diff 为 +696/−469，因为旧布局整体重排）。

## 6. 需 Lead 真机验证点

1. 深/浅色：Hero 渐变（深 #212429→#191B1F，浅 #F8F8FA→#FFFFFF）、状态栏图标可读性、卡片/发丝线对比。
2. 滚动：超过 48dp 后 48dp 小标题栏浮出（左右元素对齐、底部发丝线、无固定占位），继续滚动时列表内容从其下方正常穿过；翻页手势（引导虚线、翻页）仍工作。
3. 系统大字号 1.15x：Hero 顶部按钮与昵称行不重叠（内容底部对齐，172dp 高度余量约 2dp，属最小余量，重点看）。
4. 窄屏 411dp / 横屏 list-detail 窄列表：快捷卡 4 列、设置行标题不截断（标题 `maxLines = 1`，超长会省略）。
5. 未登录态：默认头像 + 未登录 + 登录 pill，点击跳登录页；设置分组仍可进入；无扫码/更多。
6. 「更多」菜单：身份管理跳转、退出登录 → 确认弹窗（多账号文案）→ 退出。
7. 页脚：5 连击版本号出开发者选项、长按复制版本号、检查更新按钮文案/失败提示。
8. 横屏设置栈返回（`secondarySettingsBackRestoresParent`）：`accountSettings.developer` 行可 `performScrollTo` 并进入开发者设置页。

## 7. 已知遗留（超出本任务可改文件范围）

- `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/ui/subscreens/AppearanceSettingsScreen.kt` 中的 `shouldShowAccountHistoryShortcut()` 在本页移除 duo3 快捷块后已无调用方（死代码），该文件不在允许修改范围，需后续清理。
- `ktlintCommonMainSourceSetCheck` 任务当前整体失败，告警全部位于 `HomeScreen.kt`（import 顺序、未使用 import、多余空行）与 `ZhihuMain.kt`（未使用 import、多余空行），应为并行任务 WIP 文件；本文件在报告中 0 条告警。

## 8. 自检结果

- `./gradlew :shared:compileKotlinJvm`：BUILD SUCCESSFUL（本文件无编译告警）。
- `./gradlew :app:assembleLiteDebug`：BUILD SUCCESSFUL。
- 本文件 ktlint：0 告警（`ktlintCommonMainSourceSetCheck` 报告的 5 条均在他人 WIP 文件）。
- 未执行 adb/真机/instrument test/git 操作（按任务约束，真机截图验收由 Lead 执行）。
