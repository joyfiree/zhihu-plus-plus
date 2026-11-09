# 官方知乎 Android 11.14.0 APK 静态分析报告（面向 Zhihu++ UI 对齐）

- 目标：`zhihu-11.14.0.apk`（`com.zhihu.android`，versionCode=41410，versionName=11.14.0，minSdk 21/targetSdk 34，单 base.apk，171 MB）
- 设备基准：Redmi K30 Pro 1080×2400 @420dpi，官方 App 当前为未登录游客态
- 分析方式（全部只读）：`rea` CLI（`inspect-android-package`/`search-android-classes` 等走 JADX MCP）+ WSL `aapt2`（build-tools 37.0.0）解码 `resources.arsc` 与二进制 XML + 自写 Python DEX 解析器（字符串池 / 类名 / 方法级字面量·调用·字段 / 注解）。不修改 APK、不联网、不动仓库源码。
- 证据来源缩写：
  - **[arsc]** `aapt2 dump resources`（53,568 条资源，含 `night` 变体）；**表内颜色资源命名可读**（如 `MapText02A`、`MapUIFrame99A`），但大部分 layout/dimen/string/id 资源名被混淆。
  - **[xml]** `aapt2 dump xmltree --file res/layout/<x>.xml`（权威值，dp/sp 已解码）
  - **[theme]** app 主题 `0x7f130518`（`style/a87`）及其 parent 链 + `android.jar` framework attr 名称表
  - **[dexstr]** DEX 字符串池全量提取（625,151 条唯一字符串）
  - **[dex]** 自写 DEX 解析：方法级 `const-string`/invoke/field 引用、类与方法清单（72,613 个 JADX 类 / 168,438 个 class_def）
  - **[asset]** APK `assets/` 原始文件
- 中间产物目录：`C:\Users\lvyuxing\AppData\Local\Temp\zhihu-recon\analysis\`（`aapt2_resources.txt`、`xmltree\`、`dex_strings.txt`、`dex_classes.txt`、`method_*.txt` 等）

---

## A. 配色（深浅色主题关键色值）

官方 11.14 的 UI 颜色已收敛到一套名为 **“Map”** 的设计令牌（旧的 `BL*`/`BK*`/`GBK*` 大多只是别名层，GBK→Map 的映射在 [arsc] 中可直接读到）。下表为**资源实际值**，格式 `浅色 / 深色`。

| 角色 | 令牌（资源名） | 浅色 | 深色 | 证据 |
|---|---|---|---|---|
| 框架层背景（抽屉 / 搜索胶囊等 `MapUIFrame*` 面） | `MapUIFrame10A` (0x7f060289) | `#F8F8FA` | `#212429` | [arsc]；`res/layout/aq.xml` 抽屉背景 `@0x7f060289`、`res/layout/ayr.xml` 搜索胶囊同色 [xml] |
| 主题 `windowBackground`（旧主题） | `color_fff2f4f7_black`(0x7f06052a) → `GBK09C` → `MapUIFrame09C` | `#E8E9ED` | `#000000` | [arsc] + [theme] style/a87 |
| 卡片 / 容器 / 顶栏底色 | `MapUIFrame99A` (0x7f06028f) | `#FFFFFF` | `#191B1F` | [arsc]；底栏容器 `ZHMainTabContainer` 背景=该色 [xml aq.xml] |
| 顶栏（问题/回答工具栏） | `GBK99A`(0x7f0601af) → `MapUIFrame99A` | `#FFFFFF` | `#191B1F` | [xml bwi.xml/cha.xml] + [arsc] |
| 正文主色 | `MapText02A` (0x7f060259) | `#191B1F` | `#FFFFFF` | [arsc] |
| 次级文字 1 | `MapText03A` (0x7f06025d) | `#373A40` | `#C2C6CF` | [arsc] |
| 次级文字 2 / 默认图标 | `MapText04A` (0x7f060260) | `#535861` | `#C2C6CF` | [arsc]；问题/回答工具栏标题色 `GBK04A`→该色 [xml bwi.xml/cha.xml] |
| 三级文字 | `MapText05A` (0x7f060264) | `#81858F` | `#9196A1` | [arsc] |
| 辅助说明 | `MapText06A` (0x7f060267) | `#9196A1` | `#9196A1` | [arsc] |
| 弱化/禁用 | `MapText07A` (0x7f06026a) | `#ADB0B7` | `#5A5E66` | [arsc] |
| **品牌蓝** | `MapBrand` / `MapInfo` (0x7f060223 / 0x7f06023b) | `#1772F6` | `#558EFF` | [arsc] |
| 品牌蓝浅 | `MapBrandLight` | `#A8CFFE` | `#146EF5` | [arsc] |
| 品牌蓝副 | `MapInfoSub` | `#8491A5` | `#929AAB` | [arsc] |
| 品牌蓝 50%（搜索栏竖分隔） | `MapInfo_50` (0x7f06023f) | `#80056DE8` | `#80558EFF` | [arsc]；[xml ayr.xml] 分隔线颜色 |
| 旧品牌蓝（主题 textColorLink） | `BL01` / `BL03` | `#056DE8` | `#03A9F4` | [arsc]+[theme] |
| 分割线/描边 1 | `MapUIFrame09A` (0x7f060280) | `#EBECED` | `#282B30` | [arsc] |
| 分割线/描边 2 | `MapUIFrame08A` (0x7f06027c) | `#C4C7CE` | `#282B30` | [arsc] |
| 底栏底/分栏色 | `MapUIFrame99C` (0x7f060295) | `#FFFFFF` | `#282B30` | [xml aq.xml] `bnav_dividerColorDay/Night`；[arsc] |
| 点赞/正反馈 | `MapPositiveLike` (0x7f06024f) | `#F05159` | `#F05159` | [arsc] |
| 点赞辅助 | `copy_MapPositiveLike` | `#FF3366` | `#FF5066` | [arsc] |
| 收藏 | `MapFavorite` (0x7f060237) | `#FA9D14` | `#FA9D14` | [arsc] |
| 反对/负反馈（候选） | `RD03` (0x7f0602bb) | `#F1403C` | `#F1403C` | [arsc]（**该 token 未在回答页布局中被直接引用，属推断候选**） |
| 强调色 A/B/C | `MapUIAccentA/B/C` | `#FA778F` / `#FCD60D` / `#16B89B` | 同 | [arsc] |
| 遮罩 | `MapUIFrame01A` | `#000000` | `#000000` | [arsc] |

