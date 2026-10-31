# Zhihu++ Material Design 3（MD3）重构优化计划

| 项 | 内容 |
| --- | --- |
| 文档版本 | v1.0（2026-10-31） |
| 执行者 | AI Agent（Claude Code / Codex 等）+ 人工 review |
| 主要战场 | `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/`（约 90% 改动）；`app/` 仅平台适配层 |
| 配套文档 | `CLAUDE.md`（执行纪律）、`docs/ai-ui-design-guide.md`（设置项-影响面权威地图） |

---

## 1. 文档目的与使用方式（执行者必读）

本计划把「MD3 视觉 + 动效 + 触觉 + 手感 + 配色 + 体验」的整体重构拆成**可独立验证的原子任务**。执行规则：

1. **按任务 ID 顺序或按依赖图（第 6 章）领取任务**，一次只做一个任务，一个任务对应一个可独立编译、可独立验收的提交/PR。
2. 动手前先读任务「涉及文件」列出的源码，并用 `rg` 复核（命令见 `docs/ai-ui-design-guide.md` 末尾）；本计划中的行号是 2026-10 快照，**以实际代码为准**。
3. 每个任务必须走完整执行循环（见第 9.1 节），UI 改动必须按 `CLAUDE.md`「UI 调试强制清单」用 AVD 真机验证。
4. 遇到与既有产品契约冲突（用户设置语义、testTag、返回栈行为）时，**停下来记录阻塞等待反馈，不得擅自简化**。
5. 任务完成后在本文件对应任务条目标注 `[x]` 与完成日期，并在 PR 描述附最终效果截图（真实运行来源，见 `CLAUDE.md` PR 规则）。

---

> **执行状态（2026-10-31 第二轮）**：本机已补齐构建 + 本地 AVD 能力，`assembleLiteDebug`/`assembleLiteDebugAndroidTest`/`ktlintCheck`/`:shared:jvmTest` 与定向 instrument 回归均通过。`[x]` 代码完成且有测试证据；`[~]` 代码完成、剩余验收依赖真机/账号/人工 review；`[!]` 阻塞。逐项证据见 `docs/md3-refactor/status.md`，终版截图与性能口径见 `docs/md3-refactor/final-report.md`，对比度红→绿证据见 `docs/md3-refactor/contrast-report.md`，硬编码收敛见 `docs/md3-refactor/audit.md`。

## 2. 现状基线（2026-10 调研事实，改造起点）

执行任何任务前，先了解现状，避免重复造轮子或误判债务：

### 2.1 主题 / 配色（基础好，但有结构性债务）

- 已是 **MaterialTheme + material-kolor 双轨**：`shared/.../theme/Theme.kt` 中，Android 12+ 走系统 Monet（`platformDynamicColorScheme`），否则 `dynamicColorScheme(seedColor)`（默认知乎蓝 `0xFF2196F3`）。
- 主题状态在 `ThemeManager`（全局单例 mutableStateOf，非 CompositionLocal）；持久化走 DataStore（`themeMode` / `useDynamicColor` / `customThemeColor` / `backgroundColorLight` / `backgroundColorDark`）。
- **债务 1**：`Theme.kt` L47-81 用 `copy(background=, surface=)` 强制覆盖用户自定义背景色，但 **surfaceContainer 系列、surfaceDim/surfaceBright 未同步**，破坏 MD3 surface 层级语义。
- **债务 2**：`theme/Color.kt` 兜底色是模板 Purple80/40 系列，与知乎蓝种子完全无关（仅异常路径才命中，但仍是隐患）。
- **债务 3**：`theme/Type.kt` 仅定制 `bodyLarge`，其余全默认；**Shapes 完全未定制**（MaterialTheme 未传 shapes 参数）。
- **债务 4**：组件内仍有硬编码 RGB：`ArticleScreen.kt` 点赞橙 `0xFFF57C00`、TTS 绿 `0xFF4CAF50`（已用 `harmonize(primary)` 缓解，但未走色彩角色）。

### 2.2 组件与导航（骨架标准件为主）

- 主壳 `ui/ZhihuMain.kt`：Scaffold + MD3 `NavigationBar`（`containerColor = surfaceContainerHigh`）+ `HorizontalPager` 承载主 tab + navigation-compose typed route。
- `ui/components/` 下约 36 个自定义组件；自绘（Canvas）极少，但**手势交互层大量手写**（回答切换 overscroll、拖动 FAB、底栏显隐 NestedScrollConnection）。
- material3 版本被 force 为 `org.jetbrains.compose.material3:material3:1.10.0-alpha05`（app 与 shared 一致），**不得混用 androidx material3**。

### 2.3 动效（无规范，参数裸写）

