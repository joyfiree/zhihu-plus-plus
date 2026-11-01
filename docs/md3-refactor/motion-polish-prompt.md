# 提示词：Zhihu++ 全局动效深度打磨（可直接整份复制给 AI）

> 使用方式：把本文件**全文**发给执行任务的 AI（Claude Code / Codex / Cursor Agent 均可）。
> 提示词已包含项目事实、约束、任务清单与验收标准，AI 不需要额外上下文即可开工。
> 预计工期：按「一个任务一次提交」的粒度，完整走完需要 **30–60 小时**（含实机验证与两轮微调）。

---

## 0. 你的角色与任务

你是这个项目的**动效工程师（Motion Engineer）**。目标不是"加几个动画"，而是把整个 App 的动效提升到**系统级应用**的水准：每一处状态变化都有符合物理直觉的过渡，每一次手势都有跟手的反馈，且任何时刻都不掉帧、不跳变、不打断用户操作。

这份工作要求**长时间打磨**。禁止为了"看起来做完了"而批量套模板动画。每一个动效点都必须回答三个问题：

1. **意图**：这个动效解决什么认知问题？（空间关系？因果确认？状态延续？）
2. **参数**：为什么是这个时长/曲线/弹簧？(参考 Material 3 motion spec 与真实手感)
3. **验证**：在真机上以 0.25x 慢放看过一遍了吗？快速连续操作会跳变吗？

三个问题答不上来，就不要提交。

---

## 1. 项目事实（开工前必读）

| 项 | 值 |
| --- | --- |
| 源码位置（WSL） | `/home/lvyuxing/zhihu-plus-plus` |
| Windows 构建副本 | `C:\dev\zhihu-plus-plus`（改完需 robocopy 同步） |
| 技术栈 | Kotlin 2.4.0 + Compose Multiplatform 1.11.1 + material3 **1.10.0-alpha05**（被 force，禁止升级或混用 androidx material3） |
| 构建 | JDK17 `C:\Android\jdk-17`，SDK `C:\Android\Sdk`，Gradle 9.7.1（wrapper） |
| 构建命令 | `java -Dorg.gradle.java.installations.paths=C:\Android\jdk-17 -classpath gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :app:assembleLiteDebug` |
| 变体约束 | 任务名**必须**带 `Lite` 或 `Full`（`shared` 模块靠任务名决定挂哪个源集），否则编译缺类 |
| 包名 | `com.github.zly2006.zhplus.lite`（Lite/Debug） |

### 1.1 已有的动效基建（必须复用，禁止另起一套）

| 文件 | 内容 |
| --- | --- |
| `shared/.../ui/designsystem/Motion.kt` | `DURATION_SHORT(200)/MEDIUM(300)/LONG(450)`、`Emphasized*Easing`、`StandardEasing`、`springMedium()`、`springSnappy()`、`enterTween()`、`exitTween()`、`fadeTween()`、`Gesture` 常量 |
| `shared/.../ui/components/ZhihuMotionEffects.kt` | `BounceOnChange(trigger) { }` —— 二元状态的一次性回弹 |
| `shared/.../ui/components/ZhihuNetworkImage.kt` | 图片占位色 + 240ms 淡入 |
| `shared/.../ui/components/ZhihuSkeleton.kt` | shimmer 骨架卡片 |
| `shared/.../ui/components/PaginatedList.kt` | 列表错峰入场（28ms 递增，最多 8 项） |

### 1.2 已经完成的动效（不要重复实现，可优化参数）

列表错峰入场、卡片按压缩放（`pressScale`）、底栏图标 spring 放大、赞同药丸缩放、收藏星标回弹、空态淡入、图片淡入、骨架 shimmer、导航 slide+fade 转场、FAB 贴边吸附（spring）。

---

## 2. 硬性约束（违反即回滚）

1. **testTag 不可变**：`nav_tab_*`、`article_content`、`comment_like_button_*`、`QUESTION_*`、`HOME_*` 等既有 tag 一个都不能改；UI 测试靠它们定位。
2. **偏好语义不可破坏**：`feedCardStyle`、`duo3_*`、`contentFontSize/LineHeight`、`answerSwitchSensitivity`、`disableBottomSheetRoundedCorners`、`hapticFeedbackEnabled` 的读写含义不变。
3. **KMP 原子提交**：改 `commonMain` 的 expect/契约必须同一次提交内同步所有平台 actual，保证 `:shared:compileKotlinJvm` 也能过。
4. **不引入新依赖**：动画/Haptic 一律用现有 Compose API；Lite 包体积不得增加超过 200 KB。
5. **不动信息架构**：不改导航结构、不增删底栏项、不重排页面层级。
6. **性能红线**：动画期间不得出现组合期读取动画值（`Animatable.value` / `animate*AsState` 的 `by` 委托在 `Modifier` 里读要放进 `graphicsLayer { }` lambda）；单帧 JS 预算 16.6ms。
7. **一条任务一次提交**，提交信息用中文 `feat(motion): ...` / `perf(motion): ...`。

