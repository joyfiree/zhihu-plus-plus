# 问题回答页 UI/交互对齐官方知乎 11.14：实现说明

> 任务：`task-1`（owner: answer-align-dev）。基准：`target-spec.md` §1/§3、`apk-analysis.md` §C、
> `screenshots/{top2,a1,q1,q4}.png`。本文件记录改了什么、spec 取舍、未完成项，以及需要 Lead 真机验证的点。

## 1. 改动文件

| 文件 | 改动 |
|---|---|
| `shared/.../ui/QuestionScreen.kt` | 重写页面结构：问题头 + 连续回答列表 + 透明悬浮工具栏 + 48dp 吸底操作条 + 36dp「下一篇内容」FAB + 游客上限块预留 |
| `shared/.../ui/components/AnswerWaterfallCard.kt` | 重写回答卡：作者行/赞同行/收录 chip/正文折叠/元信息行/48dp 操作行/热评卡/发丝线 |
| `shared/.../ui/AnswerEndorsementChip.kt` | 收录 chip 对齐 spec：32dp 高、14sp、左右 12/10dp、16dp 图标 |
| `shared/.../ui/ArticleScreen.kt` | 最小改动：回答正文改用 `ZhihuTypography.AnswerBody`(17/24)、赞同行 14/20、时间/IP 13/18、吸底操作区高度改用 `ZhihuSizes.AnswerActionRowHeight` |
| `shared/.../markdown/RenderMarkdown.kt` | 新增可选参数 `bodyTextStyle: TextStyle? = null`（默认 null 完全沿用原设置/主题，其它页面不变），供回答正文覆盖为 17sp/24dp |
| `shared/.../components/AnswerVerticalOverscroll.kt`、`AnswerHorizontalPager.kt` | **未改**。Lead 确认本轮 ArticleScreen 只做最小改动，先保留上下/左右切换回答手势与预览卡，整体改成官方连续列表形态留待单独一轮 |

未新增依赖、未改 `theme/`、`ui/designsystem/`、`HomeScreen.kt`、`ZhihuMain.kt`、`account/`、`data/`、`viewmodel/`；
未改 `app/src/androidTest`。

## 2. 问题页（QuestionScreen）新结构

- 去掉 `Scaffold + ZhihuTopAppBar`、沉浸态状态机（`ImmersiveScrollState`、上滑标题浮现、时间窗口判定），
  改成单层 `Box`：`PaginatedList`（内容在透明工具栏下方滚动）+ 悬浮工具栏 + 吸底操作条 + FAB。
- 列表顶部 `contentPadding.top = statusBars + 44dp`，问题头自身再留 16dp，等于官方 `状态栏 + 工具栏 + 16dp`。
- 问题头（列表首项）：
  - 22sp/31dp Bold 标题，最多 3 行，左右 16dp；
  - 13sp 副信息行：`知乎 · N 个回答 · N 浏览 · N 个关注` + 12dp 右箭头；点击该行内联展开问题详情
    （`QUESTION_DETAIL_TOGGLE_TAG` / `QUESTION_DETAIL_CONTENT_TAG` 语义保留）；
  - 工具行：`N 回答` + 默认/最新排序 + 关注问题胶囊 + 写回答胶囊（既有能力入口）；
  - 结束处 1dp 发丝线。
