# LifeTrace Execute

LifeTrace Execute 是 LifeTrace 的独立执行中心客户端，面向日常任务、项目、日历、快速收集与每日复盘。

当前项目采用两层实现：

- **浏览器高保真预览**：当前 UI 设计与交互评审的主基线，零依赖，可直接打开。
- **Android Jetpack Compose**：最终 Android 客户端实现基线，后续按浏览器预览同步高保真视觉与交互。

> 设计原则：新增或重构功能时，不删除已经确认的既有功能入口；允许调整入口位置，但必须保留功能能力。

## 信息架构

底部一级导航固定为 5 个入口：

1. 今天
2. 任务
3. 项目
4. 日历
5. 收集

“我的”通过主页面右上角头像进入，不占用底部导航；“今日复盘”保留在“今天”页面中，并拥有独立复盘页。

## 当前高保真预览

目录：

```text
web-preview/
├── index.html
├── styles.css
└── app.js
```

无需 Node.js、npm、Android SDK 或外部 CDN。

### 最快查看方式

直接双击：

```text
web-preview/index.html
```

也可以在仓库根目录启动本地 HTTP 服务：

```bash
python -m http.server 8080
```

浏览器打开：

```text
http://localhost:8080/web-preview/
```

桌面端以 360 × 800 Android 基准尺寸显示手机模拟框；窄屏/手机浏览器自动切换为全屏 App 预览。

## 已实现页面与交互

### 今天

- 问候、日期、一周日期条
- 今日焦点主卡片
- 完成率、深度工作、已完成、连续执行等概览
- 今日时间线
- 今日任务预览
- 今日复盘入口

### 任务

- 搜索
- 全部 / 进行中 / 等待 / 已完成筛选
- 优先级与状态展示
- 任务完成状态切换
- 新建任务 Bottom Sheet
- 可输入并新增 Mock 任务

### 项目

- 项目数量与整体概览
- 状态筛选
- 项目进度
- 截止日期
- 成员展示
- 暂停 / 进行中等状态

### 日历

- 月视图
- 事件日期标记
- 日期选择
- 当日日程
- 不同日程类型的状态区分

### 收集

- 文本
- 图片
- 语音
- 链接
- 文件
- 想法
- 收集箱分类
- 最近收集

### 我的

- 个人账号卡
- LifeTrace Cloud 同步状态
- 个人资料
- 账号与安全
- 设备管理
- 同步与数据
- 通知
- 外观
- 关于
- 退出登录

### 今日复盘

- 今日评分
- 心情选择
- 今日收获
- 改进项
- 明日第一优先级
- 保存后返回今天

## Android 实现

Android 目录：

```text
app/
```

技术栈：

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- minSdk 26
- targetSdk 35

当前 Compose 端已经具备主要页面、导航、公共组件和 Mock Data，但视觉精度仍需继续对齐 `web-preview`。

## 当前开发阶段

当前阶段重点不是继续扩张功能，而是：

1. 以浏览器预览为视觉基准继续提高 UI 还原度。
2. 完善任务详情、项目详情等二级页面。
3. 固化颜色、字号、间距、圆角、阴影、状态色等设计 Token。
4. 将高保真结果逐步同步回 Jetpack Compose。
5. UI 稳定后再接入 LifeTrace Cloud 数据与同步协议。

详细进度见 [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md)，界面规范见 [`docs/UI_SPEC.md`](docs/UI_SPEC.md)。