- `AnimatedVisibility` 约 35 处、`animate*AsState` 约 10 处、`AnimatedContent` 1 处、`Crossfade`/`updateTransition` 0 处。
- 导航转场：NavHost 进出均为 `slideIn/OutHorizontally(tween(300))`，底栏显隐 `tween(200)`；回答切换四向转场（`app/.../ZhihuMainAndroidContent.kt`）也是 `tween(300)`。
- 列表 item 动画统一 `spring(StiffnessMediumLow)`（`PaginatedList.kt` 等）；自定义 easing 仅 1 处。
- **无任何 Motion token / 常量类**，时长裸写 200/300ms。

### 2.4 触觉反馈（局部覆盖，缺统一策略）

- 统一走 `LocalHapticFeedback`（可跨平台），已有：文章页 Confirm ×2、评论/正文长按 LongPress、回答切换到阈值 LongPress、FAB/翻页按钮长按 LongPress。
- **空白**：底栏 NavigationBarItem 切换、Pager 切页、PullToRefresh 触发瞬间、Switch 切换、FeedCard 点赞收藏确认等。
- 特殊点：`UiComponentSupport.kt` L292 用 `NoopHapticFeedback` 屏蔽图片查看器自带震动（有注释说明，保留）。

### 2.5 手势与手感（核心差异化能力，已有实现）

- 下拉刷新已是 MD3 `PullToRefreshBox`（`components/FeedPullToRefresh.kt`，13 个列表页复用）。
- 回答切换：竖向 `AnswerVerticalOverscroll`（tanh 阻尼 + 阈值，受 `answerSwitchSensitivity` 调制）、横向 `AnswerHorizontalPager`；双击动作 `AnswerDoubleTapAction`（7 种）。
- `enable_predictive_back` 设置 key 已存在但**无运行时读取点**（预测性返回未接通）。

### 2.6 可复用验收基建

- androidTest 27 个文件：`ComposeScreenTestHost`（任意 composable 挂载 + `RecordingNavigator`）、`ZhihuMockApi`、稳定 testTag 约定（`nav_tab_*` 等）。
- 本地 AVD `Medium_Phone_2`；lite 包名 `com.github.zly2006.zhplus.lite`。

---

## 3. 目标与范围界定

### 3.1 总体目标

1. **视觉**：完整落地 MD3 设计语言——色彩角色全覆盖、完整 type scale、shape 体系、surface 层级与海拔语义正确。
2. **动效**：建立统一 Motion token 体系（时长/缓动/弹簧分档），导航与组件动效符合 MD3 强调曲线与「可中断、连贯」原则。
3. **触觉**：建立统一触觉策略（语义分级 + 用户总开关），交互全覆盖关键触点，克制不滥用。
4. **手感**：手势（回答切换、下拉刷新、Pager、双击）阻尼/阈值/转场连贯性达到「旗舰应用」水准；接通预测性返回。
5. **体验**：无障碍（TalkBack、对比度、触控目标）、性能（动画不掉帧）、文档同步，且**不引入任何功能回归**。

### 3.2 范围内

- `shared/commonMain` 的 theme、ui、components 全部共享 UI；
- `app/` 的 Android 适配层（转场 adapter、系统栏、预测性返回、manifest）；
- Android 优先验收；Desktop/macOS 只保证**可编译、不回归**（expect/actual 同步变更，见 CLAUDE.md KMP 原子提交规则）；
- lite 与 full 两个 variant。

### 3.3 范围外（明确不做）

- WebView 正文渲染路径（废弃路径，不接收任何新功能，见 CLAUDE.md）；
- 桌面端/macOS 端的视觉专项打磨（仅保证编译与行为不回归）；
- 信息架构调整（导航结构、底栏项集合、页面拆并——除非任务明确要求）；
- 更换 material3 依赖版本或引入 androidx material3 混用；
- 本计划不新增业务功能；新增设置项仅限触觉开关等体验项。

### 3.4 硬性约束

- **既有偏好语义不可破坏**：`backgroundColorLight/Dark`、`answerSwitchSensitivity`、Duo3 系列开关、`disableBottomSheetRoundedCorners` 等语义保持兼容（增强可以，改语义不可以）。
- **testTag 稳定性**：`nav_tab_*` 等既有 tag 不得变更，UI 测试依赖它们。
- **体积约束**：Lite 版 < 4 MB 是产品卖点，不引入大型新依赖（motion/haptic 均用现有 Compose API 实现）。

---

## 4. 目标设计规范基线（Token 体系定义）

所有 token 集中新建于 `shared/.../ui/designsystem/`（新建目录，避免与 theme/ 历史包袱混淆）：

### 4.1 配色规范

