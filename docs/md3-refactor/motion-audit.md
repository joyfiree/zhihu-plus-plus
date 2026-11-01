# Zhihu++ 动效审计（motion-audit）

> 配套文档：任务清单 `motion-polish-prompt.md`、执行台账 `motion-log.md`
> 审计对象：本工程全部交互动效（转场 / 微交互 / 列表 / 图片 / 手势 / 无障碍与性能）
> 审计时间：2026-11-01｜执行者：AI Agent

**状态图例**

| 标记 | 含义 |
| --- | --- |
| ✅ | 已落地，且在本环境（编译 + 99 个 instrumented 测试 + 静态扫描）可验证 |
| ⚠️ | 已落地，但本环境只能验证到「不破坏行为」这一层，手感/帧率需真机确认 |
| ⬜ | 未做：属于任务清单外的发现，或已确认豁免（理由见 §3、§6） |

---

## 1. 审计范围与方法

**代码范围**：`shared/src/commonMain` 的 UI 层（`ui/`、`ui/components`、`ui/designsystem`、`ui/article`）、`shared/src/androidMain` 的主题支撑、`app/src/{main,full}` 的 Activity 与 full 专属页面。数据层、网络层、播放器内核不在范围内。

**方法**：四层递进，每层都能给出可复核的证据。

1. **令牌集中**：所有时长、曲线、手势阈值收敛到 `Motion.kt`；调用点不再写字面量。改一个数字即可全站生效。
2. **原语复用**：把「回弹 / 数字滚动 / 筛选 chip / 对话框 / Shared Axis」五类高频动效抽成 `ZhihuMotionEffects.kt`、`ZhihuDialogs.kt`、`MotionTransitions.kt`，避免 30 多处各写一遍。
3. **静态扫描**：全仓扫 `animate*AsState` / `Animatable` 的消费位置（§4），凡是组合期读值一律改到 `graphicsLayer {}` / `drawWithContent {}` / `offset {}` 里。
4. **动态验证**：`:app:assembleLiteDebug` + `ktlintCheck` + `:shared:compileKotlinJvm` 必须全绿；再在 AVD 上跑全部 99 个 instrumented 测试（§8），最后在滑动窗口内采 `dumpsys gfxinfo`（§5）。

**本环境做不到的三件事**（贯穿全文的限制，不是遗漏）：真机振动反馈、0.25x 慢放录屏逐帧核曲线、无账号环境下进入信息流的真实帧率。

---

## 2. 动效点总清单

### 2.1 基础层（令牌与原语）

| 动效点 | 位置 | 内容 | 状态 |
| --- | --- | --- | --- |
| 时长三档 | `designsystem/Motion.kt` | `DURATION_SHORT=200`（微交互）/ `MEDIUM=300`（转场、BottomSheet）/ `LONG=450`（大面积形变） | ✅ |
| 曲线四档 | 同上 | `EmphasizedEasing` / `EmphasizedDecelerateEasing`（进入）/ `EmphasizedAccelerateEasing`（退出）/ `StandardEasing` | ✅ |
| 时长微调档 | 同上 | `countTween()` 220ms（数字滚动）/ `imageFadeTween()` 240ms（图片淡入）/ `bouncePopTween()` 110ms（回弹放大段） | ✅ |
| 弹簧三档 | 同上 | `springMedium()`（列表位移）/ `springSnappy()`（按压、chip、箭头）/ `overscrollSettle()`（手势归位，带轻过冲）/ `bounceSettle()`（回弹回落，MediumBouncy） | ✅ |
| 手势常量组 | `Motion.Gesture` | 纵向 200/80dp、横向 300/46dp、阻尼 1.2、回弹峰值 1.28x、按下 0.975x、chip 选中 1.04x、底栏阈值 12px、静默期 300ms | ✅ |
| `pressScale` | `designsystem/ZhihuMotion.kt` | `collectIsPressedAsState` → `graphicsLayer { scaleX/Y }`，可复用 modifier | ✅ |
| `BounceOnChange` | `components/ZhihuMotionEffects.kt` | `Animatable` 可中断回弹：从当前进度反向，连点不跳回 1x；峰值 1.28x | ✅ |
| `animatedCountLabel` | 同上 | `animateIntAsState` + `countTween()`，把计数变化变成滚动数字 | ✅ |
| `ZhihuFilterChip` | 同上 | 1.04x `springSnappy` + 容器/文字色 200ms 过渡；把 M3 的 `containerColor` 与 `selectedContainerColor` 赋同一动画值，禁用 M3 自带的双重颜色动画 | ✅ |
| `ZhihuAlertDialog` | `components/ZhihuDialogs.kt` | 28dp 圆角（`ZhihuShapeTokens.Dialog`）+ 主体内容 200ms 淡入与 4dp 上浮 | ✅ |
| Shared Axis X 四式 | `designsystem/MotionTransitions.kt` | `sharedAxisXEnter/Exit/PopEnter/PopExit`：进入整屏、退出 1/4 屏，位移与淡入共用同一 tween | ✅ |

