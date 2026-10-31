# Zhihu++ MD3 重构终版报告（T6.4）

> 执行时间：2026-10-31（第二轮，具备构建 + AVD 能力后）｜依据：`docs/md3-refactor-plan.md` v1.0
> 环境：WSL2 Ubuntu 26.04 / OpenJDK 17 / Gradle 9.7.1 / Android SDK 37 / 本地 AVD `Medium_Phone_2`（API 35，1080×2400，420dpi）/ lite debug 变体。

## 1. 本轮结论

| 项 | 结果 | 证据 |
| --- | --- | --- |
| 构建 | ✅ `assembleLiteDebug`、`assembleLiteDebugAndroidTest` 通过 | `/tmp/build6.log`：`BUILD SUCCESSFUL`；APK 产物 `app/build/outputs/apk/lite/debug/app-lite-debug.apk` |
| 格式化 | ✅ `ktlintFormat` + `ktlintCheck` 通过 | `FMT_EXIT=0` / `CHECK_EXIT=0` |
| 主题对比度 | ✅ `ThemeContrastTest` 3/3 通过（修复前红→修复后绿） | 见 `contrast-report.md` |
| JVM 单测 | ✅ `:shared:jvmTest` 422 个既有用例通过（首轮），主题专项修复后定向通过 | `/tmp/build5.log` |
| 定向 UI 回归 | ✅ 外观设置 / 主导航 / 回答切换 / 首页公告 4 个 instrument 用例类通过 | `instrument-regress.log` |
| 页面截图 | ✅ 9 张最终渲染截图（明/暗 × 多页面） | `screenshots/`，见 §3 |
| 帧统计 | ⚠️ 已采集，未达 `<32ms` 目标；按计划登记豁免（软件 GPU AVD） | 见 §4 |

## 2. 本轮修复的真实缺陷（设备/构建环境才能暴露）

1. **编译错误 3 处**：`ZhihuLayout` 使用了 material3 不存在的 `HorizontalDivider(startIndent=)`；`QuestionScreen` 缺 `Motion` 导入；`BlocklistSettingsScreen` 缺 `ExperimentalMaterial3Api` opt-in。→ 已修复，构建转绿。
2. **自定义背景色可读性**：品牌蓝背景上 `onSurfaceVariant` 仅 2.99:1，且 surface 阶梯亮度非单调。→ 重写 `buildZhihuColorScheme`（WCAG 极性判定 + 前景兜底 + 只变明度的阶梯），红→绿。
3. **登录页模式按钮折行**：三等分按钮用默认 24dp 内容边距，`手机号登录`/`备用网页登录` 被挤成两行。→ 内容边距收敛 12dp，实测单行。
4. **日报页错误态泄漏原始异常**：直接把 `Daily/StoriesResponse are required...` 序列化错误展示给用户。→ 改为友好文案 + 「重试」动作（复用 `ZhihuErrorState`）。
5. **滑块形态不统一**：material3 1.5 默认 Expressive 竖条 thumb（4×44dp）在设置页与圆形开关/药丸按钮语言冲突。→ 新增 `ZhihuSlider`（20dp 圆形 thumb、语义强调色），替换设置页 6 处 + 导出弹层 + 取色器 3 通道。

## 3. 最终截图（真实渲染来源）

采集方式：`Md3ScreenshotCaptureTest`（临时 instrument 用例，取证后已删除）挂载真实 composable + mock 数据，`captureToImage()` 落盘后 adb 拉取；截图文件位于 `docs/md3-refactor/screenshots/`（共 12 张 PNG + 1 份 gfxinfo 原始输出）：

