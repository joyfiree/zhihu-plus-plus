# Zhihu++ 动效打磨记录（motion-log）

> 依据：`docs/md3-refactor/motion-polish-prompt.md`
> 执行：2026-10-31 ~ 2026-11-01｜执行者：AI Agent
> 口径：每条一行；`验证` 列写明在本环境（WSL + JDK17 + 软件 GPU AVD，无真机、无振动器、无录屏评价者）能做到哪一步。

## 0. 环境与基线

| 项 | 值 |
| --- | --- |
| 基线构建 | `:app:assembleLiteDebug` 通过（改动前，1m36s） |
| 终版构建 | `:app:assembleLiteDebug` + `ktlintCheck` + `:shared:compileKotlinJvm` 全绿 |
| 设备 | WSL 内 AVD `Medium_Phone_2`（API 35 / 1080×2400 / swiftshader 软件 GPU） |
| 无法在本环境完成 | 真机振动、0.25x 慢放录屏、真机 gfxinfo、TalkBack 交互闭环 |

## 1. 基础层（先落地，后续所有任务复用）

| 内容 | 文件 | 说明 |
| --- | --- | --- |
| `Motion.Gesture` 常量组 | `ui/designsystem/Motion.kt` | 纵/横 overscroll 阈值与阻尼、回弹峰值、按下缩放、chip 选中缩放、底栏翻转阈值与静默期 |
| `countTween()` / `imageFadeTween()` / `bouncePopTween()` / `bounceSettle()` | 同上 | 220 / 240 / 110ms 三个新档位，曲线均为 `StandardEasing` 或 `EmphasizedDecelerate` |
| Shared Axis X 转场 | `ui/designsystem/MotionTransitions.kt`（新增） | `sharedAxisXEnter/Exit/PopEnter/PopExit`，位移与淡入共用同一 tween |
| `BounceOnChange` | `ui/components/ZhihuMotionEffects.kt` | 改用 token，不再内联 110ms / 1.28x |
| `animatedCountLabel` | 同上 | `animateIntAsState` + `countTween()` |
| `ZhihuFilterChip` | 同上 | 1.04x springSnappy + 容器/文字色 200ms 过渡，覆盖 11 处调用点（5 个文件） |
| `ZhihuAlertDialog` | `ui/components/ZhihuDialogs.kt`（新增） | 28dp 圆角 + 主体文字 200ms 淡入上浮，覆盖 21 个文件 33 处调用点 |

## 2. 任务台账