- 唯一色彩来源 = `MaterialTheme.colorScheme`；组件内**禁止**出现字面量 `Color(0xFF...)`（白名单例外：主题种子默认值、ColorPicker 取色盘）。
- 功能强调色（点赞橙、TTS 绿、收藏橙）建模为 **extended color**：`@Immutable data class ExtendedColors(like, favorite, tts: Color)`，由 `ZhihuTheme` 内基于种子色 `harmonize()` 推导并通过 `LocalExtendedColors` 提供。
- surface 层级：`surface` / `surfaceContainerLowest…Highest` / `surfaceDim` / `surfaceBright` 全部参与「自定义背景色」联动（见 T1.1）。
- 深色模式正文对比度 ≥ 4.5:1（body/title 级文本），辅助文本 ≥ 3:1（WCAG AA）。

### 4.2 排版规范（Type.kt 重写）

对齐 MD3 type scale（默认值即 Material 默认，按需微调但保持档位结构）：

| 角色 | 用途（本项目映射） |
| --- | --- |
| headlineSmall | 文章页大标题、LargeTopAppBar 折叠态标题 |
| titleLarge | 页面主标题、设置组标题 |
| titleMedium | 卡片标题、列表项主文本 |
| titleSmall | 卡片次要标题、BottomSheet 标题 |
| bodyLarge | 正文（阅读字号仍由 `contentFontSize` 等偏好驱动，不受此重构影响） |
| bodyMedium | 卡片摘要、评论正文 |
| bodySmall | 时间戳、元信息 |
| labelLarge | 按钮、开关标签 |
| labelMedium | 底栏标签、徽章 |
| labelSmall | 辅助标签 |

### 4.3 形状规范（Shapes 首次接入）

```kotlin
val ZhihuShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // 输入框内元素、小标签
    small = RoundedCornerShape(8.dp),        // 按钮、菜单项
    medium = RoundedCornerShape(12.dp),      // 小卡片、文本域
    large = RoundedCornerShape(16.dp),       // FeedCard、中型弹层
    extraLarge = RoundedCornerShape(28.dp),  // BottomSheet、Dialog 顶部
)
```

`MyModalBottomSheet` 必须继续尊重 `disableBottomSheetRoundedCorners` 偏好（关闭时覆写为直角）。

### 4.4 动效规范（MotionTokens，新建 `Motion.kt`）

基于 MD3 motion 规范定义（Compose 实现）：

```kotlin
object Motion {
    // 时长（ms）
    const val DURATION_SHORT = 200    // 微交互：开关、淡入淡出、图标态切换
    const val DURATION_MEDIUM = 300   // 导航转场、BottomSheet
    const val DURATION_LONG = 450     // 全屏转场、大面积形变
    // 缓动（MD3 emphasized 家族）
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)   // 进入
    val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)   // 退出
    val StandardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    // 弹簧档位
    fun <T> springMedium() = spring<T>(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = null)  // 列表 item、位置动画
    fun <T> springSnappy() = spring<T>(stiffness = Spring.StiffnessMedium)                                // 快速回弹、overscroll
}
```

迁移规则：`tween(200)` → `tween(Motion.DURATION_SHORT, easing = …)`；`tween(300)`（进入）→ `tween(Motion.DURATION_MEDIUM, easing = Motion.EmphasizedDecelerateEasing)`；退出用 EmphasizedAccelerate。overscroll/回弹类一律 spring 档位，不用 tween。

### 4.5 触觉规范（HapticPolicy，新建 `Haptics.kt`）

| 语义 | 触发点 | Compose HapticFeedbackType |
| --- | --- | --- |
| Confirm（确认性操作成功） | 点赞成功、收藏成功、Switch 切换、复制成功 | `Confirm` |
| Tick（轻量档位变化） | 底栏 tab 切换、Pager 切页、字号调节步进 | Android actual 扩展（`SEGMENT_TICK` 类常量，低版本回退 Confirm）；common 层定义为语义函数 |
| GestureThreshold（手势越阈） | 回答切换到达阈值、下拉刷新触发瞬间 | `LongPress`（现状已如此） |
| LongPress（长按菜单） | 长按评论/正文/图片 | `LongPress`（现状已如此） |

- 用户总开关 `hapticFeedbackEnabled`（默认开启；关闭后所有触觉静音，`NoopHapticFeedback` 屏蔽点除外——本就无震动）。
- 原则：**克制**。每个触点只震一次，连续滚动手势不重复震动；震动与动画确认同帧触发。

---

## 5. 分阶段实施计划

> 每个任务字段：**ID｜优先级｜依赖｜涉及文件｜要求｜预期效果｜验收**。行号是快照值，以实际代码为准。

### Phase 0：基线与基建（先行，全部 P0）

- [~] **T0.1 采集视觉基线截图**
  - 依赖：无｜优先级：P0
  - 涉及：`app/src/androidTest/`（临时测试，取图后恢复原状，遵守 CLAUDE.md instrument-test 治理）
  - 要求：本地 AVD `Medium_Phone_2` 上，对 10 个核心页面（主页/关注/热榜/日报/文章页/回答页/评论 BottomSheet/搜索/外观设置/账号页）各采集明暗两套截图，存 `docs/md3-refactor/baseline/<页面>_<light|dark>.png`；同时用现有 mock 环境记录当前导航转场手感描述（文字即可）。
  - 预期效果：后续每个阶段可用「同机同页面」对比验收。
  - 验收：20 张基线图入库；测试源码 `git status` 干净；文档表格记录每张图的采集条件（AVD 型号/API/主题/种子色）。