补充：
- app 主题 `style/a87`（0x7f130518）显式设置了 `android:windowBackground=color_fff2f4f7_black`、`android:textColorHint=color_4c000000_4cffffff`（→`GBK07A`=`MapText07A`）、`android:textColorLink=color_ff0077d9_8a03a9f4`（浅 `#056DE8` / 深 `#03A9F4`），并 `android:forceDarkAllowed=false`（v29 变体）[theme]。
- 深色模式由 `night` 资源限定符原生切换（同一颜色资源自带 `(night)` 值），不是运行时代码染色 [arsc]。
补充：官方 app 主题 `windowBackground` 为 `MapUIFrame09C` `#E8E9ED` / `#000000`（旧主题层），新版 Map 组件里“框架面”用 `MapUIFrame10A`，卡片/顶栏/底栏用 `MapUIFrame99A`；首页内容区实际底色（白底或 `#F8F8FA`）未在本次定位到的布局里显式声明，需真机取色确认，勿直接照抄。
- 底栏图标 tint 选择器 `res/color/bnav_icon_tint_color.xml`：`state_selected`/`state_checked` → `@color/GN01` `#67C23A`，默认 → `@color/blue_400` `#42A5F5` [xml]。**疑为该库默认值，官方主界面很可能由代码覆盖，真机取色可证伪/证实；此处不臆断。**

## B. 首页尺寸/排版

主要证据：`res/layout/aq.xml`（主界面根布局：抽屉 + 主 Tab 容器 + 底栏）、`res/layout/ayr.xml`（首页搜索区/搜索胶囊）[xml]；dimen 值取自 [arsc]。