### 2.2 转场

| 动效点 | 位置 | 参数 | 状态 |
| --- | --- | --- | --- |
| 主/详情导航 | `ui/ZhihuMain.kt`（两处 NavHost：主页面栈、详情栈） | 统一 `sharedAxisX*`，300ms，进入 1× / 退出 0.25× 屏宽 | ⚠️ |
| 底部弹层关闭 | `components/MyModalBottomSheet.kt` | 遮罩与 sheet 同曲线（200ms 标准曲线）；所有 dismiss 出口（点遮罩 / 返回键 / 窗口回调 / 语义动作）统一走 `animateToDismiss()`，先 `hide()` 再回调 | ✅ |
| 全站对话框 | `components/ZhihuDialogs.kt` + 21 个宿主文件（33 个调用点） | 28dp 圆角 + 内容 200ms 淡入上浮；无裸 `AlertDialog(` 残留（仅包装器自身与测试代码） | ✅ |
| 创作 FAB 图标 | `ui/HomeScreen.kt` | `rotationZ` 0→45° `springSnappy` 随菜单展开；FAB 本体 `pressScale` 与 `clickable` 共用同一 `MutableInteractionSource` | ✅ |
| 刷新 FAB 内容 | 同上 | 图标 ↔ 转圈 `AnimatedContent(fadeIn togetherWith fadeOut)`；容器固定 30dp，切换时 FAB 不被撑大 | ✅ |
| 回答切换（四向） | `components/AnswerVerticalOverscroll.kt`、`AnswerHorizontalPager.kt` | 进入用 `EmphasizedDecelerateEasing`、退出用 `EmphasizedAccelerateEasing`，300ms；阈值 80dp / 46dp 语义不变 | ⚠️ |
| 图片查看器打开 | `components/UiComponentSupport.kt` | 整层 `0.92 → 1` + 淡入，300ms；双击缩放仍由 telephoto 自管 | ✅ |
| 视频全屏 | — | **豁免**（§3） | ⬜ |

### 2.3 微交互

| 动效点 | 位置 | 参数 | 状态 |
| --- | --- | --- | --- |
| 评论点赞 | `ui/CommentScreen.kt` | 图标 `BounceOnChange(isLiked)` + `haptics.tick()` 与状态翻转同帧 + 数字 `animatedCountLabel` | ⚠️（振动需真机） |
| 关注按钮 | `ui/QuestionScreen.kt` | 容器/内容色 200ms 过渡 + 图标回弹 + `haptics.confirm()` 先于状态赋值；同时修正了 `followerCount` 用旧状态判断 ±1 的老 bug | ⚠️ |
| 赞同/评论计数 | `ui/ArticleScreen.kt`（4 处）、`CommentScreen.kt`（1 处） | `animatedCountLabel` 220ms，与药丸 1.06x `springSnappy` 同帧启动 | ✅ |
| 筛选 chip | `ZhihuFilterChip`（11 个调用点：`QuestionScreen` 3、`ArticleScreen` 1、`TopicScreen`、`SearchScreen` 等）；`ZhihuSuggestionChip`（`CommentScreen` 评论排序 2 处） | 选中 1.04x + 容器/文字色 200ms；SuggestionChip 保留「无填充 + 文字着色」外观，只补缩放，不改视觉语言 | ✅ |
| 卡片按压 | `components/FeedCard.kt`（两种卡片样式） | `pressScale(0.975x)` 与 `combinedClickable` 共用同一 `interactionSource`，缩放与涟漪同时成立 | ✅ |
| 开关 | `components/SettingItem.kt` | 触觉由整行 `clickable` 承担（Switch 自身 `onCheckedChange = null`），不干扰 thumb 位移动画 | ✅ |
| 列表项按下 | `components/PaginatedList.kt` | 统一 `springSnappy` | ✅ |

