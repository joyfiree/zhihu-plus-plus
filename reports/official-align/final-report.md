# Zhihu++ 首页与问题回答页对齐官方知乎 11.14.0 验收报告

> 日期：2026-11-09 ｜ 设备：Redmi K30 Pro（lmi）1080×2400 @420dpi（2.625x），Android 16
> 官方基线：`com.zhihu.android` 11.14.0（versionCode 41410，未登录游客态）
> 我方：`com.github.zly2006.zhplus.lite` v0.31.1 debug（登录态，覆盖安装不丢登录）
> 证据目录：`reports/official-align/`（`screenshots/` 为全部真机截图，`apk-analysis.md` 为官方 APK 静态分析，`target-spec.md` 为目标规格，`*-implementation-notes.md` 为实现记录）

## 0. 结论摘要

- **首页**与**问题回答页**的布局结构、信息层级、颜色比例、字体尺度、间距节奏与主要交互已对齐官方 11.14.0，深色/浅色两套主题均真机截图验证（像素级抽检通过）。
- **底栏**改为官方 5 槽结构（首页 / 热榜 / ＋ / 消息 / 我的），中间蓝色发布按钮、消息 tab 未读角标（登录态实测显示 75）。
- **问题回答页**实现官方形态：问题头、回答卡（作者行/赞同行/收录 chip/正文 0.618 截断+展开收起/元信息/48dp 操作行/热评卡/发丝线）、滚动后悬浮工具栏+顶部渐变遮罩、吸底操作条（当前回答操作行不可见时出现）、「下一篇内容」FAB、游客阅读上限 UI 预留。
- 8 次 `adb install -r` 覆盖安装全程保持登录态（首页/我的/未读角标均正常），登录态未被清除。
- 构建/格式/单测：`:app:assembleLiteDebug` ✅、`ktlintFormat`/`ktlintCheck` ✅、`:shared:jvmTest` ✅、`:app:compileLiteDebugAndroidTestKotlin` ✅。
- 游客会话（未登录只读浏览）已在账号/网络层实现并通过单元测试（101 suites / 437 tests），**真机端到端验证被知乎网络环境风控阻塞**：游客请求返回 `40352 / unhuman`（`系统监测到您的网络环境存在异常`），同一设备/网络的官方 App 也触发「安全验证」；UI 侧「未登录」底栏、免登录进首页、游客骨架等待均已真机验证。详见 §7 与 `guest-session-notes.md` §7。

## 1. 对齐方法（证据驱动）

1. **官方 APK 静态分析**（`rea` CLI + `aapt2` + DEX 解析，`apk-analysis.md`）：提取官方 11.14「Map」设计令牌全部颜色/尺寸，确认首页接口 `/topstory/recommend`、游客凭证 `init/udid_guest`、登录门禁文案由服务端下发等信息。
2. **官方真机取证**：在连接的 Redmi K30 Pro 上逐页截图 + `uiautomator` 层级 + 像素取样（深色 4 组状态、浅色 1 组；回答页含顶部/滚动/展开/游客上限状态），得到全部布局 dp/sp 实测值（`target-spec.md`）。
3. **我方现状基线**：改动前首页/问题页截图与 Compose 语义树；建立「官方 vs 我方」逐项差异清单。
4. **实现与迭代**：设计令牌 → 首页（搜索区/Tab/卡片/骨架/底栏）→ 问题回答页（问题头/回答卡/吸底条/工具栏/展开收起）→ 真机截图复验 → 像素级修正（3 轮）。
5. **验证**：构建 + ktlint + shared JVM 单测 + androidTest 编译 + 真机深/浅色截图 + 覆盖安装登录态检查；并排对照图见 `screenshots/compare_*.png`。

## 2. 设计令牌（官方实测 → 我方落地）

### 2.1 颜色

| 语义 | 官方浅色 | 官方深色 | 我方实现（`theme/OfficialZhihuScheme.kt`） | 真机验证 |
|---|---|---|---|---|
| 页面/卡片/顶栏/底栏底 | `#FFFFFF` | `#191B1F` | 同官方 | `our home dark` 页面/搜索区/底栏取样 `#191B1F`；浅色 `#FFFFFF` |
| 功能面（搜索胶囊、banner、chip、热评卡） | `#F8F8FA` | `#212429` | 同官方（搜索胶囊改为透明只留描边，见 §3.1） | — |
| 正文主色 | `#191B1F` | `#FFFFFF` | 同官方 | 像素抽检 |
| 次级文字 | `#535861` | `#C2C6CF` | 同官方 | 像素抽检 |
| 三级文字（IP/时间） | `#81858F` | `#9196A1` | 同官方 | 像素抽检 |
| 品牌蓝 | `#1772F6` | `#558EFF`（文字/线） | 同官方；**实心填充另用 `accentFill=#1772F6`（深浅一致）** | 搜索按钮填充实测 `#1772F6`；Tab 下划线实测 `#FFFFFF`（官方同款 onSurface 白） |
| 分割线 | `#EBECED` | `#282B30` | 同官方 | — |
| 点赞/收藏 | `#F05159` / `#FA9D14` | 同 | 写入 design system `liked/favorited` token | — |
| 角标红 | `#F5533D` | 同 | 同官方 | 未读角标 75 实拍 |

