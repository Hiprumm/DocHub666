# DocHub —— 页面风格样式设计文档（Design System 规范）

> 文档版本：v1.0
> 适用范围：DocHub 全部前端页面（Vue 3 技术栈）
> 定位：唯一视觉标准依据。本文档中所有规范均可量化、可判定，前段工程师无需二次猜测即可还原视觉。
> 关联文档：docs/01-requirements/PRD.md、GLOSSARY.md

---

## 目录

1. 设计哲学与整体风格定位
2. 设计 Token 体系
3. 基础组件视觉规范
4. 页面布局规范
5. 可访问性（A11y）标准
6. 落地与验收

---

## 1. 设计哲学与整体风格定位

### 1.1 视觉关键词 / 情绪板

DocHub 的品牌视觉关键词如下（优先序降序）：

| 关键词 | 情绪板描述 |
| --- | --- |
| **深海科技感** | 深空蓝黑背景 + 青色渐变光晕，如深海/夜空下的科技界面 |
| **专业** | 克制、规整、清晰层级，数据与操作一目了然 |
| **沉浸** | 深色低反射背景，减少视觉干扰，长文预览与检索更专注 |
| **高效** | 高对比度信息层级、果断的交互反馈、可扫读的密度控制 |

### 1.2 风格定位与产品调性的匹配逻辑

- **深色主导（Dark-first）**：与“文档管理 + AI 智能”的科技产品属性匹配，营造专业与克制的氛围；
- **青色 accent + 深蓝底**：青色（Cyan）象征“智能、数据、AI”，在深蓝底上产生清晰的视觉重心，指向“智能层”的关键动作（检索 / 合成 / 确认入库）；
- **低饱和背景、高饱和强调**：背景采用更深、更灰的蓝，让唯一的高亮色（青色系）聚焦于最重要的可操作元素，避免繁杂信息喧宾夺主；
- **密度与留白平衡**：文档列表允许较高密度（信息高效），页面级留白保证可读与从容，兼顾“管理效率”与“沉浸阅读”。

---

## 2. 设计 Token 体系

### 2.1 颜色系统（Color System）

**品牌色板（原始 11 阶，由浅至深）**

| 阶 | 色值 | 命名建议（--color-brand-XX） |
| --- | --- | --- |
| 50 | `#D1FFFF` | 最浅青白，主文本/高亮链接 |
| 100 | `#AAD9F2` | 浅青，accent / focus 边框 |
| 200 | `#85B3CB` | 青蓝，次级文本 / 边框强 |
| 300 | `#618EA5` | 中蓝，占位 / 图标 |
| 400 | `#3F6B81` | 蓝灰，禁用文本 |
| 500 | `#2E5A6F` | 深蓝灰，默认边框 |
| 600 | `#134155` | 深蓝，表面浮层(hover) |
| 700 | `#002A3D` | 更深蓝，卡片表面 |
| 800 | `#001325` | 深蓝黑，容器/侧栏 |
| 900 | `#000515` | 品牌最深，页面底 |
| 950 | `#000008` | 绝对黑，遮罩/描边 |

**语义化 Token 定义（五类用途全覆盖）**

> 以下 Token 为源码中唯一被允许使用的颜色，禁止任何硬编码色值。