- [x] **T0.2 建立 designsystem 骨架（token 先行，未接线）**
  - 依赖：无｜优先级：P0
  - 涉及：新建 `shared/.../ui/designsystem/Motion.kt`、`Shape.kt`、`Haptics.kt`、`ExtendedColors.kt`
  - 要求：按第 4 章定义实现四个文件；`Haptics.kt` 提供 `rememberHapticController()`（读 `hapticFeedbackEnabled` 偏好 + 封装 `LocalHapticFeedback`，语义函数 `confirm()/tick()/gestureThreshold()/longPress()`）；本任务只建文件 + 单测（token 值断言），不改任何调用点。
  - 预期效果：后续迁移有唯一目标 API，编译零风险。
  - 验收：`./gradlew :shared:compileKotlinMetadata` + `assembleLiteDebug` 通过；ktlint 通过；无调用点变更。

- [x] **T0.3 硬编码审计清单**
  - 依赖：无｜优先级：P0
  - 涉及：全 commonMain
  - 要求：`rg "Color\(0x" shared/src/commonMain --type kotlin`、`rg "tween\(\d" shared app`、`rg "RoundedCornerShape\(\d+\.dp" shared/src/commonMain` 输出完整清单，整理为 `docs/md3-refactor/audit.md` 表格（文件/行/值/归属任务 ID）。后续任务完成度以此表收敛为准。
  - 验收：清单覆盖三个维度；每个命中项都指派了任务 ID 或标注「白名单豁免（附理由）」。

### Phase 1：配色体系（P0，视觉根基）

- [x] **T1.1 修复自定义背景色的 surface 层级联动**
  - 依赖：T0.2｜优先级：P0
  - 涉及：`shared/.../theme/Theme.kt`、`ThemeManager.kt`、`ThemeSnapshot`（`AndroidThemeSupport.kt`）
  - 要求：用户设置自定义背景色时，除 `background`/`surface` 外，同步推导覆盖 `surfaceContainerLowest/Low/Lowest…Highest`、`surfaceDim`、`surfaceBright`（基于自定义色做同明度层级的 tonal 推导；纯白/纯黑等极端值需保底对比度）。语义不变：用户仍然「只选一个背景色」。
  - 预期效果：开启自定义背景色后，卡片（surfaceContainer）、底栏（surfaceContainerHigh）、弹层背景与用户背景色协调一致，不再出现「灰背景 + 紫卡片」断层。
  - 验收：设置深灰自定义背景 → 主页 FeedCard、底栏、BottomSheet 截图与基线对比层级连续；明暗两套、三种典型背景色（纯白/知乎蓝/深灰）截图留档；已有主题相关 androidTest 全绿。

- [x] **T1.2 功能强调色接入 ExtendedColors**
  - 依赖：T0.2｜优先级：P0
  - 涉及：`ui/ArticleScreen.kt`（L164/L821/L848 一带）、`ui/article/ArticleActionsMenu.kt`（L82）、新建 `designsystem/ExtendedColors.kt`
  - 要求：点赞橙 / 收藏橙 / TTS 绿改为 `LocalExtendedColors.current.like/favorite/tts`（内部仍是种子色 harmonize 推导）；删除组件内字面量。
  - 预期效果：换种子色后强调色自动协调（现状已 harmonize，此任务是把策略收敛到唯一出口，供 4.x 后续触点复用）。
  - 验收：`rg "0xFFF57C00|0xFF4CAF50"` 在 commonMain 零命中（除 ExtendedColors.kt 与白名单）；文章页操作栏截图对比无视觉回归。

- [x] **T1.3 兜底 ColorScheme 清理**
  - 依赖：无｜优先级：P1
  - 涉及：`theme/Color.kt`、`Theme.kt`
  - 要求：模板 Purple 系列 Light/DarkColorScheme 替换为「知乎蓝种子预生成的静态方案」（用 material-kolor 生成后固化为常量，注释标明生成来源），作为异常兜底路径。
  - 验收：强制走兜底路径（临时断点/测试）截图非紫色；正常路径行为不变。

- [x] **T1.4 深色对比度校验**
  - 依赖：T1.1｜优先级：P1
  - 要求：对主流程文本角色（onSurface/onSurfaceVariant/onSecondaryContainer 等 × 种子色 ≥ 5 种）做对比度计算（脚本或测试内实现 WCAG 公式），低于 4.5:1 的组合记录并对种子色方案加最小修正。
  - 验收：`docs/md3-refactor/contrast-report.md` 出报告；不达标的组合已修正或有豁免说明。

