# 第二轮反馈修复记录（2026-11-09）

> 用户反馈四点：① 首页 Tab 右侧耳机图标无意义；② 首页卡片点进去应从对应回答开始看；③ 热榜/消息/发布不该占底栏；④「我的」页太丑需按现代 UI 重做。
> 验证：Redmi K30 Pro 真机安装截图 + 构建/ktlint/shared jvmTest/androidTest 编译全绿。

## 1. 删除首页 Tab 右侧「听」图标

- `HomeScreen.kt`：移除 `HomeCategoryTabRow` 的听按钮与 `onListen` 启动朗读逻辑（保留 `RegisterReadingQueueSource` 供阅读器使用）；Tab 行右侧只保留频道编辑。
- 影响：朗读播放器仍可从回答页/设置进入，不受影响。

## 2. 从首页卡片进入问题时定位到对应回答

- `NavDestination.Question` 新增 `initialAnswerId: Long? = null`。
- 首页卡片点击时把 `Feed.AnswerTarget.id` 透传（回答详情兜底路径同样透传）。
- `QuestionScreen` 在回答列表加载后按 id 查找目标回答，滚动到列表对应位置（先粗定位、等布局稳定后再校正一次，避免停在上一两条回答尾部）；若第一页未命中会再补一页继续查找。
- 真机验证：首页卡片作者「风万里」→ 问题页打开后其回答作者行出现在悬浮工具栏正下方（`question_located_from_feed.jpg`）。

## 3. 底栏精简 + 首页顶部「更多」入口

- 底栏恢复为「首页 / 我的」两项（`defaultBottomBarSelectionKeys`、去掉中间蓝色＋发布槽位、去掉 NotificationTab/通知 pager 页与其未读角标；`ZhihuMainPreferencesTest` 同步恢复断言）。
- 首页顶部去掉左侧账号头像：搜索胶囊占满左侧，右侧新增 38dp「更多」按钮（宫格图标）：
  - 登录态弹层：发布想法 / 消息（带未读角标）/ 收藏夹 / 设置；
  - 游客态弹层：登录知乎 / 设置。
  - 点击消息进现有消息页（push，带返回）。
- 消息未读角标从底栏迁到该按钮（登录态 + 用户开启角标设置时回前台刷新；角标外置、不再被圆形裁剪）。
- 频道编辑弹层、Tab 行、热榜/日报 Tab 保持不变。

## 4. 「我的」页现代 UI 重构

- `AccountSettingScreen.kt` 全量重写：可折叠 Hero（渐变 172dp+状态栏 inset、64dp 头像、22sp 昵称、@ID、查看主页 pill、扫码/更多菜单含身份管理与退出登录；滚动后浮出 48dp「我的」小标题栏）→ 4 列快捷卡（发想法/收藏夹/关注订阅/浏览历史）→ 44dp 设置搜索胶囊 → 4 组设置行（内容与推荐 / 外观与阅读 / 账号与安全 / 系统）→ 页脚（版本号连续点击开开发者模式、检查更新、GitHub、AGPL）。
- 保留全部 18 个 `ACCOUNT_SETTINGS_*` 常量与入口、退出确认、扫码、进页刷新 profile、翻页手势与函数签名；旧 duo3 分支与 M3 卡片堆叠删除。
- 删除 `AppearanceSettingsScreen.kt` 中已无调用方的 `shouldShowAccountHistoryShortcut`。

## 5. 验证与截图

- 深色：`screenshots/home_dark.jpg`、`home_menu_dark.jpg`、`account_dark.jpg`、`account_scrolled_dark.jpg`、`question_located_from_feed.jpg`
- 浅色：`screenshots/home_light.jpg`、`account_light.jpg`
- 对照旧版：`screenshots/account_before.jpg`
- 构建：`:app:assembleLiteDebug` ✅；`:shared:compileKotlinJvm` ✅；`:app:compileLiteDebugAndroidTestKotlin` ✅；`:shared:jvmTest` ✅；`ktlintFormat` ✅
- 说明：验证过程中发现 App 主题被设为「暗色」，已恢复为「自动」；系统深色/浅色切换下两套主题都通过截图检查。