### 2.4 列表与加载

| 动效点 | 位置 | 参数 | 状态 |
| --- | --- | --- | --- |
| 骨架 → 内容 | `components/PaginatedList.kt` | 骨架改独立 key `zhihu_skeleton_N` + `animateItem(fadeOutSpec)`，与真卡片 fadeIn 形成交叉淡入；这是原来「骨架硬切卡片」的根因 | ✅ |
| 列表增删位移 | 同上 | `animateItem(placementSpec = springMedium())`，用户关动画时传 `null` | ✅ |
| 首屏错峰入场 | 同上 | 每项延后 28ms，最多 8 项（224ms）；用**动画时钟** `animate()` 而非 `delay()`，系统「动画时长 0」时直达终态 | ✅ |
| 加载 footer | 同上 | 转圈 `AnimatedVisibility(fadeIn/fadeOut(fadeTween))`；「加载中 → 已到底」`AnimatedContent` 交叉淡入，高度不再瞬间塌陷 | ✅ |
| 详情展开/收起 | `ui/QuestionScreen.kt` | 箭头单图标原地 `rotationZ` 0→180° `springSnappy`（不再换图标）；视口高度 450ms `EmphasizedDecelerate` 既有实现保留 | ✅ |
| 评论展开 | `ui/CommentScreen.kt` | 现有评论线程无折叠态（子评论常显 + 「查看全部」入口），**不适用**（§3） | ⬜ |
| 图片墙 | `components/FeedCard.kt` | 每张比上一张晚 40ms 淡入，最多 9 张（≤360ms）；走动画时钟 | ✅ |
| 骨架屏本身 | `components/ZhihuStates.kt` / `PaginatedList.kt` | 骨架块呼吸动画保留既有实现（`infiniteRepeatable`），未叠加新动效 | ✅ |

### 2.5 图片与媒体

| 动效点 | 位置 | 参数 | 状态 |
| --- | --- | --- | --- |
| 网络图淡入 | `components/ZhihuNetworkImage.kt` | 解码成功（`onSuccess`）→ 240ms 淡入，因此 AVIF/WebP 走同一路径；占位色统一 `surfaceContainerHighest`，与骨架同色，不出现「两段色差」 | ✅ |
| 错峰等待 | 同上 | `fadeInDelayMillis` 用动画时钟实现，关动画时立即终态 | ✅ |
| 图片查看器 | `components/UiComponentSupport.kt` | 打开 0.92→1（§2.2）；关闭为宿主 Dialog dismiss，无播放余地（§3） | ⚠️ |

### 2.6 手势与物理

| 动效点 | 位置 | 参数 | 状态 |
| --- | --- | --- | --- |
| 纵向回答切换 | `components/AnswerVerticalOverscroll.kt` | tanh 阻尼 + 200dp 上限 + 80dp 触发；预览卡改 `derivedStateOf` 提供进度、`graphicsLayer` 消费，拖动全程不再按帧重组 | ⚠️ |
| 横向回答切换 | `components/AnswerHorizontalPager.kt` | 300dp 上限 / 46dp 触发 / 阻尼 1.2，全部上收 `Motion.Gesture`，`answerSwitchSensitivity` 线性缩放路径未改 | ⚠️ |
| 下拉刷新 | `components/FeedPullToRefresh.kt` | `haptics.gestureThreshold()` 在 `onRefresh` 内触发，与阈值同帧 | ⚠️（振动需真机） |
| 刷新按钮吸附 | `components/DraggableRefreshButton.kt` | 松手 `springSnappy()`（无过冲，避免贴边「甩飞」）；拖动中 `snap()` 跟手 | ⚠️ |
| 预测性返回 | `components/MyModalBottomSheet.kt` | `predictiveBackProgress` 驱动缩放，`onCancel` 回弹至 0（既有实现保留） | ✅ |
| 评论行滑动 | `ui/CommentScreen.kt` | 前景 `offset {}` 布局期位移、背景图标 `graphicsLayer` | ✅ |
| 底栏随滚动隐藏 | `ui/ZhihuMain.kt` | 阈值 3px→12px；同方向累积、方向翻转清零；触摸滚动 300ms 静默期；**翻页按钮/键盘翻页（SideEffect 合成滚动）不受静默期限制**（见 §8 的回归事件） | ✅ |
| 详情页 Pager 视差 | 同上 | `graphicsLayer { translationX = pageOffset * width * 0.12f }`，绘制期读取，滑动零重组 | ✅ |

