# 「我的」页与设置页重构（2026-11-09 第四轮）

> 用户反馈：「我的」页还是显得空、没有设计感；很多东西应该统一塞进设置页面，这里只留一个设置入口即可；头像/昵称/ID 那块显示不协调，参考知乎官方排布。

## 1. 设置项整体迁出「我的」页 → 新增独立设置页

- 新增 `ui/SettingsScreen.kt`（562 行）：`SETTINGS_SCREEN_*` 系列 testTag，含 48dp 顶栏（返回 + 「设置」标题）、44dp 设置搜索胶囊、4 个分组（内容与推荐 / 外观与阅读 / 账号与安全 / 系统）、页脚（版本号连点 5 次开开发者、检查更新 / GitHub / AGPL、长按复制提示）。
- 新增路由 `Account.Settings`：`NavDestination.kt` 定义 + `ZhihuMain.kt` 注册 `composable<Account.Settings> { SettingsScreen() }`，并加入 `isAccountDetailDestination()` 以在平板双栏时按详情页处理。
- 「我的」页不再有任何设置项；右上角一个齿轮 `ACCOUNT_SETTINGS_OPEN_SETTINGS_TAG` 进入设置页。退出登录与身份管理也随「账号与安全」组一起搬到设置页（`SETTINGS_SCREEN_LOGOUT_TAG` / `SETTINGS_SCREEN_IDENTITY_MANAGEMENT_TAG`）。
- 原有深链不受影响：`Account.AppearanceSettings` / `RecommendSettings` / `SystemAndUpdateSettings` / `SettingsSearch` / `DeveloperSettings` / `OpenSourceLicenses` / `IdentityManagement` 仍可直接导航，因为设置页只是入口收敛，子页面路由没动。

## 2. 头像 / 昵称 / 知乎号按官方排布重做

- 72dp 圆形头像 + 昵称（`ContentTitle` 22sp Bold）+ 知乎号，**同一行垂直居中对齐**；副标题文案为「知乎号 @xxx」，不再是裸 `@xxx`。
- 右侧只有扫码（`ACCOUNT_SETTINGS_SCAN_TAG`，登录态可用时显示）与设置齿轮两个 40dp 圆形按钮，都在资料行内，不再单独占一行。
- 去掉了旧的渐变 Hero 与 48dp 折叠小标题栏：官方「我的」页顶部就是平铺的资料区，没有额外的折叠动画层。
- 统计行紧随资料行（回答 / 文章 / 粉丝 / 关注，数字 17sp Bold + 标签 12sp 三级色），点击进自己主页对应 tab。

## 3. 「我的」页内容不再是纯设置表

登录态从上到下：资料区 → 4 列创作快捷卡（发想法 / 收藏夹 / 关注订阅 / 浏览历史）→ 「我的内容」分组（我的回答 / 文章 / 想法 / 收藏 / 提问，各自跳自己主页对应 tab）。

未登录态：登录引导卡 + 「浏览历史」「设置」两个本地可用入口，避免整页只有一张登录卡。

## 4. 变更文件

- `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/ui/SettingsScreen.kt`（新增）
- `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/ui/AccountSettingScreen.kt`（重写，1014 → 616 行）
- `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/navigation/NavDestination.kt`（`Account.Settings`）
- `shared/src/commonMain/kotlin/com/github/zly2006/zhihu/ui/ZhihuMain.kt`（路由注册 + 详情页判定）

## 5. 验证

- `:shared:compileKotlinJvm` ✅、`:app:assembleLiteDebug` ✅、`ktlintFormat` ✅、`:shared:jvmTest` ✅、`:app:compileLiteDebugAndroidTestKotlin` ✅、`:shared:ktlintCommonMainSourceSetCheck` ✅
- 真机（Redmi K30 Pro 1080×2400 深色）截图：
  - `screenshots/mine_guest_dark.jpg`（未登录）
  - `screenshots/mine_logged_in_dark.jpg`（登录态，种子账号）
  - `screenshots/settings_dark.jpg`（独立设置页）
- 说明：验收登录态时使用了临时 instrumentation 用例种入假账号数据并截图（取完即删除用例），随后 `pm clear` 清掉了种子数据；真实知乎登录态在本次之前已丢失，需重新登录。

## 6. 已知遗留

- 真实账号登录态需要在设备上重新登录（本次会话中途因在真机跑 `connectedAndroidTest` 被卸载重置）。
- `connectedLiteDebugAndroidTest` 在本机会卸载 app（可用 `-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true` 避免），跑真机 instrument 前务必备份 `files/account.json` 或改用 AVD。
