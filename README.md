# LifeTrace Execute

LifeTrace Execute 是 LifeTrace 的独立执行中心客户端，面向日常任务、项目、日历、快速收集与每日复盘。

当前项目采用两层实现：

- **浏览器高保真预览**：当前 UI 设计与交互评审的主基线，零依赖，可直接打开。
- **Android Jetpack Compose**：最终 Android 客户端实现基线，后续按浏览器预览同步高保真视觉与交互。

正式 Android 客户端采用 **Local-first + LifeTrace Cloud Sync**，不会新建第二套云端。云端直接复用 `zhouxingxing1279/LifeTrace` 中的 Rust + Axum + PostgreSQL Cloud、Auth v1 与 Sync v1。

> 设计原则：新增或重构功能时，不删除已经确认的既有功能入口；允许调整入口位置，但必须保留功能能力。

## 文档

- [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md)：长期需求台账，后续所有新增/变更需求首先记录在这里。
- [`docs/EXECUTION_PLAN.md`](docs/EXECUTION_PLAN.md)：**完整项目执行计划、LifeTrace Cloud 接入方案与分阶段验收标准。**
- [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md)：当前完成度与下一阶段计划。
- [`docs/UI_SPEC.md`](docs/UI_SPEC.md)：界面与组件规范。

## 信息架构

底部一级导航固定为 5 个入口：

1. 今天
2. 任务
3. 项目
4. 日历
5. 收集

“我的”通过主页面右上角头像进入，不占用底部导航；“今日复盘”保留在“今天”页面中，并拥有独立复盘页。

新增功能仍嵌入现有信息架构：

- **番茄时钟**：位于任务页，不新增一级导航。
- **重要日期**：位于日历页，并提供独立管理页。

## 当前高保真预览

目录：

```text
web-preview/
├── index.html
├── styles.css
├── app.js
├── features-v3.css
└── features-v3.js
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

桌面端以约 360 × 800 Android compact 信息密度显示手机模拟框；窄屏/手机浏览器自动切换为全屏 App 预览。

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
- **番茄时钟**
  - 25 / 5 经典模式
  - 50 / 10 深度专注模式
  - 开始 / 暂停 / 重置
  - 真实前端倒计时
  - 关联任务
  - 今日番茄轮次
  - 页面切换后计时状态保持

### 项目

- 项目数量与整体概览
- 状态筛选
- 项目进度
- 截止日期
- 成员展示
- 暂停 / 进行中等状态

### 日历

- 月视图
- 普通事件日期标记
- 日期选择
- 当日日程
- 不同日程类型的状态区分
- **重要日期**
  - 生日 / 纪念日 / 里程碑 / 其他
  - 仅一次 / 每年重复
  - 公历 / 农历
  - 农历年、月、日与闰月输入
  - 新增 / 编辑 / 删除
  - 重要日期独立管理页
  - 公历重要日期月历标记

> 浏览器原型目前只负责农历的完整输入与交互设计，不自行使用简化算法进行农历换算。正式 Android/数据层需要使用可靠的历法实现。

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

当前 Compose 端已经具备主要页面、导航、公共组件和 Mock Data，但视觉与功能版本仍落后于 `web-preview`。

正式实现按 [`docs/EXECUTION_PLAN.md`](docs/EXECUTION_PLAN.md) 推进，核心链路为：

```text
Compose UI
→ Domain / Repository
→ Room / SQLite
→ sync_outbox
→ LifeTrace Cloud Auth + Sync v1
→ PostgreSQL
→ 其他 LifeTrace 客户端
```

## LifeTrace Cloud 对接

主云端仓库：`zhouxingxing1279/LifeTrace`

已确认可直接复用：

- 原生客户端登录 `/api/v1/auth/login`
- access / refresh token
- Device / Session
- `/api/v1/sync/capabilities`
- `/api/v1/sync/push`
- `/api/v1/sync/pull`
- `/api/v1/sync/snapshot`
- changeId 幂等
- server cursor
- optimistic conflict
- tombstone 删除传播
- execution task / project / calendar / memo / reminder 等双向同步实体
- 文件元数据与 S3 兼容对象存储

Execute 1.0 还需要在 LifeTrace 主仓库补齐 execution 域强类型 DTO，并新增 `execution.important_date` 与 `execution.focus_session`。

## 当前开发阶段

当前执行顺序以 `docs/EXECUTION_PLAN.md` 为准：

1. 收敛浏览器高保真交互；
2. 冻结 Execute 1.0 字段级领域模型；
3. 在 LifeTrace 主仓库加固 execution Cloud contracts；
4. Android 补齐可重复构建、本地数据库和 Repository；
5. 接入 Cloud Auth；
6. 实现 Local-first Outbox + Push/Pull/Snapshot/Conflict；
7. 先以 Task 完成第一条双设备纵向同步链路；
8. 再扩展 Project / Calendar / Collection / Review / Profile；
9. 落地重要日期农历和番茄后台计时；
10. 完成 Release Gate 后发布。