**主结构（`res/layout/aq.xml`）**
| 元素 | 值 | 来源 |
|---|---|---|
| 主内容 FrameLayout 底部内边距 | `48dp`（`@dimen/ah8`=0x7f070685） | [xml] + [arsc] |
| 底栏容器 `ZHMainTabContainer` 高 | `wrap_content`，`elevation=8dp`，背景 `MapUIFrame99A` | [xml] |
| 底栏 `BottomNavViewWithDivider` 高 | `wrap_content`；`bnav_maxWidth=640dp`；`bnav_dividerColorDay/Night=MapUIFrame99C` | [xml] |
| 左侧抽屉宽度 | `288dp`，背景 `MapUIFrame10A` | [xml] |
| 底栏条目文案（字符串 id） | 首页 `0x7f121dd9`、关注 `0x7f121ddb`、看山 `0x7f121ddc`、消息 `0x7f121ddf`、未登录 `0x7f121de1` | [arsc] strings |

**首页顶部搜索区（`res/layout/ayr.xml`）**
| 元素 | 值 | 来源 |
|---|---|---|
| 搜索胶囊（`ZHCardView`）高度 | **38dp** | [xml] |
| 胶囊圆角 | **19dp**（= 高/2，全圆角） | [xml] |
| 胶囊背景色 | `GBK10A` → `MapUIFrame10A` `#F8F8FA` / `#212429` | [xml]+[arsc] |
| 胶囊 elevation | `0dp` | [xml] |
| 左侧圆标图标（`ZHDraweeView`） | **18×18dp**，`marginStart=8dp`、`marginEnd=1dp`，默认 `gone` | [xml] |
| “搜索”按钮（`ZHImageView`） | 宽高 `@dimen/j0`=**34dp**，`padding=8dp`（即图标本体≈18dp） | [xml]+[arsc] |
| “搜索”文字（`ZHTextView`） | **14sp、bold**；`marginLeft=@dimen/hs`=**12dp**、`marginRight=14dp`；文案字面量 “搜索” | [xml]+[arsc] |
| 热词胶囊竖直分隔线 | 宽 **1.5dp**、高 `@dimen/hs`=**12dp**、颜色 `MapInfo_50` `#80056DE8`/`#80558EFF` | [xml]+[arsc] |
| 热词切换控件 | `SearchTextSwitcherV2`，`width=0dp + weight=10`，`maxWidth=150dp`；外层 `paddingStart=11dp` | [xml] |

**其他首页类元素**
- 首页分类 Tab 使用 `com.zhihu.android.base.widget.ZHTabLayout`（36 个布局使用该控件）；**首页 Tab 行高度/字号未定位到具体布局文件 → UNKNOWN**（布局名单被混淆 + Tab 文案来自服务端 tab 配置）。
- 首页卡片由 `com.zhihu.android.viewholder.RecommendFeedItemViewHolder`（`SugarHolder` 子类）绑定，`<clinit>` 中出现 `'title'` 与 `'getTitle()Lcom/zhihu/android/base/widget/ZHTextView;'`（说明标题控件为 `ZHTextView`，数据绑定风格），并引用 `EComFeedItem.medias` 等字段 [dex]。**卡片的 padding / 标题字号行高 / 作者行 / 摘要 / 操作行精确 dp 未在资源表中定位 → UNKNOWN**（卡片可能由 GX 模板引擎渲染：`assets/ZH-template/gx_question_feed_answer_short_container/...`、`com.zhihu.android.app.feed.template.component.TemplateBlockContainer`）。
- 底栏高度只由 `wrap_content` + 内容区 `paddingBottom=48dp` 约束（**具体 item 高度 dp UNKNOWN**，`com.zhihu.android.bottomnav` 库的 dimen 资源名被混淆，代码中通过 `getDimensionPixelSize` 读取）。

## C. 问题回答页尺寸/排版

证据：`res/layout/bwi.xml`（问题页工具栏）、`res/layout/cha.xml` + `res/layout/ch_.xml`（回答页工具栏）、`res/layout/a08.xml`（回答 Pager 容器）、`res/layout/a06.xml`（回答内容容器）[xml]。