### 2.7 无障碍与性能

| 动效点 | 位置 | 结论 | 状态 |
| --- | --- | --- | --- |
| G1 组合期读动画值 | 全仓 24 个声明点 | 18 处绘制/布局期消费、3 处本轮修复、其余为颜色/文本动画的固有重组（明细见 §4） | ✅ |
| G2 整页模糊 | `ui/HomeScreen.kt`、`ui/QuestionScreen.kt` | 移除首页整页 `Modifier.blur()`（遮罩 0.16→0.32 补偿层次）、移除问答遮罩 `blur(12dp)`（渐变改 4 档 stop 补偿） | ✅ |
| G3 动画时长 0 | `PaginatedList.kt`、`ZhihuNetworkImage.kt` | 两处手写 `delay()` 错峰改为 `animate()` 时钟；其余由 Compose 自动直达终态 | ⚠️（需真机切换开发者选项确认） |
| G4 帧率 | 见 §5 | 环境口径不可用于判定 <32ms | ⚠️ |
| G5 无障碍节点 | 全部改动 | 新增 `Box` / `graphicsLayer` / `AnimatedContent` / `AnimatedVisibility` 均不产生语义节点；`ZhihuAlertDialog` 只在 `text` 外套一个无语义 `Column`；底栏图标缩放只改绘制矩阵 | ✅（节点数不变，TalkBack 朗读顺序未变） |

---

## 3. 豁免清单

| 项 | 位置 | 为什么不做 | 替代方案 |
| --- | --- | --- | --- |
| C1 首页大标题折叠 | `ui/HomeScreen.kt` 顶栏 | 首页顶栏是「搜索胶囊 + 头像/未读角标」，不是 Material 意义上的大标题。做成随滚折叠会把**搜索入口**一起收走，属于功能损失换动效，不划算 | 顶栏保持常驻；层次感由卡片阴影与圆角承担 |
| E3 视频全屏转场 | 回答内视频 | 视频在 WebView 内播放，全屏按钮由网页侧 JS 控制，Compose 拿不到这个状态，无法插入 Shared Axis Y | 保留 WebView 自带全屏行为 |
| E2 关闭方向 | 图片查看器 | 关闭是宿主 `Dialog` 的 `dismiss()`，窗口先被拆掉，动画没有播放余地；要做得先重构成 in-app overlay | 打开方向保留 0.92→1 + 淡入 |
| C4 双行顶栏标题 | `components/TwoRowsTopAppBar.kt` | 既有实现用 alpha 交叉淡化 + `clearAndSetSemantics` 切换语义，效果等价且**不改布局高度**；换 `AnimatedContent` 会引入高度抖动，并触发 `ArticleScreenInstrumentedTest` 的布局断言 | 保留既有实现 |
| D3 评论展开收起 | `ui/CommentScreen.kt` | 评论线程没有折叠态（子评论常显 + 「查看全部」入口跳转），没有可动画的展开按钮 | 「查看全部」走页面转场 |
| A6 关注按钮宽度动画 | `ui/QuestionScreen.kt` | 按钮 `weight(1f)` 平分一行，宽度由父约束决定，做宽度动画是空操作 | 只做颜色 + 图标回弹（已实现） |

---

## 4. G1 扫描结论：组合期读取动画值

**扫描口径**：全仓 `animateFloatAsState` / `animateDpAsState` / `animateIntAsState` / `animateColorAsState` / `Animatable(...)` 共 **24 个声明点**，逐个确认其值在哪里被读。

