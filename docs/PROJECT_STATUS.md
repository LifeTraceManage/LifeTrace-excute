# LifeTrace Execute 项目进度

更新时间：2026-08-27

## 1. 当前定位

LifeTrace Execute 是 LifeTrace 的独立 Android 执行中心客户端，负责日常执行场景，不承担财务模块。

当前建立两套实现载体：

- `web-preview/`：UI 设计、交互验证和快速评审基线。
- `app/`：最终 Android Compose 客户端。

正式 Android 采用 **Local-first + LifeTrace Cloud Sync**：本地 Room/SQLite 保证离线执行，云端直接复用 `zhouxingxing1279/LifeTrace` 中已有的 Rust + Axum + PostgreSQL Cloud、Auth v1 与 Sync v1，不建设第二套 Execute 后端。

当前工作方式：**需求先进入 `docs/REQUIREMENTS.md`，浏览器确认交互；工程实施和验收顺序统一以 `docs/EXECUTION_PLAN.md` 为准。**

## 2. 文档基线

- `docs/REQUIREMENTS.md`：长期需求台账，所有新增/变更需求的 Source of Truth。
- `docs/EXECUTION_PLAN.md`：完整项目执行计划、云端接入、测试策略和 Release Gate。
- `docs/UI_SPEC.md`：视觉、导航和组件规范。
- `docs/PROJECT_STATUS.md`：项目当前完成度与下一阶段计划。

## 3. 已确认的信息架构

### 一级导航

底部固定 5 个入口：

1. 今天
2. 任务
3. 项目
4. 日历
5. 收集

### 非底部入口

- 我的：右上角头像进入。
- 今日复盘：从“今天”进入。
- 重要日期：从“日历”进入管理页。
- 番茄时钟：内嵌在“任务”页面，不新增一级导航。

### 功能保护原则

新增、重构、合并入口时不得删除已确认的既有功能。允许调整入口位置和视觉层级，但不得为了新增功能移除项目、收集、复盘、账号/设置等能力。

## 4. 浏览器高保真预览进度

状态：**主要页面和本轮新增需求已完成前端设计，可用于视觉与交互评审。**

### 今天

已实现：

- 问候与日期
- 一周日期条
- 今日焦点主卡
- 今日完成率
- 深度工作 / 已完成 / 连续执行摘要
- 今日概览
- 时间线
- 今日任务
- 今日复盘入口

### 任务

已实现：

- 搜索
- 全部 / 进行中 / 等待 / 已完成筛选
- 任务状态与优先级
- 完成状态切换
- 新建任务 Bottom Sheet
- Mock 任务创建
- **番茄时钟高保真卡片**
- **真实前端倒计时**
- **开始 / 暂停 / 重置**
- **25/5 与 50/10 模式**
- **关联任务选择**
- **一级页面切换后保持当前计时状态**

待完成：

- 任务详情页
- 编辑任务
- 截止日期与提醒
- 项目归属选择完善
- Android 后台计时、通知、进程恢复策略

### 项目

已实现：

- 项目概览
- 项目状态
- 进度
- 截止日期
- 成员展示
- 状态筛选

待完成：

- 项目详情页
- 项目任务列表
- 项目里程碑 / 阶段
- 项目统计摘要

### 日历

已实现：

- 月视图
- 日期选择
- 普通事件标记
- 当日日程
- 日程类型区分
- **重要日期摘要卡片**
- **重要日期独立管理页**
- **生日 / 纪念日 / 里程碑 / 其他类型**
- **仅一次 / 每年重复**
- **公历 / 农历选择**
- **农历年份、月、日与闰月输入设计**
- **重要日期新增 / 编辑 / 删除交互**
- **公历重要日期在月历中的独立标记**

待完成：

- 正式农历 ↔ 公历换算实现
- 农历每年重复日期映射到对应公历日期
- 重要日期提醒策略
- 与任务截止日期联动
- 日历事件详情
- 周视图 / 日视图评估

> 浏览器原型只完成历法输入和展示设计，不把简化算法冒充正式农历换算。正式 Android/数据层必须使用可靠历法实现。

### 收集

已实现：

- 文本 / 图片 / 语音 / 链接 / 文件 / 想法
- 收集箱分类
- 最近收集

待完成：

- 收集内容详情
- 分类 / 转任务 / 转项目
- 真实文件和媒体选择流程

### 我的

已实现：

