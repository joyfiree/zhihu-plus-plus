# 官方 App 对齐证据账本

> 目标：对照官方知乎 Android 11.14.0 的布局/交互/配色比例，把 Zhihu++ 首页与问题回答页对齐。
> 设备：Redmi K30 Pro（lmi），1080×2400 @ 420dpi（2.625x），Android 16，系统深色/浅色。
> 官方基线：`com.zhihu.android` versionName=11.14.0 / versionCode=41410（未登录游客态）。
> 我方 APK：`com.github.zly2006.zhplus.lite` v0.31.1（debug，登录态），真机覆盖安装。
> 状态值：`UNKNOWN` / `PROVED` / `INVALID`。截图全部位于 `reports/official-align/screenshots/`。

## 能力图

| 能力 | 入口 | 操作 | 成功态 | 失败/空态 | 归属 | 状态 |
|---|---|---|---|---|---|---|
| 首页推荐流（官方游客） | 启动默认落地 | 垂直滚动 | 卡片流可读、可点开问题 | 弱网骨架 | Android 官方 | PROVED（h1.jpg / ui1.xml） |
| 首页推荐流（我方） | 启动默认落地 | 垂直滚动 | 同左 | 骨架 + 错误重试 | Zhihu++ | PROVED（hD4.jpg / hL1.jpg） |
| 首页搜索区 | 顶部 | 点击搜索框 | 进入搜索页 | — | Android 官方 | PROVED（h1.jpg / crop_official_top.jpg） |
| 首页分类 Tab | 搜索区下方 | 点击切换 | 关注/推荐/热榜/故事/知识/圈子 | — | Android 官方 | PROVED（h1.jpg / crop_official_tabs.jpg） |
| 首页分类 Tab（我方） | 搜索区下方 | 点击切换 | 关注/推荐/热榜/日报（项目现有内容源） | — | Zhihu++ | PROVED（hD4.jpg；故事/知识/圈子无数据源，不做） |
| 首页底栏 | 屏幕底部 | 点击 | 首页/看山/＋/消息/未登录 | 游客点消息→登录 | Android 官方 | PROVED（h1.jpg / crop_official_nav.jpg） |
| 首页底栏（我方） | 屏幕底部 | 点击 | 首页/热榜/＋/消息/我的（消息带未读角标） | 未登录点我的→登录入口 | Zhihu++ | PROVED（hD4.jpg，角标 75 像素验证） |
| 问题回答页（官方游客） | 首页点问题标题 | 滚动/展开 | 问题标题+回答列表、吸底操作条 | 游客读若干条后提示登录 | Android 官方 | PROVED（q1/top2/a1/q4.jpg） |
| 问题回答页（我方） | 首页点问题标题 | 滚动/展开 | 同上（含排序行/工具行等既有能力） | 空/错状态走 PaginatedList | Zhihu++ | PROVED（qA1/qA2/qL1.jpg） |
| 回答展开/收起 | 回答内容底部渐隐区 | 点「展开 ⌄」/「收起 ⌃」 | 展开全文 / 收起 + 回到展开点附近 | — | 双方 | PROVED（官方 q1.jpg；我方 exp2/qA2.jpg） |
| 吸底操作条 | 回答页底部 | 阅读长回答 | 当前回答的作者+关注+五组操作 | 卡片操作行可见时隐藏 | 双方 | PROVED（官方 top2.jpg；我方 exp2.jpg） |
| 游客阅读上限 | 回答页底部 | 游客继续滚动 | 「登录查看更多精彩回答 →」 | — | Android 官方 | PROVED（q4.jpg）；我方 UI 预留（guestReadLimitReached 默认 false） |

## 首屏优先级与空间预算（首页）

