# LifeTrace Execute 工程实施记录

更新时间：2026-08-28

> 本文档只记录已经提交到代码仓的实现事实、验证证据和剩余阻塞。设计意图看 `REQUIREMENTS.md`，完整计划看 `EXECUTION_PLAN.md`。

## 2026-08-27 ～ 2026-08-28：Local-first / Cloud 第一阶段

### LifeTrace 主仓库

仓库：`zhouxingxing1279/LifeTrace`

已提交：

- `9180525c20d7233cdce7118a4c6d3425d3276fb5`
  - 新增 `AppId::EXECUTE_ANDROID = lifetrace-execute-android`。
- `ded783a0848fde553155f5ace5a2ffa966cfbf3a`
  - Execute Android 进入 supported app 授权逻辑；
  - 授予 account / devices / sync / execution / habits / reviews / files 所需权限。
- `fad49f3e5e3690ab998ff00957249050e2eb64e3`
  - Registry 新增 `execution.important_date`；
  - Registry 新增 `execution.focus_session`；
  - 两者均为 UserOwned / Bidirectional / Optimistic。
- `56e08ed4bf0403e4b0e2c490dcc91ceaadc5697f`
  - generic `EntityPayload` 接受两个新 execution entity；
  - 当前使用 RegisteredJson 进入统一 Sync v1。

验证：

- 本轮 LifeTrace contract tests 已通过；
- Cloud tests / Clippy / Docker / PostgreSQL smoke 所在完整 workflow 在记录本文时仍继续执行，未提前标记全绿。

已确认但未完成：

- `AuthService::capabilities()` 的信息性 `supportedApps` 硬编码列表仍需补 Execute；
- execution payload 仍大量使用 RegisteredJson；
- ImportantDate / FocusSession 强类型 DTO / Schema 尚未定义。

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
- access-token-expired 单次刷新重放；
- 登录成功后保存安全会话；
- 登录后立即 enqueue 首次 Task Sync。

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
- 标题；
- 描述；
- TODO / IN_PROGRESS / WAITING / DONE；
- priority；
- dueAt；
- scheduledAt；
- serverVersion；
- localVersion；
- project dependency 字段；
- sync payload 映射。

#### Task Sync

已实现：

```text
data/sync/
├── TaskSyncCoordinator.kt
├── TaskSyncWorker.kt
└── SyncScheduler.kt
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
- accepted 后下一 change rebase；
- 本地写入后 3 秒防抖式 OneTimeWork；
- NetworkType.CONNECTED 网络约束；
- 每 6 小时周期兜底；
- 登录后的首次立即同步；
- retryable / 429 / 5xx / IOException 指数退避；
- 401 / 403 不无限重试。

#### Task UI

已实现：

```text
presentation/tasks/TasksViewModel.kt
ui/screens/TasksScreen.kt
ui/components/TaskDateTimePicker.kt
```

正式运行时：

- 不再读取 `MockData.todayTasks`；
- Room Flow 驱动列表；
- 搜索；
- 状态筛选；
- 新建 Bottom Sheet；
- 编辑 Bottom Sheet；
- 标题 / 描述；
- TODO / 进行中 / 等待 / 已完成；
- 优先级；
- 完成 / 恢复；
- 删除；
- scheduledAt / dueAt；
- Android 原生日期与时间选择器；
- 本地时区展示；
- UTC Instant 持久化；
- 手动同步；
- Cloud 未连接引导。

任务卡片点击现在进入编辑，不再把整行点击误作为“完成”；完成动作保留在明确的 Checkbox 上。

Preview 仍允许使用独立样例数据，但不会作为生产数据源。

#### Cloud UI

`CloudConnectionScreen` 已提供“立即同步任务”，会调用正式 TaskSyncCoordinator 并展示 Snapshot / Push / Pull / Conflict / Rejected 数量。

登录完成后 UI 会提示首次同步已排队，实际执行由 WorkManager 负责。

#### Android CI

工作流：

```text
.github/workflows/android-ci.yml
```

固定：

- JDK 17；
- Gradle 8.10.2；
- assembleDebug；
- testDebugUnitTest；
- lintDebug。

已验证：

```text
0e0aafbb35ceddbf3508ebce823a70b121567a04
run 33133755880
workflow SUCCESS
```

该 Gate 验证了 WorkManager、本地修改自动 enqueue、Application/Manifest 网络配置等代码。

随后：

```text
b139424b855f6ac0bacde9e6728ddd1cdd8ac87e
run 33134713967
assembleDebug       PASS
testDebugUnitTest   PASS
lintDebug           PASS
workflow            SUCCESS
```

该 Gate 进一步验证了“Cloud 登录后首次同步排队”代码。

因此当前 Android Build 状态为：**真实 CI 已确认通过。**

## 当前禁止提前关闭的 Gate

以下任何一项未验证前，不得在文档中写“产品已完成 / 云同步已最终验收”：

1. 真机或模拟器完整业务启动回归；
2. Cloud 真实账号 login；
3. Task push 到 PostgreSQL；
4. 第二设备 pull；
5. duplicate；
6. conflict；
7. tombstone；
8. snapshot rebuild；
9. 断网修改后恢复网络由 WorkManager 自动上传；
10. Reminder / Project / recurrence 与剩余模块同步。

Android compile/test/lint Gate 已通过，不再属于未验证项。

## 下一实施批次

- Task 冲突解决页面与 server/local resolution；
- Reminder / Project 归属 / recurrence / occurrence；
- Auth capabilities Execute AppId 展示；
- execution 强类型 DTO / Schema；
- Project Local-first；
- ImportantDate / Calendar Local-first；
- Android 后台番茄计时与 FocusSession；
- 双设备真实 E2E；
- Release Gate。
