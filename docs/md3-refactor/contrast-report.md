# 主题对比度校验报告（T1.4）

> 执行时间：2026-10-31（第二轮，具备构建能力后）｜执行方式：`:shared:jvmTest --tests "*ThemeContrastTest*"`｜执行者：AI Agent

## 1. 校验范围

`shared/src/jvmTest/.../theme/ThemeContrastTest.kt` 覆盖「用户自定义背景色」这条最容易出问题的路径：

- 浅色背景：纯白、知乎蓝 `#2196F3`、米黄 `#F5F1E8`、浅灰 `#EEEEEE`
- 深色背景：纯黑、深灰 `#121212`、深蓝 `#102A43`
- 断言：`onSurface` / `onBackground` 对背景 ≥ 4.5:1（WCAG AA 正文），`onSurfaceVariant` ≥ 3:1（辅助文本），且 5 档 surface 阶梯明度单调。

## 2. 首轮结果（修复前，红）

```
ThemeContrastTest > surfaceLadderStaysOrdered FAILED
  背景/知乎蓝 的 surface 阶梯明度不单调：[0.09814485, 0.116963685, 0.10854953, 0.09621388, 0.081893586]
ThemeContrastTest > lightBackgroundsKeepBodyTextReadable FAILED
  浅色/知乎蓝 onSurfaceVariant 对比度 2.9906933，低于 3.0
```

根因两条：

1. **阶梯非单调**：原实现按「参考配色的 HSL 明度差」平移，但每个 surface 角色又混入了自身色相/饱和度；对知乎蓝这种高饱和背景，不同角色的 HSL 明度排序不能保证最终亮度排序。
2. **辅助文本对比度不足**：原实现直接沿用参考配色的 `onSurfaceVariant`；当自定义背景是「中间明度、高饱和」的品牌蓝时，这个前景色只有 2.99:1。

## 3. 修复内容

`theme/ZhihuColors.kt::buildZhihuColorScheme`：

- **背景极性判定改为 WCAG 判据**：比较黑/白前景在该背景上的对比度，谁更高就选哪套参考配色，避免用 HSL 明度误判「品牌蓝」这类中间明度色。
- **阶梯只变明度**：surface 阶梯保留用户所选背景的色相/饱和度，仅按参考配色的相对明度差平移；同一色相/饱和下明度单调 ⇒ 亮度单调。
- **前景色 WCAG 兜底**：`onSurface` / `onBackground` 达不到 4.5:1 时替换为对比度更高的黑/白；`onSurfaceVariant` 用 `onSurface` 72% 透明叠加到背景上推导，达不到 3:1 时退回 `onSurface`。
- 新增 `designsystem/ColorMath.kt::contrastRatio()`（WCAG 2.1 公式，供主题与测试共用）。

## 4. 修复后结果（绿）

```
./gradlew :shared:jvmTest --tests "com.github.zly2006.zhihu.theme.ThemeContrastTest"
BUILD SUCCESSFUL
```

`ThemeContrastTest` 3/3 通过；同一轮 `:shared:jvmTest` 全量 422 个用例（除主题专项外的既有用例）在首轮即全部通过，修复只影响自定义背景色路径。

## 5. 已知边界

- 校验口径是 `lightColorScheme()/darkColorScheme()`（M3 默认参考配色）。实际运行时的 material-kolor 动态配色与之明度结构一致，测试用于锁定「推导算法」而非某个具体色值。
- 极端背景（纯黑/纯白）会走「默认背景」快速路径，不重新推导阶梯，因此天然保持 M3 参考配色的对比度。