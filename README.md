# LifeTrace Execute

LifeTrace Execute 是 LifeTrace 的独立执行中心 Android 客户端，负责今天、任务、项目、日历、收集与复盘等日常执行场景。

项目采用：

- `web-preview/`：浏览器高保真设计与交互评审基线；
- `app/`：Jetpack Compose 正式 Android 客户端；
- `zhouxingxing1279/LifeTrace`：统一 LifeTrace Cloud，Rust + Axum + PostgreSQL。

正式客户端采用 **Local-first + LifeTrace Cloud Sync**。业务写入先落本机 Room 与 Outbox，网络不是本地执行的前置条件。

> 功能保护规则：新增或重构时不得删除已经确认的项目、收集、复盘、我的、重要日期、番茄钟等能力。允许调整入口，但不能用“简化”为理由移除功能。

## 文档入口

后续开发统一从 [`docs/README.md`](docs/README.md) 进入。

开发相关文档已经集中整理到 `docs/development/`：

- [`docs/development/README.md`](docs/development/README.md)：开发文档阅读顺序、状态定义和维护规则；
- [`docs/development/REQUIREMENTS.md`](docs/development/REQUIREMENTS.md)：长期需求 Source of Truth；
- [`docs/development/FOUNDATION_EXECUTION_PLAN.md`](docs/development/FOUNDATION_EXECUTION_PLAN.md)：当前最高优先级的 **全功能 1.0 交付执行文档**，按 Phase Gate 连续推进直到所有已确认功能真实实现；
- [`docs/development/EXECUTION_PLAN.md`](docs/development/EXECUTION_PLAN.md)：完整长期实施计划、Cloud 架构与最终 Release Gate；
- [`docs/development/PROJECT_STATUS.md`](docs/development/PROJECT_STATUS.md)：当前真实完成度与下一阶段；
- [`docs/development/IMPLEMENTATION_LOG.md`](docs/development/IMPLEMENTATION_LOG.md)：工程实施记录、提交证据与已知阻塞；
- [`docs/development/UI_SPEC.md`](docs/development/UI_SPEC.md)：视觉、导航与组件规范。

> 开始新的开发批次前，至少阅读 `docs/README.md`、`docs/development/README.md`、`FOUNDATION_EXECUTION_PLAN.md` 和 `PROJECT_STATUS.md`。

## 固定信息架构

底部一级导航固定：

1. 今天
2. 任务
3. 项目
4. 日历
5. 收集

“我的”通过右上角头像进入；“今日复盘”从今天页进入；“番茄时钟”位于任务页；“重要日期”位于日历页。

## 浏览器高保真预览

```text
web-preview/
├── index.html
├── styles.css
├── app.js
├── features-v3.css
└── features-v3.js
```

浏览器版本已经覆盖主导航、今天、任务、项目、日历、收集、我的、复盘，以及重要日期和番茄钟高保真交互。

> 浏览器预览只作为设计基线，不计入 Android 正式功能完成度。

## Android 当前实现

技术栈：

- Kotlin 2.0.21
- Jetpack Compose / Material 3
- Navigation Compose
- Room / SQLite
- Kotlin Coroutines
- Android Keystore
- WorkManager
- minSdk 26 / targetSdk 35

### 已建立的正式基础设施

```text
Compose UI
    ↓
ViewModel
    ↓
Domain / Repository
    ↓
Room
    ├── tasks
    ├── sync_outbox
    ├── sync_state
    └── sync_conflicts
    ↓
TaskSyncCoordinator
    ↓
LifeTrace Sync v1
    ├── capabilities
    ├── snapshot
    ├── push
    └── pull
    ↓
LifeTrace Cloud / PostgreSQL
```

当前只有 Task 已进入完整数据链；Today / Project / Calendar / Collection / Review / Profile 的大量内容仍为 Mock 或静态 UI。后续完成度统一按 `docs/development/FOUNDATION_EXECUTION_PLAN.md` 的纵向闭环标准判断。

### Cloud Auth

Android 已接入：

- `lifetrace-execute-android` AppId；
- `/api/v1/auth/login`；
- `/api/v1/auth/refresh`；
- `/api/v1/auth/logout`；
- access / refresh token；
- 安装级稳定 `deviceId`；
- Android Keystore + AES-GCM 安全会话存储；
- HTTPS-only Cloud origin；
- access token 过期后受控刷新并最多重放一次；
- execution / sync / account / device 等最小必要 Scope 校验。

### Tasks 第一条正式纵向链

任务页运行时已经从 `MockData.todayTasks` 切换到 Room / Repository：

- 真实本地任务列表；
- 搜索；
- 状态筛选；
- 新建 / 编辑 / 删除；
- 标题 / 描述；
- 优先级；
- 完成 / 恢复；
- `scheduledAt` / `dueAt`；
- Android 日期时间选择；
- Task + Outbox 同事务；
- WorkManager 自动同步；
- 手动 Cloud 同步；
- Snapshot / Push / Pull；
- accepted / duplicate / rejected；
- conflict 持久化；
- tombstone；
- 同实体连续编辑 rebase。

冲突解析的 DAO / Resolver / ViewModel / Bottom Sheet 已存在，但当前 `TasksScreen` 尚未把处理入口完整接通，因此仍需要完成 UI 闭环。

## 构建与验证

工作流：

```text
.github/workflows/android-ci.yml
```

固定环境：

- JDK 17
- Gradle 8.10.2
- `:app:assembleDebug`
- `:app:testDebugUnitTest`
- `:app:lintDebug`

截至 2026-08-28，代码基线提交 `b190bce3e3d3b82b914ddff952bec8c69d59a8ba` 的 Android CI run `33135872033` 已成功完成。

但当前仓库还没有 `app/src/test`，因此下一阶段必须建立真实业务单测，不能只以 Gradle test task PASS 作为质量完成证据。

## 当前最高优先级

按 [`docs/development/FOUNDATION_EXECUTION_PLAN.md`](docs/development/FOUNDATION_EXECUTION_PLAN.md) 连续执行，目标是完成当前规划内全部功能：

1. F0：Task 冲突闭环 + 真实测试基线；
2. F1：Generic Sync Core + Execution Contracts；
3. F2：Project 完整纵向链；
4. F3：Task recurrence / occurrence / waiting / reminder / dependency / completion / subtask；
5. F4：Calendar + Important Date + Reminder / Notification；
6. F5：Collection 六类入口 + Tags + Files + Voice；
7. F6：Daily Review + Weekly Review；
8. F7：Goal / Habit 正式接入；
9. F8：Pomodoro / FocusSession；
10. F9：Today 最终真实聚合；
11. F10：Profile / Devices / Settings / Data；
12. F11：全实体 Sync / Offline / 双设备 E2E / Release。

1.0 的完成定义是：**所有已确认功能均形成真实纵向闭环，生产路径无 MockData、核心按钮无空操作、核心数据可持久化/离线运行、需要同步的实体全部可跨设备同步，并有真实自动化测试与发布证据。**