| 消费位置 | 数量 | 判定 |
| --- | --- | --- |
| `graphicsLayer { }` / `Canvas { drawRect }` / `drawWithContent { }` / `drawBehind { }` | 13 | ✅ 绘制期读取，动画期间**零重组** |
| `offset { IntOffset(...) }` 布局 lambda | 3 | ✅ 只触发布局，不触发重组 |
| `derivedStateOf` 收敛为布尔后再进组合 | 4（含本轮新增 2） | ✅ 每帧 Float 被收敛成一次状态翻转 |
| 颜色过渡（chip 容器/文字、关注按钮、赞同药丸、预览卡） | 5 | ⚠️ Compose 无法把颜色放进 `graphicsLayer`，必须在组合期读；影响面是单个小控件，且渐变期间本来就该重绘 |
| 文本计数（`animatedCountLabel`） | 2 | ⚠️ 数字即文本，只能在组合期更新；这是「数字滚动」效果的固有成本 |
| 手势期局部重组（评论行背景图标块） | 1 | ⚠️ 拖动时 `offsetX.value != 0f` 触发，范围仅一个 `Icon` |

**本轮修复的 3 处**（都是「整块内容按帧重组」，属于真正值得修的）：

1. `ui/PeopleScreen.kt`：折叠条的 `Modifier.offset(y = lerp(...))` + `.alpha(...)` → `graphicsLayer { translationY / alpha }`。
2. `ui/components/AnswerVerticalOverscroll.kt`：预览卡的 `progress: Float` 参数 → `progressProvider: () -> Float`，颜色改 `animateColorAsState`，透明度进 `graphicsLayer`；出现条件改 `derivedStateOf`。拖动全程从「按帧重组整张卡片」降到「跨 0 / 跨阈值各一次」。
3. `ui/components/VerticalReadingProgressBar.kt`：淡出保护条件 `if (alpha <= 0f && !isDragging) return` 在组合期读动画值 → 改为 `derivedStateOf { alphaState.value > 0.001f || isDragging }`，重组次数从「淡出 300ms 内每帧」降到 2 次。

**相邻但未改的一处**：同一文件里 `progress` 是 `scrollValue` 的派生值，仍在组合期参与 `thumbOffsetY` 计算，滚动时会按帧重组这条进度条。它不是动画值（是滚动派生值），改法应是把 `thumbOffsetY` 挪进 `offset { }`。已记入 §6。

---

## 5. G4 性能采样

**环境**：AVD `Medium_Phone_2`（API 35 / 1080×2400 / `swiftshader_indirect` **软件 GPU**），无窗口、无快照；采样命令 `adb shell dumpsys gfxinfo <pkg>`。

**为什么不能在信息流上采**：Lite 包首次启动必须走完 3 步使用须知 → 登录（手机号 / 扫码 / 网页登录），本环境没有可用账号，**信息流页面不可达**。第一次尝试（脚本盲滑）采到 `Total frames rendered: 0`，正是因为当时停在只有两个按钮的须知页。

**改用可控窗口**：instrumented 测试会真实渲染 Compose 界面，于是「在测试进程存活期间注入滑动、只对滑动窗口 `reset` 后统计」——这样采样的是真实 Compose 渲染，而不是空转。

| 路径 | 窗口内帧数 | Janky | P50 | P90 | P95 | P99 |
| --- | --- | --- | --- | --- | --- | --- |
| 问答详情（列表滚动 + 详情展开） | 267 | 68（25.5%） | 31ms | 65ms | 101ms | 300ms |
| 主导航 / 转场（切 tab + 滑动） | 162 | 49（30.3%） | 17ms | 150ms | 200ms | 350ms |

**结论与口径判定**

