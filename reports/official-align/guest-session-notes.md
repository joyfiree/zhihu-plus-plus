# 未登录游客会话与只读浏览（官方同款 udid_guest）

- 任务：task-3「未登录游客会话与只读浏览」
- 分支：`refactor/md3-ui`
- 范围：账号/网络层（common + android account/data 层 + Android 启动 bootstrap），不改任何 UI 文件。
- 结论：冷启动无激活账号时，App 在后台（不阻塞首屏）调用官方 `init/udid_guest` 链路拿游客凭证，写入独立 `guestSession`；
  未登录只读请求因此可以带游客 `z_c0`/`d_c0` Cookie 与 x-zse-96 签名；退出登录保留游客会话；初始化失败静默降级并可重试。

## 1. 设计

### 1.1 协议复用（不复制请求代码）

`ZhihuPhoneLoginClient` 本来就在手机号登录前实现了一套完整的官方游客预热链路：

1. `POST https://www.zhihu.com/udid` 预热 web 设备 Cookie `d_c0`（签名必需；此 Cookie 不能出现在加密的移动协议请求里，否则 `init/udid_guest` 返回 500）。
2. `POST https://api.zhihu.com/api/account/prod/init/udid_guest`：加密表单（`ZhihuMessageBodyEncryptor`）+ `x-app-id`/`x-sign-version`/`x-req-ts`/`x-req-signature` + 手机号登录同一套 headers，响应为 `{udid, guest:{access_token, token_type, cookie}}`。

本次只做抽取，不新增请求代码：

- 新增 `data class ZhihuGuestSession(cookies, accessToken, tokenType, udid)`（`ZhihuPhoneLoginClient.kt`）。
- 新增公开 `suspend fun ZhihuPhoneLoginClient.initializeGuestSession(): ZhihuGuestSession`：
  调用原 `ensureGuestToken()`，返回 **web `d_c0` + 游客 Cookie（含 `z_c0`）+ 游客 token + udid**。
  返回的 Cookie 是快照，不写回移动协议客户端的 Cookie 仓库（`d_c0` 仍只在 web/业务请求里使用），手机号登录链路行为不变。
- `ensureGuestToken()` 里原本只拼 `authorization` 字符串；现在同时保存 `guestAccessToken` / `guestTokenType` 供返回值使用（`authorization` 仍是同一字符串，登录请求头不变）。

### 1.2 会话存储（谁持有 / 生命周期）

- 权威状态：`ZhihuAccountStore.accountsState: StateFlow<ZhihuAccounts>`（唯一权威，继续沿用）。
- `ZhihuAccounts` 新增字段 `guestSession: ZhihuAccountSession? = null`；
  `session` getter 优先级：**激活账号 → guestSession → 空 `ZhihuAccountSession()`**。
- 游客会话的字段约定：
  - `login = false`（官方游客不是登录用户，UI/写操作门禁继续按未登录处理）
  - `cookies` = `d_c0` + 游客 Cookie（`z_c0`、`q_c0`、`_xsrf`、`BEC` 等服务端下发的原样字段）
  - `mobileAccessToken` / `mobileTokenType` = 游客 token
  - `mobileDeviceId` = `init/udid_guest` 返回的 udid（新增字段，未来移动端请求可作 `x-udid`）
  - `userAgent` 保持默认 web UA（游客 init 的 Android UA 只用于 init 请求本身）
- 持久化：沿用 `account.json`（`ZhihuAccounts` JSON），旧格式 `{activeAccountId, accounts}` 可直接解码（新字段有默认值；旧版本读新文件也会忽略未知字段）。
- `ZhihuAccountRepository` 语义修正：
  - `save(login=true)`：只写账号列表，**保留**已有 guestSession（旧实现会整文件覆盖）。
  - `save(login=false)`：只写 `guestSession`，**不再 `clear()` 整个文件**，不会误删登录账号列表。
  - `saveAccounts`：账号列表和 guestSession 都空时删除文件。
  - `clear()`：显式清空整个文件（保留给需要彻底清数据的调用点）。
- `ZhihuAccountState`：`saveCurrent` 无激活账号时写 guestSession；`saveGuestSession` 供已登录时预存游客会话；
  `remove` / `selectOrAdd` / `removeCurrent` 都改用 `copy`，不再丢 guestSession；`removeCurrent` 在游客态是 no-op（没有可退出的账号）。
