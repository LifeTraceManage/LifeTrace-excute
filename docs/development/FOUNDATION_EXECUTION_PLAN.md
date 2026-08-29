# LifeTrace Execute 基础可用版本执行文档

更新时间：2026-08-29

> 本文档基于 `main` 分支当前代码进行重新盘点，目标不是继续增加页面数量，而是把 LifeTrace Execute 从“高保真 UI + 单一 Task 纵向链”推进为**可以真实日常使用的 Android 执行工具**。
>
> 核心原则：**一个功能只有完成 Domain → Room → Repository → UI → 离线/Sync → Test 的纵向闭环，才记为“已实现”。只有 Compose 页面、MockData、静态数字或空 `onClick` 不计入完成度。**

---

## 1. 当前项目真实状态

### 1.1 已经真正落地的部分

当前 Android 项目已经具备一批有价值的底层基础设施，这些代码应继续复用，而不是推倒重来。

#### Cloud Auth / Session

已存在：

- `lifetrace-execute-android` AppId；
- LifeTrace Cloud 登录、刷新、退出；
- access token / refresh token；
- Android Keystore + AES-GCM 会话保护；
- 安装级稳定 `deviceId`；
- HTTPS-only Cloud origin；
- Sync capabilities / schema capability 校验；
- 登录后首次 Task Sync 调度。

这部分已经超过 UI 原型阶段，是正式基础设施。

#### Room / Local-first 基础

当前 Room 数据库包含：

```text
tasks
sync_outbox
sync_state
sync_conflicts
```

Task 写入与 Outbox 已使用同一事务，这是正确的 Local-first 方向。

#### Task 第一条正式纵向链

Task 当前已经具备：

- Room Flow 列表；
- 新建 / 编辑 / 删除；
- 标题 / 描述；
- TODO / 进行中 / 等待 / 已完成；
- 优先级；
- `scheduledAt` / `dueAt`；
- 本地时间选择、UTC Instant 持久化；
- 搜索 / 筛选；
- 本地写入后进入 Outbox；
- Snapshot / Push / Pull；
- accepted / duplicate / rejected；
- conflict 持久化；
- tombstone 下行删除；
- WorkManager 后台同步；
- 手动同步。

这部分是当前产品中唯一接近“真实业务模块”的能力。

#### Task 冲突基础设施

当前代码已经存在：

```text
TaskConflictResolver.kt
TaskConflictResolutionSheet.kt
TasksViewModel.keepServer()
TasksViewModel.keepLocal()
```

但 `TasksScreen` 目前没有真正打开 `TaskConflictResolutionSheet`，因此当前仍属于“后台逻辑存在、用户闭环未完成”。

#### CI

当前 `Android CI` 已对最新 `main` 提交执行：

```text
assembleDebug
testDebugUnitTest
lintDebug
```

最新 `b190bce...` workflow 成功。

但需要特别说明：当前仓库没有 `app/src/test`，因此 `testDebugUnitTest` 成功目前主要表示 Gradle 测试任务可执行，**不能等同于业务逻辑已经被单元测试覆盖**。

---

### 1.2 当前仍属于 UI 外壳的模块

#### Today

当前 Today 页面仍使用：

- 固定用户名 `Alex`；
- 固定日期；
- 固定统计数字；
- `MockData.timeline`；
- `MockData.todayTasks`。

所以 Today 目前不是“今日执行中心”，只是视觉稿。

#### Projects

当前 Projects：

- 数据来自 `MockData.projects`；
- “新建项目”按钮 `onClick = {}`；
- 没有 Project Domain；
- 没有 Project Room Entity；
- 没有 Project Repository；
- 没有 Project ViewModel；
- 没有 Project Sync。

因此 Project 当前基本为纯外壳。

#### Calendar

当前 Calendar：

- 固定显示 `2026年8月`；
- 简单 `(1..31)` 生成日期；
- 不处理真实星期偏移；
- 不支持月份切换；
- 日程仍来自 `MockData.timeline`；
- 没有 CalendarEvent / ImportantDate 数据层。