- 个人账号卡
- Cloud 同步状态
- 个人资料
- 账号与安全
- 设备管理
- 同步与数据
- 通知
- 外观
- 关于
- 退出登录

### 今日复盘

已实现：

- 今日评分
- 心情选择
- 今日收获
- 改进项
- 明日第一优先级
- 保存返回

待完成：

- 历史复盘
- 复盘保存状态
- 与明日任务联动

## 5. Android Compose 进度

状态：**基础工程和核心页面已建立，但仍处于 UI / Mock 阶段，尚未接入正式本地数据层和 LifeTrace Cloud。**

已实现：

- Kotlin + Jetpack Compose + Material 3 工程骨架
- Bottom Navigation
- Today / Tasks / Projects / Calendar / Collection
- Profile
- Review
- 公共组件
- Mock Data
- Compose Preview

待完成：

- Gradle Wrapper 与可重复 Android 构建
- 浏览器新版视觉体系同步
- `REQ-CAL-001` 重要日期
- `REQ-TASK-001` 番茄时钟
- 二级详情页
- ViewModel / Domain / Repository 分层
- Room / SQLite 本地数据库
- `sync_outbox / sync_state / sync_conflicts`
- LifeTrace Cloud Auth client
- LifeTrace Sync v1 client
- 后台同步与 WorkManager
- 离线、冲突、snapshot、tombstone 处理

## 6. LifeTrace Cloud 对齐状态

已核对主仓库 `zhouxingxing1279/LifeTrace`。

### 已存在，可直接复用

Cloud 技术栈：

- Rust
- Axum
- PostgreSQL
- Docker 部署
- 共享 `lifetrace-contracts`
- OpenAPI / JSON Schema

认证：

- `/api/v1/auth/login`
- access token / refresh token
- Session
- Device

同步：

- `/api/v1/sync/capabilities`
- `/api/v1/sync/push`
- `/api/v1/sync/pull`
- `/api/v1/sync/snapshot`
- changeId 幂等
- server cursor
- baseServerVersion
- optimistic conflict
- tombstone

Cloud 已注册执行域双向同步实体：

```text
execution.goal
execution.weekly_review
execution.project
execution.recurrence_rule
execution.task
execution.task_dependency
execution.task_occurrence
execution.waiting_item
execution.calendar_event
execution.calendar_occurrence
execution.memo
execution.memo_tag
execution.memo_tag_relation
execution.reminder
execution.completion_result
execution.entity_link
```

### 1.0 前需要补齐

- execution 域当前 RegisteredJson 契约升级为强类型 DTO / Schema 校验；
- 新增 `execution.important_date`；
- 新增 `execution.focus_session`；
- Android 端 Auth / Sync client；
- Android Local-first 数据层与 Outbox；
- 双设备 E2E。

重要日期的农历原始字段必须作为 Source of Truth；番茄计时偏好优先复用 `user.preference`，完成的专注记录同步为 `execution.focus_session`。

## 7. 当前设计方向

- 白色 / 极浅灰基础背景
- 蓝色为主强调色
- 绿色表示完成 / 正向状态
- 橙色用于提醒和复盘
- 紫色用于重要日期等特殊信息
- 红色仅用于高风险 / 高优先级 / 错误
- 浅边框 + 低强度阴影
- 高信息密度但避免后台管理式堆叠
- 正式 SVG / Material 风格图标
- Android compact viewport 基准约 360 × 800 dp

## 8. 下一阶段任务

执行顺序以 [`EXECUTION_PLAN.md`](./EXECUTION_PLAN.md) 为唯一工程基线：

1. 评审并收敛现有浏览器高保真交互。
2. 冻结 Execute 1.0 字段级领域模型。
3. 在 LifeTrace 主仓库加固 `execution.*` contract，新增 ImportantDate / FocusSession。
4. 补齐 Android Gradle Wrapper，保证项目可重复构建。
5. 建立 Room + Repository + Outbox / State / Conflict 本地层。
6. 接入 LifeTrace Cloud Auth。
7. 实现 capabilities / snapshot / pull / push Sync Coordinator。
8. **先以 Task 做第一条纵向端到端链路**：本地新增 → Cloud → 第二设备同步。
9. 扩展到 Project / Calendar / Collection / Review / Profile。
10. 实现可靠农历换算、后台番茄计时和通知。
11. 执行离线、重复提交、冲突、删除、snapshot、双设备回归。
12. 所有 Release Gate 通过后发布 1.0。