| ID | 文件 | 参数 | 验证 | 微调轮数 |
| --- | --- | --- | --- | --- |
| A1 评论点赞 | `ui/CommentScreen.kt` | `BounceOnChange(isLiked)` 峰值 1.28x、110ms + spring；数字 `countTween()` 220ms；`haptics.tick()` 与状态翻转同帧 | 编译通过；连点语义由 `Animatable.animateTo` 可中断性保证（二次点击从当前进度反向，不跳 1x） | 1（峰值 1.3→1.28） |
| A2 关注按钮 | `ui/QuestionScreen.kt` | `animateColorAsState` 200ms（secondary→tertiary 容器）；图标 `BounceOnChange`；`haptics.confirm()` 在状态赋值前触发 | 编译通过；宽度不做动画（按钮 `weight(1f)`，做宽动画是空操作） | 1 |
| A3 赞同/评论数 | `ui/ArticleScreen.kt` | `animatedCountLabel` 220ms，与药丸 `springSnappy` 同帧启动 | 编译通过；4 处计数点全部替换 | 1 |
| A4 筛选 chip | 6 文件 13 处 | `ZhihuFilterChip`：1.04x `springSnappy` + 200ms 容器/文字色；评论排序 chip 走 `ZhihuSuggestionChip`（保留无填充外观，只加 1.04x，文字色由调用处 `animateColorAsState` 过渡） | 编译通过；全仓 `FilterChip(` 无裸用（仅组件内部实现 + 1 处静态标签 chip，见 audit §6）；`CommentScreenInstrumentedTest` 6/6 通过 | 2（1.06→1.04；收尾复核补上评论排序 chip） |
| A5 卡片按压 | `ui/components/FeedCard.kt` | 分割线样式补 `pressScale` + 与 `combinedClickable` 共用同一 `MutableInteractionSource` | 编译通过；两种样式参数统一 0.975x | 0 |
| A6 开关触觉 | `ui/components/SettingItem.kt` | 既有 `haptics.confirm()` 覆盖全站唯一 Switch 出口（`onCheckedChange = null`，点击由整行承担），本轮复核无需改动 | 代码复核 | 0 |
| A7 列表 footer | `ui/components/PaginatedList.kt` | 转圈 `AnimatedVisibility(fadeIn/fadeOut(fadeTween()))`；「加载中 → 已到底」用 `AnimatedContent` 交叉淡入 | 编译通过；footer 高度不再瞬间塌陷 | 1 |
| B1 导航转场 | `ui/ZhihuMain.kt` | 两处 NavHost 统一走 `sharedAxisX*`（位移 1×/0.25× 屏宽 + 同长淡入淡出，300ms） | 编译通过；参数收敛到单文件，进入/返回可中断由 `AnimatedContentTransitionScope` 保证 | 0 |
| B2 弹层关闭 | `ui/components/MyModalBottomSheet.kt` | 所有 dismiss（遮罩/返回键/窗口回调/语义动作）统一走 `animateToDismiss`；遮罩改 `Motion.fadeTween()` 与 sheet 的 `FastEffects` 同档 | 编译通过；消除了两处「直接 hide 再回调」的旁路 | 0 |
| B3 全站弹窗 | `ui/components/ZhihuDialogs.kt` + 21 文件 | 圆角 28dp（`ZhihuShapeTokens.Dialog`）；主体文字 200ms 淡入 + 4dp 上浮 | 编译通过；`AlertDialog(` 在 `shared/commonMain`、`app` 主线已无裸用（测试代码除外） | 0 |
| B4 创作 FAB | `ui/HomeScreen.kt` | 图标 `rotationZ` 0→45° `springSnappy`；FAB 自身 `pressScale` 共用 interactionSource | 编译通过 | 1（旋转角度 90→45） |
| B5 刷新 FAB | `ui/HomeScreen.kt` | `AnimatedContent(fadeIn togetherWith fadeOut)`，容器固定 30dp | 编译通过；转圈不再撑大 FAB 内容 | 0 |
| B6 四向切换 | `AnswerVerticalOverscroll.kt` / `AnswerHorizontalPager.kt` | 四个方向的阈值/阻尼全部上收 `Motion.Gesture`，纵向 80dp/横向 46dp 语义保留 | 编译通过；`answerSwitchSensitivity` 线性缩放路径未改 | 0 |
| C1 首页标题折叠 | — | **豁免**：首页顶栏是「搜索胶囊 + 头像/角标」而非大标题，折叠会隐藏搜索入口；详见 audit §3 | — | — |
| C2 底栏隐藏 | `ui/ZhihuMain.kt` | 阈值 3px→12px；同方向累积、方向翻转清零；触摸滚动 300ms 静默期 | 编译通过；**首版误把 `SideEffect` 事件整体丢弃，导致翻页按钮/键盘翻页不再隐藏底栏，被 `ZhihuMainNavigationInstrumentedTest` 拦下，已改为「静默期只作用于触摸输入」**（详见 audit §8） | 2 |
| C3 Pager 视差 | `ui/ZhihuMain.kt` | `graphicsLayer { translationX = pageOffset * width * 0.12f }`，绘制阶段读取 | 编译通过；滑动过程零重组 | 1（0.15→0.12） |
| C4 双行顶栏 | `ui/components/TwoRowsTopAppBar.kt` | 既有实现已用 alpha 交叉淡化（`TopTitleAlphaEasing`）+ `clearAndSetSemantics` 切换语义，优于 `AnimatedContent`（不改布局高度，`ArticleScreenInstrumentedTest` 断言不受影响） | 代码复核 | 0 |
| D1 骨架→内容 | `ui/components/PaginatedList.kt` | 骨架改用独立 key `zhihu_skeleton_N` + `animateItem(fadeOutSpec)`，与真卡片的 fadeIn 形成交叉淡入 | 编译通过；这是原「骨架硬切卡片」的根因 | 1 |
| D1b 骨架流光 | `ui/components/ZhihuSkeleton.kt` | 无限 `rememberInfiniteTransition` → 扫光 3 轮（1.4s/轮）后静止；位移从组合期 `Float` 改为 `State<Float>`，在 `drawBehind {}` 里读取 | 改前该 shimmer 会让 `waitForIdle()` 永不返回，首页加载期 `ZhihuMainNavigationInstrumentedTest` 整条挂死（实测 600s 未结束）；改后单跑 10s 通过、全量回归通过 | 1 |
| D2 增删动画 | `ui/components/PaginatedList.kt`、`CommentScreen.kt` | 已有 `animateItem` 统一 `springMedium()`，本轮复核未改 | 代码复核 | 0 |
| D3 展开/收起 | `ui/QuestionScreen.kt` | 问题详情展开箭头 `rotationZ` 0→180° `springSnappy`（同一个图标原地转，不换图标） | 编译通过；评论线程无折叠态（子评论常显 + 「查看全部」入口），已在 audit 记录 | 1 |
| D4 图片墙 | `ui/components/FeedCard.kt` | 每张比上一张晚 40ms 开始淡入，最多 9 张（360ms） | 编译通过；走动画时钟，关动画时立即终态 | 0 |
| E1 图片淡入 | `ui/components/ZhihuNetworkImage.kt` | 固定 `imageFadeTween()` 240ms；占位色与骨架同为 `surfaceContainerHighest`；走 `onSuccess` 因此 AVIF/WebP 同路径 | 编译通过 | 0 |
| E2 图片查看器 | `ui/components/UiComponentSupport.kt` | 整层 `0.92 → 1` + 淡入，`enterTween()` 300ms；双击缩放仍由 telephoto 自管（spring） | 编译通过；关闭方向为宿主 Dialog dismiss，未做对称动画（如实记录） | 1 |
| E3 视频全屏 | — | **豁免**：视频是 WebView 内播放，全屏由网页侧控制，Compose 无转场可加 | — | — |
| F1 纵向手感 | `ui/components/AnswerVerticalOverscroll.kt` | 阈值/阻尼/回弹全部来自 `Motion.Gesture` + `Motion.overscrollSettle()`；预览卡出现条件改 `derivedStateOf`，透明度进 `graphicsLayer` | 编译通过；拖动全程从「按帧重组」降为「跨 0/跨阈值各一次」 | 1 |
| F2 下拉刷新 | `ui/components/FeedPullToRefresh.kt` | 既有 `haptics.gestureThreshold()` 在 `onRefresh` 内，触发点与震动同帧 | 代码复核 | 0 |
| F3 刷新按钮吸附 | `ui/components/DraggableRefreshButton.kt` | `springSnappy()`（`DampingRatioNoBouncy`）：贴边吸附不过冲，避免"甩飞"；拖拽中 `snap()` 保证跟手 | 代码复核 | 0 |
| F4 预测性返回 | `ui/components/MyModalBottomSheet.kt` | 既有 `predictiveBackProgress` 驱动缩放，`onCancel` 回弹到 0 | 代码复核 | 0 |
| G1 组合期读动画值 | 全仓扫描 | 修复 `PeopleScreen`（offset/alpha→`graphicsLayer`）、`AnswerVerticalOverscroll`（预览卡→`derivedStateOf` + `graphicsLayer`）、`VerticalReadingProgressBar`（淡出保护条件→`derivedStateOf` 布尔）；24 个声明点分类见 audit §4 | `grep` 逐条复核，见 audit §4 | 0 |
| G2 整页模糊 | `ui/HomeScreen.kt`、`ui/QuestionScreen.kt` | 去掉首页整页 `Modifier.blur()`（改为 scrim 0.16→0.32）；去掉问答遮罩的 `blur(12dp)`（渐变改 4 档 stop） | 编译通过 | 1 |
| G3 动画时长 0 | `PaginatedList.kt`、`ZhihuNetworkImage.kt` | 两处手写 `delay()` 错峰改为 `animate()` 动画时钟，系统关动画时立即终态；其余 `animate*AsState`/`Animatable` 由 Compose 自动处理 | 逻辑复核（需真机在开发者选项切换验证） | 0 |
| G4 gfxinfo | — | 见 §3 | 环境口径不可判定 <32ms（软件 GPU），数值如实记录 | — |
| G5 无障碍节点 | 全部改动 | 新增的 `Box`/`graphicsLayer`/`AnimatedContent`/`AnimatedVisibility` 均不产生语义节点；`ZhihuAlertDialog` 只在 `text` 外多包一个无 `semantics` 的 Column | 静态复核（节点数不变） | 0 |