所以当前不是实际可使用的日历。

#### Collection

当前 Collection：

- 文本 / 图片 / 语音 / 链接 / 文件 / 想法入口全部为空点击；
- 收集箱数字来自 `MockData.captureBuckets`；
- 没有 memo / collection 本地实体；
- 没有持久化与同步。

因此当前完全不能执行真实“收集”。

#### Review

当前 Review：

- 输入只保存在 Compose `remember`；
- 点击“完成复盘”直接返回；
- 不写数据库；
- 不同步；
- 完成任务数字为固定值；
- 心情评分图标没有可选择状态。

因此 Review 当前数据会随页面销毁而丢失。

#### Profile

Cloud Connection 是真实功能，但 Profile 本身：

- 头像、用户名、邮箱仍为静态样例；
- 大多数设置卡片没有真实导航或操作；
- 设备管理、通知设置、数据管理等仍为空壳。

---

## 2. 当前完成度判断

以下为工程判断，不是自动统计指标。

| 模块 | 当前状态 | 判断 |
| --- | --- | --- |
| Cloud Auth / Secure Session | 已有正式代码 | 基础可用，需 E2E |
| Local-first / Outbox | Task 已落地 | 架构方向正确 |
| Task CRUD | 已进入正式链路 | 中高完成度 |
| Task Conflict | 数据层基本完成 | UI 未闭环 |
| Project | Mock UI | 基本未实现 |
| Today | Mock 聚合页 | 基本未实现 |
| Calendar | 静态月历 | 基本未实现 |
| Important Date | Web 设计 + Cloud entity | Android 未实现 |
| Collection | Mock UI | 未实现 |
| Review | 临时表单 | 未实现 |
| Pomodoro | Web 设计 + Cloud entity | Android 未实现 |
| Profile / Device | Cloud 入口真实，其余大多静态 | 低完成度 |
| Unit Tests | 无 `app/src/test` | 未建立有效测试基线 |
| Release | CI Debug Gate 有效 | 未达到发布条件 |

当前项目更准确的描述应是：

> **“底层 Auth / Sync 基础设施 + Task 单模块原型已经成型，但产品级基础功能尚未形成。”**

不能因为五个一级页面都能打开，就把这些页面计入实际功能完成度。

---

## 3. 基础可用版本 Definition of Done

本轮不以“所有高级需求一次做完”为目标，而是先做出一个**每天可以真实使用**的版本。

基础可用版本必须满足：

### 3.1 业务层

1. 用户可以真实创建、修改、完成、删除任务；
2. 用户可以真实创建项目，并把任务归属项目；
3. “今天”展示真实的今日任务、逾期任务、今日安排，而不是 MockData；
4. 日历能显示真实月份和真实日期，并展示任务/事件；
5. 用户至少能进行文本、想法、链接三类快速收集；
6. 每日复盘可以保存，并能再次读取；
7. Task 冲突可以由用户选择“接受云端”或“保留本地”；
8. 基础提醒可以产生 Android 通知；
9. 重要日期可以新增、编辑、删除并出现在日历；
10. 番茄钟至少完成开始、暂停、恢复、重置、后台恢复与完成记录。

### 3.2 数据层

上述所有核心业务都必须满足：

```text
Domain Model
    ↓
Room Entity / DAO
    ↓
Repository
    ↓
ViewModel
    ↓
Compose UI
```

需要云同步的实体继续增加：

```text
Repository Local Write
    ↓ 同一事务
Entity + Sync Outbox
    ↓
Sync Engine
    ↓
LifeTrace Cloud
```

### 3.3 质量层

- 生产路径禁止依赖 `MockData`；
- 核心按钮禁止空 `onClick = {}`；
- App 重启后数据不丢失；
- 首次登录成功后，断网仍能操作核心数据；
- 网络恢复后自动同步；
- 至少建立真实 Repository / Mapper / Conflict / Date Logic 单测；
- CI 必须运行到真实测试，不允许“0 个业务测试但显示 PASS”作为完成证据。

---

## 4. 执行原则