| 元素 | 值 | 来源 |
|---|---|---|
| 问题工具栏 `QuestionToolBarWrapperView` 水平内边距 | `@dimen/hl`=**0dp**（`paddingStart/End`） | [xml]+[arsc] |
| 问题工具栏背景 | `GBK99A` → `MapUIFrame99A` `#FFFFFF` / `#191B1F` | [xml]+[arsc] |
| 问题工具栏内 `QuestionToolBarContainerView` 高 | `?attr/0x7f040089`（主题属性，未解析为具体 dp）→ **UNKNOWN** | [xml] |
| 问题标题/副标题文字样式 | `title_appearance=style/a8p`、`sub_appearance=style/a8o` | [xml bwi.xml] |
| 副标题字号 | `@dimen/az8`=**12sp**；颜色 `GBK04A`→`MapText04A` `#535861`/`#C2C6CF` | [arsc] |
| 回答工具栏 `AnswerToolBarWrapperView` | 与问题工具栏同结构（padding 0dp、背景 `MapUIFrame99A`），额外 `tintColor=GBK04A` | [xml cha.xml] |
| 回答页底部条（`include`）高度 | **58dp**，宽 `wrap_content`，默认 `gone`；引用布局 `@layout/0x7f0d08d1` | [xml a08.xml] |
| 回答 Pager 内容 `AnswerPagerContentView` | 全屏（match_parent） | [xml a08.xml] |
| 回答内容 `AnswerContentView` | 全屏；同布局内视频占位 `marginTop=200dp`、`minHeight=300dp` | [xml a06.xml] |
| ViewStub 展开布局 | `@layout/0x7f0d07a9`（a06）、`@layout/0x7f0d0871`（a08） | [xml] |

**UNKNOWN（未在资源中找到，不能臆断）**：问题正文标题字号/行高、回答卡间距、作者行（头像/昵称/关注按钮）尺寸、正文字号/行高、渐隐遮罩高度、圆形展开按钮尺寸、吸底操作条具体高度（仅知底部 include=58dp）、悬浮返回按钮尺寸/边距、图片圆角。原因：回答正文/卡片由富文本引擎（`com.zhihu.android.zrichCore.*`、`ZRichTextView`）与 GX 模板渲染，对应布局/dimen 资源名混淆且部分参数由服务端模板下发。建议真机截图量测作为补充证据。

## D. 游客会话机制

**1）游客初始化端点（确凿）**
- 端点字面量：`https://api.zhihu.com/api/account/prod/init/udid_guest`
  - 证据：[dexstr]（classes7.dex 字符串池，唯一命中）
- 相关类与方法（[dex] 方法级调用/字段/字符串提取）：
  - `com.zhihu.android.app.modules.account.model.AccountRemoteSource`
    - `sendUdidGuestRequest()`：`Observable.create`，字符串常量 `'create<UDIDGuestInfo> {...}'`
    - `requestGuestToken()`：调用 `com.zhihu.android.app.t0.c.c`（c()/d()，请求执行）；匿名内部类 `AccountRemoteSource$requestGuestToken$1` 的 `onResponseSuccess()` 调 `new UDIDGuestInfo(...)`、`onResponseFailed()/onRequestError()` 走 error
    - `requestBoth()`：调用 `com.zhihu.android.api.model.Authorisation;->createGuest`、`com.zhihu.android.cloudid.CloudIDHelper`（f/h/d）
  - `com.zhihu.android.app.modules.account.model.AccountRepositoryImpl#getUdidGuestInfo`
  - `com.zhihu.android.app.modules.account.GuestLoginServicesImpl#getUDIDGuestInfo`
  - `com.zhihu.android.account.provider.FutureAdapterConfigImpl#getUDIDGuestInfo / hasUdid`
  - 模型：`com.zhihu.android.api.model.UDIDGuestInfo`（字段 `deviceId`、`guest`；getter `getDeviceId()/getGuest()`）