- 回答卡（每条之间：卡内上下各 16dp + 1px 通栏发丝线，无卡片底色）：
  - 作者行 47.6dp：头像 36dp、作者名 17sp/21 SemiBold、认证 18dp、签名 13sp、右侧「+关注」72×30 胶囊 + 分享 24dp；
  - 「N 人赞同了该回答」行 28.6dp：14sp 三次级色 + 10dp 右箭头；
  - 收录 chip：来自 `answer.endorsements`，无字段不渲染；
  - 正文 17sp/24dp；完整高度超过 **0.618 屏** 时截断：底部 24dp 竖向渐变 + 右下「展开 ⌄」（横向渐变垫底），
    展开后文末「收起 ⌃」，`animateContentSize` 300ms；
  - 元信息行 20dp：`YYYY-MM-DD HH:mm · IP 属地`（缺失项省略）；
  - 48dp 操作行：左「欢迎参与讨论」132×36 胶囊，右 5 组等宽（赞同/反对/收藏/评论/分享），数字 12sp 在上、图标 26dp；
    赞同/反对复用官方三角矢量 `ic_vote_up/down_24dp`，收藏用 Material 星形线性图标（最接近官方），评论/分享用 Material 线性图标；
  - 热评卡：功能底 + 8dp 圆角，头像 20dp / 昵称 13sp / 内容 14sp 两行 / 右侧点赞 12sp。
- 悬浮工具栏（44dp 行高，statusBars inset）：
  - 左返回 24dp；滚动后返回按钮加 48dp 半透明圆底；
  - 滚动 ≥160dp 后中部显示单行问题标题（点击回顶），顶部时不显示（无「邀请回答」能力，按 spec「无能力则隐藏」处理）；
  - 右：评论 24dp / 问题日志 24dp / 分享 24dp；问题头完全滚出后追加「写回答」胶囊。
- 吸底操作条 48dp + 顶部发丝线：
  - **判定不是时间窗口**：`LazyListState.layoutInfo` 取可见高度最大的回答 → 卡片操作行通过
    `onGloballyPositioned` 把窗口坐标回传给页面 → 操作行不在 `[工具栏底边, 屏幕底 − 导航栏 − 48dp]` 内时显示，
    重新进入可见范围即隐藏；内容底部 padding 固定留出 48dp + 导航栏，保证滚到列表末也能把操作行滚到吸底条上方；
  - 左侧 32dp 头像 + 作者名 15sp + 关注/已关注（复用 `POST/DELETE /api/v4/members/{url_token}/followers`，与 PeopleScreen 同一契约）；
  - 右侧 5 组操作与当前回答联动（赞同/反对/收藏/评论/分享均命中当前回答）。
- 「下一篇内容」FAB：36dp 圆形底 + 下箭头，右侧 16dp；位于吸底条上方 16dp（无吸底条时距导航栏 16dp）；
  点击 `animateScrollToItem(下一条, -topInset)`，下一条从工具栏下方开始；最后一条时隐藏。
- 游客上限块：`QuestionScreen(question, guestReadLimitReached = false)`，默认 false 不渲染；
  传 true 时列表 footer 渲染 119dp「登录后查看更多精彩回答」+ 右箭头，点击跳 `Login`。业务状态等游客任务提供后从调用点传入。

## 3. spec 取舍 / 偏离（需要 Lead 知悉）

1. **工具栏右侧与官方不同**：官方是「写回答 + 搜索」，我们没有该页的搜索入口，改为「评论 / 问题日志 / 分享」3 个 24dp 图标
   （都是既有能力）；滚动后追加写回答胶囊。顶部中部按 spec「无邀请能力则隐藏」，不显示邀请回答。
2. **写回答/关注问题入口留在问题头工具行**（官方在工具栏或详情页）：`QUESTION_WRITE_ANSWER_BUTTON_TAG`、
   `QUESTION_FOLLOW_BUTTON_TAG` 必须留在 LazyList 内，`QuestionScreenInstrumentedTest.performScrollToNode` 依赖它；
   按任务约定我不改 `app/src/androidTest`，所以保留在列表内。
3. 副信息行保留「N 浏览」（官方无），工具行保留 `N 回答 / 默认 / 最新`（官方无排序控件）：分别为了
   `waitUntilTextExists("345 浏览")` 与 `onNodeWithText("12 回答")` 两条 instrumented 断言，以及项目既有排序能力。
