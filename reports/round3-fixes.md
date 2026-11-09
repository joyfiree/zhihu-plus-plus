# 第三轮反馈修复记录（2026-11-09）

> 用户真机反馈 5 点：① 进入问题页先到顶部再跳到回答、出戏，要求问题头减半且外露回答排第一；② 偶发跳错/落到默认列表顶部；③ 回答底部评论数恒 0；④ 回答作者头像/名字偏大、分享按钮重复；⑤「我的」页空、用户主页丑且没设计感。
> 验证：Redmi K30 Pro（1080×2400，深/浅色）真机截图 + 构建/ktlint/shared jvmTest/androidTest 编译全绿。

## 1. 问题页：消跳动 + 目标回答「外露」到第一条

- 删除进入问题时两次 `scrollToItem` 跳转；列表永远从问题头开始，命中目标前只渲染骨架，不先闪错内容。
- 新增 `renderItems`：命中 `initialAnswerId` 时，问题头之后第一条固定是该回答，其他按接口顺序、同 id 过滤去重；吸底条/下一篇 FAB/列表 key 全部基于同一列表。
- 定位策略：先用回答详情确认归属（`ContentDetailCache`，首页入口常已缓存）→ 置顶回答卡并预热详情/投票/评论/关注态；详情归属与本问题不一致时以详情为准跳正确问题；详情取不到才续载查找（上限 3 页 / 80 条），仍找不到才提示并展示列表。用户切排序会清掉置顶。
- 问题头压缩：标题 22sp/31 3 行 → 20sp/26 2 行，纵向间距 16/8→4/4，工具行压到 24dp（13sp 小号关注/写回答 pill）。展开态问题头约 165→115dp；全部 testTag 与详情展开/排序/关注/写回答能力保留。

## 2. 回答底部评论数 0

- 根因：回答详情请求前就置 `loadedAnswerIds[id] = true`，失败后不再重试 → `answer` 永远为 null → 评论数恒 0。
- 修复：改为 `loadingAnswerIds` 在请求中标记、`finally` 清除，失败可重试；操作行与吸底条统一走 `answer.commentCount > item.cardMetrics.comment > 0` 回退。

## 3. 作者行尺寸 / 分享去重

- 头像 36→32dp、作者名新增 `AnswerAuthorName` 16sp/21 SemiBold、签名 12sp/17、作者行高 47.6→44dp。
- 删除作者行右侧重复分享按钮，保留底部 48dp 操作行的分享。

## 4. 「我的」页：统计 + 压缩留白

- Hero 增加 4 列统计（回答/文章/粉丝/关注）：数字 17sp Bold、标签 12sp 三级色、大数 compact；点击打开自己主页对应 tab。
- `/api/v4/me` 的 `self` 只可靠返回回答/文章数；粉丝/关注计数改用成员资料接口 `members/{urlToken}` 补齐（进入页面时请求，失败回退 self 解析值）。
- 留白：Hero 内容驱动高度、扫码/更多并入头像行省一行；Hero→快捷卡 16→12、快捷卡 84→80、卡→搜索 16→12、组间 16→12、设置行 60→56、底部 32→24。
- 保留全部 18 个 `ACCOUNT_SETTINGS_*` 入口/行为与函数签名。

## 5. 用户主页 PeopleScreen 现代 UI 重设计

- 固定 44dp 工具行（40dp 返回 + 常显搜索，去掉搜索按钮 alpha 渐隐）。
- 72dp 头像、22sp Bold 昵称 + 认证、13sp headline、认证明细、GitHub 星标行、紧凑统计行（16sp SemiBold + 12sp 三级色）。
- 操作行：关注 → 36dp 品牌蓝 pill；拉黑/屏蔽推荐/屏蔽其提问 → 30dp outlined chip（FlowRow，全部保留 tag 与点击）；**本人主页不显示这排对自己无意义的操作**。
- 标签栏 44dp LazyRow、15sp、选中 SemiBold + 24×3dp accent 下划线；左右 padding 收紧后 9 个 tab 一屏可见。
- 回答/文章列表改为 12dp 圆角 functionalSurface 紧凑卡片（12×14dp 内边距、8dp 间距）：问题标题 15sp 2 行 / 摘要 14sp 2 行 / 元信息 12sp（赞同·评论·时间）+ ⋮；行点击与 item tag 不变。其余 tab 保持原实现。

## 6. 验证与截图

- 问题页：`reports/question-page-fixes/`（团队截图 + Lead 复验帧：350ms/3s 布局一致、目标作者第一条、评论数非 0、滚动后操作行正常）。
- 我的页：`reports/ui-redesign/screenshots/account_dark.jpg`、`account_light.jpg`（统计 3/1/4/44 与主页一致）。
- 用户主页：`reports/people-page-redesign/screenshots/`（本人深/浅色、他人深/浅色、收起态）。
- 构建：`:app:assembleLiteDebug` ✅、`ktlintFormat` ✅、`:shared:jvmTest` ✅、`:app:compileLiteDebugAndroidTestKotlin` ✅、`:shared:ktlintCommonMainSourceSetCheck` ✅。
- 本地定向 instrument（PeopleScreenInstrumentedTest）因设备上 kotlinx-coroutines-test 的 `Exception handler was not found via a ServiceLoader` 环境问题无法运行（与本次改动无关），该契约以编译 + 真机截图 + CI 为准。
