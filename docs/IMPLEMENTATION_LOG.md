# LifeTrace Execute 工程实施记录

更新时间：2026-08-28

> 本文档只记录已经提交到代码仓的实现事实、验证证据和剩余阻塞。设计意图看 `REQUIREMENTS.md`，完整计划看 `EXECUTION_PLAN.md`。

## 2026-08-27 ～ 2026-08-28：Local-first / Cloud 第一阶段

### LifeTrace 主仓库

仓库：`zhouxingxing1279/LifeTrace`

已提交：

- `9180525c20d7233cdce7118a4c6d3425d3276fb5`
  - 新增 `lifetrace-execute-android` AppId。
- `ded783a0848fde553155f5ace5a2ffa966cfbf3a`
  - Execute Android 进入 supported app 授权逻辑；
  - 授予 account / devices / sync / execution / habits / reviews / files 所需权限。

已确认但未完成：

- `AuthService::capabilities()` 的信息性 `supportedApps` 硬编码列表仍需补 Execute；
- execution payload 仍大量使用 RegisteredJson；
- `execution.important_date` 未注册；
- `execution.focus_session` 未注册。

### LifeTrace Execute Android

仓库：`zhouxingxing1279/LifeTrace-excute`

#### Cloud Auth

已实现代码：

```text
core/cloud/
├── CloudContract.kt
├── CloudHttpTransport.kt
├── CloudSessionManager.kt
├── DeviceIdentityStore.kt
├── LifeTraceCloudClient.kt
├── LifeTraceSyncClient.kt
├── SecureSessionStore.kt
└── SyncModels.kt
```

功能：

- HTTPS-only Cloud origin；
- login / refresh / logout；
- Execute AppId；
- scope 校验；
- Sync capabilities；
- Keystore AES-GCM；
- access-token-expired 单次刷新重放。

#### Room / Outbox

已实现：

```text
data/local/
├── LifeTraceExecuteDatabase.kt
├── LifeTraceExecuteDao.kt
├── TaskEntity.kt
└── SyncEntities.kt
```

表：

- `tasks`
- `sync_outbox`
- `sync_state`
- `sync_conflicts`

关键约束：任务写入与 Outbox 同事务。

#### Task Repository

已实现：

```text
domain/task/ExecutionTask.kt
data/repository/TaskRepository.kt
data/repository/TaskWireMapper.kt
```

任务支持：

- create；
- update；
- complete / reopen；
- delete；
- serverVersion；
- localVersion；
- project dependency；
- sync payload 映射。

#### Task Sync

已实现：

```text
data/sync/TaskSyncCoordinator.kt
```

覆盖：

- task-scoped snapshot；
- task-scoped cursor；
- Outbox push；
- pull；
- accepted；
- duplicate；
- rejected blocked；
- conflict persistence；
- tombstone delete；
- refresh-token session manager；
- 同实体离线连续修改串行发送；
- accepted 后下一 change rebase。

#### Task UI

已实现：

```text
presentation/tasks/TasksViewModel.kt
ui/screens/TasksScreen.kt
```

正式运行时：

- 不再读取 `MockData.todayTasks`；
- Room Flow 驱动列表；
- 搜索；
- 状态筛选；
- 新建 Bottom Sheet；
- 优先级；
- 完成 / 恢复；
- 删除；
- 手动同步；
- Cloud 未连接引导。

Preview 仍允许使用独立样例数据，但不会作为生产数据源。

#### Cloud UI

`CloudConnectionScreen` 已新增“立即同步任务”，会调用正式 TaskSyncCoordinator 并展示 Snapshot / Push / Pull / Conflict / Rejected 数量。

#### CI

新增：

```text
.github/workflows/android-ci.yml
```

固定：

- JDK 17；
- Gradle 8.10.2；
- assembleDebug；
- testDebugUnitTest；
- lintDebug。

当前验证结论：

- CI 配置：已提交；
- Workflow run：尚未从 GitHub API 观察到；
- Android Build：**未确认通过**。

## 当前禁止提前关闭的 Gate

以下任何一项未验证前，不得在文档中写“已完成”：

1. Android CI 真实 compile/test/lint；
2. 真机或模拟器启动；
3. Cloud 真实账号 login；
4. Task push 到 PostgreSQL；
5. 第二设备 pull；
6. duplicate；
7. conflict；
8. tombstone；
9. snapshot rebuild。

## 下一实施批次

- Task 编辑 / dueAt / scheduledAt / reminder；
- 冲突解决页面；
- WorkManager 自动同步；
- Project Local-first；
- Cloud execution typed DTO；
- ImportantDate / FocusSession；
- Calendar / Pomodoro 正式实现。