| 文件 | 页面 | 主题 |
| --- | --- | --- |
| `home_light.png` / `home_dark.png` | 首页信息流（FeedCard list 样式、搜索药丸、通知入口、FAB） | 明 / 暗 |
| `main_shell_dark.png` | 主壳（5 项 M3 NavigationBar + 搜索栏 + FAB） | 暗 |
| `search_light.png` | 搜索页（搜索药丸、历史区） | 明 |
| `appearance_settings_light.png` / `appearance_settings_dark.png` | 外观与阅读设置（LargeTopAppBar、分组圆角列表、SegmentedButton、ZhihuSlider） | 明 / 暗 |
| `home_custom_bg_light.png` | 自定义背景色（米黄）下的首页：验证 surface 阶梯联动 | 明 |
| `appearance_settings_custom_bg_dark.png` | 自定义背景色（深蓝）下的设置页：容器层级/文字对比度 | 暗 |
| `question_light.png` | 问题页（大标题、统计、M3 按钮、回答排序、回答列表） | 明 |
| `notifications_dark.png` | 消息页（居中顶栏、分类入口） | 暗 |
| `daily_dark.png` | 日报页错误态（ZhihuErrorState + 重试） | 暗 |
| `login_after_fix.png` | 登录页（真实 App 冷启动截图，修复模式按钮折行后） | 明 |

> 限制：本机没有可用知乎账号（`.secret/account.json` 与备份均不存在），无法在真实登录态下截图；上表截图来自 mock 登录态 + mock API 的真实 Compose 渲染，不是设计稿或旧图。登录页（`login_after_fix.png`）为真实 App 冷启动截图。

## 4. 动效性能（T6.2，AVD 口径）

采集方式：临时用例在真实 Compose 列表中滚动 30 条首页信息流（12 次上滑 + 12 次下滑），同一进程内 `dumpsys gfxinfo reset` → 滚动 → `dumpsys gfxinfo`，原始输出见 `screenshots/gfxinfo-home-scroll.txt`。

```
Total frames rendered: 239
Janky frames: 66 (27.62%)
50th percentile: 34ms   90th percentile: 48ms   95th percentile: 53ms   99th percentile: 150ms
```

未达计划 `P95 < 32ms`。**豁免判定**：本机 AVD 使用 SwiftShader 软件 GL（`hw.gpu.mode=swiftshader_indirect`，无硬件 GPU 直通），GPU 直方图存在 46 帧 4950ms 的超时级样本；instrument 进程还叠加了测试框架同步开销，该口径不能代表真机性能。真机/硬件加速 CI 上的数值需另行采集，已登记为待补项而不是宣称达标。

## 5. 功能与交互保持正常的验证

- **导航结构**：主/详情 NavHost 改为共享轴式 enter+exit 对（进入从右滑入并淡入，退出左移 1/4 并淡出；pop 对称），`nav_tab_*` testTag、底部导航显隐、双栏/单栏分支未改；`ZhihuMainNavigationInstrumentedTest` 通过。
- **回答切换**：overscroll 阈值/阻尼/回弹值未改（仅收敛为具名常量并补注释），四向转场参数统一到 `Motion`；`AnswerVerticalOverscrollInstrumentedTest` 通过。
- **设置页**：分组/行高/开关/搜索索引语义未改，仅视觉与滑块组件替换；`AppearanceSettingsScreenInstrumentedTest` 通过。
- **首页**：FeedCard 两种样式（divider/card）与 Duo3 分支语义保留，只统一取值来源；`HomeAnnouncementRegressionInstrumentedTest` 通过。
- **触觉**：所有触点经 `HapticController`，总开关在触发瞬间读取；未新增/删除业务动作。
- **设置项**：`enable_predictive_back` 从「无运行时读取」补齐到 `MyModalBottomSheet`（控制自绘弹层是否跟手预览返回），开关文案同步说明系统级动画由 Android 版本决定；自定义背景/主题色/字号等既有偏好 key 语义未变。

## 6. 未完成 / 不能声称完成的部分

1. **全量 instrument 回归未在本地执行**（遵守 CLAUDE.md：全量交 CI）；仅执行 4 个相关用例类 + 2 个临时取证类。
2. **真实登录态截图与 TalkBack 走查**：无账号、无可用无障碍服务交互环境，未完成；已保留 `contentDescription=null` 审计清单。
3. **真机/硬件加速帧统计、预测性返回逐版本实测（API 33/34+）**：需真机或远端 AVD，未完成。
4. **Git 提交 / PR**：按项目约定需人工 review 后再提交；所有改动停留在工作区，未 commit。

---

## 7. 第三轮补拍与细节打磨（2026-10-31 续）

本轮在 AVD 上补拍了此前未覆盖的页面，并修复了截图暴露出的三处视觉/无障碍问题。全部截图位于 `docs/md3-refactor/screenshots/round3/`（12 张，真实 Compose 渲染 + mock 数据）：