| 类别 | Token 名 | 色值 / 计算 | 用途 |
| --- | --- | --- | --- |
| **背景层 Background** | `--color-bg-base` | `#000515` | 页面最底层背景 |
| | `--color-bg-canvas` | `#000515` | 内容区画布（与 base 一致） |
| | `--color-bg-sunken` | `#000008` | 最深层（如代码块、深注区域） |
| **容器层 Surface** | `--color-surface-card` | `#001325` | 卡片 / 表单元背景 |
| | `--color-surface-raised` | `#002A3D` | 模态框 / 浮层 / 下拉 |
| | `--color-surface-hover` | `#134155` | 卡片 hover 时背景 |
| | `--color-surface-overlay` | `rgba(0,0,8,.62)` | 遮罩黑（基于 950） |
| **文本层级 Text** | `--color-text-primary` | `#D1FFFF` | 主文本（最高对比） |
| | `--color-text-secondary` | `#AAD9F2` | 次级文本 / 强调 |
| | `--color-text-tertiary` | `#85B3CB` | 三级文本 / 辅助说明 |
| | `--color-text-placeholder` | `#618EA5` | 输入框 placeholder |
| | `--color-text-disabled` | `#3F6B81` | 禁用文本 |
| | `--color-text-on-accent` | `#001325` | 在青色底上的文字（深色） |
| **边框 Border** | `--color-border-default` | `#2E5A6F` | 常规边框 / 分隔线 |
| | `--color-border-strong` | `#618EA5` | 强调边框 / 分割 |
| | `--color-border-input` | `#3F6B81` | 输入框默认边框 |
| **交互态 States** | `--color-accent` | `#AAD9F2` | 主色（主按钮 / 选中 / 链接） |
| | `--color-accent-hover` | `#D1FFFF` | 主色 hover |
| | `--color-accent-active` | `#85B3CB` | 主色 active/按下 |
| | `--color-accent-soft` | `rgba(170,217,242,.14)` | 主色柔和底（选中等） |
| | `--color-focus-ring` | `#AAD9F2` | 焦点光晕（focus-visible） |
| | `--color-border-disabled` | `#2E5A6F` | 禁用边框 |
| | `--color-loading` | `#85B3CB` | 加载指示色 |

### 2.2 语义色规范（Success / Warning / Error / Info）

> 语义色为**补充色系**，独立于青色品牌主色，仅在传达状态时使用，且**必须配合图标或文字**（见 §5.3 色觉友好）。

| 语义 | Token 名 | 深色主题色值 | 浅底文字 | 对比度原则 |
| --- | --- | --- | --- | --- |
| Success 成功 | `--color-success` | `#3DDC84` | 正文用 `#D1FFFF` | 前景(状态图标/文字) ≥ 3:1 |
| Warning 警告 | `--color-warning` | `#FFB86B` | 正文用 `#D1FFFF` | 前景 ≥ 3:1，正文用亮色避黄暗 |
| Error 错误 | `--color-error` | `#FF6B6B` | 正文用 `#D1FFFF` | 前景 ≥ 3:1，正文避免红暗底 |
| Info 信息 | `--color-info` | `#60A5FA` | 正文用 `#D1FFFF` | 前景 ≥ 3:1 |

**深色主题选取原则**
- 一律选用**中度亮度、高饱和**的强调色（非纯高饱和），保证在深蓝底上既不刺眼又有足够对比；
- 状态文字不直接用语义纯色（如纯红/纯黄）作正文，而是语义色**在 text-primary 上书写**，语义色只作图标/描边/浅底；
- 每种语义色均需配 `--color-xxx-soft`（12%～16% 透明底）用于 Toast / Badge 底色。

### 2.3 字体系统（Typography）

| 层级 | 字号 | 字重 | 行高 | 字间距 | 用途 |
| --- | --- | --- | --- | --- | --- |
| `--text-display` | 36px | 700 | 1.2 | -0.5px | 首页/大标题 |
| `--text-title` | 28px | 700 | 1.25 | -0.4px | 页面主标题 H1 |
| `--text-h1` | 24px | 700 | 1.3 | -0.3px | 章节标题 |
| `--text-h2` | 20px | 600 | 1.35 | -0.2px | 区块标题 |
| `--text-body-lg` | 17px | 400 | 1.6 | 0 | 正文（大） |
| `--text-body` | 15px | 400 | 1.6 | 0 | 正文（默认） |
| `--text-caption` | 13px | 400 | 1.5 | 0 | 辅助说明 |
| `--text-micro` | 12px | 500 | 1.4 | 0.2px | 标签/时间戳 |