- Android 同步：`AccountData.Data` 增加 `mobileDeviceId` 并双向映射（`AccountData.data` 直接读 store，无需第二份镜像）。

### 1.3 初始化入口与状态

`ZhihuAccountStore` 新增：

- `suspend fun ensureGuestSession(): Boolean`
  - 已有可用游客会话（`mobileAccessToken` + `mobileDeviceId` + `d_c0` 齐全）→ 直接 true，不发请求。
  - `Mutex` 单飞去重：并发调用只初始化一次，后到者复用结果。
  - 失败：捕获所有非取消异常，写 `Log.w`，返回 false（**不抛给首屏**）。
  - 未登录：`save(guest)` 复用同一个 account client（游客前后 `login` 都是 false，identity 不变），只就地更新其 Cookie 仓库，**不关闭正在被首屏使用的 HttpClient**。
  - 已登录：`saveGuestSession(guest)` 只存存储，供退出登录后复用，不替换登录中的 client。
- `suspend fun initializeGuestSession()`（private）：创建临时 `ZhihuPhoneLoginClient`，调用协议方法，转成 `ZhihuAccountSession`，`finally` 关闭临时 client。
- `val hasGuestSession: Boolean`：已持久化且凭证齐全。
- `enum class GuestSessionState { IDLE, INITIALIZING, READY, FAILED }` + `val guestSessionState: StateFlow<GuestSessionState>`：
  给 UI 层（Lead 负责接线）判断「未登录首屏是否应等游客凭证就绪」，并在失败时降级。

### 1.4 Android 启动 bootstrap

`app/src/main/java/.../MainActivity.kt`：

- `onCreate`（`AccountData.loadData(this)` 之后）：`lifecycleScope.launch(Dispatchers.IO) { androidZhihuAccountStore(this@MainActivity).ensureGuestSession() }`，不阻塞 `setContent`。
- `onStart`：首次 onStart（与 onCreate 紧邻）不重复请求；之后每次回前台，若 `!hasGuestSession` 再后台重试一次。

### 1.5 数据流（时序）

冷启动未登录：

1. `MainActivity.onCreate` → `AccountData.loadData` → `accountsState` 从 `account.json` 读（可能只有旧 guestSession 或为空）。
2. 启动协程 `ensureGuestSession`：`/udid` 预热 → `init/udid_guest` → 构造 `ZhihuAccountSession(login=false, cookies=d_c0+z_c0, mobileAccessToken, mobileDeviceId)` → `store.save`。
3. `accountsState.session` 变为游客会话；`AccountData.data.cookies` 同步非空（无需额外镜像）。
4. 页面 `rememberPaginationEnvironment(allowGuestAccess=false)` → `httpClient()` = account client（已就地更新游客 Cookie）→ `authenticatedCookies()` 返回游客 Cookie → `signZhihuFetchRequest` 用 `d_c0` 计算 `x-zse-96` → 请求带游客 Cookie + 签名。
5. 首页 `allowGuestAccess=true` 且 `loginForRecommendation=true`（默认）同样走 account client；关掉该设置时仍按既有语义发匿名请求（本次不改该语义）。

登录 / 退出 / 多账号：

- 登录成功后 `selectOrAdd` 只改账号列表与 activeAccountId，guestSession 保留。
- 退出最后一个账号：`removeCurrent → remove → persist(accounts 为空, guestSession 保留)`，`session` 自动回退到游客会话，`replaceClient()` 用游客 Cookie 建新 client，**无需重新请求**。
- 多账号切换：`switchAccount` / `login` / `removeAccount` 均保留 guestSession（单测覆盖）。

## 2. 改动文件

common（shared/src/commonMain）：

- `account/ZhihuPhoneLoginClient.kt`：新增 `ZhihuGuestSession`、`initializeGuestSession()`；`ensureGuestToken()` 保存 token 字段（请求头/表单不变）。
- `account/ZhihuAccountRepository.kt`：`ZhihuAccountSession.mobileDeviceId`；`ZhihuAccounts.guestSession` 与 `session` 回退；`save`/`saveAccounts`/`clear` 语义；`hasGuestCredentials()`。
- `account/ZhihuAccountClient.kt`：`GuestSessionState`、`ensureGuestSession()`、`hasGuestSession`、`guestSessionState`；`ZhihuAccountState` 保留 guestSession；refresh/switch 复制 `mobileDeviceId`。