4. **问题详情不再默认展示 180dp 预览**，改为点击副信息行展开（官方问题页本身不展示问题正文）。展开后完整渲染，
   底部保留「收起问题」。instrumented 测试的 tag 与点击→展开路径保持可用。
5. 「N 人听过」：`Feed.AnswerTarget` 无该字段，按 spec「字段缺失则不显示」不渲染。
6. 「禁止转载」：`reshipmentSettings` 取值语义没有真实证据，暂不渲染，等数据面确认后再补。
7. 收录 chip 的数据源是 `answer.endorsements`（接口字段），颜色/图标沿用项目已有的 endorsement 映射，无字段不渲染。
8. 展开某条长回答后滚离视口**不再自动收起**（旧逻辑是为「吸底栏属于展开回答」服务的；现在吸底栏跟随当前回答，
   保留展开可以减少阅读中断）。展开状态仍 `rememberSaveable`。
9. 热评阈值/接口不变：`root_comment?order=score&limit=1` + 10 赞门槛。
10. 「N 人赞同了该回答」行暂未接投票人列表（问题页原本没有该入口，本轮不扩功能）；ArticleScreen 的投票人弹层未动。
11. 吸底条在打开评论/收藏/分享弹层时仍显示（弹层是独立 overlay，位于其上方）。是否要在弹层打开时隐藏吸底条，留给真机验收判断。

## 4. 未完成 / 后续

- 游客上限业务状态（游客任务）接通后，在调用点传 `guestReadLimitReached = true`；UI 已就绪。
- 官方「禁止转载」、投票人列表入口：待数据/能力确认后再补。
- `AnswerVerticalOverscroll` / `AnswerHorizontalPager` 的「官方连续列表形态」改造未做（Lead 确认本轮 ArticleScreen 最小改动）。
- ArticleScreen 的两行 AppBar 仍是旧结构（作者信息在顶栏），未改成官方「悬浮工具栏 + 内容内作者行」，按 Lead 指示后续单独一轮评估。

## 5. 需要 Lead 真机验证的交互点

1. **吸底条出现/隐藏**：顶部时首条回答操作行在屏下 → 显示；滚动到该操作行完全进入「工具栏下方 ~ 导航栏上方」→ 隐藏；
   继续滚到列表底部不出现「永远显示」；快速滑动时不闪跳、不错位。
2. **吸底条与当前回答联动**：滚到第 2/3 条回答时头像/作者名/赞同数/收藏态/关注态跟随切换；点击赞同/反对/收藏/评论/分享命中当前回答。
3. **展开/收起**：0.618 屏截断位置、24dp 渐变、右下「展开 ⌄」点击热区与文字可读性；展开后文末「收起 ⌃」；300ms 高度动画；
   收起后滚动位置稳定在该回答附近。
4. **下一篇 FAB**：位置（右侧 16dp；有吸底条时在其上方 16dp，无吸底条时距导航栏 16dp）；点击后下一条回答从工具栏下方开始而不是被工具栏遮住；
   最后一条回答时 FAB 隐藏。
5. **游客上限块**：临时把 `guestReadLimitReached` 传 true 验证列表末尾 119dp 居中蓝色文字 + 右箭头，点击进登录页；默认 false 不出现。
6. **悬浮工具栏**：顶部无标题；滚动 ≥160dp 后单行标题出现、点击回顶；问题头完全滚出后出现写回答胶囊；返回箭头滚动后有半透明圆底；
   评论/日志/分享三个入口可点。
7. **深浅色**：页面底色/卡片同色 `#FFFFFF` / `#191B1F`；功能底 `#F8F8FA` / `#212429`；次/三级文字、赞同红/蓝、收藏橙；
   重点抽查吸底条、讨论胶囊、热评卡、收录 chip。
8. **大字号（系统字体 1.15x）**：问题头工具行（12 回答 / 默认 / 最新 / 关注 / 写回答）在 411dp 宽下是否溢出；
   操作行 5 组图标与数字是否挤压；工具栏滚动后标题 + 3 图标是否够宽。