- **发起时机**：官方在“隐私弹窗/隐私说明”流程后门闩等待 UDID/Token，证据为 DEX 中的日志字符串：`FirstPrivacyDialog …… 等待UDID/Token`、`PrivacyExplainActivity …… 等待UDID/Token`、`GrowthUDIDLoopHelper 获取 UDID/Token 成功，UDID = `、`PreinstallAppLauncher.preinstallWaitUDIDAndToken` 等 [dexstr]。即：**首次启动（含游客）先完成隐私同意，再请求 `init/udid_guest` 拿游客 token/UDID，随后才发起业务请求**。
- **存储位置**：`com.zhihu.android.app.util.GuestUtils`（`isGuest()/isNotGuest()`）负责游客态判断；SharedPreferences 键名可从 DEX 字符串读到：`preference_id_need_show_guest_intro`、`preference_id_guest_topic_test`、`preference_id_guest_test_type`；另有 `GuestLoginFragment`、`Lcom/zhihu/android/account/GuestLoginInterface;`、`Lcom/zhihu/android/api/model/GuestResponse;`、端点 `/guest/self` [dexstr]。**具体 token 写入哪个 prefs 文件/键未逐字节证实 → 部分 UNKNOWN。**
- **请求头/签名**：
  - DEX 中存在 `Authorization`、`AUTHORIZATION`、`AUTHORIZATION_PREFIX`、`buildAuthorization`、`addAuthorizationHeader`、`composeRequestAuthorization`、`Invalid signing date in Authorization header.` [dexstr] → 说明采用标准 `Authorization` 头承载（游客与登录用户同一通道，值由 `Authorisation.createGuest` 生成）。
  - DEX 字符串中 **没有** `x-zse-96`、也没有 `d_c0`（只有 `z_c0`）[dexstr] → 签名头与 `d_c0` cookie 很可能由 SecNeo 加固的逻辑或 native 生成。证据：DEX 中存在大量 `Lcom/secneo/apkwrapper/H;->d`（字符串解密桩）调用（`AccountRemoteSource.<clinit>/requestBoth/...` 均有），可解释“URL 明文存在于字符串池但无 `const-string` 引用”的现象。
  - **结论**：游客态浏览不需要登录，App 通过 `init/udid_guest` 获取游客凭证并写入 Cookie（`z_c0` 存在 [dexstr]）+ `Authorization` 头，后续 feed/回答接口携带同一套游客凭证。`x-zse-96`/`d_c0` 生成细节 → UNKNOWN（不在 DEX 明文层）。

## E. 登录门禁规则

**1）游客阅读上限的判定位置（确凿）**
- 文案实际值为 **“登录查看更多精彩回答”**（注意：不是“登录后查看更多…”）；DEX 字符串唯一命中 [dexstr]。
- 该文案由**数据模型**提供，而非本地 UI 硬编码：
  - `com.zhihu.android.question.api.model.LoginPromptData#getDisplayText()` 引用该字符串（classes12.dex）
  - `com.zhihu.android.feature.short_container_feature.model.LoginPromptUINode#getDisplayText()` 引用该字符串（classes9.dex）
  - [dex] 方法级 `const-string` 交叉引用
- 相关资源名（DEX 字符串/资源 id 名）：`guest_login_to_view`、`guest_login_prompt_button`、`dialog_guest_guide_login`、`guest_prompt_dialog_title_default/pin`、`guest_entry`、`passport_fragment_guest_login` [dexstr]。
- 回答页登录引导逻辑集中在 `com.zhihu.android.answer.utils.AnswerGuestGuideUtils`（含内部类 `GrowLoginFeature`、`GrowChainGuideName`、`GrowLoginBean`），属“增长实验”驱动的登录引导 [dex]（类名 + 内部类名；方法体未逐行解读）。**“阅读约 3 条后出现”的阈值常量未在 DEX 字符串层找到（可能来自服务端 LoginPrompt 数据或实验配置）→ UNKNOWN，建议以真机为准。**

**2）首页 Tab / Feed 接口（供项目层对照）**
- 首页推荐流：`/topstory/recommend`；带参变体 `/topstory/recommend?tsp_ad_cardredesign=0&feed_card_exp=card_corner|1&v_serial=1&isDoubleFlow=0` [dexstr]
- 热榜：`/topstory/hot-list/question-recommend`、`/topstory/hot-list/missed`、`/topstory/hot-list/weekly`、`/topstory/hot-list/weekly-option`、`/topstory/hot-lists/total`、`/v2/topstory/hot-lists/everyone-seeing?offset=0&limit=20`；`https://billboard-er.zhihu.com/topstory/hot-list` [dexstr]
- Tab 配置/埋点：`/feed/render/tab/config`、`/feed/render/revisit/tag_config`、`/feed-root/sections/*`、`/feed/render/recommend_interest/check` 等 [dexstr]
- 首页 banner：`https://api.zhihu.com/commercial_api/banners_v3/app_topstory_banner` [dexstr]
- 服务接口类线索：`TopStoryService.java`、`getMTopStoryService` [dexstr]（源码类名被混淆，Service 接口注解中的 URL 未能在 DEX 明文定位）
- **问题回答列表接口**（如 `/questions/{id}/answers`）**未在 DEX 明文中找到 → UNKNOWN**；游客阅读回答走的是同一套接口 + 游客凭证（依据 D `init/udid_guest` + `Authorization` 证据推断）。
- 关于 `feed/topstory`：本版本 DEX 中**没有**该字面量，只有 `/topstory/recommend` 系列 → 项目层应对照 `/topstory/recommend`，不要沿用旧 PRD 的 `feed/topstory`。

