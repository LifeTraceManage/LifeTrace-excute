# LifeTrace Execute

LifeTrace Execute 是 LifeTrace 的独立 Android 执行中心客户端。当前仓库从高保真 UI 原型开始，直接使用 **Jetpack Compose + Material 3** 实现，设计稿与最终 Android 代码保持同一套组件体系。

## 当前界面

底部一级导航固定为 5 个入口：

- 今天
- 任务
- 项目
- 日历
- 收集

“我的”通过各主页面右上角头像进入，不占用底部导航。每日复盘保留在“今天”页面中，并拥有独立复盘页。

### 今天

- 今日概览
- 时间线
- 今日任务预览
- 今日复盘入口

### 任务

- 搜索
- 全部 / 进行中 / 等待 / 已完成筛选
- 任务列表
- 快速新建

### 项目

- 独立一级入口
- 项目状态
- 进度
- 截止日期
- 成员数

### 日历

- 月视图
- 当日日程

### 收集

- 文本、图片、语音、链接、文件、想法
- 收集箱分类

### 我的

- 个人资料
- LifeTrace Cloud 登录状态
- 账号与安全
- 设备管理
- 通知
- 同步与数据
- 通用设置

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Android minSdk 26 / targetSdk 35

## 运行

使用近期版本 Android Studio 打开仓库，等待 Gradle Sync 完成后运行 `app`。

> 当前提交聚焦 UI/交互原型，数据均为 Mock Data；后续接入 LifeTrace Cloud 执行中心同步协议。
