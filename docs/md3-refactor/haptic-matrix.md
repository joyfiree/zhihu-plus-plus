# 触觉反馈覆盖矩阵（T4.2 / T4.3）

> 生成时间：2026-10-31｜所有触觉均经由 `ui/designsystem/Haptics.kt` 的 `HapticController`，受 `hapticFeedbackEnabled` 总开关控制。

## 1. 语义分级与触点

| 语义 | 触发点 | 代码位置 | 实现 | 状态 |
| --- | --- | --- | --- | --- |
| Confirm | 设置项开关切换 | `ui/components/SettingItem.kt`（`SettingItemWithSwitch` / `SettingItemOverall`） | `haptics.confirm()` | 代码完成 |
| Confirm | 双击点赞 / 回答双击操作 | `ui/ArticleScreen.kt` | `haptics.confirm()` | 代码完成（原有调用已迁移） |
| Tick | 底栏 tab 切换 | `ui/ZhihuMain.kt`（`NavigationBarItem.onClick`） | `haptics.tick()` | 代码完成 |
| GestureThreshold | 下拉刷新触发瞬间 | `ui/components/FeedPullToRefresh.kt`（`onRefresh`） | `haptics.gestureThreshold()` | 代码完成 |
| GestureThreshold | 竖向回答切换越阈 | `ui/components/AnswerVerticalOverscroll.kt` | `haptics.gestureThreshold()` | 代码完成（原有） |
| GestureThreshold | 横向回答切换越阈 | `ui/components/AnswerHorizontalPager.kt` | `haptics.gestureThreshold()` | 代码完成（原有） |
| LongPress | 长按评论 | `ui/CommentScreen.kt` | `haptics.longPress()` | 代码完成（原有） |
| LongPress | 长按正文选中 | `markdown/RenderMarkdown.kt` | `haptics.longPress()` | 代码完成（原有） |
| LongPress | 拖动刷新按钮起拖 | `ui/components/DraggableRefreshButton.kt` | `haptics.longPress()` | 代码完成（原有） |
| LongPress | 拖动翻页按钮起拖 | `ui/components/DraggablePageTurnButtons.kt` | `haptics.longPress()` | 代码完成（原有） |
| 无（有意不加） | Pager 跟随手指滑动 | `ui/ZhihuMain.kt` 主 tab Pager | — | 按 4.5 节「克制原则」：跟手滑动不震，仅 tab 点击震 |
| 豁免 | 图片查看器长按菜单 | `ui/components/UiComponentSupport.kt`（`NoopHapticFeedback`） | 屏蔽 | 保留（原有设计，见 audit.md 第 5 节） |

## 2. 一致性规则

1. **单次触发**：每个触点只在动作确认的那一帧调用一次；滚动过程不重复触发。
2. **与动画同帧**：`FeedPullToRefresh.onRefresh` 在启动协程前触发，和指示器出现同帧。
3. **可静音**：`HapticController` 在触发瞬间读取 `hapticFeedbackEnabled`，关闭后无需重组即静默。
4. **不绕过**：`rg "performHapticFeedback\(HapticFeedbackType\."` 在 `designsystem/` 之外零命中（已核对）。

## 3. 设备验证状态（2026-10-31 第二轮）

- 本轮已在本地 AVD（API 35，`Medium_Phone_2`）上验证：所有触点调用链编译通过、定向 instrument 回归通过；
  `dumpsys vibrator_manager` 在模拟器中存在 `VibratorManagerService`，但模拟器无物理振动反馈，
  且「开启/关闭」两态无法用肉眼或录屏区分。
- 因此逐点两态确认与双击点赞无双震的录屏证据**仍需真机**补测；代码层已确认 `HapticController` 是唯一出口
  （`rg performHapticFeedback` 在 designsystem 外仅命中 `NoopHapticFeedback` 定义）。