**3）游客可用性结论**
- 游客凭证由 `Authorisation.createGuest`（`AccountRemoteSource.requestBoth` 调用）创建，与登录用户共用同一授权头机制 → 首页/回答接口在游客态可正常携带凭证请求（这是“无登录也能浏览”的根因）[dex]。
- 需要登录的操作由资源字符串统一收口：`注册/登录后继续操作`、`注册/登录后点赞`、`注册/登录后回答问题`、`登录后才能发表评论` 等（DEX 字符串清单可见，`0x7f120327` 等）[arsc strings]。
- 未登录态底栏最后一项显示“未登录”（字符串 id `0x7f121de1`）[arsc]。

## F. 其他可对齐常量

**1）底栏 5 项与图标/动画资源**
- 底栏实现：`com.zhihu.android.bottomnav.BottomNavViewWithDivider`（继承 `com.zhihu.android.bottomnav.core.BottomNavView`），在 `res/layout/aq.xml` 中声明 [xml][dex]。
- 底栏动效资源（Lottie JSON，日/夜两套共 20 个）[asset]：
  - `assets/Tabbar_Feed.json`、`Tabbar_Notifications.json`、`Tabbar_Shop.json`、`Tabbar_Video.json`、`Tabbar_More.json` 及 `Tabbar_explore2_*` 系列；夜间版 `assets/Night_Tabbar_*.json`
  - 每个动画：画布 **28×28 px**、`fr=60`、`ip=0`、`op=27` ⇒ **时长 = 27/60 = 450ms**（如 `Tabbar_Feed.json`）
- 图标资源命名体系：`R2` 资源类中可直接读到语义名，例如 `zhicon_icon_24_user_arrow_right`（24dp 图标体系，后缀 `_24_`）[dex]。其他具体 `zhicon_*` 名称需逐类提取，**底栏每个图标的具体 drawable 名 UNKNOWN**（混淆 + SecNeo 字符串加密）。

**2）圆角/分割线等可复用 dp**
- 搜索胶囊：高 **38dp**、全圆角**19dp**、elevation **0dp**
- 顶栏/工具栏：水平 padding **0dp**（问题页/回答页）；副标题 **12sp**
- 底栏：`elevation 8dp`、`maxWidth 640dp`、内容区底部预留 **48dp**
- 竖分隔线：宽 **1.5dp**、高 **12dp**、色 `#80056DE8 / #80558EFF`
- 图标尺寸实例：搜索区圆标 **18dp**；搜索图标按钮 **34dp**（padding 8dp）；回答页底部条 **58dp**

**3）动效时长**
- 底栏 Lottie：**450ms**（可确定）
- 其他过渡动画（页面切换、展开按钮、渐隐动画）**UNKNOWN**：`res/anim` 有 270 个动画资源但文件名/用途均混淆，且 DEX 中存在 `Lcom/zhihu/android/base/util/duration` 类线索未能定位具体常量。

---

## 对 Zhihu++ 实现最有价值的 10 条结论