- P50 落在 17~31ms，正好是软件 GPU 下「合成一帧 1080×2400 全屏」的物理下限（约 30fps）；P95/P99 的抬升主要来自 `input swipe` 注入造成的合成手势（每帧都在改滚动位置）与首帧字体/图片栅格化，而**不是**动效曲线本身。
- 因此本轮**不宣称达到 P95 < 32ms**：该口径要求硬件 GPU。上一轮 MD3 重构在同一环境测得 P95=53ms 并登记环境豁免，本轮同属一类，登记为**环境阻塞**而非「已达标」。
- 未做改动前/后对比采样：采样当时工作区里同时存在两批未提交改动（MD3 版式打磨 + 本轮动效），`git stash` 有丢改动风险，故只给绝对值。**替代证据**是 §8：99 个 instrumented 测试全绿，说明行为无回归。
- 已知的一次真实回归与它的数据无关——底栏自动隐藏在翻页路径上失效，是被 `ZhihuMainNavigationInstrumentedTest` 抓出来的（§8），说明测试比帧率数字更能守住「手感没坏」。

**真机复测步骤（留给后续）**

```bash
adb shell dumpsys gfxinfo <pkg> reset
# 手动滑动信息流 10 秒 / 上下翻 5 个回答 / 进出详情页各 3 次
adb shell dumpsys gfxinfo <pkg> > gfxinfo.txt
# 关键指标：Janky frames 比例、95th percentile、Number Slow issue draw commands
adb shell dumpsys gfxinfo <pkg> framestats > frames.txt   # 需要逐帧时用这个
```

---

## 6. 发现但未纳入本轮

| 缺口 | 位置 | 影响 | 建议改法 / 为什么这轮不做 |
| --- | --- | --- | --- |
| 登录须知 3 步之间是硬切 | `account/LoginScreen.kt` | 首次启动连续点三次「确认」，页面瞬间替换，没有方向感 | 用 `AnimatedContent(sharedAxisX*)` 包住 `noticeStep`。**不做**的原因：每步的 `testTag` 是唯一值（`login_notice_step_1/2/3`），动画期间新旧两页同时存在，`onNodeWithTag` 会命中 2 个节点，需要同步改现有测试；属「改测试」而非「带动效」，超出本轮范围 |
| 阅读进度条 `progress` 仍在组合期读 | `components/VerticalReadingProgressBar.kt` | 滚动时这条 12dp 宽的进度条按帧重组（范围很小） | 把 `thumbOffsetY` 挪进 `Modifier.offset { }`；因不属「动画值」而未做，见 §4 末 |
| 颜色过渡类动画必然在组合期消费 | chip、关注按钮、赞同药丸、预览卡 | 渐变期间各自重组 | Compose 没有「把颜色写进绘制层」的等价物；除非整块改 `drawBehind` 手绘，代价大于收益 |
| 静态标签 chip 未套 1.04x | `app/src/full/.../NLPKeywordManagementScreen.kt` | 关键词预览用 `FilterChip(selected = true, onClick = {})`，是标签不是开关 | 套 `ZhihuFilterChip` 会让它**永久**放大 4%、改变静态版式；保留 M3 默认样式更合理。（评论排序 chip 有真实选中态，不属此类，已迁 `ZhihuSuggestionChip`） |
| 顶栏自动隐藏的 padding 收拢 | `ui/ZhihuMain.kt` | 底栏隐藏靠 `AnimatedVisibility` 滑出，内容区 padding 同帧变化，无二次动画 | 现状已是 MD3 推荐做法；额外做 padding 动画会让列表在滑动中「追着」底栏移动 |

---

## 6.1 收尾复核阶段发现并修复

这两项不在原任务清单里，是交付前回溯「为什么测试会挂」「A4 是否真的全覆盖」时查出来的。

| 缺陷 | 位置 | 现象 | 修法 |
| --- | --- | --- | --- |
| 骨架流光用无限动画 | `components/ZhihuSkeleton.kt` | `rememberInfiniteTransition` 让 Compose 测试的 `waitForIdle()` 永不返回：首页加载期骨架常驻时，`ZhihuMainNavigationInstrumentedTest.pageTurnHidesAndRevealsBottomBarWhenAutoHideEnabled` 整条挂死（实测跑满 600s 未结束，且没有断言失败信息，说明卡在 idle 而非断言） | 改为扫光 3 轮（1.4s/轮）后停在终点，之后骨架是静态占位；位移从组合期 `Float` 改为 `State<Float>`，在 `drawBehind {}` 里求值，骨架卡片不再逐帧重组（顺带满足 G1） |
| 评论排序 chip 未参与 A4 | `ui/CommentScreen.kt` | 「最热 / 最新」两个 `SuggestionChip` 有真实选中态，但只硬切文字颜色，没有 1.04x 缩放、没有颜色过渡 | 新增 `ZhihuSuggestionChip`：保留 SuggestionChip「无填充 + 文字着色」外观（不硬换成 `ZhihuFilterChip` 的药丸填充，避免改变既有视觉语言），补 1.04x `springSnappy`；颜色经 `animateColorAsState(fadeTween)` 过渡。两个 `testTag` 原样保留 |