### 原则 1：停止继续铺 UI 外壳

在 Foundation Gate 完成前，不再新增只有视觉效果的新页面。

每完成一个模块，必须同时补齐数据和行为闭环。

### 原则 2：第二个业务实体上线前先把 Sync 从 Task 专用改成可扩展

当前 `TaskSyncCoordinator` 将：

- entity type；
- snapshot；
- pull apply；
- accepted apply；
- conflict；

全部硬编码为 `execution.task`。

如果直接复制一份 `ProjectSyncCoordinator`、`MemoSyncCoordinator`，很快会出现大量重复和一致性问题。

因此 Project 正式同步前，先抽取：

```text
SyncEngine
├── SyncScope / EntityType
├── SyncEntityHandler
├── Outbox Push
├── Pull
├── Snapshot
├── Conflict Store
└── Retry Policy

TaskSyncHandler
ProjectSyncHandler
CalendarSyncHandler
MemoSyncHandler
ReviewSyncHandler
```

Outbox / SyncState / SyncConflict 继续共享。

### 原则 3：不把 UI 状态当业务数据

`remember { mutableStateOf(...) }` 只能用于暂时的界面状态。

以下内容必须进入 Repository / Room：

- 任务；
- 项目；
- 日程；
- 重要日期；
- 收集内容；
- 复盘；
- FocusSession；
- 用户业务偏好。

### 原则 4：所有“可点击”必须有结果

如果功能未实现：

- 不允许保留看似可用但无响应的按钮；
- 可以显示“后续版本”或 disabled 状态；
- 核心流程按钮必须真实工作。

---

## 5. Phase F0：修正当前 Task 闭环 + 建立测试基线

这是第一优先级，不新增新模块。

### F0-1 接通 Task Conflict UI

需要完成：

- `TasksScreen` 增加冲突处理入口；
- 打开现有 `TaskConflictResolutionSheet`；
- 连接 `keepServer()`；
- 连接 `keepLocal()`；
- 冲突处理完成后 UI 自动刷新；
- 保留本地后自动 enqueue sync；
- 无冲突时入口隐藏。

验收：

```text
A 设备修改任务
B 设备修改同一任务并先同步
A 再同步
→ 出现冲突
→ 用户可以选择接受云端或保留本地
→ 处理后该实体不再 blocked
```

### F0-2 建立真实单元测试目录

新增：

```text
app/src/test/java/com/lifetrace/execute/
```

第一批必须测试：

- `TaskWireMapper` serialize / deserialize；
- nullable 字段；
- status / priority wire value；
- `TaskRepository` create / update / delete + Outbox；
- Task localVersion 递增；
- conflict keepServer；
- conflict keepLocal rebase；
- accepted 后下一条 Outbox rebase；
- 日期/时区辅助逻辑。

需要使用 Room in-memory test DB 或 repository fake DAO，确保测试真正验证本地事务行为。

### F0-3 文档状态校准

同步修复：

- README 中过时的 CI 描述；
- PROJECT_STATUS 中冲突 UI 状态；
- IMPLEMENTATION_LOG 中已实现但未接通的能力。

#### F0 Gate

只有以下全部满足才进入 Project：

- Task 冲突 UI 可操作；
- 至少存在真实业务单测；
- assembleDebug PASS；
- testDebugUnitTest PASS；
- lintDebug PASS；
- Task CRUD / offline / conflict smoke PASS。

---

## 6. Phase F1：抽取可扩展 Sync Core

目标：避免每新增一个实体复制整套 Task 同步器。

### 6.1 目标结构

建议：

```text
data/sync/
├── SyncEngine.kt
├── SyncEntityHandler.kt
├── SyncRegistry.kt
├── SyncScheduler.kt
├── SyncWorker.kt
├── ConflictRepository.kt
└── handlers/
    └── TaskSyncHandler.kt
```

### 6.2 `SyncEntityHandler` 职责

每个业务实体只负责：

- `entityType`；
- snapshot item 如何写入 Room；
- pull upsert 如何写入 Room；
- pull delete 如何删除本地；
- accepted 如何更新 `serverVersion`；
- rebase 时如何重新生成 payload。