1. **颜色直接抄“Map”令牌**：页面背景 `#F8F8FA / #212429`（MapUIFrame10A）、卡片/顶栏/底栏底 `#FFFFFF / #191B1F`（MapUIFrame99A）、正文 `#191B1F / #FFFFFF`（MapText02A）、次级 `#535861 / #C2C6CF`（MapText04A）、**品牌蓝 `#1772F6 / #558EFF`**（MapInfo）、分割线 `#EBECED / #282B30`（MapUIFrame09A）。这些是渲染链路里真正被布局引用的色，不要再沿用旧 PRD 的 `#0084FF` 系。
2. **点赞态是红粉色 `#F05159`（MapPositiveLike），收藏是橙色 `#FA9D14`（MapFavorite）**；不是蓝色。反对态候选 `#F1403C`（RD03，需真机确认）。
3. **首页搜索胶囊**：38dp 高 + 19dp 全圆角 + 背景 `#F8F8FA/#212429` + 内部左圆标 18dp + “搜索”14sp bold + 右侧 34dp 搜索按钮（含 8dp padding）+ 热词区 12dp 竖线（`#80056DE8`）/最大宽 150dp。这一条能直接对齐官方顶部视觉。
4. **官方 11.14 首页不再是“卡片整体圆角+阴影”风格**：主容器只有底栏 `elevation 8dp`，搜索胶囊 elevation=0；内容区给底栏预留 `48dp`。项目层应优先用“扁平卡片 + 细分割线（#EBECED/#282B30）”而不是重阴影。
5. **游客态必须实现 `init/udid_guest` 链路**：端点 `https://api.zhihu.com/api/account/prod/init/udid_guest`，在隐私同意后触发，返回 `UDIDGuestInfo{deviceId, guest}` 并生成游客 `Authorization`；否则无法复现官方“未登录可正常浏览”的行为。对应类：`AccountRemoteSource/AccountRepositoryImpl/GuestLoginServicesImpl`。
6. **登录门禁不要靠客户端硬编码条数**：官方文案由服务端 `LoginPromptData/LoginPromptUINode.displayText` 下发（“登录查看更多精彩回答”），本地阈值未在 DEX 明文出现。Zhihu++ 若要对齐，应优先解析服务端 LoginPrompt 节点，而不是写死“3 条”。
7. **首页接口用 `/topstory/recommend`**（含 `tsp_ad_cardredesign/feed_card_exp/v_serial/isDoubleFlow` 参数模式），**不要用旧 PRD 的 `feed/topstory`**；热榜用 `/topstory/hot-list/*` 与 `/v2/topstory/hot-lists/*`。
8. **问题页/回答页顶栏是“0 水平 padding 的自绘 Toolbar 包裹器”**（`QuestionToolBarWrapperView`/`AnswerToolBarWrapperView` + 内部 ContainerView），标题走 `title_appearance` 样式、副标题 12sp。实现时不要套 Material TopAppBar 的默认 16dp inset。
9. **问题页底部条高度已知为 58dp**（回答 Pager 的 `include`），可作为“欢迎参与讨论”输入条/吸底操作条的基准高度；其余回答正文相关尺寸必须真机量测（APK 中由富文本/GX 模板渲染，无可读资源）。
10. **底栏动效资产可直接复刻**：Lottie 画布 28×28、60fps、27 帧 = 450ms，夜/日两套；底栏容器 `maxWidth 640dp`、`elevation 8dp`、分割线用 `MapUIFrame99C`，图标采用 `zhicon_icon_24_*` 命名体系。

---

## 附：未能取得（UNKNOWN）清单

| 项 | 原因 |
|---|---|
| 首页分类 Tab 行高度/字号；卡片 padding、标题字号/行高、作者行、摘要、操作行 | 布局/dimen 资源名混淆，卡片可能由 GX 模板或服务端模板渲染，未能在资源表定位 |
| 问题标题字号/行高、回答卡间距、正文 17sp 等正文字号/行高 | 富文本 `zrichCore` 渲染 + 资源混淆 |
| 渐隐高度、圆形展开按钮尺寸、图片圆角、悬浮返回按钮尺寸/边距 | 同上 |
| 底栏 item 高度 dp | `com.zhihu.android.bottomnav` 库 dimen 资源名混淆，运行期 `getDimensionPixelSize` 读取 |
| `x-zse-96` 请求头、`d_c0` cookie 生成 | DEX 无明文（SecNeo 加固/native），仅确认 `z_c0`、`Authorization`、`AUTHORIZATION_PREFIX` 存在 |
| 游客 token 具体落盘文件/键名 | 仅确认 `GuestUtils`、`preference_id_guest_*` 等键名存在 |
| 问题回答列表接口 URL（`/questions/{id}/answers` 形态） | DEX 明文中未找到，疑被加密/拼接 |
| 游客阅读上限具体阈值（3 条?） | 服务端 LoginPrompt 数据或实验配置决定，DEX 明文无阈值常量 |
| 其他过渡动画时长（除底栏 450ms） | `res/anim` 270 个文件用途混淆，未定位到具体常量 |
