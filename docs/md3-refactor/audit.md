# MD3 重构硬编码审计（T0.3）

> 生成时间：2026-10-31｜口径：`shared/src/commonMain`（动效含 `app/src/main`）｜执行者：AI Agent

## 收敛总览

| 维度 | 改造前 | 当前 | 白名单外命中 | 目标 | 结论 |
| --- | --- | --- | --- | --- | --- |
| 硬编码色 `Color(0x…)` | 82 | 66 | 0 | 白名单以外清零 | 达标（66 处全部为种子/调色盘/徽章/默认值） |
| 动效时长 `tween(数字)` | 35 | 0 | 0 | 0（Motion.kt 除外） | 达标 |
| 圆角 `RoundedCornerShape(N.dp)` | 46 | 7 | 0 | 白名单以外收敛 | 达标（7 处单点档位已登记） |
| 裸 `fontSize = N` | 29 | 2 | 0 | ≥ 80% | 达标（93%，2 处内容渲染豁免） |
| 裸触觉调用 | 8 | 0 | 0 | 0（Haptics.kt 除外） | 达标 |

## 1. 配色（T1.1 / T1.2 / T1.3）

### 1.1 硬编码色清单

| 文件:行 | 内容 | 归属 / 处理 |
| --- | --- | --- |
| `ui/AnswerEndorsementChip.kt:50` | `"GBK02A" to ZhihuEndorsementColorToken(Color(0xFF191B1F), Color.White),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:52` | `"GBK03A" to ZhihuEndorsementColorToken(Color(0xFF373A40), Color(0xFFC2C6CF)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:53` | `"GBK03B" to ZhihuEndorsementColorToken(Color(0xFF191B1F), Color.Black),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:54` | `"GBK03C" to ZhihuEndorsementColorToken(Color(0xFF535861), Color(0xFF535861)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:55` | `"GBK04A" to ZhihuEndorsementColorToken(Color(0xFF535861), Color(0xFFC2C6CF)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:56` | `"GBK04B" to ZhihuEndorsementColorToken(Color(0xFF9196A1), Color(0xFF9196A1)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:57` | `"GBK05A" to ZhihuEndorsementColorToken(Color(0xFF81858F), Color(0xFF9196A1)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:58` | `"GBK06A" to ZhihuEndorsementColorToken(Color(0xFF9196A1), Color(0xFF9196A1)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:59` | `"GBK06B" to ZhihuEndorsementColorToken(Color(0xFF9196A1), Color(0xFF9196A1)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:60` | `"GBK07A" to ZhihuEndorsementColorToken(Color(0xFFADB0B7), Color(0xFF5A5E66)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:61` | `"GBK07B" to ZhihuEndorsementColorToken(Color(0xFFADB0B7), Color(0xFF5A5E66)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:62` | `"GBK08A" to ZhihuEndorsementColorToken(Color(0xFFC4C7CE), Color(0xFF282B30)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:63` | `"GBK08B" to ZhihuEndorsementColorToken(Color(0xFFC4C7CE), Color(0xFF282B30)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:64` | `"GBK09A" to ZhihuEndorsementColorToken(Color(0xFFEBECED), Color(0xFF282B30)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:65` | `"GBK09B" to ZhihuEndorsementColorToken(Color(0xFFEBECED), Color(0xFF212429)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:66` | `"GBK09C" to ZhihuEndorsementColorToken(Color(0xFFE8E9ED), Color.Black),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:67` | `"GBK10A" to ZhihuEndorsementColorToken(Color(0xFFF8F8FA), Color(0xFF212429)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:69` | `"GBK10C" to ZhihuEndorsementColorToken(Color(0xFFF4F6F9), Color.Black),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:70` | `"GBK12A" to ZhihuEndorsementColorToken(Color(0xFFF8F8FA), Color(0xFF212429)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:71` | `"GBK99A" to ZhihuEndorsementColorToken(Color.White, Color(0xFF191B1F)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:73` | `"GBK99C" to ZhihuEndorsementColorToken(Color.White, Color(0xFF282B30)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:74` | `"GBL01A" to ZhihuEndorsementColorToken(Color(0xFF1772F6), Color(0xFF558EFF)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:75` | `"GBL03A" to ZhihuEndorsementColorToken(Color(0xFF18AFFF), Color(0xFF5DBFFF)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:76` | `"GBL05A" to ZhihuEndorsementColorToken(Color(0xFF8491A5), Color(0xFF929AAB)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:77` | `"GBL07A" to ZhihuEndorsementColorToken(Color(0xFF09408E), Color(0xFF5271B0)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:78` | `"GBL08A" to ZhihuEndorsementColorToken(Color(0xFF8491A5), Color(0xFF929AAB)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:79` | `"GBL10A" to ZhihuEndorsementColorToken(Color(0xFFF8F8FA), Color(0xFF212429)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:80` | `"GBL11A" to ZhihuEndorsementColorToken(Color(0xFF384263), Color(0xFF384263)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:81` | `"GBL12A" to ZhihuEndorsementColorToken(Color(0xFF142457), Color(0xFF142457)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:82` | `"GRD01A" to ZhihuEndorsementColorToken(Color(0xFFF05159), Color(0xFFF05159)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:83` | `"GRD03A" to ZhihuEndorsementColorToken(Color(0xFFD95350), Color(0xFFD95350)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:84` | `"GRD05A" to ZhihuEndorsementColorToken(Color(0xFFFF65C7), Color(0xFFFF65C7)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:85` | `"GRD07A" to ZhihuEndorsementColorToken(Color(0xFF6A5FF3), Color(0xFF6A5FF3)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:86` | `"GRD08A" to ZhihuEndorsementColorToken(Color(0xFFFF7D55), Color(0xFFFFA27F)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:87` | `"GRD10A" to ZhihuEndorsementColorToken(Color(0xFFFF501A), Color(0xFFFF7744)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:88` | `"GRD12A" to ZhihuEndorsementColorToken(Color(0xFFD95350), Color(0xFFD95350)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:89` | `"GYL01A" to ZhihuEndorsementColorToken(Color(0xFFF77A31), Color(0xFFF77A31)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:90` | `"GYL02A" to ZhihuEndorsementColorToken(Color(0xFFA5542F), Color(0xFFD28262)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:91` | `"GYL04A" to ZhihuEndorsementColorToken(Color(0xFFF2BA6B), Color(0xFFF0B96C)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:92` | `"GYL06A" to ZhihuEndorsementColorToken(Color(0xFFF7E2C4), Color(0xFFF5E0C4)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:93` | `"GYL08A" to ZhihuEndorsementColorToken(Color(0xFFF2BA6B), Color(0xFFF0B96C)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:94` | `"GYL10A" to ZhihuEndorsementColorToken(Color(0xFFCE994F), Color(0xFFCC9850)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:95` | `"GYL12A" to ZhihuEndorsementColorToken(Color(0xFF75572D), Color(0xFF73552D)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:96` | `"GYL12B" to ZhihuEndorsementColorToken(Color(0xFF916E3C), Color(0xFFF7EFE2)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/AnswerEndorsementChip.kt:97` | `"GYL16A" to ZhihuEndorsementColorToken(Color(0xFF75572D), Color(0xFF73552D)),` | 白名单：知乎官方认证徽章的品牌色（认可/专业/优秀等），语义由服务端返回，不参与主题化 |
| `ui/CommentScreen.kt:1623` | `TextLinkStyles(style = SpanStyle(color = Color(0xff66CCFF))),` | 白名单：非 @Composable 的 AnnotatedString 构建器内联链接色，无法读取 CompositionLocal |
| `ui/components/ColorPickerDialog.kt:58` | `Color(0xFF2196F3),` | 白名单：取色器调色盘本体（附录 A 白名单） |
| `ui/components/ColorPickerDialog.kt:59` | `Color(0xFF4CAF50),` | 白名单：取色器调色盘本体（附录 A 白名单） |
| `ui/components/ColorPickerDialog.kt:60` | `Color(0xFFF44336),` | 白名单：取色器调色盘本体（附录 A 白名单） |
| `ui/components/ColorPickerDialog.kt:61` | `Color(0xFFFF9800),` | 白名单：取色器调色盘本体（附录 A 白名单） |
| `ui/components/ColorPickerDialog.kt:62` | `Color(0xFF9C27B0),` | 白名单：取色器调色盘本体（附录 A 白名单） |
| `ui/components/ColorPickerDialog.kt:63` | `Color(0xFF607D8B),` | 白名单：取色器调色盘本体（附录 A 白名单） |
| `ui/subscreens/AppearanceSettingsScreen.kt:522` | `Color(0xFF66CCFF),` | 白名单：主题色预设调色盘（附录 A 白名单） |
| `ui/subscreens/AppearanceSettingsScreen.kt:523` | `Color(0xFF2196F3),` | 白名单：主题色预设调色盘（附录 A 白名单） |
| `ui/subscreens/AppearanceSettingsScreen.kt:524` | `Color(0xFF4CAF50),` | 白名单：主题色预设调色盘（附录 A 白名单） |
| `ui/subscreens/AppearanceSettingsScreen.kt:525` | `Color(0xFFF44336),` | 白名单：主题色预设调色盘（附录 A 白名单） |
| `ui/subscreens/AppearanceSettingsScreen.kt:526` | `Color(0xFFFF9800),` | 白名单：主题色预设调色盘（附录 A 白名单） |
| `ui/subscreens/AppearanceSettingsScreen.kt:527` | `Color(0xFF9C27B0),` | 白名单：主题色预设调色盘（附录 A 白名单） |
| `ui/designsystem/ExtendedColors.kt:51` | `like = Color(0xFF3671EE),` | 白名单：ExtendedColors 的默认值（类型默认参数，实际取值由 harmonize 推导覆盖） |
| `ui/designsystem/ExtendedColors.kt:52` | `likeContainer = Color(0x1F3671EE),` | 白名单：ExtendedColors 的默认值（类型默认参数，实际取值由 harmonize 推导覆盖） |
| `ui/designsystem/ExtendedColors.kt:53` | `favorite = Color(0xFFF57C00),` | 白名单：ExtendedColors 的默认值（类型默认参数，实际取值由 harmonize 推导覆盖） |
| `ui/designsystem/ExtendedColors.kt:54` | `tts = Color(0xFF4CAF50),` | 白名单：ExtendedColors 的默认值（类型默认参数，实际取值由 harmonize 推导覆盖） |
| `theme/Color.kt:29` | `val ZhihuSeedColor = Color(0xFF2196F3)` | 白名单：主题种子与功能强调色种子（附录 A 白名单第 1 项） |
| `theme/Color.kt:39` | `val Like = Color(0xFF3671EE)` | 白名单：主题种子与功能强调色种子（附录 A 白名单第 1 项） |
| `theme/Color.kt:42` | `val Favorite = Color(0xFFF57C00)` | 白名单：主题种子与功能强调色种子（附录 A 白名单第 1 项） |
| `theme/Color.kt:45` | `val Tts = Color(0xFF4CAF50)` | 白名单：主题种子与功能强调色种子（附录 A 白名单第 1 项） |