---

## 3. 工作方法（每个动效点都走这个循环）

```
① rg 复核该文件现状（不要凭记忆改）
② 写清楚：意图 / 参数理由 / 预期观感（3 行注释或提交说明）
③ 最小实现（优先复用 Motion token 与现有原语）
④ assembleLiteDebug 必须通过
⑤ 真机/模拟器：正常速度看 1 遍 + 开发者选项动画 0.25x 慢放看 1 遍
⑥ 快速连续操作（连点、进页立刻返回、连续切回答）验证可中断、无跳变
⑦ 与上一版录屏并排对比，至少微调 1 轮参数
⑧ 更新 docs/md3-refactor/motion-log.md（本任务一行）
```

**禁止**：不改参数就交；只在一台设备上试；用「看起来还行」代替慢放验证。

---

## 4. 任务清单（按批次执行，逐条勾选）

> 每条的「验收」都要真机录屏；标 ⚠️ 的改动会移动布局坐标，必须连带核对测试断言。

### 批次 A：微交互确认感（P0，先做）

| ID | 位置 | 要求 | 验收 |
| --- | --- | --- | --- |
| A1 | `ui/CommentScreen.kt` 点赞行 | `BounceOnChange(trigger = isLiked)` 包图标；点击时 `haptics.tick()`；点赞数用 `animateIntAsState` 滚动 | 连点 5 次不抖、不重叠 |
| A2 | `ui/QuestionScreen.kt` 关注按钮 | 关注成功：按钮 width/color 用 `animateFloatAsState`+`animateColorAsState` 过渡到「已关注」，配合 `haptics.confirm()` | 状态切换无跳变 |
| A3 | `ui/home` 赞同数 | 数字用 `animateIntAsState(value, tween(220))`，与药丸缩放同帧 | 慢放确认同帧 |
| A4 | 所有 `FilterChip`/`AssistChip` | 选中态用 `animateColorAsState` + 1.04x scale（`Spring.StiffnessMedium`） | 连续切换不抖 |
| A5 | 卡片点击 | 统一走 `pressScale(interactionSource)`；确保 `clickable` 与 `pressScale` 共用同一个 `MutableInteractionSource` | 按压有回弹，不吞点击 |
| A6 | 开关 Switch | 切换时 `haptics.confirm()`，并确认 thumb 动画未被自定义 colors 破坏 | 触觉与动画同帧 |
| A7 | `ui/components/PaginatedList.kt` footer | 转圈用 `AnimatedVisibility(fadeIn/fadeOut(fadeTween()))` 包一层 | 出现/消失不闪 |

### 批次 B：转场与连续性（P1）

| ID | 位置 | 要求 | 验收 |
| --- | --- | --- | --- |
| B1 | `ui/ZhihuMain.kt` 主/详情 NavHost | 统一 Shared Axis X：enter `slideInHorizontally(enterTween) + fadeIn`，exit `slideOutHorizontally(exitTween) + fadeOut`；确认「进入→立刻返回」可中断 | 连按返回不跳变 |
| B2 | `ui/components/MyModalBottomSheet.kt` | 统一关闭出口：所有 dismiss 先 `sheetState.hide()` 再回调；scrim 与 sheet 用同一曲线 | 无「瞬间消失」 |
| B3 | 24 处 `AlertDialog` | 抽 `ZhihuAlertDialog` 包装：28dp 圆角、统一按钮排布、进入用 dialog 默认 + 内容 `fadeIn` | 全站弹窗观感一致 |
| B4 | `ui/HomeScreen.kt` 创作 FAB | 打开菜单时 Add 图标 `rotationZ` 0→45°（`springSnappy`），FAB 自身 `pressScale` | 图标旋转连贯 |
| B5 | `ui/HomeScreen.kt` 刷新 FAB | 图标 ↔ 转圈用 `AnimatedContent(fadeIn togetherWith fadeOut)`，容器尺寸统一 30dp | 不抖动 |
| B6 | `ui/ArticleScreen.kt` 回答切换 | 四向转场保持方向语义，进入 `EmphasizedDecelerate`、退出 `EmphasizedAccelerate`，时长 `DURATION_MEDIUM` | 连续切 5 个回答不卡 |

### 批次 C：滚动联动（P2，改结构，需谨慎）

| ID | 位置 | 要求 | 验收 |
| --- | --- | --- | --- |
| C1 | 首页顶部 | 大标题随滚动折叠：`TopAppBarDefaults.exitUntilCollapsedScrollBehavior`，标题字号在 28→20 之间插值 | 折叠/展开跟手 |
| C2 | 底栏 | 上滑隐藏、下拉出现，用 `slideInVertically(enterTween)`；滚动停止 300ms 内不反复触发 | 快速上下滑不闪 |
| C3 | `ui/ZhihuMain.kt` Pager | 页面切换加视差：`graphicsLayer { translationX = offset * 0.15f }`（lambda 内读 offset） | 60fps，无重组风暴 |
| C4 | 详情页顶栏 | `TwoRowsTopAppBar` 展开↔收起时标题与副标题交叉淡入（`AnimatedContent` + `fadeThrough`） | ⚠️ 会移动标题底边，需核对 `ArticleScreenInstrumentedTest` |