通用 Sync Engine 负责：

- session；
- push batching；
- retry；
- snapshot pagination；
- cursor；
- conflict 持久化；
- rejected blocked；
- WorkManager。

### 6.3 Scope 策略

不要在全模块完成前直接使用一个“全 execution cursor”。

可以继续使用实体 scope：

```text
entities:execution.task
entities:execution.project
entities:execution.calendar_event
entities:execution.important_date
entities:execution.memo
...
```

等实体处理器全部稳定后，再评估合并 scope。

#### F1 Gate

- Task 行为与重构前一致；
- Task Snapshot / Push / Pull / Conflict tests 继续通过；
- 新增第二个 Handler 不需要复制整个 Coordinator。

---

## 7. Phase F2：Project 完整纵向实现

Project 是 Task 之后最优先的业务实体，因为 Task 已经预留 `projectId`。

### 7.1 Domain

建议最小模型：

```text
ExecutionProject
- id
- userId
- title
- description
- status: active / paused / done / archived
- startAt?
- dueAt?
- createdAt
- updatedAt
- localVersion
- serverVersion?
- modifiedByDevice?
```

### 7.2 Room

新增：

```text
projects
```

并升级 Room database version，建立正式 migration。

从这一阶段开始禁止通过 destructive migration 逃避 schema migration。

### 7.3 Repository

实现：

- observeProjects；
- createProject；
- updateProject；
- pause / resume；
- complete；
- archive；
- delete；
- Project + Outbox 同事务。

### 7.4 UI

替换 `MockData.projects`：

- 项目列表来自 Room Flow；
- 新建项目可用；
- 编辑项目可用；
- 状态筛选可用；
- 项目详情显示真实任务；
- 项目进度从 `linkedTasks.done / linkedTasks.total` 计算，不使用静态 Float。

### 7.5 Task 联动

Task 编辑页增加 Project 选择：

- 可选“无项目”；
- 可选 active project；
- 修改后更新 Task Outbox dependencies；
- Project 删除/归档时定义任务关系处理策略。

#### F2 Gate

```text
新建 Project
→ 新建 Task 并选择 Project
→ Project 页面看到该 Task
→ 完成 Task
→ Project 进度实时变化
→ App 重启数据仍存在
→ 断网修改后恢复网络可同步
```

---

## 8. Phase F3：把 Today 变成真实执行中心

Today 不能单独维护一套数据，必须是现有业务数据的聚合视图。

### 8.1 Today Query

至少聚合：

- `scheduledAt` 在今天的任务；
- `dueAt` 在今天的任务；
- 已逾期未完成任务；
- IN_PROGRESS 任务；
- 今日 calendar events；
- 今日 important dates；
- active projects 摘要。

### 8.2 去除静态内容

删除生产路径中的：

- 固定 `Alex`；
- 固定 `8月27日`；
- 固定 `8 / 3 / 2`；
- `MockData.timeline`；
- `MockData.todayTasks`。

用户名读取当前 Cloud session；日期读取系统时间与用户时区。

### 8.3 Today 交互

用户必须可以直接在 Today：

- 完成任务；
- 打开任务详情；
- 查看逾期；
- 进入项目；
- 进入日历事件；
- 进入每日复盘。

#### F3 Gate

Today 页所有数字和列表都能通过真实 Room 数据变化而实时更新。

---

## 9. Phase F4：真实 Calendar + Important Date

### 9.1 Calendar 基础

使用 `java.time.YearMonth` 正确生成月历：

- 正确星期偏移；
- 上月 / 下月；
- 回到今天；
- 选中日期；
- 当前日期高亮；
- 每日事件数量/标记。

### 9.2 Calendar Event

新增正式：

```text
ExecutionCalendarEvent
CalendarEventEntity
CalendarRepository
CalendarViewModel
```

基础功能：

- 新建事件；
- 标题；
- 开始/结束时间；
- 全天；
- 描述；
- 编辑；
- 删除；
- 离线；
- Cloud Sync。

