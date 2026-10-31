# MD3 重构执行状态（核准表）

> 执行时间：2026-10-31｜依据：`docs/md3-refactor-plan.md` v1.0｜执行者：AI Agent
> 配套：`audit.md`（硬编码收敛）、`haptic-matrix.md`（触觉矩阵）、`contrast-report.md`（对比度）、`final-report.md`（终版验收）、`docs/ai-ui-design-guide.md`（token 契约）

## 0. 本轮执行环境（与首轮差异）

首轮执行环境没有 JDK/Android SDK，无法编译或上设备；**本轮已补齐构建与 AVD 能力**：

- WSL2 Ubuntu 26.04 + OpenJDK 17 + Gradle 9.7.1 + Android SDK 37（`local.properties` 指向本机 Android SDK）；
- 本地 AVD `Medium_Phone_2`：API 35 / 1080×2400 / 420dpi / `swiftshader_indirect` 软件 GPU；
- 已执行：`assembleLiteDebug`、`assembleLiteDebugAndroidTest`、`ktlintFormat`/`ktlintCheck`、`:shared:jvmTest`、定向 instrument 回归、真实 Compose 渲染截图、`dumpsys gfxinfo`。

状态口径不变：✅ = 代码完成且有本轮真实构建/测试证据；🟡 = 代码完成但验收依赖真机/账号等本环境无法提供的能力；⛔ = 阻塞。

## 1. 任务核准表（2026-10-31 第四轮更新）

| 任务 | 状态 | 本轮证据 / 改动 | 待补 |
| --- | --- | --- | --- |
| T0.1 视觉基线截图 | 🟡 | 无「重构前」可执行基线（首轮无构建能力），已改为采集**重构后** 12 张截图（11 张页面 + 登录页）作为终版对照（`docs/md3-refactor/screenshots/`） | 若需前后对比，需用重构前 APK 在同机补基线 |
| T0.2 designsystem 骨架 | ✅ | `designsystem/` 6 文件；新增 `DesignSystemTokensTest`（时长/间距/触控下限）；`:shared:jvmTest` 通过 | — |
| T0.3 硬编码审计 | ✅ | `audit.md` 更新：色 66（白名单外 0，grep 67 含 1 处 KDoc 示例）、`tween(数字)` 0（`Motion.kt` 内 2 处为注释示例）、圆角字面量仅剩 7 处单点档位、裸字号 2 处内容渲染豁免 | — |
| T1.1 自定义背景 surface 联动 | ✅ | 重写 `buildZhihuColorScheme`：WCAG 极性判定 + 只变明度阶梯 + 前景兜底；自定义背景截图 2 张 | — |
| T1.2 强调色接入 ExtendedColors | ✅ | 组件内 `0xFFF57C00/0xFF4CAF50` 零命中；收藏/TTS/赞同走 `extendedColors` | 文章页操作栏截图（无登录态） |
| T1.3 兜底 ColorScheme 清理 | ✅ | 模板 Purple 系列已删除；material-kolor 纯 Kotlin 无平台兜底分支，以消除路径完成 | — |
| T1.4 深色对比度校验 | ✅ | 首轮红：知乎蓝 `onSurfaceVariant` 2.99:1、阶梯非单调 → 修复后 `ThemeContrastTest` 3/3 绿（红→绿证据见 `contrast-report.md`） | — |
| T2.1 Typography 完整落地 | ✅ | 15 角色（中文行高/零字距）；裸字号 29→2（均为内容渲染豁免） | — |
| T2.2 Shapes 接入 | ✅ | `MaterialTheme(shapes = ZhihuShapes)`；`ZhihuShapeTokens` 统一卡片/缩略图/弹层圆角；新增 `ZhihuDropdownAnchor`（8dp 圆角下拉字段）替换 8 处 OutlinedTextField；收藏夹列表改为 filled card；`disableBottomSheetRoundedCorners` 语义保留 | Duo3 开/关 × 明/暗四组合截图已完成 |
| T3.1 导航转场对齐 MD3 | ✅ | 主/详情 NavHost 改为 enter+exit 成对的共享轴转场（旧页左移 1/4 + 淡出）；四向回答切换统一参数来源；导航 instrument 测试通过 | 需要录屏级手感确认（本机无录屏评价者） |
| T3.2 组件动效 token 迁移 | ✅ | `tween(数字)` 在 commonMain 业务代码零命中；`spring` 回弹收敛到 `Motion.overscrollSettle()` | — |
| T3.3 列表与滚动动画统一 | 🟡 | `PaginatedList` item 动画走 `Motion.springMedium()`；分页插入行为未改 | 「新增/删除项动画只在真实变化时触发」需真机观察 |
| T4.1 触觉总开关 | ✅ | `HapticController` 触发瞬间读偏好；外观设置「交互反馈」分组已接入；设置搜索可检索 | 触觉两态需真机振动器 |
| T4.2 触点覆盖矩阵 | 🟡 | 底栏 tab tick、设置开关 confirm、下拉刷新 gestureThreshold、长按 longPress 均走控制器；Pager 跟手滑动按「克制」原则不加 | `dumpsys vibrator_manager` 两态（模拟器无振动器） |
| T4.3 触觉审计 | 🟡 | `haptic-matrix.md` 登记；`NoopHapticFeedback` 豁免保留；无绕过调用（`rg` 仅命中定义处） | 双击点赞无双震的录屏证据 |
| T5.1 overscroll 手感 | ✅ | 阈值/阻尼/回弹收敛为具名常量并注释物理含义；回弹统一 `Motion.overscrollSettle()`；`answerSwitchSensitivity` 联动语义未改；`AnswerVerticalOverscrollInstrumentedTest` 通过 | 灵敏度 0.5/1.0/2.0 三档实机线性手感 |
| T5.2 下拉刷新手感 | ✅ | `onRefresh` 接 `gestureThreshold()`；indicator 色维持 primaryContainer 体系 | 4 个列表页抽测（无登录态仅代码级） |
| T5.3 预测性返回 | ✅ | `enable_predictive_back` 补齐运行时读取：控制 `MyModalBottomSheet` 是否跟手预览返回；文案改为如实描述「系统级动画由 Android 版本决定」；manifest `enableOnBackInvokedCallback` 保持开启 | API 34+ 真机 peek 动画实测（模拟器可启动但未做手势录像） |
| T5.4 触控目标审查 | ✅ | `audit.md` 第 6 节：M3 IconButton 48dp、FeedCard 菜单 24→48dp 等 | 抽查 10 触点 |
| T6.1 无障碍走查 | 🟡 | 审计 80 个 `IconButton`/12 个 FilledIconButton+FAB：唯一真实交互缺口（GitHub Token 可见性切换）已补描述，其余 14 处 null 均为装饰/带文字标签位置；清单与结论见 final-report §7 | TalkBack 实机闭环（本机无无障碍交互环境） |
| T6.2 动效性能验证 | 🟡 | 已采集 gfxinfo：P95 53ms（软件 GPU AVD + instrument 开销），未达 32ms；按计划登记**环境豁免**并在 `final-report.md` 写明口径 | 真机/硬件加速环境复测 |
| T6.3 文档同步 | ✅ | `status.md`/`audit.md`/`contrast-report.md`/`final-report.md`/`ai-ui-design-guide.md` 同步 | — |
| T6.4 全量回归与终版对比 | 🟡 | 终版 32 张截图；构建/安装/定向 37 个 instrument 用例通过（9 个相关用例类）；**release lite 构建 + 临时签名安装 + 启动冒烟通过** | 全量 instrument 交 CI；真实登录态截图 |