android（shared/src/androidMain）：

- `data/AccountData.kt`：`Data.mobileDeviceId` 与双向映射。

app：

- `MainActivity.kt`：游客会话 bootstrap（onCreate 后台初始化 + onStart 失败重试）。

测试：

- `shared/src/commonTest/.../account/ZhihuPhoneLoginClientTest.kt`：新增 `initializeGuestSessionReturnsOfficialUdidGuestCredentials`
  （断言 `/udid` → `init/udid_guest` 两次请求、URL/加密表单/签名头/`x-app-id`/`x-sign-version`/`x-req-ts`/`x-req-signature` 与手机号登录一致、不携带 `d_c0`、返回值含 web `d_c0` 与游客 `z_c0`/`q_c0`、重复调用幂等）。
- `shared/src/commonTest/.../account/ZhihuGuestSessionTest.kt`：新增 9 个用例
  - 初始化成功 → 会话字段/cookies 正确 + 落盘可重读（`session.login == false`）
  - 并发调用单飞只初始化一次
  - 已有游客会话时零请求
  - 已登录时只预存游客会话，不替换登录会话/客户端
  - 初始化失败静默、返回 false、不破坏已登录账号、状态 FAILED、只尝试一次
  - repository 同时保存登录账号与游客会话、login=false 保存不再清账号
  - 退出登录回退游客会话且不清除
  - 游客 → 登录 → 退出 全周期保留游客 Cookie（且不重复初始化）
  - 多账号切换不覆盖 guestSession

## 3. 验证结果

### 3.1 单元测试（MockEngine，不联网）

```
./gradlew :shared:jvmTest
```
- 全量：101 个 suite / 437 个测试 / 0 失败 / 0 error。
- 账号相关：`ZhihuGuestSessionTest` 9/9、`ZhihuPhoneLoginClientTest` 7/7、`ZhihuAccountClientTest` 9/9、
  `ZhihuAccountRepositoryTest` 6/6、`QrLoginClientTest` 3/3。

### 3.2 真实协议冒烟（临时 JVM 测试，已删除）

用真实的 `ZhihuPhoneLoginClient.initializeGuestSession()` 对线上 API 跑了一次（临时 `jvmTest`，验证后已删除，不留在仓库）：

- `/udid` → `init/udid_guest` 全部成功，真实返回：
  - cookies：`_xsrf`、`BEC`、`z_c0`（`gt2.0A...`，69 字符）、`d_c0`（47 字符）
  - `token_type=bearer`、access_token 69 字符、`udid` 43 字符
  - 说明加密表单 / 签名头 / 设备信息字段组合被官方接口接受，游客会话可真实创建。
- 后续业务请求（热榜/推荐/搜索，带游客 Cookie + `x-zse-96`，分别测试带/不带 `Authorization`）在本机 WSL 出口 IP 上全部返回：
  `403 {"error":{"need_login":true,"redirect":"https://www.zhihu.com/account/unhuman?...","code":40352,"message":"系统监测到您的网络环境..."}}`
- 对照实验：**同一 IP 上用 curl 匿名请求同一批接口也是 403 unhuman**（甚至无 Cookie 的裸请求），
  因此这是知乎对本机网络环境的风控，不是游客协议失败，也无法在本机判断 `Authorization` 头是否必要。
  → 服务端接受度必须由 Lead 在真机/正常网络验收（见 §5/§6）。

### 3.3 编译与静态检查

- `./gradlew :shared:compileKotlinJvm` 通过。
- `./gradlew :shared:compileTestKotlinJvm` 通过。
- `./gradlew :app:compileLiteDebugKotlin` 通过（MainActivity bootstrap 已随 shared 变更编译进去）。
- ktlint：`:shared:ktlintCommonMainSourceSetCheck`、`:shared:ktlintAndroidMainSourceSetCheck`、
  `:shared:ktlintCommonTestSourceSetCheck`、`:app:ktlintMainSourceSetCheck` 全部通过（未对他人文件跑 format）。

## 4. 失败与重试

- 初始化失败（断网/风控/接口变更）：`ensureGuestSession` 吞异常返回 false，`guestSessionState = FAILED`，首屏不阻塞、不崩溃；
  `accountsState` 保持原样（无账号时仍是空游客会话，请求按旧逻辑失败并走既有错误提示）。