- **字族栈**：`--font-sans: "Inter", "PingFang SC", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", Arial, sans-serif;`
- **数字/数据**：使用等宽数字 `font-variant-numeric: tabular-nums`，用于配额、用量、时间等对齐场景。

### 2.4 间距系统（Spacing Scale）

> 基于 **4px 基础刻度**，推荐优先偶数是 8px。

| Token | 值 | 典型场景 |
| --- | --- | --- |
| `--space-1` | 4px | 图标与文字间隙 |
| `--space-2` | 8px | 紧凑间距（标签内部） |
| `--space-3` | 12px | 控件组间距 |
| `--space-4` | 16px | 卡片内边距（基准） |
| `--space-6` | 24px | 区块间距 |
| `--space-8` | 32px | 内容区与侧栏间距 |
| `--space-12` | 48px | 大区块 / 页面留白 |
| `--space-16` | 64px | 页面级顶部留白 |

### 2.5 圆角与阴影（Radius & Elevation）

| Token | 值 | 用途 |
| --- | --- | --- |
| `--radius-xs` | 4px | 标签、小控件 |
| `--radius-sm` | 8px | 输入框、按钮、复选框 |
| `--radius-md` | 12px | 卡片默认 |
| `--radius-lg` | 16px | 模态框、引用卡片 |

**阴影 / 发光（glow）**
| Token | 值 | 用途 |
| --- | --- | --- |
| `--shadow-card` | `0 2px 8px rgba(0,0,8,.28)` | 卡片默认浮起 |
| `--shadow-popover` | `0 8px 24px rgba(0,0,8,.45)` | 下拉/浮层 |
| `--shadow-modal` | `0 12px 40px rgba(0,0,8,.6)` | 模态框 |
| `--glow-accent` | `0 0 12px rgba(170,217,242,.35)` | 主按钮 / 焦点强调 |
| `--glow-focus` | `0 0 0 3px rgba(170,217,242,.28)` | focus-visible 光晕 |

### 2.6 动效系统（Motion）

**缓动曲线**
| Token | 值 |
| --- | --- |
| `--ease-in` | `cubic-bezier(0.4, 0, 1, 1)` |
| `--ease-out` | `cubic-bezier(0, 0, 0.2, 1)` |
| `--ease-in-out` | `cubic-bezier(0.4, 0, 0.2, 1)` |

> 所有入场/退场动效默认使用 `--ease-out`（从慢到快出），避免拖沓；状态切换用 `--ease-in-out`。

**时长档位**
| Token | 值 | 场景 |
| --- | --- | --- |
| `--dur-fast` | 120ms | hover / 光标反馈 |
| `--dur-base` | 240ms | 浮出 / 淡入 / 切换 |
| `--dur-slow` | 400ms | 页面级进场 / 模态框 |

**常用动效模式**
| 模式 | 实现建议 |
| --- | --- |
| 淡入 Fade | `opacity 0→1`，`--dur-base` |
| 浮起 Rise | `translateY(8px)→0` + `opacity`，240ms，`--ease-out` |
| 加载 Loading | 旋转/脉动，180ms 匀速循环；文字“生成中”提示 |
| 位移 Slide | 侧栏展开 `max-width`/`translateX`，400ms，`--ease-in-out` |

**`prefers-reduced-motion` 降级**
```
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
  }
}
```
即：系统开启“减少动态效果”时，所有动效退化为瞬时/单帧。

---

## 3. 基础组件视觉规范（四态：default / hover / disabled / loading）

### 3.1 按钮（Button）

规格：默认高度 `36px`，紧凑 `32px`，大（触屏）`44px`；内边距 `0 16px`；`--radius-sm`。