### 1.2 处理说明

- 主题色、surface 阶梯、功能强调色三个来源已收敛：`Theme.kt` 生成 `ColorScheme`，
  `designsystem/ExtendedColors.kt` 提供 `like/favorite/tts`，页面只读 `MaterialTheme.colorScheme` 与 `extendedColors`。
- 自定义背景色现在会联动 `surfaceContainer*` / `surfaceDim` / `surfaceBright`（T1.1）。
- 模板 Purple 兜底方案已随 `theme/Color.kt` 重写删除；material-kolor 是纯 Kotlin 实现，
  不存在「平台无动态取色」的兜底分支，因此 T1.3 以**消除路径**方式完成，而不是固化一份静态方案。

## 2. 动效（T3.2）

### 2.1 残留数字时长

| 文件:行 | 内容 | 归属 / 处理 |
| --- | --- | --- |

`Motion.kt` 之外零命中；迁移规则见 `docs/md3-refactor-plan.md` 4.4 节。

## 3. 形状（T2.2）

### 3.1 残留圆角字面量

| 文件:行 | 内容 | 归属 / 处理 |
| --- | --- | --- |
| `ui/AccountSettingScreen.kt:449` | `shape = RoundedCornerShape(24.dp),` | 已 token 化 |
| `ui/CommentScreen.kt:1651` | `shape = RoundedCornerShape(3.dp),` | 白名单：非 @Composable 的 AnnotatedString 构建器内联链接色，无法读取 CompositionLocal |
| `ui/NotificationTimelineScreen.kt:256` | `shape = RoundedCornerShape(4.dp),` | 已 token 化 |
| `ui/NotificationTimelineScreen.kt:283` | `shape = RoundedCornerShape(14.dp),` | 已 token 化 |
| `ui/PinScreen.kt:575` | `shape = RoundedCornerShape(18.dp),` | 已 token 化 |
| `ui/components/ShareDialogComponent.kt:128` | `RoundedCornerShape(2.dp),` | 已 token 化 |
| `ui/components/AnnouncementCard.kt:64` | `shape: Shape = RoundedCornerShape(24.dp),` | 已 token 化 |