---

## 7. 真机复测清单

| 项 | 关注点 |
| --- | --- |
| 振动（A1 / A2 / A6 / F2） | 需要真实振动器：点赞 `tick`、关注 `confirm`、开关 `confirm`、下拉刷新阈值 `gestureThreshold` 是否与视觉同帧 |
| 慢放录屏 | 0.25x 逐帧核对 A1 回弹峰值 1.28x、A4 chip 1.04x、B4 图标 45°、B5 FAB 30dp 不跳动 |
| 帧率 | 硬件 GPU 上按 §5 命令采信息流 / 回答切换 / 转场三条路径，目标 P95 < 32ms |
| 动画时长 0 | 开发者选项把三档动画全关：确认无空白帧、无错位、列表错峰立即终态（G3） |
| 120Hz 屏 | `springSnappy` 在 120Hz 上的收敛是否「太快看不清」，必要时上调 stiffness |
| TalkBack | 底栏图标缩放、对话框内容淡入、图片查看器整层缩放后，节点数与朗读顺序不变（G5） |
| 手势边界 | `answerSwitchSensitivity` 调到最小/最大时，80dp/46dp 阈值线性缩放是否仍符合直觉 |

---

## 8. 回归验证记录

| 阶段 | 命令 | 结果 |
| --- | --- | --- |
| 静态检查 + 编译 | `./gradlew ktlintCheck :shared:compileKotlinJvm :app:assembleLiteDebug` | BUILD SUCCESSFUL |
| 行为回归（第一次） | `./gradlew :app:connectedLiteDebugAndroidTest`（全量） | 99 tests / **1 failed** |
| 修复后复跑 | 同上 | 99 tests / **0 failed** ✅ |
| 帧率采样 | 见 §5 | 已记录，环境口径 |

**那次唯一的失败值得单独记一笔**（这是本轮唯一被自动化抓到的真实回归）：

- 用例：`ZhihuMainNavigationInstrumentedTest.pageTurnHidesAndRevealsBottomBarWhenAutoHideEnabled`
- 现象：`ComposeTimeoutException: Condition still not satisfied after 5000 ms`（底栏没有隐藏）
- 根因：我给底栏的 `NestedScrollConnection` 加了 `if (source != NestedScrollSource.UserInput) return`，想「只响应手指滚动」。但翻页按钮/键盘翻页是通过 `NestedScrollDispatcher` 以 **`NestedScrollSource.SideEffect`** 合成滚动事件的（`DraggablePageTurnButtons.kt` 的注释明确写了「自动隐藏栏也应响应翻页」）。过滤掉 SideEffect 等于把「翻页时自动隐藏底栏」整条功能掐掉——这是水石墨用户的高频路径。
- 修法：不再按 source 过滤；**静默期只作用于触摸滚动**（`if (source == UserInput) { 300ms 判定 }`）。抖动来自手指来回蹭，而翻页与惯性是离散意图，不该被拦。方向累积 12px + 方向翻转清零继续保留，频闪仍被治住。
- 结论：**回归测试拦住了比帧率数字更重要的东西**。也正因为这条用例只有 1 个测试类被跑到时才暴露（第一次用逗号分隔的 class 过滤只执行了第一个类），后续都改为跑全量。

---

## 9. 与 `motion-log.md` 的对应

`motion-log.md` 是逐条任务台账（ID / 文件 / 参数 / 微调轮数），本文档是状态审计（全量清单 / 豁免 / 扫描结论 / 采样数据）。两份文档的记录一致；本文档 §4、§5 补充了台账里只有结论没有数据的部分。