### 批次 D：列表与加载（P1）

| ID | 位置 | 要求 | 验收 |
| --- | --- | --- | --- |
| D1 | `PaginatedList` | 骨架 → 内容用 `Crossfade(fadeTween())`，避免"骨架突然变卡片" | 首屏加载不跳版 |
| D2 | 列表项增删 | `animateItem(fadeInSpec, fadeOutSpec, placementSpec)` 全用 `springMedium()`；确认插入分页数据时不闪烁 | 滚到底加载不抖 |
| D3 | 评论展开/收起 | `animateContentSize(springMedium())`，箭头图标旋转 180° | 子评论展开顺滑 |
| D4 | 图片墙 | 网格图片逐张淡入（index * 40ms，最多 9 张） | 不卡顿 |

### 批次 E：图片与媒体（P2）

| ID | 位置 | 要求 | 验收 |
| --- | --- | --- | --- |
| E1 | `ZhihuNetworkImage` | 保证 AVIF/WebP 解码后仍淡入；占位色与骨架色一致 | 弱网下无白块 |
| E2 | 图片查看器 | 打开时 `scaleIn(0.92f) + fadeIn`，关闭反向；双击缩放用 spring | 打开关闭连贯 |
| E3 | 视频播放器 | 进入全屏用 Shared Axis Y | 无黑闪 |

### 批次 F：物理手感（P2）

| ID | 位置 | 要求 | 验收 |
| --- | --- | --- | --- |
| F1 | `AnswerVerticalOverscroll` | 阻尼/阈值/回弹收敛到 `Motion.Gesture`，与 `answerSwitchSensitivity` 联动语义不变 | 0.5/1.0/2.0 三档线性 |
| F2 | 下拉刷新 | 触发瞬间 `haptics.gestureThreshold()`，指示器缩放跟手 | 触发点与震动同帧 |
| F3 | `DraggableRefreshButton` | 拖拽跟手、松手 spring 吸附（已改，确认过冲幅度） | 吸附不甩飞 |
| F4 | 边缘返回 | 预测性返回进度驱动缩放/圆角（`predictiveBackProgress`） | 手势取消可回弹 |

### 批次 G：性能与无障碍（P1，收尾前必须做）

| ID | 要求 | 验收 |
| --- | --- | --- |
| G1 | 全仓扫描 `Modifier` 内组合期读取动画值，改为 `graphicsLayer { }` 内读取 | `rg "\.value" ui/**/*.kt` 逐条复核 |
| G2 | 移除整页 `Modifier.blur()` 动画（首页遮罩），改 scrim alpha | 低端机不掉帧 |
| G3 | 系统「动画时长 0」时所有动画直接跳到终态（`LocalMotionDurationScale` 或被 Compose 自动处理的部分要实机确认） | 关动画后无卡顿/无错位 |
| G4 | `dumpsys gfxinfo` 采主页滚动、回答切换、转场三条路径 | P95 < 32ms（模拟器口径），记录数值 |
| G5 | TalkBack 下新加的动画不改变语义节点数量 | 节点数与改动前一致 |

---

## 5. 交付物

1. 代码：每任务一次提交，信息格式 `feat(motion): 评论点赞回弹与触觉同步`。
2. `docs/md3-refactor/motion-log.md`：每条任务一行（ID / 文件 / 参数 / 录屏文件名 / 微调轮数）。
3. `docs/md3-refactor/motion-audit.md`：全部动效点清单与状态（✅/⬜/⚠️）。
4. PR 描述必须包含：意图、参数选择理由、慢放截图/录屏、gfxinfo 数值、回归的测试用例列表。

---

## 6. 反模式清单（看到就删）

- 用 `tween(300)` 这种裸数字，而不走 `Motion.*`。
- 给列表每一项都加"飞入"，导致首屏像烟花。
- 弹窗/弹层只有进入没有退出。
- 动画期间改变布局尺寸（应只改 `graphicsLayer` 的 alpha/scale/translation）。
- 同一个交互两处震动。
- 用无限循环动画装饰静态内容。
- 为了"动效丰富"给所有图标加跳动。

---

## 7. 完成定义（DoD）

1. 批次 A–G 全部勾选，或对某项写明豁免理由与替代方案。
2. 三条关键路径（主页滚动 / 回答切换 / 页面转场）连续操作 1 分钟无卡顿、无跳变。
3. 全站不存在「瞬间出现/消失」的可视元素（骨架、空态、弹层、FAB 除外且已动画化）。
4. 关掉系统动画后功能完全可用。
5. `motion-log.md` 与 `motion-audit.md` 与代码一致。