| 元素 | 任务/频率 | 优先级 | 同组 | 顺序 | 宽度/行数 | 折叠占用 | 空间来源 | 状态 |
|---|---|---|---|---|---|---|---|---|
| 搜索区（头像/热词/搜索按钮） | 高频导航 | P0 | 顶部工具组 | 1 | 通栏 40dp | 固定 | 原顶部 | PROVED（双方像素测量：74–178px=40dp） |
| 分类 Tab | 高频切换 | P0 | 顶部工具组 | 2 | 通栏 40dp | 固定 | 原 Tab 行 | PROVED（双方 179–284px=40dp） |
| 卡片标题 | 内容主信息 | P0 | 卡片 | 3 | 2 行/18sp Bold | 固定 | 卡片 | PROVED（h1 vs hD4） |
| 作者行（头像+昵称+关注数 chip） | 识别来源 | P1 | 卡片 | 4 | 1 行/20dp 头像 | 固定 | 卡片 | PROVED（h1 裁图 crop_author_row.jpg；hD5 已渲染 chip） |
| 摘要正文/图片 | 决策阅读 | P0 | 卡片 | 5 | 3 行/15sp + 138dp 图 | 固定 | 卡片 | PROVED（h1 vs hD4） |
| 操作行（赞同/收藏/评论/✕） | 中频操作 | P1 | 卡片 | 6 | 1 行/20dp 图标 | 固定 | 卡片 | PROVED（双方 24dp 行高） |
| 底栏（5 槽 + 中心＋） | 高频导航 | P0 | 底栏组 | 7 | 通栏 60dp | 固定 | 原底栏 | PROVED（双方 2179–2337px=60dp；图标 28dp） |

## 产品与协议

| 能力 | 产品证据 | 最终请求 | 响应契约 | 边界证据 | 状态 |
|---|---|---|---|---|---|
| 首页/问题/回答接口的游客可用性 | 官方游客可浏览 | `init/udid_guest` 游客凭证（APK 分析 §D） | 游客 cookies + Authorization | 官方游客读 3 条后提示登录 | PROVED（官方真机 + APK 证据） |
| 我方游客会话 | 无账号冷启动 | 官方 init/udid_guest（复用 ZhihuPhoneLoginClient） | ZhihuAccounts.guestSession | 游客骨架等待/底栏未登录/失败静默重试 | PROVED（协议+单测+UI 接线；真机被 40352 unhuman 风控阻塞） |
| 登录门禁文案 | 「登录查看更多精彩回答」 | 服务端 LoginPromptData 下发 | — | 官方真机 q4.jpg | PROVED（官方） |

## 项目数据流与预算

| 能力 | 现有入口 | client | 模型 | 状态/UI | 新请求数 | 依据 | 状态 |
|---|---|---|---|---|---|---|---|
| 首页推荐流 | HomeScreen / HomeFeedViewModel | 既有 web/mobile client | 既有 | 既有 + 新视觉 | 0 | h1 对照 | PROVED |
| 问题回答页 | QuestionScreen / ArticleScreen | 既有 | 既有 | 既有 + 新视觉 | 0 | top2/a1 对照 | PROVED |
| 消息未读角标 | ZhihuMain 底栏 | `mobileHomeFeedHttpClient` | `MobileNotificationMessageOverview` | 底栏 Badge | 1（回前台刷新，登录态才请求） | 官方底栏行为 | PROVED（hD4.jpg 角标 75） |
| 游客会话 | 主壳/Account 层 | 既有 ZhihuPhoneLoginClient 协议 | `ZhihuAccountSession`（guestSession） | 游客骨架等待、底栏「未登录」 | 1 次初始化 + 回前台重试 | APK 分析 §D + 真机 40352 证据 | PROVED（协议与 UI 接线；端到端被服务端风控阻塞） |

## 稳定性

| 场景 | 尝试 | 成功 | 状态码/错误分布 | 分页独有样本 | 重试结果 | 状态 |
|---|---:|---:|---|---|---|---|
| 官方首页首屏 | 多次真机冷启 | 成功（触发风控前） | 200 | 滚动多屏 | — | PROVED |
| 我方首页首屏 | 10+ 次冷启/覆盖安装 | 成功 | 200 | 分页正常 | — | PROVED |
| 覆盖安装保持登录态 | 8 次 `adb install -r` | 全部成功 | — | — | — | PROVED（每次安装后首页/我的仍为登录态，未读角标 75 存在） |
| 官方游客风控 | 3 次 | 触发安全验证 | WebView 安全验证 | — | 需人工验证 | PROVED（限制已记录，官方取证以已抓素材为准） |

## 完成审计

- [x] 官方首页截图/尺寸/颜色逐项对照完成（h1 + ui1.xml + 像素扫描）
- [x] 官方回答页截图/尺寸/颜色逐项对照完成（top2/q1/a1/q4 + 像素扫描）
- [x] 游客浏览行为实现完成（协议+存储+UI 接线），真机端到端被知乎 `40352 unhuman` 网络风控阻塞（官方 App 同网同样触发，证据见 guest-session-notes.md §7）
- [x] 当前 APK 真机截图与官方并排对比（首页深/浅、问题页深/浅）
- [x] 登录态覆盖安装后保持（8 次覆盖安装验证）
- [x] 构建 / ktlint / shared jvmTest 通过