### Phase 2：排版与形状（P1）

- [x] **T2.1 Typography 完整落地**
  - 依赖：T0.2｜优先级：P1
  - 涉及：`theme/Type.kt` 及各页面散落的字体样式（以 `rg "fontSize|FontWeight|TextStyle(" shared/src/commonMain` 清单为准）
  - 要求：实现第 4.2 节完整 scale；**阅读正文字号/行高/段间距仍由 `contentFontSize/contentLineHeight/contentBlockSpacing` 偏好驱动，不并入 Typography**（两者职责分离：UI 框架文字走 Typography，阅读内容走阅读偏好）。
  - 预期效果：标题层级、卡片文本、按钮标签有可预期的视觉节奏；移除散落的裸 fontSize。
  - 验收：审计清单中裸 `fontSize` 命中数收敛 ≥ 80%（正文渲染路径豁免）；主页/文章页/设置页截图与基线对比仅排版节奏变化、无布局破坏。

- [x] **T2.2 Shapes 接入 + 组件圆角对齐**
  - 依赖：T0.2｜优先级：P1
  - 涉及：`theme/Theme.kt`、`components/MyModalBottomSheet.kt`、`components/FeedCard.kt`（含 Duo3 样式分支）、各 Dialog
  - 要求：`MaterialTheme(shapes = ZhihuShapes)` 接入；FeedCard 卡片圆角走 `MaterialTheme.shapes.large`（Duo3 `duo3_card_appearance` 分支语义不变，只统一取值来源）；BottomSheet 顶部 `extraLarge` 且尊重 `disableBottomSheetRoundedCorners`。
  - 验收：审计清单裸 `RoundedCornerShape` 收敛（白名单：进度条、圆形头像等语义特殊项）；开/关 Duo3 四组合截图对比无回归。

### Phase 3：动效体系（P1，可与 Phase 2 并行）

- [x] **T3.1 导航转场对齐 MD3**
  - 依赖：T0.2｜优先级：P1
  - 涉及：`ui/ZhihuMain.kt`（L698-710 主 NavHost、L534-541 详情 NavHost）、`app/.../ZhihuMainAndroidContent.kt`（L62-87 四向转场）
  - 要求：进入 `slideIn + fadeIn` 用 `DURATION_MEDIUM + EmphasizedDecelerateEasing`；退出 `EmphasizedAccelerateEasing`；popExit 对称；四向回答切换转场保持方向语义、统一参数来源。**当前「进入有动画、退出 None」的不对称现状**改为 MD3 推荐的 enter+exit 成对（若实测性能或视觉不接受的特定页面，记录豁免理由）。
  - 预期效果：页面进出连贯有方向感，符合系统应用手感。
  - 验收：AVD 录屏对比基线；「进入→立即返回」连续快速操作动画可中断不跳变；桌面端编译通过、行为不回归。

- [x] **T3.2 组件动效 token 迁移**
  - 依赖：T0.2、T0.3 清单｜优先级：P1
  - 涉及：`AnimatedVisibility` 35 处、`animate*AsState` 10 处、`spring(StiffnessMediumLow)` 各点、`TwoRowsTopAppBar.kt` L720 自定义 easing
  - 要求：按第 4.4 节迁移规则逐处替换；`TwoRowsTopAppBar` 自定义 easing 评估后替换为 Emphasized 家族或豁免（附实测理由）；替换后逐处 AVD 目视确认无手感劣化。
  - 预期效果：全局动效时长/缓动一致，`rg "tween\(\d"` 在 commonMain 零命中（白名单：Motion.kt 自身与阅读器翻页等已论证的特殊档位）。
  - 验收：审计清单「动效」维度收敛 100%；关键动效点（底栏显隐、投票按钮态、BottomSheet、设置项条件显隐）录屏/截图留档。

- [~] **T3.3 列表与滚动动画统一**
  - 依赖：T3.2｜优先级：P2
  - 涉及：`PaginatedList.kt`、`CommentScreen.kt`、`AppearanceSettingsScreen.kt`
  - 要求：LazyColumn item `springMedium()` 统一；新增/删除项动画只在数据真实变化时触发（避免首次加载全列表飞入）；分页加载插入不闪烁。
  - 验收：主页快速滚到底触发分页，目视无跳变；评论区展开/收起子评论动画连贯。

### Phase 4：触觉反馈体系（P1，可与 Phase 2/3 并行）

- [x] **T4.1 触觉总开关设置项**
  - 依赖：T0.2｜优先级：P1
  - 涉及：`designsystem/Haptics.kt`、`ui/subscreens/AppearanceSettingsScreen.kt`（或交互设置组）、`docs/ai-ui-design-guide.md`（同步 preference 映射）
  - 要求：新增 `hapticFeedbackEnabled`（默认 true）；`rememberHapticController()` 读取它；按 `ai-ui-design-guide.md` 新设置项检查清单补 settingKey/testTag/文档。
  - 验收：关闭开关后 4.2 节全部触点静音；文档映射表已更新；设置项测试通过。