| 类型 | default | hover | disabled | loading | 说明 |
| --- | --- | --- | --- | --- | --- |
| **主按钮 Primary** | `bg=--color-accent`, `text=--color-text-on-accent` | `bg=--color-accent-hover` + `--glow-accent` | `bg=--color-border-disabled`, text 40% 透明 | 前置 14px spinner（`--color-loading`） | 关键推进动作（确认入库/保存/生成） |
| **次按钮 Secondary** | `bg=--color-surface-raised`, `border=--color-border-strong`, `text=--color-text-primary` | `bg=--color-surface-hover` | `border=--color-border-disabled`, text 40% | spinner 同色 40% | 常驻次级操作 |
| **文字按钮 Text** | `transparent`, `text=--color-accent` | 底 `--color-accent-soft` | `text=--color-text-disabled` | — | 低频链接式操作 |
| **图标按钮 Icon** | `36px` 方形, `transparent` | `bg=--color-surface-hover` | 图标 40% | — | 工具栏图标 |

### 3.2 输入框（Input）

| 状态 | 规格 |
| --- | --- |
| default | 高度 `36px`，`padding: 0 12px`，`border: 1px solid --color-border-input`，`bg=--color-surface-card`，`text=--color-text-primary` |
| hover | `border=--color-border-strong` |
| focus-visible | `border=--color-accent` + `box-shadow: var(--glow-focus)`，**焦点光晕 3px** |
| disabled | `bg=--color-surface-card`, `text/`placeholder 40%, `border=--color-border-disabled` |
| **错误态 error** | `border=--color-error` + `box-shadow: 0 0 0 3px rgba(255,107,107,.22)`，下方提示 `--text-caption` 用 `--color-error` |
| placeholder | `--color-text-placeholder` |

### 3.3 复选框 / 开关 / 单选框

| 控件 | default | 选中 checked | hover | disabled | 尺寸 |
| --- | --- | --- | --- | --- | --- |
| 复选框 Checkbox | 16px 方块，`border=--color-border-strong`, 白底(rgba 覆盖深色) | 背景 `--color-accent`，勾型 `--color-text-on-accent` | 底 `--color-accent-soft` | 内容 40%，不可点 | 16×16 |
| 开关 Switch | 24×14px 轨道 `--color-border-disabled`，滑块 12px `--color-text-primary` | 轨道 `--color-accent`，滑块 `--color-bg-base` | 轨道微亮 | 40% | 24×14 |
| 单选框 Radio | 16px 圆环 `--color-border-strong` | 中心圆点 `--color-accent` | 底 `--color-accent-soft` | 40% | 16×16 |

### 3.4 卡片 / 模态框 / Toast

**卡片 Card**：`bg=--color-surface-card`, `radius=--radius-md`, `border=1px solid --color-border-default`, hover 提升 `bg=--color-surface-hover` + `--shadow-card`。

**模态框 Modal**：`bg=--color-surface-raised`, `radius=--radius-lg`, `--shadow-modal`；遮罩 `--color-surface-overlay`；入场 240ms 淡入+浮起；关闭 Escape / 点遮罩。

**Toast 通知**：`bg=--color-surface-raised`, `radius=--radius-md`, 左缘按语义色 4px，图标用对应语义色；自动消失 4s（可配置）；文案 ≥ `--text-caption`。

### 3.5 链接与导航元素

| 元素 | 规格 |
| --- | --- |
| 链接 Link | `--color-accent`，hover 下划线，focus 用 `--glow-focus` |
| 顶栏 Header | 高 `64px`，`bg=--color-bg-canvas`, `border-bottom: 1px solid --color-border-default`，品牌区 + 用户区 |
| 侧边导航 Nav | 宽 `240px`，`bg=--color-bg-canvas`；当前项 `bg=--color-accent-soft` + 左 3px `--color-accent` 指示条；hover `bg=--color-surface-card` |

---

## 4. 页面布局规范

### 4.1 页面骨架（App Shell）

