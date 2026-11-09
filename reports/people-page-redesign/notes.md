# 用户主页（PeopleScreen）现代 UI 重设计

范围：`shared/src/commonMain/kotlin/com/github/zly2006/zhihu/ui/PeopleScreen.kt`（只改展示，ViewModel/网络/include/测试未动）。

## 新结构（自上而下）

1. **固定工具行**（44dp，位于状态栏 inset 下）
   - 左：40dp 圆形返回按钮，新 tag `people_screen_back_button`，点击走 `navigator.onNavigateBack()`。
   - 右：40dp 圆形搜索按钮，沿用 `PEOPLE_SCREEN_SEARCH_BUTTON_TAG`；去掉了旧实现“滚动后 alpha 渐隐 / 渐变底”的 overlay，改为常显。
   - 工具行与资料区之间只留 4dp，无大段空白。
2. **可折叠资料区**（`CollapsibleHeader` 的内容，tag `people_screen_header`）
   - 72dp 圆形头像（`people_screen_avatar`，点击看大图逻辑不变）+ 昵称 22sp Bold（`ZhihuTypography.ContentTitle`，不用 M3 titleLarge）+ 认证 badge（`people_screen_official_badge`，`AuthorBadge` 不变）
   - headline 13sp 三级色（`ContentSignature`，最多 2 行）
   - 认证明细：12sp 三级色、16dp 图标、单行；从原来的名字右列移到全宽下方，密集认证时更省高度
   - GitHub 星标行（`people_screen_github_stars`，13sp，点击外链不变）
   - **统计行**：回答/文章/粉丝/关注 4 项等分横排，数字 16sp SemiBold + 标签 12sp 三级色；四个 stat tag 保留，点击切 tab 行为不变；不用 M3 Button
   - **操作行**：FlowRow
     - 关注/取消关注：36dp 品牌蓝 pill（`accentFill` 底 + `onPrimary` 白字；已关注 `functionalSurface` 底 + 次级文字），沿用 `viewModel.isFollowing` 分支
     - 拉黑 / 屏蔽推荐 / 屏蔽其提问：30dp 高 outlined chip（12sp、1dp `divider` 描边、Pill 圆角），三个 tag 与点击回调完全保留
3. **标签栏**：44dp `LazyRow`，文字 15sp；选中 SemiBold + `primaryText`，未选中 `tertiaryText`；选中项 24×3dp accent 短下划线
   - tag `people_screen_tab_row`、每个 tab 的 `people_screen_tab_$index` 保留
   - 点击仍 `pagerState.animateScrollToPage(index)`；当前页变化时标签栏自动滚动到选中项（补回原 `PrimaryScrollableTabRow` 的跟随滚动）
4. **内容 pager**（`people_screen_pager` 不变）；回答/文章两个 tab：
   - 排序条收敛为两个紧凑 chip（按热度/按时间），保留原 sort tag
   - 列表改为紧凑卡片：12dp 圆角（`ZhihuShapeTokens.Card`）、`contentFirstColors.functionalSurface` 底、内边距 12dp × 14dp、卡片间 8dp；视觉左右外边距 12dp（页面 8dp + 列表 4dp）
   - 回答：问题标题 15sp SemiBold（2 行）+ 摘要 14sp 次级色（2 行）+ 12sp 三级色元信息（赞同 / 评论 / 相对时间，compact count）+ 右上 ⋮ 菜单
   - 文章：标题 16sp SemiBold（2 行）+ 摘要 14sp（2 行）+ 元信息；`thumbnailInfo` 有封面时右侧 96×72dp 缩略图
   - 行点击：`destination.withReadingQueueSource(readingQueueSourceId)` 后 `navigator.onNavigate`，与原 `FeedCard` 默认点击完全等价；item tag `people_screen_answer_item_$id` / `people_screen_article_item_$id` 保留
5. 其余 tab（动态/收藏/提问/想法/专栏/粉丝/关注/关注订阅）的 item 组件与行为保持原样，只在页级保留了原来的 8dp 内容边距。

## 保留的 tag / 行为