- [~] **T4.2 触点覆盖矩阵补齐**
  - 依赖：T4.1｜优先级：P1
  - 涉及：`ui/ZhihuMain.kt`（NavigationBarItem onClick）、`components/FeedPullToRefresh.kt`（刷新触发瞬间）、`SettingItem.kt`（Switch 切换）、`ui/ArticleScreen.kt`（点赞/收藏成功，收敛到 Confirm 出口）
  - 要求：按第 4.5 节矩阵逐点接入；每点只震一次；Pager 主 tab 切页是否加 tick 先做小步 A/B 体验评估再定（默认建议：底栏 tab 有、Pager 跟随滑动无）。
  - 预期效果：关键操作均有符合语义的轻量反馈，且不会震个不停。
  - 验收：AVD 上逐点操作 + `adb shell dumpsys vibrator_manager`（或 logcat vibrator 事件）确认触发与静音两态；矩阵表勾选记录在 `docs/md3-refactor/haptic-matrix.md`。

- [~] **T4.3 触觉审计与豁免登记**
  - 依赖：T4.2｜优先级：P2
  - 要求：全量 `rg "HapticFeedback" 复核；图片查看器 NoopHapticFeedback 屏蔽点保留并在 audit.md 登记豁免理由；确认无遗漏双震（同一操作两处触发）。
  - 验收：audit.md 完整；无双震案例（录屏佐证双击点赞路径）。

### Phase 5：手势与操作手感（P2，依赖 Phase 3）

- [x] **T5.1 回答切换 overscroll 手感调优**
  - 依赖：T3.2｜优先级：P2
  - 涉及：`components/AnswerVerticalOverscroll.kt`（L214/L307 spring、tanh 阻尼）、`AnswerHorizontalPager.kt`（L107）、`AnswerSwitchSensitivity`
  - 要求：阻尼曲线、回弹 spring、触发阈值的参数统一收敛为具名常量（`Motion` 或组件伴生对象）并注释物理含义；保证与 `answerSwitchSensitivity` 联动语义不变；阈值触发时转场（见 T3.1）与震动（T4.2）同帧。
  - 验收：灵敏度 0.5/1.0/2.0 三档实测切换距离符合线性预期；快速连续切换不卡顿不跳回答；已有 overscroll androidTest 全绿。

- [x] **T5.2 下拉刷新手感**
  - 依赖：T4.2｜优先级：P2
  - 涉及：`components/FeedPullToRefresh.kt`
  - 要求：触发瞬间接 `gestureThreshold` 触觉；indictor 颜色维持 primaryContainer 体系；松手回弹参数对齐 Motion token。
  - 验收：13 个复用列表页抽测 4 个（主页/热榜/搜索/通知）行为一致。

- [x] **T5.3 预测性返回接通**
  - 依赖：T3.1｜优先级：P2
  - 涉及：`app/src/main/AndroidManifest.xml`（`android:enableOnBackInvokedCallback`）、`app/.../MainActivity.kt`、设置项 `enable_predictive_back`（现有 key，补运行时读取点）
  - 要求：Android 14+ 启用 predictive back 手势动画；开关默认值与现有设置语义对齐（`ai-ui-design-guide.md` 已登记该 key）；桌面端不受影响。
  - 验收：AVD API 34+ 实测返回手势有 peek 动画、开关可关闭；`rg enable_predictive_back` 命中运行时读取点；Android 13 及以下无行为变化。

- [x] **T5.4 触控目标与手势冲突审查**
  - 依赖：无｜优先级：P2
  - 要求：核心可点元素（卡片操作图标、底栏项、文章操作栏）触控区 ≥ 48dp（必要时 `minimumInteractiveComponentSize` 或 padding 扩展）；审查双击点赞 vs 图片查看器双击缩放、长按正文 vs 拖动选区的冲突现状并记录（有已知问题建 issue，不在本计划内强改）。
  - 验收：走查表记录在 audit.md；抽查 10 个触点尺寸合规。

### Phase 6：整体体验收尾（P2）

- [~] **T6.1 无障碍走查**：TalkBack 朗读核心页面（主页/文章/评论/设置），补齐 icon contentDescription 缺失（`rg "contentDescription = null"` 清单化处理装饰性图标，操作图标必须可朗读）；验收：TalkBack 走查记录 + 修复清单收敛。
- [~] **T6.2 动效性能验证**：AVD 上 `adb shell dumpsys gfxinfo com.github.zly2006.zhplus.lite` 对主页滚动、回答切换、导航转场三个路径采帧；标准：P95 帧时间 < 32ms（ Lite debug 变体、模拟器口径，记录数值即可，超标项定位修复或登记豁免）。
- [x] **T6.3 文档同步**：更新 `docs/ai-ui-design-guide.md`（新增 `hapticFeedbackEnabled` 映射、`enable_predictive_back` 状态变化、designsystem 目录说明）；`CLAUDE.md` 如有新的通用经验按其格式追加。
- [~] **T6.4 全量回归与终版对比**：重跑 T0.1 十页面截图（同条件）与基线对比出 `docs/md3-refactor/final-report.md`；跑既有全部 androidTest（交给 CI，遵守 CLAUDE.md「本地不跑全量 instrument test」规则）；release lite 变体构建 + 安装冒烟。

---

## 6. 任务优先级与依赖关系

### 6.1 依赖图（→ 表示前置）

```
T0.1 基线截图 ──────────────────────────────┐（验收对照物，被所有视觉任务引用）
T0.2 designsystem 骨架 ──┬→ T1.1 → T1.4    │
                        ├→ T1.2            │
                        ├→ T2.1（可与P1配色并行）
                        ├→ T2.2            │
                        ├→ T3.1 ─→ T5.1   │
                        │      └→ T5.3     │
                        ├→ T3.2 → T3.3     │
                        └→ T4.1 → T4.2 → T4.3