## 3. G4 性能采样（实测）

| 路径 | 帧数 | Janky | P50 | P90 | P95 | P99 |
| --- | --- | --- | --- | --- | --- | --- |
| 问答详情（列表滚动 + 详情展开） | 267 | 25.5% | 31ms | 65ms | 101ms | 300ms |
| 主导航 / 转场（切 tab + 滑动） | 162 | 30.3% | 17ms | 150ms | 200ms | 350ms |

环境：AVD `Medium_Phone_2`（API 35 / 1080×2400 / `swiftshader_indirect` 软件 GPU）。
采样方式：instrumented 测试进程存活期间注入滑动，只对滑动窗口 `reset` 后统计（信息流页面受登录限制不可达，第一次盲滑采到 0 帧）。

**口径说明**：P50 的 17~31ms 已是软件 GPU 合成 1080×2400 全屏的物理下限（≈30fps），P95 的抬升来自 `input swipe` 注入与首帧栅格化，不是动效曲线本身。因此本轮**不宣称达到 P95 < 32ms**（该口径要求硬件 GPU），与上一轮 T6.2（P95=53ms）同属**环境豁免**。真机复测命令与检查项见 `motion-audit.md` §5 / §7。

## 4. 回归验证

| 阶段 | 命令 | 结果 |
| --- | --- | --- |
| 静态检查 + 编译 | `ktlintCheck` + `:shared:compileKotlinJvm` + `:app:assembleLiteDebug` | BUILD SUCCESSFUL |
| 行为回归（首轮） | `:app:connectedLiteDebugAndroidTest` 全量 | 99 tests / 1 failed（`pageTurnHidesAndRevealsBottomBarWhenAutoHideEnabled`） |
| 修复后复跑 | 同上 | 99 tests / **0 failed** |

失败用例与修法见 `motion-audit.md` §8：我把 `NestedScrollConnection` 的 `SideEffect` 事件整体丢弃，而翻页按钮正是用 `SideEffect` 合成滚动的，导致「翻页时隐藏底栏」失效；已改为静默期只作用于触摸输入。
