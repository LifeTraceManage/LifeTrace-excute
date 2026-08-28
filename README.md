# LifeTrace Execute

LifeTrace Execute 是 LifeTrace 的独立执行中心 Android 客户端，负责今天、任务、项目、日历、收集与复盘等日常执行场景。

项目采用：

- `web-preview/`：浏览器高保真设计与交互评审基线；
- `app/`：Jetpack Compose 正式 Android 客户端；
- `zhouxingxing1279/LifeTrace`：统一 LifeTrace Cloud，Rust + Axum + PostgreSQL。

正式客户端采用 **Local-first + LifeTrace Cloud Sync**。业务写入先落本机 Room 与 Outbox，网络不是本地执行的前置条件。

> 功能保护规则：新增或重构时不得删除已经确认的项目、收集、复盘、我的、重要日期、番茄钟等能力。允许调整入口，但不能用“简化”为理由移除功能。

## 文档

- [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md)：长期需求 Source of Truth。
- [`docs/EXECUTION_PLAN.md`](docs/EXECUTION_PLAN.md)：完整实施计划、Cloud 架构与 Release Gate。
- [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md)：当前真实完成度与下一阶段。
- [`docs/IMPLEMENTATION_LOG.md`](docs/IMPLEMENTATION_LOG.md)：工程实施记录、提交证据与已知阻塞。
- [`docs/UI_SPEC.md`](docs/UI_SPEC.md)：视觉、导航与组件规范。

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

直接打开：

```text
web-preview/index.html
```

或：

```bash
python -m http.server 8080
```

访问：

```text
http://localhost:8080/web-preview/
```

浏览器版本已经覆盖主导航、今天、任务、项目、日历、收集、我的、复盘，以及重要日期和番茄钟高保真交互。

## Android 当前实现

技术栈：

- Kotlin 2.0.21
- Jetpack Compose / Material 3
- Navigation Compose
- Room / SQLite
- Kotlin Coroutines
- Android Keystore
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

LifeTrace 主仓库已经允许 `lifetrace-execute-android` 登录并授予 Execute 所需执行域权限。

### Tasks 第一条正式纵向链

任务页运行时已经从 `MockData.todayTasks` 切换到 Room / Repository：

- 真实本地任务列表；
- 搜索；
- 全部 / 进行中 / 等待 / 已完成筛选；
- 新建任务 Bottom Sheet；
- 优先级；
- 完成 / 恢复；
- 删除；
- 手动 Cloud 同步；
- 未登录时引导进入 Cloud 连接页。

本地任务写入采用同事务：

```text
Task Entity + Sync Outbox
```

Sync Coordinator 已覆盖：

- 首次 Snapshot；
- cursor Pull；
- Outbox Push；
- `changeId` 幂等结果；
- accepted / duplicate；
- conflict 持久化并阻塞同实体后续变更；
- rejected 阻塞，不无限重试；
- delete tombstone 下行；
- 同一实体离线连续编辑时按队头串行发送并在服务端确认后 rebase；
- Task 使用独立 sync scope cursor，避免未实现模块的数据被错误跳过。

Cloud 页面已经提供“立即同步任务”入口和同步结果摘要。

## LifeTrace Cloud

正式云端仓库：`zhouxingxing1279/LifeTrace`

复用能力：

- Rust + Axum + PostgreSQL；
- Auth v1；
- Device / Session；
- Sync v1 `capabilities / push / pull / snapshot`；
- changeId 幂等；
- server cursor；
- baseServerVersion；
- optimistic conflict；
- tombstone；
- execution task / project / calendar / memo / reminder 等同步实体；
- 文件元数据与 S3 兼容对象存储。

仍需要在主仓库完成：

- Auth capabilities 列表显式加入 Execute Android；
- execution 域从 RegisteredJson 逐步升级为强类型 DTO / Schema；
- 新增 `execution.important_date`；
- 新增 `execution.focus_session`。

## 构建与验证

仓库已经加入：

```text
.github/workflows/android-ci.yml
```

固定环境：

- JDK 17
- Gradle 8.10.2
- `:app:assembleDebug`
- `:app:testDebugUnitTest`
- `:app:lintDebug`

当前仓库仍未提交 Gradle Wrapper。CI 文件已经存在，但截至 2026-08-28 尚未观察到 GitHub Actions 运行记录，因此当前状态是 **“构建 Gate 已配置，真实构建结果待确认”**，不能标记为构建通过。

Compose Preview 文件为：

```text
app/src/main/java/com/lifetrace/execute/ui/PreviewCatalog.kt
```

## 下一阶段

1. 获取并修复第一次真实 Android CI 编译结果；
2. 完成 Task 编辑、截止日期、提醒与冲突解决 UI；
3. 补齐 Cloud Auth capabilities 的 Execute AppId；
4. 加固 LifeTrace execution 强类型契约；
5. 注册 `execution.important_date` / `execution.focus_session`；
6. 将 Project / Calendar / Collection / Review 迁移到同一 Local-first 架构；
7. 实现 Android 重要日期、公农历与提醒；
8. 实现后台可靠番茄计时、通知和 focus_session；
9. 完成双设备、离线、冲突、删除、Snapshot Release Gate。