### 9.3 Important Date

实现 `execution.important_date`：

- once / yearly；
- solar / lunar；
- birthday / anniversary / milestone / other；
- 新建 / 编辑 / 删除；
- 显示下一次发生日期；
- 与日历聚合；
- 提醒。

农历转换必须使用经过验证的实现，并补边界测试，不允许使用浏览器原型中的占位转换逻辑。

#### F4 Gate

用户可以：

```text
打开真实当前月
→ 新建日程
→ 新建生日
→ 在对应日期看到标记
→ 重启 App 数据仍存在
→ 第二设备同步后可看到相同内容
```

---

## 10. Phase F5：Collection 最小可用闭环

第一版先做高频、低权限的三种类型：

- 文本；
- 想法；
- 链接。

图片 / 文件 / 语音放到基础闭环之后，不允许因为这些复杂能力阻塞“收集”本身可用。

### 10.1 数据模型

优先复用 Cloud `execution.memo`。

最小字段：

```text
id
userId
type: text / idea / link
title?
content
sourceUrl?
createdAt
updatedAt
localVersion
serverVersion
modifiedByDevice
```

### 10.2 UI

- 快速收集按钮真实打开输入；
- 保存后立即进入 Inbox；
- Inbox 展示真实条目；
- 支持编辑；
- 支持删除；
- 支持类型筛选；
- 空状态真实。

#### F5 Gate

飞行模式可以新增文本、关闭 App、重新打开后仍存在；恢复网络后可同步。

---

## 11. Phase F6：Review 真实持久化

Review 不再使用只存在于 `remember` 的文本。

### 11.1 数据

复用 LifeTrace 的 daily review 能力，字段至少包含：

- reviewDate；
- reflection；
- learning；
- tomorrowPlan；
- moodScore；
- completedTaskCount；
- totalTaskCount；
- createdAt / updatedAt。

任务完成数量必须从真实 Task 查询生成。

### 11.2 UI

- 自动加载当天已保存 Review；
- 编辑自动保留草稿或明确保存；
- 心情 1-5 可选择；
- 点击完成真实写入 DB；
- 可以查看历史复盘列表。

#### F6 Gate

完成 Review 后杀进程重开，内容仍然存在并可再次编辑。

---

## 12. Phase F7：Reminder + Pomodoro

### 12.1 Task Reminder

先实现基础可靠提醒：

- Android 13+ 通知权限；
- NotificationChannel；
- Task Reminder 本地实体；
- 创建/修改任务时调度提醒；
- 删除/完成任务时取消对应提醒；
- 设备重启/时间变化后重新调度。

第一版可以接受系统调度的非严格毫秒级精度；是否申请 Exact Alarm 权限单独评估，不把高权限作为基础版本前置条件。

### 12.2 Pomodoro

不能只做 Compose 倒计时。

状态模型至少持久化：

```text
mode
focusSeconds
breakSeconds
phase
startedAt
pausedAt?
remainingSecondsWhenPaused?
linkedTaskId?
round
```

计时必须基于真实时间差计算，而不是依赖每秒 coroutine 累减作为唯一事实来源。

支持：

- 25/5；
- 50/10；
- 开始；
- 暂停；
- 恢复；
- 重置；
- 关联任务；
- 页面切换不丢；
- 进程恢复；
- 完成通知；
- 写入 `execution.focus_session`。

#### F7 Gate

```text
开始 25 分钟专注
→ 切到其他页面
→ App 退后台
→ 再次打开
→ 剩余时间正确
→ 到时产生通知
→ FocusSession 被保存
```

---

## 13. Phase F8：Profile / Device / Sync 可观测性

Profile 不需要一次实现全部设置，但不能继续展示大量假入口。

基础版本保留真实功能：

- 当前用户 display name / email；
- Cloud 连接状态；
- 当前设备 ID / device name；
- 最近同步时间；
- pending outbox 数；
- blocked 数；
- conflicts 数；
- 手动同步；
- logout。

如果 Cloud Device API 已稳定，再加入：

