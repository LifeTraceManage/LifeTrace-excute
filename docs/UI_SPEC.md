# LifeTrace Execute UI / Interaction Specification

更新时间：2026-08-27

## 1. 目标

该文档定义 LifeTrace Execute 当前 UI 与交互基线。浏览器高保真预览 `web-preview/` 是当前视觉评审基准，Android Jetpack Compose 应逐步与其保持一致。

本规范强调两个原则：

1. 高保真：浏览器预览应尽量接近最终 Android 成品，而不是线框图。
2. 功能完整：优化导航与层级时不得删除已经确认的功能。

## 2. 设备与布局基准

主基准：

```text
360 × 800 dp
```

桌面浏览器：

- 使用手机设备框展示。
- 页面内容区域独立滚动。
- 保留 Android 状态栏、底部导航与手势条的视觉模拟。

移动浏览器：

- 自动使用全屏布局。
- 不强制显示桌面手机外壳。

## 3. 导航结构

### 底部 Navigation Bar

固定 5 个 destination：

| 入口 | 作用 |
| --- | --- |
| 今天 | 日常执行总览与复盘入口 |
| 任务 | 任务搜索、筛选、创建和执行 |
| 项目 | 项目列表、状态和进度 |
| 日历 | 日期与日程组织 |
| 收集 | 快速捕获信息与 Inbox 整理 |

要求：

- 使用正式 SVG / Material 风格图标。
- Active destination 使用蓝色图标/文字与浅蓝背景指示。
- Inactive destination 使用中性灰。
- 不使用 `○`、`□`、`◇` 等字符充当正式图标。

### 我的

- 不占用底部导航。
- 主页面右上角头像进入。
- “我的”是完整账号与设置中心，而不是简单个人资料页。

### 今日复盘

- 保留在“今天”中作为高权重入口。
- 点击进入独立页面。
- 不从产品中移除。

## 4. 视觉 Token

当前浏览器预览基准色：

```text
Primary Blue       #2563EB
Primary Blue Soft  #EAF1FF
Ink                #18212F
Muted              #6B7280
Background         #FAFBFF
Surface            #FFFFFF
Surface Muted      #F4F6FA
Border              #E4E7EC
Orange              #F59E0B
Orange Soft         #FFF4E5
Green               #16A36A
Green Soft          #E9F8F1
Red                 #DC4C4C
```

语义：

- 蓝色：主操作、选中状态、核心产品强调。
- 绿色：完成、成功、习惯完成等正向状态。
- 橙色：复盘、提醒、次级注意事项。
- 红色：错误、紧急、高优先级。

## 5. 圆角与层级

建议层级：

```text
小控件 / Chip       8–10
输入框 / 普通卡片   12–14
主卡片              16–18
特殊大卡片          20–24
设备框              30+
```

卡片层级以浅边框为主，阴影保持低强度。避免所有元素都使用明显投影。

## 6. Typography

中文优先字体：

```text
Noto Sans SC
PingFang SC
Microsoft YaHei
System UI fallback
```

层级建议：

- Screen title：22–24，Bold / 700+
- Section title：14–16，Semibold / Bold
- Card title：12–14，Semibold
- Body：11–13
- Meta / caption：9–11

界面应保持紧凑，但文字不得因追求高密度而过小到影响真机可读性。

## 7. 页面规范

### Today

信息顺序建议：

1. 问候 + 日期 + 头像
2. 一周日期条
3. 今日焦点
4. 今日概览
5. 时间线
6. 今日任务
7. 今日复盘

今日焦点是首页视觉主卡片，应明显区别于普通统计卡片。

### Tasks

必须保留：

- 搜索
- 状态筛选
- 优先级提示
- 截止时间
- 新建任务

创建任务优先使用 Bottom Sheet，以保持 Android 移动端交互习惯。

后续 Task Detail 至少需要考虑：

- 标题
- 状态
- 项目归属
- 截止日期
- 提醒
- 优先级
- 备注
- 子任务

具体是否全部进入首版实现，可后续根据产品范围裁剪，但现阶段不得通过 UI 重构删除已经存在的任务能力。

### Projects

项目卡片至少展示：

- 项目名
- 状态
- 进度
- 截止日期
- 成员 / 协作信息（如果存在）

项目作为一级功能保留独立底部导航入口。

### Calendar

月视图需要：

- 当前月份
- 当前 / 选中日期
- 有事件日期标记
- 下方对应日期的日程列表

任务截止日期与日历事件后续应统一映射到同一时间视图。

### Collection

快速收集入口固定保留：

- 文本
- 图片
- 语音
- 链接
- 文件
- 想法

收集页不仅是创建入口，还需要保留 Inbox / 分类整理能力。

### Profile / My

至少保留：

- 个人资料
- 登录 / Cloud 状态
- 账号与安全
- 设备管理
- 同步与数据
- 通知
- 外观
- 通用设置
- 关于

未来增加设置项时，可以增加分组和二级页面，但不得把“我的”缩减为单纯头像资料卡。

### Review

当前字段：

- 今日评分
- 心情
- 今日收获
- 改进项
- 明日第一优先级

保存操作完成后返回“今天”。后续应补充历史复盘查看。

## 8. 组件规范

优先沉淀公共组件：

- ScreenHeader
- BottomNavigation
- SectionHeader
- MetricCard
- TaskRow
- ProjectCard
- TimelineItem
- StatusChip
- SearchField
- BottomSheet
- SettingsRow
- EmptyState

浏览器和 Compose 组件命名不需要完全一致，但视觉语义与交互职责应保持一致。

## 9. 浏览器预览与 Compose 同步规则

当前工作流：

```text
浏览器快速设计/验证
        ↓
确认视觉与交互
        ↓
抽取 Design Token / Component
        ↓
Jetpack Compose 实现
        ↓
Android Preview / 真机验证
```

浏览器代码不是最终 Android 产品代码，因此涉及 Android 特有行为时，以 Compose / Material 3 与 Android 平台规范为最终准则。

## 10. 当前下一步

UI 优先级：

1. Today 首页继续精修。
2. Task Detail。
3. Project Detail。
4. Design Token 固化。
5. Compose 同步。

在上述内容收敛前，不优先继续增加新的一级导航功能。