| 文件 | 页面 / 验证点 |
| --- | --- |
| `article_light.png` / `article_dark.png` | 文章/回答页：标题层级、作者行、正文行高、底部操作栏（赞同/收藏/评论）、朗读悬浮按钮，明暗两套 |
| `people_light.png` | 个人主页：资料头部、认证徽章、关注/拉黑操作、统计行、PrimaryTabRow、回答排序 |
| `pin_light.png` | 想法页：正文、作者关注按钮、赞同/评论操作、话题区 |
| `comment_sheet_dark.png` | 评论底部弹层（设备级截图）：标题、排序 chips、评论行、输入栏 |
| `appearance_settings_light.png` | 外观设置：LargeTopAppBar、分组列表、SegmentedButton、ZhihuSlider |
| `content_filter_settings_light.png` | 内容过滤设置：统一后的紧凑下拉字段（RecommendationMode / 质量屏蔽） |
| `reading_settings_light.png` | 朗读设置：字段排序行、开关+对勾、reorder 箭头 |
| `system_settings_light.png` | 系统与更新：Token 输入、开关组、主按钮、防沉迷下拉 |
| `home_duo3_cards_light.png` | Duo3 卡片样式：`surfaceContainerHigh` 容器 + 20dp 圆角 + 0 投影 |
| `main_shell_light.png` | 主壳（浅色）：底部 5 项 NavigationBar、搜索药丸、FAB |
| `notifications_dark.png` | 消息页：M3 圆形 tonal 容器承载分类图标 |

本轮修复：

1. **设置页下拉字段视觉割裂**：原实现把 `OutlinedTextField` 塞进圆角设置行，形成「行里再套一个带边框方块」。新增 `ui/components/ZhihuDropdown.kt` 的 `ZhihuDropdownAnchor`（`surfaceContainerHighest` 底 + 8dp 圆角 + 当前值 + 下拉箭头），替换外观设置 5 处、内容过滤 2 处、系统设置 1 处，共 8 处；展开/收起与无障碍语义仍由 `ExposedDropdownMenuBox.menuAnchor` 提供，相关字段 testTag 保持。
2. **消息页分类入口缺少视觉落点**：4 个 36dp 裸图标改为 56dp `secondaryContainer` 圆形容器 + 26dp 图标，与底部导航/设置药丸语言一致；未读 Badge、clickable、`notification_category_*` testTag 语义不变。
3. **无障碍缺口**：对 80 个 `IconButton` + 12 个 `FilledIconButton/FAB` 做 contentDescription 审计，唯一真实的交互缺口是 GitHub Token 的密码可见性切换（`contentDescription = null`），已补「显示/隐藏 GitHub Token」；其余 14 处 null 均位于带文字标签的行内或纯装饰位置。
4. 回归：`AppearanceSettingsScreenInstrumentedTest` 3/3 通过（下拉改造后底部栏编辑、高亮跳转等语义未回归）；本轮截图 12/12 成功。

新增实机观察（未在本轮改动，登记为后续打磨项）：

- 评论弹层为全屏高度时顶部没有可见的 drag handle，虽然返回手势/下滑仍可关闭，但可发现性偏弱；
- 个人主页在头像 URL 为空时头部缺少占位图形，真实账号下由 Coil 加载头像，暂不处理；
- 文章页发布/编辑时间按原产品设计右对齐，保持不改。

---

## 8. 第四轮补拍与收尾打磨（2026-10-31 续）

本轮补拍 8 张截图（`docs/md3-refactor/screenshots/round4/`，含 release 冒烟图），并修复截图暴露的 3 处细节：