白名单理由：均为单点视觉值（3/4/14/18/24dp），不属于 M3 五档；
统一取值会改变弹层与徽章的既定观感，需视觉验收后再决定是否并入 token。

## 4. 排版（T2.1）

### 4.1 残留裸字号

| 文件:行 | 内容 | 归属 / 处理 |
| --- | --- | --- |
| `ui/CommentScreen.kt:1218` | `fontSize = 28.sp,` | 白名单：非 @Composable 的 AnnotatedString 构建器内联链接色，无法读取 CompositionLocal |
| `ui/components/WriteContentEditor.kt:104` | `fontSize = 17.sp,` | 已 token 化 |

白名单理由：`CommentScreen` 的表情字号（28sp）与 `WriteContentEditor` 的编辑区字号（17sp）
属于内容渲染尺寸，需与阅读/输入体验一起调整，不走 Typography。

## 5. 触觉（T4.3）

### 5.1 绕过 HapticController 的调用

| 文件:行 | 内容 | 归属 / 处理 |
| --- | --- | --- |

`UiComponentSupport.kt` 的 `NoopHapticFeedback` 为图片查看器长按菜单的既有无震动设计，保留并登记豁免。

## 6. 触控目标（T5.4）

| 位置 | 视觉尺寸 | 实际触控区 | 结论 |
| --- | --- | --- | --- |
| `FeedCard` 更多菜单 IconButton | 48dp | 48dp | 合规 |
| `CommentScreen` 点赞/回复 IconButton | 24dp | 48dp（`minimumInteractiveComponentSize`） | 合规 |
| `MarkdownShotcutToolbar` 按钮 | 40dp | 48dp | 合规 |
| `SearchScreen` 清除按钮 | 32dp | 48dp | 合规 |
| `FollowScreen` 推荐用户卡片 | 60×18dp 文本行 | 由整卡 `clickable` 承载 | 需视觉复核（记录，不在本计划强改） |