T0.3 审计清单 →（T1.2/T2.1/T2.2/T3.2 的收敛度量基准）
T1.3（独立）、T5.4（独立）
Phase 6 全部依赖其所属验收对象完成
```

### 6.2 优先级与批次建议

| 批次 | 任务 | 说明 |
| --- | --- | --- |
| 批次 1（P0，先做） | T0.1–T0.3 | 基建与度量基准，一次 PR |
| 批次 2（P0） | T1.1、T1.2 | 配色根基，消除结构性债务 |
| 批次 3（P1，三条线可并行） | 线 A：T2.1→T2.2；线 B：T3.1→T3.2→T3.3；线 C：T4.1→T4.2 | 若用多 agent 并行，按 CLAUDE.md Subagent 规则分派互不冲突的文件集（线 A 触 theme/components 视觉、线 B 触动效调用点、线 C 触交互组件，注意 `ZhihuMain.kt`/`ArticleScreen.kt` 是三线共享热点，**共享文件的任务必须串行**） |
| 批次 4（P1/P2） | T1.3、T1.4、T4.3 | 补齐项 |
| 批次 5（P2） | T5.1–T5.4 | 手感精修 |
| 批次 6（P2，收尾） | T6.1–T6.4 | 体验验收与文档 |

**并行冲突提示**：`ZhihuMain.kt`、`ArticleScreen.kt`、`AppearanceSettingsScreen.kt`、`FeedPullToRefresh.kt` 被多条任务线触碰，并行 agent 分派时必须保证同一文件同一时间只有一个 owner。

---

## 7. 各优化领域要求与预期效果汇总（横向视图）

| 领域 | 现状痛点 | 目标要求（详见第 4/5 章） | 预期效果 | 对应任务 |
| --- | --- | --- | --- | --- |
| 配色 | surface 层级断裂、硬编码强调色、模板兜底色 | 角色全覆盖 + ExtendedColors + 层级联动 + 对比度达标 | 任意种子色/背景色下全局协调；换色不再断层 | T1.1–T1.4 |
| UI 组件 | Typography 近零定制、无 Shapes、圆角散写 | 完整 type scale + shape token 接入组件 | 视觉节奏统一，卡片/弹层/按钮圆角成体系 | T2.1–T2.2 |
| 动效 | 时长裸写、缓动随意、无中断策略 | MotionTokens 全覆盖 + MD3 强调曲线 + 可中断 | 转场方向感与系统一致，全局手感统一 | T3.1–T3.3 |
| 触觉 | 局部覆盖、无开关、无语义分级 | HapticPolicy 四级语义 + 总开关 + 覆盖矩阵 | 关键操作有轻反馈、可一键关闭、不滥用 | T4.1–T4.3 |
| 手势手感 | 参数散落、预测性返回未接、触控目标未审计 | 手势参数收敛 + 预测性返回 + 48dp 审查 | 回答切换/刷新/返回手感达旗舰水准 | T5.1–T5.4 |
| 整体体验 | 无障碍未走查、动效性能未量化 | TalkBack 走查 + 帧时间量化 + 文档同步 | 可量化的验收报告，无功能回归 | T6.1–T6.4 |

---

## 8. 验收标准

### 8.1 每个任务的通用门槛（缺一不可）

1. `./gradlew assembleLiteDebug` 通过；`./gradlew ktlintFormat` 后无 diff。
2. 与任务相关的既有 androidTest 定向用例通过（本地只跑定向，全量交 CI，遵守 CLAUDE.md）。
3. UI 可见变化：AVD 真机截图/录屏与基线对比留档（截图来源必须真实运行，禁设计稿替代）。
4. `git status` 无测试残留（临时取证代码已恢复，遵守 instrument-test 治理）。
5. Code review 通过后才可合并（CLAUDE.md 强制）；PR 标题中文 `refactor:/feat:` 前缀，描述含改动/原因/影响/验证 + 截图。

### 8.2 领域专项验收

- **配色**：audit.md 硬编码色清单收敛至白名单；三种典型背景色 × 明暗截图层级连续；对比度报告达标。
- **组件**：裸 fontSize / RoundedCornerShape / tween 时长三项清单收敛率达标（见各任务）。
- **动效**：快速连续操作（进入即返回、连续切回答）无可感知跳变；录屏留档。
- **触觉**：haptic-matrix.md 全矩阵勾选；开关关闭全静音；vibrator 系统事件佐证。
- **手感**：灵敏度三档线性实测；预测性返回 API 34+ 生效、低版本无变化。
- **体验**：gfxinfo 三路径 P95 < 32ms（记录口径）；TalkBack 核心页面可完成「读标题→进文章→返回」闭环。

### 8.3 最终 Definition of Done（T6.4 退出条件）

1. 全部任务勾选完毕或附「豁免 + 理由 + 计划」。
2. 终版十页面截图对比报告（final-report.md）完成，无未解释的视觉回归。
3. CI（GitHub Actions）全量 instrument test 绿。
4. release lite 变体构建、安装、冒烟通过（启动→浏览→切回答→设置换主题→返回）。
5. `docs/ai-ui-design-guide.md` 与本文件状态同步。

---

## 9. 执行协议与风险控制

### 9.1 单任务执行循环（每个任务照此走）

```
读任务 → rg 复核涉及文件现状 → 最小步实现
→ assembleLiteDebug + ktlintFormat
→ AVD 安装启动（包名 com.github.zly2006.zhplus.lite）
→ 按强制清单 dump/截图验证 → 与基线对比
→ （涉登录态时按 Login JSON Backup 流程恢复，不要卡在登录页）
→ 更新 audit/matrix 文档 → 标记任务完成 → 提 PR 等 review
```

### 9.2 风险清单

| 风险 | 缓解 |
| --- | --- |
| material3 1.10.0-alpha05 是 alpha 版，个别组件 API 不稳定 | 不升级版本；只用已在项目中出现过的 API 面；新组件 API 先在 spike 分支验证 |
| `ThemeManager` 全局单例与 CompositionLocal 混用导致重组失效 | 重构期间不改变状态架构（架构迁移不在本计划范围）；只改「色值怎么算」，不改「状态怎么发」 |
| KMP：改 common 契约必须同步所有平台 actual | 遵守 CLAUDE.md 原子提交规则：一个任务内 common + 各平台 actual 一起改完、一起可编译 |
| 动效参数迁移引发视觉/性能回归 | 每处迁移独立 commit；T0.1 基线对照；gfxinfo 量化 |
| 触觉滥用造成体验倒退 | 4.5 节「克制原则」+ 逐点 A/B 评估 + 总开关兜底 |
| 桌面端编译破坏 | 每个涉及 common 的任务完成后额外跑 `:shared:compileKotlinJvm`（或桌面 assemble）确认 |
| 改动跨 Duo3 开关组合状态爆炸 | 涉 Duo3 的任务按 开/关 × 明/暗 四组合截图验证 |

### 9.3 明确禁止

- 禁止为通过验收修改测试断言去迎合实现（回归必须红→绿）。
- 禁止把临时截图测试、取证代码留在提交里。
- 禁止顺手改信息架构、导航结构或新增与本计划无关的「改进」。
- 禁止引入新的大型依赖（含动画库、haptic 库）。

---

## 附录 A：白名单豁免登记（动态维护）

| 位置 | 豁免项 | 理由 |
| --- | --- | --- |
| `theme/Color.kt` 种子默认值 `0xFF2196F3` | 硬编码色 | 产品默认主题色（知乎蓝），T1.3 后仅存在于种子定义 |
| `ColorSchemeScreen.kt` / `ColorPickerDialog.kt` | 硬编码色 | 取色器 UI 本体 |
| `UiComponentSupport.kt` L292 `NoopHapticFeedback` | 触觉屏蔽 | 图片查看器长按菜单手感稳定（有注释，保留） |
| `RenderMarkdown` 正文字号/行高 | 裸 fontSize | 阅读偏好驱动（`contentFontSize` 等），不走 Typography |

## 附录 B：参考资料

- Material Design 3 规范：m3.material.io（motion / color / typography / shape / haptics 章节）
- Compose Motion 对应实现：`androidx.compose.animation` 文档（spring/tween/Easing）
- 本项目纪律与流程：`CLAUDE.md`；UI 语义地图：`docs/ai-ui-design-guide.md`