| 文件 | 页面 / 验证点 |
| --- | --- |
| `search_results_light.png` | 搜索结果页：搜索药丸、PrimaryTabRow、用户结果卡、过滤规则说明、分页 loading |
| `collection_light.png` | 收藏夹列表：修复后的 M3 filled 卡片行（标题+描述+统计）、删除操作 |
| `write_answer_light.png` | 写回答页：顶栏发布按钮、编辑器、预览/图片/草稿悬浮操作、Markdown 快捷工具栏 |
| `settings_search_light.png` | 设置搜索页：与首页/搜索页统一后的药丸搜索框、分组结果卡 |
| `account_screen_light.png` | 账号页：头像/马甲切换/退出、功能入口列表、设置搜索入口、关于区 |
| `comment_sheet_handle_dark.png` | 评论弹层：新增可见 drag handle 条 + 标题，验证下滑关闭可发现性 |
| `home_duo3_cards_dark.png` | Duo3 卡片样式暗色；与 `home_light` / `home_dark` / `round3/home_duo3_cards_light` 一起覆盖 Duo3 开/关 × 明/暗四组合 |

本轮修复：

1. **评论弹层可发现性**：`DragHandleTitle` 原来只有标题文字，全屏高度时看不出可下滑关闭。增加 `BottomSheetDefaults.DragHandle()` 指示条 + 居中标题（使用 `titleMedium` token，移除硬编码 `26.sp` 行高与 Bold 覆盖）。
2. **收藏夹列表粗糙**：原实现是 4dp 投影、无水平留白的卡片，只显示标题，行高浪费。改为 `surfaceContainerLow` + 16dp 圆角 + 0 投影 + 16dp 页面 gutter 的 M3 filled card；补充描述（最多 2 行）与「N 条内容 · N 赞」元信息，删除按钮保持 48dp 触控区与 error 色语义。
3. **设置搜索框形态不一致**：由 4dp 直角 OutlinedTextField 改为 filled 药丸（`surfaceContainerHighest` + `ZhihuShapeTokens.Pill`），与首页/搜索页同一语言。

回归（本轮改动后实机执行）：`CollectionScreenInstrumentedTest`、`SettingsSearchScreenInstrumentedTest`、`CommentScreenInstrumentedTest`、`PinScreenInstrumentedTest` 共 **14/14 通过**。

截图仍暴露但**未在本轮改动**的项：

- 搜索结果里的「规则：文章；作者粉丝数 < 50 或文章赞数 < 20」是内容过滤的透明化说明（产品既有行为），不是调试信息泄漏；样式上可考虑折叠为 banner，属信息架构调整，不在本轮范围。
- 写回答页右下角三个悬浮操作（预览/图片/草稿）视觉权重偏重，合并成可展开 FAB 会改变既有 testTag 与操作路径，留待产品确认。

---

## 9. Release 构建与冒烟（T6.4 DoD 第 4 项）

- `./gradlew assembleLiteRelease`：`BUILD SUCCESSFUL`（2m27s），产物 `app/build/outputs/apk/lite/release/app-lite-release-unsigned.apk`，**4,030,917 字节（3.84 MiB / 4.03 MB 十进制）**。
- 产物为 unsigned（项目未配置 release 签名）。为完成安装冒烟，用本机 debug keystore 临时签名后安装到 AVD，**不代表可发布的正式签名**。
- 安装 → `am start MainActivity` → 进程存活（pid=4890）→ 首启法律提示页正常渲染（`screenshots/round4/release_smoke.png`），logcat 无 `FATAL/AndroidRuntime` 崩溃。R8/资源裁剪未造成启动期 ServiceLoader/反射类缺失。
- **待确认**：Lite 版「< 4 MB」卖点按十进制字节口径已略超（4.03 MB），按 MiB 口径为 3.84 MiB。本次重构未主动引入新依赖，需与基线 release 包对比后决定是否属于既有波动或需要体积收敛。

### 9.1 DoD 对照

| DoD 项 | 状态 |
| --- | --- |
| 全部任务勾选或附豁免理由 | ✅（20 项：14 ✅ / 6 🟡 均为设备或账号绑定项，理由在 `status.md`） |
| 终版截图对比报告 | ✅ 32 张真实渲染截图（round2 12 / round3 12 / round4 8 含 release 冒烟；部分页面明暗/样式重复覆盖）+ 本报告 |
| CI 全量 instrument test 绿 | 🟡 本机执行 9 个相关用例类 37/37 通过；GitHub Actions 全量需推送后确认 |
| release lite 构建/安装/冒烟 | ✅ 见本节 |
| 文档同步 | ✅ `status.md` / `audit.md` / `final-report.md` / `contrast-report.md` / `ai-ui-design-guide.md` |