- 设备列表；
- 撤销设备 session。

尚未实现的“主题、语言、隐私导出”等入口必须明确 disabled / coming later，不能伪装成可操作页面。

---

## 14. 测试策略

### 14.1 Unit Tests

必须覆盖：

- wire mapper；
- Repository transaction；
- Outbox；
- conflict resolution；
- Sync handler；
- recurrence；
- YearMonth calendar generation；
- important date occurrence；
- pomodoro time recovery。

### 14.2 Instrumentation / UI Smoke

至少建立：

- Task 新建；
- Project 新建；
- Task 选择 Project；
- Today 出现 Task；
- Collection 保存；
- Review 保存。

### 14.3 Sync E2E

正式 Release 前必须执行：

1. A 新建 → B pull；
2. B 修改 → A pull；
3. 离线 A 修改 → 恢复网络；
4. duplicate changeId；
5. A/B 并发修改 conflict；
6. delete tombstone；
7. snapshot rebuild；
8. cursor expired / snapshot required；
9. 100+ changes batching；
10. 多实体同时 pending outbox。

---

## 15. 推荐开发批次

不要一次开 8 个模块并行铺 UI。

推荐按以下批次推进：

```text
Batch 1
F0 Task 闭环 + Test Baseline

Batch 2
F1 Generic Sync Core

Batch 3
F2 Project + Task Project Relation

Batch 4
F3 Today Real Data

Batch 5
F4 Calendar + ImportantDate

Batch 6
F5 Collection

Batch 7
F6 Review

Batch 8
F7 Reminder + Pomodoro

Batch 9
F8 Profile / Sync Observability

Batch 10
Full E2E + Release
```

每个 Batch 必须独立达到：

```text
compile
+ unit test
+ lint
+ local persistence
+ offline smoke
+ sync smoke（如果该模块需要云同步）
+ docs update
```

前一 Batch 未达到 Gate，不进入下一批。

---

## 16. Foundation Release Gate

只有以下全部满足，才能把项目状态从“开发原型”改成“基础可用版本”。

### Core Data

- [ ] 生产路径无 `MockData`；
- [ ] 核心可点击控件无空 `onClick`；
- [ ] Task 正式 CRUD；
- [ ] Project 正式 CRUD；
- [ ] Calendar Event 正式 CRUD；
- [ ] Important Date 正式 CRUD；
- [ ] Collection text/idea/link 正式 CRUD；
- [ ] Daily Review 正式持久化；

### Execution

- [ ] Today 使用真实数据；
- [ ] Task Project 归属；
- [ ] Reminder 通知；
- [ ] Pomodoro 进程恢复；
- [ ] FocusSession 保存；

### Sync

- [ ] Sync Core 不再只支持 Task；
- [ ] Project / Calendar / ImportantDate / Memo / Review 可同步；
- [ ] conflict UI 可处理；
- [ ] 双设备 E2E；
- [ ] 离线恢复；
- [ ] tombstone；
- [ ] snapshot rebuild；

### Quality

- [ ] 存在真实 Unit Tests；
- [ ] assembleDebug PASS；
- [ ] testDebugUnitTest PASS；
- [ ] lintDebug PASS；
- [ ] 关键 UI smoke PASS；
- [ ] Room migration test PASS；
- [ ] 文档状态与代码一致。

---

## 17. 当前立即开始的任务

按优先级：

1. 将现有 `TaskConflictResolutionSheet` 真正接入 `TasksScreen`；
2. 建立 `app/src/test` 和第一批 Task 数据层测试；
3. 修复 README / PROJECT_STATUS 文档漂移；
4. 抽取 Task 专用 SyncCoordinator 为可注册 Handler 的 Sync Core；
5. 建立 Project Domain / Room / Repository / UI / Sync；
6. Task 编辑页接 Project 选择；
7. Today 改为真实 Repository 聚合；
8. 再进入 Calendar / Collection / Review。

这 8 项完成后，LifeTrace Execute 才会从“Task Demo + 多页面外壳”进入真正的产品开发阶段。