> 官方深色实测重要修正：首页搜索胶囊内部**没有填充色**（与页面同为 `#191B1F`），只有 1.5dp `#1772F6` 描边；Tab 选中下划线是 **onSurface 白**（不是品牌蓝）；Tab 未选中文字也是白色（仅字重不同）。这三处已在第三轮像素复验后修正。

### 2.2 字体（`theme/Type.kt` 的 `ZhihuTypography`）

| 令牌 | 值 | 官方依据 |
|---|---|---|
| ContentTitle | 22sp / 31dp Bold | 官方问题标题 3 行行盒 92dp |
| FeedTitle | 18sp / 25dp Bold | 官方卡片标题 2 行 130px≈49.5dp |
| FeedSummary | 15sp / 22dp | 官方卡片摘要行盒 ~21.9dp |
| FeedAuthorName | 15sp / 20dp Medium | 官方作者名行盒 54px≈20.6dp |
| FeedMeta | 13sp / 18dp | 关注数/计数 |
| ContentAuthor | 17sp / 21dp SemiBold | 回答作者名 |
| ContentSignature | 13sp / 18dp | 作者签名 |
| AnswerBody | 17sp / 24dp | 官方回答正文行盒 64px≈24.4dp |
| AnswerVoteLine | 14sp / 20dp | 「N 人赞同了该回答」 |
| AnswerMeta | 13sp / 18dp | 时间 · IP |
| ActionCount | 12sp / 16dp | 操作行数字 |
| TabTitle | 17sp / 24dp（选中 Bold） | 官方 Tab 文字宽 90px≈17sp |
| BottomNavLabel | 12sp / 16dp | 底栏文字 |

### 2.3 尺寸 / 间距（`ui/designsystem/Dimens.kt`）

| 项 | 官方实测 | 我方 |
|---|---|---|
| 搜索行高/胶囊高 | 105px = 40dp | 40dp，像素实测 74–178px=40dp ✓ |
| 胶囊圆角/描边 | 全圆角 / 1.5dp `#1772F6` | 20dp / 1.5dp accentFill ✓ |
| 搜索按钮视觉 | ~56×30dp，圆角 15dp | 56×30dp ✓ |
| Tab 行高 / 单项宽 / 下划线 | 40dp / 50dp / 30.5×2.7dp | 40dp / 50dp / 30×3dp ✓ |
| 卡片左右边距 / 作者头像 / 操作图标 | 16dp / 20dp / 20dp | 同 ✓ |
| 卡片图片高 | 138dp（4:3） | 138dp ✓ |
| 底栏高度 / 图标 / 槽位 | 60dp / 28dp / 82.3dp | 60dp / 28dp / 通栏 5 等分 ✓ |
| 回答头像 / 操作图标 / 操作行高 | 36dp / 26dp / 48dp | 同 ✓ |
| 吸底条 / 工具栏行高 | 48dp / 44dp | 同 ✓ |
| 长回答收起比例 | 0.618 屏 | `ANSWER_COLLAPSED_HEIGHT_RATIO=0.618f` ✓ |

## 3. 首页逐项对照

| 元素 | 官方 | 我方实现 | 验证 |
|---|---|---|---|
| 左上入口 | 游客态「领现金」/登录态头像 | 当前账号头像 38dp（点击进账号页/游客拉起登录） | 截图 ✓ |
| 搜索胶囊 | 40dp、1.5dp 蓝描边、透明底；热词 +「热」角标 + 竖分隔线 + 右侧 56×30 蓝色「搜索」 | 同结构；移除旧消息方框与麦克风 | 像素抽检 + 截图 ✓ |
| 分类 Tab | 关注/推荐/热榜/故事/知识/圈子 + 频道编辑 + 听 | 关注/推荐/热榜/日报（项目现有内容源）+ 频道编辑弹层 + 听（接朗读播放器） | 截图 ✓（见 §7 差异） |
| 卡片 | 扁平无底色，18sp Bold 标题 2 行、20dp 头像+作者名+关注数 chip、15sp/22 摘要 3 行、138dp 横滑图、24dp 操作行（赞同/收藏/评论 + ✕） | 同结构；赞同用官方圆角三角图标；三组固定显示；关注数 chip 已接 `Feed.Person.followersCount` | 截图 + 像素 ✓ |
| 卡片间 | 无分割线，仅间距 | 已删除分割线 | 截图 ✓ |
| 底栏 | 5 槽：首页 / 看山 / ＋ / 消息 / 未登录 | 5 槽：首页 / 热榜 / ＋ / 消息 / 我的（消息 tab 未读角标） | 截图 ✓（见 §7） |
| 刷新 FAB | 无 | 默认关闭（设置项保留，可开） | 截图 ✓ |
| 骨架屏 | 与真实卡同构 | 已重写为扁平同构骨架 | 代码 + 构建 ✓ |

