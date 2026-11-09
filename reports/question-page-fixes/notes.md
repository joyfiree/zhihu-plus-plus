# 问题页修复（task-6）：消跳动 / 目标回答置顶 / 评论数 / 作者行尺寸

> 对应 task-6。改动文件：`ui/QuestionScreen.kt`、`ui/components/AnswerWaterfallCard.kt`、
> `ui/designsystem/Dimens.kt`、`theme/Type.kt`；真机截图在本目录。

## 1. 消跳动 + 目标回答「外露」到第一条

- **去掉初始滚动定位**：删除原 `LaunchedEffect { scrollToItem(targetIndex+1); delay(120); scrollToItem(...) }`，
  进入问题时列表永远停在顶部（`firstVisibleItemIndex=0`），不再自动上滑。
- **目标回答置顶**：`QuestionScreen(question.initialAnswerId)` 命中时，渲染列表用重排后的 `renderItems`：
  问题头之后第一条就是目标回答，其余按接口顺序；命中前列表不渲染其他回答（显示骨架），避免先闪一条再换。
- **定位策略**（不再依赖前两页）：
  1. 先用 `paginationEnvironment.getOrFetchContentDetail(Article(Answer, id))` 取回答详情（ContentDetailCache 通常已缓存）；
     命中且 `detail.question.id == 当前问题` → 直接用详情构造一条置顶回答卡（`FeedDisplayItem(..., raw = detail)`），
     同时写入 `answerDetails/voteStates/voteCounts/authorFollowStates`，第一条卡立即有正文/赞同/评论数。
  2. 详情 `question.id` 与本问题不一致 → 以详情为准跳转到正确问题（`Question(detail.question.id, ..., initialAnswerId)`）并提示。
  3. 详情取不到（如游客态接口被要求登录）→ 等首页数据就绪后按 id 逐页续载查找，上限 3 个额外页 / 80 条；
     找到则把该条重排到首位，仍找不到提示「未找到这条回答」并正常展示列表。
- 吸底条「当前回答」、下一篇 FAB、列表 key 全部基于同一份 `renderItems`。
- 用户主动切换排序时清空置顶、按新排序正常展示。

## 2. 问题头压缩到约原来的 60%（受既有 testTag 可见性约束）

- 标题：22sp/31 最多 3 行 → **20sp/26 最多 2 行**。
- 纵向间距：顶部 16→4、标题后 8→4、副信息行后 8→4、底部 16→8。
- 工具行（N 回答 / 默认 / 最新 / +关注问题 / 写回答）：整行压到 24dp，字号 13sp，
  关注与写回答改成 24dp 小号 pill（原 30dp + M3 大按钮）。
- 展开态问题头实测从约 165dp 降到约 115dp（含 divider），1080×2400 下第一条回答作者行从约 23% 屏高提前到约 18%。
- 保留全部 tag 与能力：`QUESTION_TITLE_TAG`、`QUESTION_DETAIL_TOGGLE_TAG`（仍可点开详情）、
  `QUESTION_SORT_DEFAULT_TAG/UPDATED`、`QUESTION_FOLLOW_BUTTON_TAG`、`QUESTION_WRITE_ANSWER_BUTTON_TAG`；
  详情展开后仍会滚回列表顶部。

## 3. 评论数显示 0

- 根因：item 详情请求前就把 `loadedAnswerIds[id] = true`，失败后不再重试 → `answer` 永远为 null → 评论数恒 0。
- 修复：改为 `loadingAnswerIds` 记录请求中状态，失败后清除，滚回来可重试；详情成功才写 `answerDetails`。
- 回退链：操作行与吸底条统一为 `answer.commentCount > item.cardMetrics.comment > 0`
  （卡片新增 `commentCount` 入参，由 QuestionScreen 解析后传入；吸底条同样取当前项 metrics）。
- 真机验证：1724 回答的问题里首条评论数 27、第二条 63；31 回答问题吸底条 154。

## 4. 作者行尺寸 + 去重分享

- 头像 `ZhihuSizes.AnswerAvatarSize` 36→32dp；作者名新增 `ZhihuTypography.AnswerAuthorName` 16sp/21 SemiBold；
  签名新增 `ZhihuTypography.AnswerAuthorSignature` 12sp/17；作者行高 47.6→44dp。
- 删除作者行右侧重复的分享按钮（底部 48dp 操作行保留分享），作者行右侧只留「+ 关注」。
- 顶部问题标题使用新增 token `ZhihuTypography.QuestionHeaderTitle` 20sp/26 Bold。

## 5. 真机验证（Redmi K30 Pro 1080×2400 深色，`com.github.zly2006.zhplus.lite`，adb 覆盖安装）

| 截图 | 内容 | 结果 |
|---|---|---|
| `q1_india_31answers_entry.jpg` | 首页答案卡「印度人知道自己被世界网友嘲讽吗？」→ 作者「苟且」 | 首屏问题头 + 第一条即苟且回答，无跳动；吸底条评论 154 |
| `q2_hermes_24answers_entry.jpg` | 首页答案卡「如何评价 Hermes…Arete-27B？」→ 作者「不打码」 | 第一条即不打码，赞同 55，评论 0（卡片本身即 0） |
| `q3_huang_1724answers_entry.jpg` | **1724 回答**的问题，首页答案卡 → 作者「Tawen」 | 首屏即 Tawen 回答，说明前两页找不到时走详情置顶成功；吸底条评论 27 |
| `q3_huang_entry_400ms.jpg` / `q3_huang_entry_2600ms.jpg` | 进入后 400ms 与 2600ms 两帧 | 两帧布局一致（问题头在顶 + Tawen 回答），无「先到顶再跳」过程 |
| `q3_huang_1724answers_scrolled.jpg` | 下滑后卡片操作行 | 第一条评论数 27、第二条 63（非 0）；吸底条在操作行可见时正确隐藏 |

## 6. 遗留 / 风险

- 问题头再往下压需要连同 `QuestionScreenInstrumentedTest`（`performScrollToNode` 后要求写回答/关注/排序 tag 可见）
  一起改，当前在既有测试契约内已压到约 115dp；如用户仍觉得高，需要 Lead 同意同步改 instrumented 测试。
- 「详情取不到 → 续载查找」分支无法在真机稳定构造（需匿名/接口失败态），代码路径已实现但未真机覆盖。
- 置顶卡片来自回答详情：若详情与列表数据的 `voteupCount/commentCount` 存在时间差，以详情为准（与官方详情页一致）。