```
┌────────────────────────────────────────────────────────┐
│ Header (64px) ─ 品牌 / 面包屑 / 检索入口 / 用户+通知      │
├──────────┬─────────────────────────────────────────────┤
│ Sidebar  │  Content                                     │
│ (240px)  │  (灵活自适应, 最大 1200px 居中或 1920 全宽)     │
│ 导航/空间 │  面包屑 / 页面标题 / 操作条 / 列表|详情          │
└──────────┴─────────────────────────────────────────────┘
```

- **顶栏 Header**：高 `64px`，fixed，含品牌、全局检索、用户菜单。
- **侧边栏 Sidebar**：宽 `240px`（可折叠至 64px 图标态）；部门空间切换 + 导航。
- **内容区 Content**：`padding: 24px 32px`；最大内容宽 `1200px` 居中，超过则留自适应左右留白。

### 4.2 栅格系统与响应式断点

**栅格**：12 列栅格，`gutter=24px`；内容区宽度随断点变化。

| 断点 | 设备/范围 | 侧边栏 | 内容 Padding | 栅格列行为 | 说明 |
| --- | --- | --- | --- | --- | --- |
| **≥1440px** | 大屏桌面 | 240px | 24/32px | 12 列全展开 | 列表多列 / 编辑器全宽 |
| **≥1024px** | 中屏/平板横 | 240px(可折叠) | 24px | 6–12 列弹性 | 详情可分栏，但保可读 |
| **≥768px** | 平板竖 | 折叠为抽屉(64px 图标) | 16px | 4–12 列 | 主操作收进顶栏 |
| **375px（移动）** | 手机 | 抽屉式(滑入) | 16px | 全部单列堆叠 | **禁止横向滚动** |

- 一律使用弹性布局（flex/grid）与 `min/max-width` clamp，禁止固定死宽。
- 375px 下：列表转单列卡片、表格转“卡片化”堆叠、操作按钮收缩为图标或底部操作条。

### 4.3 页面级三大状态视觉规范

**空状态 Empty**
- 居中 48px 线性图标（`--color-text-tertiary`）；主文案 `--text-h2`，副文案 `--text-caption`；1 个主操作按钮（发起首次上传/新建）。
- 三类空态文案配套：无文档 / 无检索结果 / 无权限无内容。

**加载态 Loading**
- 局部：单行/区块用骨架屏（`bg=--color-surface-card` 脉冲），首次进入用骨架；
- 全局：居中 spinner + “加载中/生成中”次级文案（AI 合成遵循 PRD 进度提示规范，见 US-EMP-06）。

**错误态 Error**
- 整页：居中 48px 红色调线性图标（`--color-error`）+ `--text-h1` + 描述 + 主操作“重试/返回”；
- 局部：行内错误条（Toast 或行内红字 `--color-error`）；区分“权限 403”与“系统异常 5xx”两种引导话术。

---

## 5. 可访问性（A11y）标准

### 5.1 文本对比度（WCAG 2.1 AA）

| 用途 | 标准 | 实际要求 |
| --- | --- | --- |
| 正文/常规文本 | AA 对比度 | **≥ 4.5:1** |
| 大字（≥ 18.66px 粗 / 24px 常规） | AA | **≥ 3:1** |
| 图标 / 控件边框（必需识别） | AA | 依特性 ≥ 3:1（趋势着色配图标） |
| disabled 元素 | AA | 豁免（但不承载关键信息） |

> 注：`--color-text-primary(#D1FFFF)` 对 `--color-bg-base(#000515)` 对比 ~15:1+，远超 AAA；应避免在对比度 <4.5 的组合中放置正文。

### 5.2 焦点可见性 / 键盘导航 / ARIA