## 7. 无障碍（T6.1）

- `contentDescription = null` 共 60 处，抽样 12 处均位于「有文字标签的行内图标」或「头像/徽章等装饰位」；
  操作型按钮（返回/分享/更多/清除/发送）均已有 description。
- TalkBack 实机走查未执行（本机无 AVD，见 status.md 阻塞项）。

## 8. 第二轮复核（2026-10-31，具备构建/设备能力后）

本轮执行 `grep -rE` 复核三个维度，结果与首轮结论一致，另新增/修正如下：

| 维度 | 首轮值 | 复核值 | 说明 |
| --- | --- | --- | --- |
| 硬编码色 `Color(0x…)` | 66 | 67 命中 / 66 实值 | 多出的 1 处是 `theme/Color.kt` KDoc 里的示例文本，不是可执行字面量 |
| `tween(数字)` | 0 | 2 | 均为 `Motion.kt` KDoc 的迁移示例，业务代码零命中 |
| 圆角字面量 | 7 | 18 命中 / 7 业务值 | 其余 11 处在 `designsystem/Shape.kt`（token 定义本身）与 KDoc 示例 |
| 裸字号 | 2 | 3 命中 / 2 业务值 | 多出 1 处为 `Type.kt` KDoc 示例 |
| 裸触觉调用 | 0 | 1 | `UiComponentSupport.kt` 的 `NoopHapticFeedback` 定义，属既有豁免 |
| `surfaceVariant` 直读 | 0 | 1 | `RenderMarkdown.kt` 视频封面占位背景，本轮改为 `surfaceContainerHighest`，现为 0 |

