# 仿官方风格 UI 重构（2026-11）

> 背景：`refactor/md3-ui` 分支把 UI 全面迁移到了 Material Design 3 视觉体系（见 `docs/md3-refactor/`）。
> 2026-11 起方向调整为**仿知乎官方 App**：官方的配色、图标、动效和交互比 MD3 更适合这个客户端。
> 本次重构复用了 MD3 阶段沉淀的 token 基建（`designsystem/`、统一顶栏、触觉、Motion 令牌），只替换了视觉语言本身。

## 官方风格的四根支柱

### 1. 配色（`theme/OfficialZhihuScheme.kt`）

官方观感来自一组**固定值**，不是种子推导。默认种子色（`ZhihuSeedColor` = `#056DE8`）时直接使用手调方案：

| 角色 | 浅色 | 深色 | 说明 |
| --- | --- | --- | --- |
| primary | `#056DE8` | `#4E87EE` | 官方品牌蓝；深色提亮一档保证文字对比度 |
| 页面底 | `#F6F6F6` | `#121212` | 「灰底白卡 / 暗底亮卡」的底层 |
| 卡片 | `#FFFFFF` | `#1E1E1E` | |
| 正文 | `#1A1A1A` | `#EDEDED` | |
| 次要文本 | `#76808F` | `#9AA3B2` | 官方灰蓝调，为保住灰底 3:1 对比度略深于官方 |
| 发丝线 | `#EBEBEB` | `#33363B` | outlineVariant |

- 用户自定义种子色 / Material You 动态取色的路径**不变**（仍走 material-kolor 推导）；只有默认种子命中官方手调。
- 「自定义背景色」仍经过 `buildZhihuColorScheme` 平移到整套 surface 阶梯。
- 对比度契约由 `ThemeContrastTest.officialZhihuSchemeKeepsContrastAndLadder` 锁住（正文 ≥4.5:1、辅助文本在卡片与页面 ≥3:1、surface 阶梯明度单调）。

### 2. 形状（`designsystem/Shape.kt`）

官方圆角克制且偏小，MD3 Expressive 的 24-28dp 大圆角被收敛：

| 令牌 | 新值 | 旧值 |
| --- | --- | --- |
| 卡片 / FeedCard / Section | 12dp | 24/18/28dp |
| BottomSheet 顶部 | 16dp | 28dp |
| Dialog | 16dp | 28dp |
| Thumbnail | 6dp | 16dp |
| Badge | 药丸 | 10dp 圆角矩形 |
| `Shapes.small`（按钮/chip） | 药丸 | 8dp |

### 3. 图标（`ZhihuMain.kt` 底栏 + 既有资源）

- 底栏改为官方「未选中描边灰 / 选中实心蓝」双态图标（`Icons.Outlined.*` ↔ `Icons.Filled.*`）。
- 移除 M3 的胶囊指示器与 1.18x 图标缩放动画；官方是即时换色。
- 赞同/反对的圆角三角箭头（`ic_vote_up/down_24dp.xml`）与官方一致，保留。

### 4. 交互与排版

- 底栏：整宽平铺白底 + 顶部发丝线，替代 M3 浮动胶囊；testTag、触觉、双击回顶、自动隐藏逻辑全部保留。
- 顶栏：白色卡片层级（`zhihuTopAppBarColors` 默认 `zhihuSurfaces.card`），滚动前后同色，靠灰底页面分层——官方样式。设置页从 M3 大标题栏收敛为普通 `ZhihuTopAppBar`。
- 信息流卡片默认启用官方排版（`duo3_card_layout` 默认 true：作者行置底、摘要 4 行），标题默认 16sp（`duo3_card_large_title` 默认 false）。
- 加载骨架（`ZhihuSkeletonCard`）重写为与官方卡片同构：两行标题条 / 三行摘要条 + 右侧 112×84 缩略图占位 / 底部头像+名字+计数行。旧版是 M3 Expressive 时代的「16:9 通栏封面 + 居中条 + 隔卡交替」，与真实卡片从未对齐，加载完成会跳版；`CardExpressive` 令牌随之删除。
- 排版：顶栏标题 18sp Medium、按钮文字 15sp。
- 动效：二元状态回弹从 1.28 收敛到 1.12（官方克制风）；Motion 其余令牌与共享轴转场不变。

## 保留的用户能力

动态取色、自定义种子色、自定义背景色、深色模式、`feedCardStyle` 分割线样式、Duo3 各开关（可整体关闭回到紧凑排版）均未删除；本次只改**默认观感**与公共 token。

## 验证

- `ThemeContrastTest`（含新增 `officialZhihuSchemeKeepsContrastAndLadder`）/ `DesignSystemTokensTest` / 全量 `:shared:jvmTest` 通过。
- `assembleLiteDebug` + `ktlintFormat` 通过。
- 本地 AVD（API 29 arm64）实测截图与像素校验（`docs/zhihu-official-style/screenshots/`）：
  - `home_light.png`：顶栏/卡片 `#FFFFFF`、页面 `#F6F6F6`、底栏选中 tab 命中官方蓝 `#056DE8`、次要文本命中 `#76808F`、正文命中 `#1A1A1A`；
  - `home_dark.png`：页面 `#121212`、卡片与顶/底栏 `#1E1E1E`，无浅色残留；
  - `home_skeleton_light.png` / `home_skeleton_dark.png`：断网下骨架常驻时逐行扫描验证结构（标题条 → 摘要+右侧 112×84 占位 → 底部作者行），扫光条色 `#E9EAEC`/`#313136`；
  - `answer_light.png`：回答页操作栏（赞同/反对/收藏/评论/更多）；
  - `account_dark.png` / `appearance_settings_dark.png`：账号页与设置页新顶栏。
- 限制：本机无账号备份（无 `~/.zhihu-plus-plus/backups` 与 `.secret/account.json`），未登录状态下回答正文不可加载，回答页截图只覆盖框架；登录态页面的复检留给有备份的环境或 CI。tab 切换、底栏 testTag、免登录首页均已实测。