- **`:focus-visible`**：所有可交互元素必须呈现焦点外观（默认 `--glow-focus` + 边框变 `--color-accent`）；禁止 `.outline: none` 而无替代。
- **`:focus`（鼠标点击时）**：不强制显示 ring，但不移除焦点语义。
- **键盘导航顺序**：按 DOM 视觉顺序；模态框 focus 圈闭（Trap）；顶栏可 Tab 直达；侧边栏为可折叠的 `aside` 带展开图标。
- **ARIA 标注原则**：
  - 图标按钮必须有 `aria-label`；
  - 状态变化（Loading/Empty/Error）用 `role="status"` / `aria-live="polite"` 通知；
  - 模态框用 `role="dialog" aria-modal="true"` + 正确标题关联 `aria-labelledby`；
  - Toast 用 `role="alert"`。

### 5.3 色觉友好（禁止仅用颜色传达状态）

- 任何状态（成功/警告/错误/进行中）**必须同时具备 ≥2 种信号**：颜色 +（图标 / 文字 / 形状 / 描边）。
- 例：错误输入框 = 红边框 + 错误图标 + 红字提示文案；不可仅靠换色表达。
- 图表/趋势数据除颜色外须配图形符号（实心/空心、线型）或直接数值标注。

---

## 6. 落地与验收

### 6.1 Token → CSS Variables 对照速查表（简版）

| 类别 | Token | 值 |
| --- | --- | --- |
| 品牌 | `--color-brand-50…950` | `#D1FFFF … #000008` |
| 背景 | `--color-bg-base / -canvas / -sunken` | `#000515 / #000515 / #000008` |
| 表面 | `--color-surface-card / -raised / -hover` | `#001325 / #002A3D / #134155` |
| 文本 | `--color-text-primary/-secondary/-tertiary/-placeholder/-disabled` | `#D1FFFF / #AAD9F2 / #85B3CB / #618EA5 / #3F6B81` |
| 边框 | `--color-border-default/-strong/-input` | `#2E5A6F / #618EA5 / #3F6B81` |
| 交互 | `--color-accent/-hover/-active/-soft / -focus-ring / -loading` | `#AAD9F2 / #D1FFFF / #85B3CB / rgba…14 / #AAD9F2 / #85B3CB` |
| 语义 | `--color-success/-warning/-error/-info` | `#3DDC84 / #FFB86B / #FF6B6B / #60A5FA` |
| 字体 | `--text-*` / `--font-sans` | 见 §2.3 |
| 间距 | `--space-1…16` | `4…64px` |
| 圆角 | `--radius-xs/-sm/-md/-lg` | `4/8/12/16px` |
| 阴影 | `--shadow-*` / `--glow-*` | 见 §2.5 |
| 动效 | `--ease-*` / `--dur-*` | 见 §2.6 |

### 6.2 开发验收 Checklist

> 任一页面上线前，须逐项通过。

- [ ] **无硬编码色值**：全部颜色经 `var(--color-…)`；`grep -EI '#[0-9a-fA-F]{3,8}'` 仅命中 Token 定义文件。
- [ ] 字体/字号/间距/圆角/阴影均引用 Token，无孤立 magic value。
- [ ] 任一组件覆盖 default / hover / focus-visible / disabled / loading / error 六态（适用项）。
- [ ] 375px 视口**无横向滚动**（`overflow-x` 无内容溢出；列表、表格做卡片化）。
- [ ] 对比度抽检：用工具（如 Axe / heading 检查）对正文与关键文本抽查 ≥4.5:1（大字 ≥3:1）。
- [ ] 状态不只用颜色：每个状态有图标/文字佐证；Toast/Badge 样式到位。
- [ ] `:focus-visible` 有可见焦点环；键盘顺序合理；模态框 focus 圈闭。
- [ ] `prefers-reduced-motion: reduce` 下动效失效且界面可正常使用。
- [ ] 现有交互（上传/检索/合成/确认入库）在 Web 端与移动端均满足 §1/§2 视觉规范。

---

> **使用说明**：本文档为唯一视觉标准。新增任何色值/字号/间距/动效前，必须先在本设计系统补充 Token 并登记，禁止绕过 Token 直接作用于组件，确保全站一致性与可维护性。