## 2. 本轮修复的真实缺陷（详见 final-report.md §2）

1. 3 处编译错误（`HorizontalDivider(startIndent)`、缺 `Motion` 导入、缺 M3 opt-in）。
2. 自定义背景色可读性（品牌蓝 `onSurfaceVariant` 2.99:1、surface 阶梯亮度非单调）→ WCAG 修复并红→绿。
3. 登录页三档模式按钮折行（默认 24dp 内容边距）→ 12dp 收敛后单行。
4. 日报错误态泄漏原始序列化异常 → 友好文案 + 重试。
5. material3 1.5 默认 Expressive 竖条滑块与全局形态语言冲突 → 新增 `ZhihuSlider`（圆形 thumb、语义强调色）并在设置页/导出/取色器统一。

## 3. 关键决策与偏差（需 review）

1. **T0.1 改为「终版截图」而不是「前后对比」**：重构前 APK 在本环境不可构建，无法产出同机基线；已用 11 张终版截图 + 结构化报告替代，并在报告标注限制。
2. **T1.3 以「消除路径」代替固化静态 fallback**：material-kolor 为纯 Kotlin，保留静态方案是死代码。
3. **T3.1 采用「旧页左移 1/4 + 淡出」的共享轴转场**：比 iOS 式覆盖更符合 MD3，且两页同时动画后可中断；如实机 review 认为位移过重，可只调 `-it / 4` 系数。
4. **T5.3 预测性返回落地为「自绘弹层跟手预览」开关**：manifest 级系统动画无法运行时开关，因此把开关绑定到本应用可控的弹层 preview，并修改文案避免误导。
5. **T6.2 不宣称达标**：软件 GPU 模拟器口径下 P95=53ms，登记环境豁免，真机需复测。
6. **滑块形态**：保留离散 tick（表达 steps 语义），只把 thumb 从 Expressive 竖条换成 20dp 圆形。

## 4. 建议的下一步

1. 有真机后补：TalkBack 走查、触觉两态、预测性返回 peek 录屏、真实登录态 10 页面截图、gfxinfo 真机复测。
2. 全量 instrument 回归交 GitHub CI。
3. 人工 review 通过后再 commit / 提 PR（项目约定：review 前不合并）。