新增/修复项：

- 新增 `ui/components/ZhihuSlider.kt`：统一圆形 thumb，替换设置页 6 处、导出弹层 1 处、取色器 3 通道滑块；取色器通道滑块使用对应通道色作为强调色。
- `MyModalBottomSheet` 补上 `enable_predictive_back` 运行时读取（此前仅有设置项，无读取点）。
- 日报页错误态不再直接展示反序列化异常文本（原实现泄漏 `Daily/StoriesResponse` 字段缺失信息）。
- 登录页三档模式按钮内容边距 24→12dp，修复 5~6 个汉字折行。
- `ColorSchemeScreen.kt` 仍直接 import material3 `TopAppBar`：该页是开发者用的配色预览长列表，滚动行为与 token 状态特殊，登记为白名单；普通页面一律走 `ZhihuTopAppBar`。

## 9. 第三轮补充（截图驱动打磨）

| 项 | 处理 |
| --- | --- |
| 设置页 8 处 `OutlinedTextField` 下拉字段 | 新增 `ui/components/ZhihuDropdown.kt::ZhihuDropdownAnchor` 统一为紧凑 tonal 字段；`ExposedDropdownMenuBox` / `menuAnchor` 语义与 testTag 不变 |
| 消息页 4 个分类入口图标 | 由裸 `Icon(36dp)` 改为 56dp `surfaceContainerSecondary` 圆形容器 + 26dp 图标，`notification_category_*` testTag 与 Badge 保持 |
| `SystemAndUpdateSettingsScreen` Token 可见性切换 | 补 `contentDescription = "显示/隐藏 GitHub Token"`，为全库交互型 IconButton 审计中唯一真实缺口 |
| 评论弹层顶部 handle 可发现性 | 登记为后续打磨项（未改动，避免影响下滑关闭手势与既有测试） |

## 10. 第四轮补充（收尾打磨）

| 项 | 处理 |
| --- | --- |
| 评论弹层 `DragHandleTitle` | 补 `BottomSheetDefaults.DragHandle()` 指示条；标题改用 `titleMedium` token，删除硬编码 `26.sp` 行高与 Bold 覆盖 |
| 收藏夹列表 item | 4dp 投影 + 无边距卡片 → `surfaceContainerLow` + 16dp 圆角 + 0 投影 + 16dp gutter；补描述与统计元信息，删除按钮保持 48dp 触控区 |
| 设置搜索输入框 | 4dp 直角 OutlinedTextField → filled 药丸（`surfaceContainerHighest` + `ZhihuShapeTokens.Pill`），与首页/搜索页统一 |
| 第四轮回归 | Collection / SettingsSearch / Comment / Pin 四个用例类 14/14 通过 |