9. **返回/状态恢复**：展开状态、问题详情展开状态、滚动位置在返回后是否保持；列表恢复位置是否因问题头高度变化而偏移。
10. **性能**：吸底条判定依赖卡片操作行 `onGloballyPositioned` 每帧回传窗口坐标（有阈值去抖），
    请留意长列表快速滑动是否有掉帧；如果明显，可以改为按 `layoutInfo` + 卡片已知结构推算，但精度会下降。

## 6. 自检

- `./gradlew :shared:compileKotlinJvm --console=plain` → **BUILD SUCCESSFUL**（2026-11-09，含 Lead/首页并行改动后的工作树）。
- `./gradlew :shared:ktlintCommonMainSourceSetCheck` → 本任务改动文件无报错；
  工作树内剩余报错在 `ui/ZhihuMain.kt`、`ui/designsystem/Dimens.kt`（Lead 正在改的文件，不属本任务改动范围）。
- instrumented 测试未运行（无设备权限）；按任务约定不跑 adb/instrument，语义 tag 与文本断言已保留，详见 §3.2/§3.3。

## 7. 第一轮真机验收修复（ans2/ans4/exp2 反馈）

| 反馈 | 修复 |
|---|---|
| P0-1 滚动后内容穿过工具栏、无遮罩 | 工具栏下层新增 `Brush.verticalGradient` 顶部遮罩：`contentFirstColors.page` 不透明起，覆盖 `statusBar + 44dp + 12dp`，最后约 12dp 渐隐到透明（不整块铺不透明底）。位于列表之上、工具栏之下，不拦截触摸 |
| P0-2 滚动后右侧 3 图标 + 写回答把标题挤到约 102dp | 滚动态右侧只保留「写回答 pill + 更多 ⋮」；评论/问题日志/分享收进 `DropdownMenu`，菜单项保留原 testTag（`QUESTION_COMMENTS_BUTTON_TAG` / `QUESTION_VIEW_LOG_BUTTON_TAG` / `QUESTION_SHARE_BUTTON_TAG`）；标题 `weight(1f)` 占满剩余宽度。未滚动状态维持原 3 图标布局 |
| P0-3 滚动后右侧图标无悬浮底 | 滚动态返回按钮、更多 ⋮ 都补 48dp 圆形 `floatingSurface` 半透明底 |
| P1-4 吸底条缺 32dp 头像、名字贴左 | 吸底条头像改为「有图渲染、无图渲染 32dp 圆形功能底 + 人形图标占位」，名字/关注不再贴左 |
| P1-5 吸底条名字位置偏移 | 同上，头像占位后再接 8dp 间距 + 15sp 名字 + 13sp 关注，与官方 32dp + 15sp + 13sp 结构一致 |
| P2-6 收起后滚动位置 | 回答正文新增 `BringIntoViewRequester` 锚点：展开→收起且高度动画结束后，把「展开 ⌄」按钮带回视口附近（官方「收起后停在展开按钮附近」） |

修复后自检：`:shared:compileKotlinJvm` BUILD SUCCESSFUL、`:app:assembleLiteDebug` BUILD SUCCESSFUL（见本轮回报）；ktlint 本任务文件无报错。

## 8. 第二轮待验收点（增量）

1. 滚动后工具栏：标题可读、内容不再从图标/标题上直接穿过；⋮ 菜单三项可点且行为与原图标一致（评论/日志/分享）。
2. 吸底条左侧头像占位与名字对齐；展开态吸底条（exp2 场景）复测。
3. 展开一屏以上长回答 → 点文末「收起」→ 观察是否平滑滚回截断处附近（不是直接跳回回答顶部）。
4. 顶部未滚动状态：右侧仍是 3 图标（评论/日志/分享），工具栏无可读性遮罩副作用（页面顶部无内容穿过时遮罩不可见）。