- 全部 `PEOPLE_SCREEN_*` tag 常量保留，新增 `PEOPLE_SCREEN_BACK_BUTTON_TAG`（`people_screen_back_button`）。
- 展开态四个操作按钮全部可见且位于 tag 栏上方；滚动收起仍由 `TopAppBarDefaults.enterAlwaysScrollBehavior` + `CollapsibleHeader` 驱动（`heightOffsetLimit` = 资料区实测高度），滚动后 tag 栏上移；收起态只剩工具行 + 标签栏。
- `CollapsibleHeader` 的收起方式从「固定顶部、从底部裁切」改为「资料区整体上移 + 高度裁切」：顶部头像先滑出、底部操作区最后消失，收起后不会残留头像/昵称，视觉更接近跟随列表滚走；同时给可见高度加了 `coerceIn`，避免头高变化时出现负高度布局。
- 搜索条件与跳转不变（`Search(restrictedMemberHashId, restrictedMemberName)`），tag 位置仍满足「右上区域、在 header 宽度内、center.y < avatar.bottom」。
- 关注/拉黑/屏蔽推荐/屏蔽其提问回调、关注数刷新、头像大图、GitHub 外链、操作失败提示全部复用原实现。
- 阅读队列：`RegisterReadingQueueSource` 与点击时 `withReadingQueueSource` 均保留。
- 颜色全部走 `contentFirstColors` / `MaterialTheme.colorScheme`，无裸色值；字号优先 `ZhihuTypography`，间距/尺寸优先 `ZhihuSpacing` / `ZhihuSizes`。深浅色共用同一套 token。

## 收敛掉的 M3 / 旧组件

- 关注：`Button` / `FilledTonalButton` → 自绘 36dp pill（`accentFill` / `functionalSurface`）。
- 拉黑等三个操作：`OutlinedButton` → 30dp outlined chip。
- 标签栏：`PrimaryScrollableTabRow` + `Tab` → 44dp `LazyRow`（更矮，字号/下划线/间距可控）。
- 排序：两个整行宽 `OutlinedButton` → 两个紧凑 chip。
- 回答/文章列表：`FeedCard`（M3 Card + 作者行 + 大字号）→ 专用紧凑卡片；动态 tab 继续用 `FeedCard`。
- 搜索按钮：自适应 TopAppBar overlay（alpha 渐隐 + 渐变底）→ 工具行固定圆形按钮。
- 统计项：裸 Column → 等分可点击槽位（保留切 tab）。

## 已知取舍 / 待真机验证点

1. **文章封面**：`DataHolder.Article` 只映射 `thumbnailInfo`，接口 include 里的 `thumbnail` 字段当前没有反序列化（改数据模型超出本文件范围）。因此紧凑文章卡只在 `thumbnailInfo` 非空时显示 96×72 封面，线上很可能不显示；要保证封面需要数据层的独立改动。
2. **缩略图圆角**：规格写 8dp，仓库约束页面不得直写 `RoundedCornerShape`，改用 `ZhihuShapeTokens.Thumbnail`（6dp）。
3. **展开态高度**：认证明细 4 项 + GitHub 行时资料区约 300dp，四个操作按钮仍在首屏且位于标签栏上方；需要真机复核小屏 / 系统大字号下的表现。
4. **触控目标**：标签栏 44dp、操作 chip 30dp、关注 pill 36dp，均按设计规格低于 48dp 无障碍下限，属于本次紧凑化的明确取舍。
5. **卡片 ⋮ 菜单**：保留原 `FeedCard` 在本页菜单里的等价入口「外观设置」；不再保留长按弹菜单（入口由 ⋮ 承担）。
6. **收起过程**：折叠改为资料区整体上移，中途截断位置与旧实现不同，需要真机确认滚动过程无跳动、无残留遮挡。
7. **构建/格式**：`:shared:compileKotlinJvm`、`:app:assembleLiteDebug`、`:app:compileLiteDebugAndroidTestKotlin` 通过；`./gradlew ktlintFormat` 与 `runKtlintCheckOverCommonMainSourceSet` 通过。按分工未跑 instrument test / 真机，由 Lead 截图验收。
8. **并行构建环境提示**：多人同时在同一工作树跑 Gradle 时，遇到过 `shared/build/intermediates/compile_library_classes_jar/androidMain/bundleAndroidMainClassesToCompileJar/classes.jar` 被写成 324 字节空 jar，导致 `:app` 编译出现大量 `Unresolved reference` 的假失败；删除该 jar 后重跑即恢复（已验证前后 `:app:compileLiteDebugAndroidTestKotlin --rerun-tasks` 全绿）。
9. **已验证证据**：`--rerun-tasks` 全量重编 `:app:compileLiteDebugAndroidTestKotlin`（62 个任务全部执行）成功；最终 APK 内已包含 `people_screen_back_button` / `PeopleTabRow` 符号。