- 重试路径：`MainActivity.onStart` 每次回前台且 `!hasGuestSession` 时后台重试一次（首次 onStart 与 onCreate 去重）。
- 重试策略：失败只尝试一次/轮次，不在协程里循环；并发调用由 Mutex 合并。
- 已有有效游客会话时不会因为重试而重复发请求。

## 5. 未完成项 / 风险 / Lead 接线点

### 5.1 首屏首个请求与初始化的竞态（需要 Lead 在 UI 层决策）

`ensureGuestSession` 是非阻塞后台初始化。若首页在初始化完成前就发起首个只读请求，该请求会以「空 Cookie」构造
（`signZhihuFetchRequest` 拿不到 `d_c0` → 无 `x-zse-96`），大概率失败一次；初始化完成后后续刷新/重进页面即正常。
Ktor 的 `x-zse-96` 在请求构建阶段签名，client 内部等待无法补签，因此这个门闩只能在「发起请求前」等待。

已提供 `accountStore.guestSessionState: StateFlow<GuestSessionState>` 与 `ensureGuestSession()` 供 Lead 在
`HomeScreen`/`ZhihuMain` 里做官方同款门闩（官方 APK 正是「隐私同意后等待 UDID/Token 再发业务请求」）：
未登录首屏可在 `IDLE/INITIALIZING` 显示骨架，`READY` 后再加载；`FAILED` 静默降级为普通未登录。
本任务按约定未改 `HomeScreen`/`ZhihuMain` 等 UI 文件。

### 5.2 游客 `Authorization` 未注入通用业务 client

官方游客请求同时带 `Authorization: bearer <guest token>` + Cookie。本项目现有登录用户业务链路只依赖 Cookie + `x-zse-96`
（登录用户也不发 `Authorization`），为不改变既有登录请求行为、且本机无法验证服务端对游客 `Authorization` 的要求，
本次只把游客 token/udid 持久化到会话（`mobileAccessToken` / `mobileTokenType` / `mobileDeviceId`），**未**给通用
account HTTP client 注入 `Authorization`。
若真机验收发现游客只读接口稳定 401/403（不是 unhuman 风控页），下一步最小改动是在 `createAccountHttpClient` 里仅对
「`login == false` 且 token 非空」的会话加 `Authorization` 头（不涉及登录用户）。

### 5.3 真实设备验收需要真实设备信息

官方 `init/udid_guest` 的设备信息来自真机（`phoneLoginDeviceInfo` 的 androidMain actual）；本机真实冒烟用的是 JVM actual 的
模拟设备信息但接口已接受。真机验收直接用 Android actual 即可，无需额外伪造。
（APK 分析 §D：官方在隐私弹窗后等待 UDID/Token；本实现不接入隐私弹窗——项目现有 Lead 的「跳过登录」偏好即游客入口。）

### 5.4 其他已知边界

- `loginForRecommendation` 语义未改：关闭时首页推荐仍按「不带凭证」发匿名请求（游客也一样，可能失败）。
- 写操作不做游客优化：赞同/关注/收藏/评论/发布/私信/通知设置等继续走既有登录提示路径（Lead 后续接线）。
- 游客凭证过期/被服务端失效时，当前只在「本地凭证缺失」时重新初始化；若真机出现游客态 401，建议在
  `handleFetchFailure`/`executeZhihuAuthenticatedRequest` 的 401 分支增加一次 `ensureGuestSession` 重试（本次未做，避免扩大改动面）。
- `AccountData.Data` 新增 `mobileDeviceId` 字段；`AccountData.data` 直接读 store，不存在第二份镜像。
- 既有行为提醒（非本次引入）：`MainActivity.onCreate` 每 24h 会做一次 web token 刷新，未登录时该请求必然失败并弹
  「刷新登录状态失败」；游客冷启动首日也会触发。该逻辑不在本任务允许改的文件语义范围内，若 Lead 认为打扰可另行加
  `AccountData.data.login` 守卫。
- 游客登录前 `store.session.cookies` 已含游客 Cookie，因此 `QrLoginPane(initialCookies=AccountData.data.cookies)`、
  `UiSupportFiles.hasRequiredCookie` 等读取方会看到游客值；对登录门禁（`login`）无影响，写操作仍要求登录。