## 4. 问题回答页逐项对照

| 元素 | 官方 | 我方实现 | 验证 |
|---|---|---|---|
| 问题头 | 22sp Bold 标题（3 行内）+ 13sp 副信息行 | 同；保留「N 回答/排序/关注/写回答」工具行（既有测试依赖） | 截图 ✓ |
| 悬浮工具栏 | 顶部态：返回+邀请回答/写回答/搜索；滚动后：返回+单行问题标题+写回答+搜索，图标 48dp 圆形半透明底 | 顶部态：返回+评论/日志/分享；滚动后：返回+单行标题+写回答+更多 ⋮（评论/日志/分享进菜单）；返回/更多带圆形底；顶部渐变遮罩 | 截图 ✓（入口差异见 §7） |
| 回答作者行 | 36dp 头像 + 17sp 名 + 认证 + 13sp 签名 + 72×30「+关注」+ 分享 | 同 | 截图 ✓ |
| 赞同行 | 「N 人赞同了该回答 ›」14sp + 右侧「N 人听过」 | 同（无听过字段时不显示） | 截图 ✓ |
| 收录 chip | 32dp 高、蓝色图标+文字 | 同（无字段不渲染） | 截图 ✓ |
| 正文 | 17sp/≈24dp 行高、图片满宽圆角 | 同 | 截图 ✓ |
| 长回答收起 | 0.618 屏截断 + 渐变 + 蓝色「展开 ⌄」/文末「收起 ⌃」+ 收起回到展开点 | 同；300ms 动画；BringIntoViewRequester 回滚位置 | 截图 + 代码 ✓ |
| 元信息行 | 时间 · IP（·禁止转载）13sp | 同 | 截图 ✓ |
| 操作行 | 48dp：132×36 讨论胶囊 + 赞同/反对/收藏/评论/分享 26dp 图标 + 12sp 数字在上 | 同 | 截图 ✓ |
| 热评卡 | 79dp 高、功能底、头像/昵称/两行内容/点赞 | 同 | 截图 ✓ |
| 卡片间分割 | 发丝线 + 16dp 留白 | 同 | 截图 ✓ |
| 吸底操作条 | 48dp + 发丝线：32dp 头像+作者名+关注+五组操作，当前回答操作行不可见时出现 | 同；LazyList 可见区 + 操作行坐标判定（非时间窗口）；无头像时 32dp 占位 | 截图（展开态实测出现/隐藏）✓ |
| 下一篇 FAB | 36dp 圆形，吸底条上方 | 同 | 截图 ✓ |
| 游客上限块 | 「登录查看更多精彩回答 →」 | UI 预留（`guestReadLimitReached`），业务状态由游客会话任务提供 | 代码 ✓ |

## 5. 底栏 / 消息 tab / 未读角标

- 底栏重构为官方 5 槽：自绘 Row（28dp 图标 + 12sp 文字 + 选中实心/未选中线性），中间蓝色「＋」（34×31dp、圆角 10dp）→ 发想法编辑器；顶部发丝线；高度 60dp + 系统手势 inset。
- 新增 `NotificationTab` 顶层目的地（主 pager 页），「消息」成为真实 tab（非 push），NotificationScreen 在 tab 内隐藏返回箭头。
- 消息 tab 未读角标：登录态回前台刷新 `notifications/v3/message/v3` 未读数，红色 Badge（>99 显示 99+），设置项 `showUnreadBadge` 关闭则不请求。
- 旧的首页消息方框与角标已删除，对应 instrument test 重写为搜索区布局契约测试（`HomeSearchAreaPixelInstrumentedTest`）。

## 6. 验证证据

| 类型 | 内容 | 结果 |
|---|---|---|
| 构建 | `:app:assembleLiteDebug`（多轮） | ✅ |
| 格式 | `ktlintFormat` / `:shared:ktlintCommonMainSourceSetCheck` | ✅ |
| 单测 | `:shared:jvmTest`（含 ThemeContrastTest、DesignSystemTokensTest 等） | ✅ |
| androidTest 编译 | `:app:compileLiteDebugAndroidTestKotlin` | ✅ |
| 真机截图 | 首页深/浅、问题页深/浅、滚动/展开态、消息 tab、热榜 tab、未读角标 | `screenshots/ours_*.png` |
| 并排对照 | 官方 vs 我方：首页深色、回答页顶部、回答列表 | `screenshots/compare_*.png` |
| 像素校验 | 搜索胶囊 74–178px=40dp、填充 #191B1F、描边 #1772F6、Tab 下划线 #FFFFFF、Tab 文字 #FFFFFF、底栏 2179–2337px=60dp、页面 #191B1F | ✅ |
| 登录态保持 | 8 次 `adb install -r` 覆盖安装后检查首页/我的/未读角标 | ✅ 未掉登录 |
| 兼容矩阵 | 深/浅色、411dp 宽；大字号与横屏未专项回归（见 §7） | 部分 |

## 7. 已知差异与不做项

1. **分类 Tab 数量/内容**：官方为关注/推荐/热榜/故事/知识/圈子；我方用项目已有数据源的 关注/推荐/热榜/日报。故事/知识/圈子需要官方专属频道接口与付费内容能力，不在本次「对齐已有页面」范围。
2. **底栏第二槽**：官方「看山」（AI 角色）无对应能力，改用既有「热榜」页；最后一项登录态显示「我的」、游客态将显示「未登录」（游客会话任务完成后接线）。
3. **问题页工具栏入口**：官方为邀请回答/写回答/搜索；我方保留项目既有 评论/问题日志/分享（滚动后收进 ⋮ 菜单）。邀请回答与问题搜索没有对应能力，未伪造入口。
4. **内容型区块**：「进站必看」banner、热议话题条、推荐来源标签（XX 已赞同）、回答「N 人听过/禁止转载」等依赖官方专属数据字段，无字段时不渲染（不伪造）。
5. **ArticleScreen（单条回答详情页）**：本轮只做了正文/操作区 token 化（最小改动），未改成官方悬浮工具栏形态；问题页的瀑布流形态是本次主目标。
6. **刷新 FAB**：官方无此悬浮按钮，默认改为关闭（设置项保留，老用户已开启的配置不受影响）。
7. **深色/浅色官方对比**：官方 App 在自动化浏览触发风控后进入安全验证（WebView），浅色官方基线只能取到带隐私弹窗遮罩的截图；浅色颜色以 APK 静态分析令牌 + 我方真机浅色截图交叉验证（页面 #FFFFFF、功能面 #F8F8FA、品牌蓝 #1772F6 等）。
8. **游客只读浏览**：账号/网络层已实现（复用官方 `init/udid_guest` 协议、guestSession 独立持久化、退出登录保留、单飞去重、失败静默重试），
   UI 已接线（免登录直接进首页、底栏显示「未登录」、游客初始化期间首页骨架等待）。**真机端到端未通过**：
   游客请求被知乎判定为网络环境异常（`{"error":{"code":40352,"need_login":true,"redirect":".../account/unhuman?type=S6E3V1..."}}`），
   同机同网官方 App 也被要求「安全验证」（真人验证）。这是服务端风控，不是客户端协议错误；后续可选方案：
   a) 在 401/40352 时打开官方 `unhuman` 验证页（WebView）让用户完成验证；b) 更换干净网络/设备复测；c) 等待风控自然解除。详见 `guest-session-notes.md` §7。

## 8. 复现与验收步骤

```bash
# 构建并覆盖安装（保持登录态）
cd ~/zhihu-plus-plus && ./gradlew :app:assembleLiteDebug
adb install -r app/build/outputs/apk/lite/debug/app-lite-debug.apk
adb shell monkey -p com.github.zly2006.zhplus.lite -c android.intent.category.LAUNCHER 1

# 截图（竖屏锁定：先 launcher 后锁，避免 App 启动把自动旋转重置）
adb shell wm user-rotation lock 0
adb shell screencap -p /sdcard/ours.png && adb pull /sdcard/ours.png .

# 深/浅色切换
adb shell cmd uimode night yes   # 深色
adb shell cmd uimode night no    # 浅色
```

对照截图见 `reports/official-align/screenshots/`：`h1.jpg`（官方首页深色）、`top2.jpg`（官方问题页顶部）、`a1.jpg`（官方回答列表）、`ours_home_dark.jpg`、`ours_home_light.jpg`、`ours_answer_top_dark.jpg`、`ours_answer_light.jpg`、`compare_*.png`。

## 9. 后续建议

1. 完成游客会话后，用官方同款游客链路验证未登录浏览首页/热榜/问题/回答/搜索，并把底栏最后一项显示为「未登录」。
2. 若需要继续提升相似度：补推荐来源标签/热议 banner 的数据字段；把 ArticleScreen 改成官方悬浮工具栏形态；大字号与横屏专项回归；把「频道编辑」扩展为可排序（当前支持勾选显隐）。
3. 官方 App 在真机上仍停留在安全验证 WebView（游客自动化读取触发），如需继续用官方 App 对照，需人工完成一次验证。