## 6. Lead 真机验收步骤（建议）

1. 清应用数据（或删除 `files/account.json`）→ 冷启动，不登录、点「跳过登录」。
2. 观察 logcat：`ZhihuAccountStore` 无游客初始化失败日志；等待 1~3s 后首页推荐/热榜可正常加载。
3. 用 `adb shell run-as <applicationId> cat files/account.json` 检查落盘：
   `guestSession.login == false`，`cookies` 含 `d_c0` 与 `z_c0`，`mobileAccessToken` 和 `mobileDeviceId` 非空。
4. 依次验证：首页推荐、热榜、问题详情、回答页、搜索、用户页（只读）均能加载；写操作弹登录。
5. 登录一个账号 → 退出登录：应直接回到游客可浏览状态，且 `account.json` 中账号列表被清空但 `guestSession` 保留。
6. 多账号：登录 A、B 后切换/删除当前账号，游客会话不丢；删完账号后回到游客浏览。
7. 断网冷启动：首屏不崩溃、不阻塞；恢复网络后切后台再回前台，应自动补初始化（`hasGuestSession` 重试）。
8. 若首屏首个请求报错但下拉刷新/重进页面正常，即 §5.1 竞态，按 5.1 的 `guestSessionState` 门闩接线即可。
9. 若所有游客只读接口稳定 401/403（而非 unhuman 风控页），按 §5.2 加游客 `Authorization` 头后再验。

## 7. Lead 真机验收结果（2026-11-09，Redmi K30 Pro / Android 16）

### 7.1 已通过

1. **真机安装验证**：用 `:app:assembleFullDebug`（需 `ANDROID_NDK_HOME=~/android-sdk/ndk/29.0.14206865`）生成 `com.github.zly2006.zhplus`，
   全新安装（无账号）冷启动：未出现强制登录页，直接进入首页；底栏最后一项显示「未登录」（`HomeScreen` 免强制登录 + `ZhihuMain` labelOverride 已接线）；
   搜索框显示游客占位「搜索知乎内容」；游客初始化期间首页显示骨架（guestSessionState 门闩生效）。
2. **游客初始化请求确实发出并完成**：初始化完成后首页请求能进入业务错误分支（说明请求已带游客凭证与 `x-zse-96` 签名发出，而非空 Cookie 直接失败）。
3. **单测与编译**：本任务 9+1 例全部通过；`:shared:jvmTest` 101 suites / 437 tests 全绿。

### 7.2 端到端被服务端风控阻塞（非代码问题）

游客首页首个 `feed/topstory` 响应原文（临时调试日志捕获，随后已回退调试代码）：

```json
{"error":{"need_login":true,"redirect":"https://www.zhihu.com/account/unhuman?type=S6E3V1&need_login=true&session=...","code":40352,"message":"系统监测到您的网络环境存在异常，为保证您的正常访问，请点击下方验证按钮进行验证。在您验证完成前，该提示将多次出现。"}}
```

对照实验：
- **同一台手机/同一网络下的官方 App**（11.14.0 游客态）同样被要求「安全验证」（WebView `unhuman` 验证页），
  证明是该网络环境被知乎风控，而不是 Zhihu++ 请求协议问题。
- 切换手机到蜂窝网络（LTE）后重试仍返回同一 `40352`；尝试更换默认 User-Agent 为现代 Chrome 也无变化（实验已回退，未保留）。
- 空 Cookie/无签名请求会得到不同的「not json object」错误，本次是「合法 JSON + 40352」 = 服务端风控页，而非签名/凭证缺失。

### 7.3 后续建议

1. 在 `handleFetchFailure`/`fetchJson` 收到 `code=40352 / need_login` 时，打开官方 `unhuman` 验证页（WebView）并让用户完成验证；
   完成后本地游客 Cookie 通常会被服务端刷新，再重试即可。官方 App 也是这样处理的。
2. 换到信誉良好的网络（如新手机热点、公司网络）复测；或在风控解除后复测。
3. 游客 `Authorization` 头注入（§5.2）在本次证据下不是阻塞点（40352 是网络风控，不是鉴权失败），暂不需要。
4. 风控验证通过后，再补拍游客态首页/问题页/搜索结果截图作为端到端证据。
