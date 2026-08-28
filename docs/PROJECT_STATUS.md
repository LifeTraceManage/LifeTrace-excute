# LifeTrace Execute 项目进度

更新时间：2026-08-28

## 1. 当前阶段

LifeTrace Execute 已从“纯 UI / Mock 原型”进入 **Android Local-first + LifeTrace Cloud Sync 第一条正式业务链已可构建阶段**。

当前两套载体继续保留：

- `web-preview/`：高保真设计和交互评审基线；
- `app/`：正式 Android Compose 客户端。

正式云端继续复用 `zhouxingxing1279/LifeTrace` 的 Rust + Axum + PostgreSQL Cloud，不建设第二套 Execute 后端。

当前工程状态：

```text
浏览器设计：核心信息架构已建立
Android UI：核心页面已建立
Cloud Auth：已接入
Room / Repository：Task 已接入
Sync v1：Task 第一条纵向链已实现
后台同步：WorkManager 已接入
Android CI：assembleDebug / unit test / lint 已通过
全模块云端化：未完成
真实双设备 E2E：未完成
```

## 2. 信息架构保护状态

以下能力全部继续保留：

- 底部：今天 / 任务 / 项目 / 日历 / 收集；
- 我的：右上角头像进入；
- 今日复盘：今天页进入；
- 番茄时钟：任务页；
- 重要日期：日历页。

本轮数据层和同步层重构没有删除 Project、Collection、Review、Profile 等原有页面和入口。

## 3. 浏览器高保真设计

状态：**主要设计基线已完成。**

已经覆盖今天、任务、项目、日历、收集、我的、今日复盘、重要日期、公历/农历输入、番茄时钟、25/5、50/10、任务关联以及浏览器内真实倒计时。

浏览器版本只承担设计验证。农历正式换算、Android 后台番茄计时不能直接照搬浏览器原型实现。

## 4. Android Compose

### 4.1 基础 UI

已完成：

- Compose / Material 3；
- Bottom Navigation；
- Today / Tasks / Projects / Calendar / Collection；
- Profile；
- Review；
- Cloud Connection；
- 公共组件；
- `PreviewCatalog.kt` Compose Preview。

### 4.2 Cloud Connection

状态：**核心登录、会话与自动任务同步基础设施已实现。**

已完成：

- `lifetrace-execute-android` AppId；
- HTTPS Cloud origin 校验；
- Auth capabilities 探测；
- `/api/v1/auth/login`；
- `/api/v1/auth/refresh`；
- `/api/v1/auth/logout`；
- access token / refresh token；
- 安装级稳定 deviceId；
- Android Keystore + AES-GCM 会话存储；
- 密码不落盘；
- access token 过期时受控刷新；
- Scope 校验；
- Sync protocol / schema capabilities 校验；
- Cloud 页面显示账号、Scope、Sync/Schema 版本；
- Cloud 页面提供“立即同步任务”；
- 登录成功保存会话后立即 enqueue 首次 Task Sync；
- 首次设备可由 Worker 进入 Snapshot / Push / Pull；
- 本地任务写入后自动 enqueue 同步；
- 每 6 小时周期性兜底同步；
- WorkManager 仅在网络可用时运行；
- retryable / 429 / 5xx / IO 错误受控指数退避；
- 401 / 403 不做无限重试。

待完成：

- 设备列表与撤销 UI；
- Session 管理；
- 更完整的认证错误 UX；
- WorkManager 状态/失败可观测性；
- 真实网络恢复场景 E2E。

### 4.3 Task Domain / Room / Repository

状态：**基础 CRUD 与主要编辑字段已进入正式数据链。**

已实现：

```text
ExecutionTask
TaskEntity
TaskRepository
TaskWireMapper
TasksViewModel
TasksScreen
```

Room 当前表：

```text
tasks
sync_outbox
sync_state
sync_conflicts
```

任务本地写操作采用同一 Room Transaction：

```text
BEGIN
  upsert/delete task
  insert sync_outbox
COMMIT
```

运行时任务页已经移除对 `MockData.todayTasks` 的依赖，正式使用 Room Flow。

已支持：

- 新建任务；
- 查看列表；
- 编辑标题；
- 编辑描述；
- TODO / 进行中 / 等待 / 已完成；
- 低 / 普通 / 高 / 紧急优先级；
- 完成 / 恢复；
- 删除；
- 搜索；
- 状态筛选；
- `scheduledAt`；
- `dueAt`；
- Android 原生日期 / 时间选择器；
- 本地时间展示、UTC Instant 持久化；
- 空状态；
- 未登录引导 Cloud；
- 手动同步；
- 本地写入后后台同步调度。

待完成：

- Reminder；
- Project 选择；
- 重复任务 / occurrence；
- waiting item 完整工作流；
- 冲突解决 UI；
- 任务详情进一步完善。

### 4.4 Task Sync Coordinator

状态：**协议处理代码与 Android 自动调度已实现，真实 Cloud 双设备 E2E 待验收。**

已有流程：

```text
Cloud 登录
  ↓
首次 Sync Worker
  ↓
Snapshot(execution.task)
  ↓
保存 Task scope cursor
  ↓
Outbox Push
  ↓
accepted / duplicate / conflict / rejected
  ↓
Cursor Pull
  ↓
Room

本地修改
  ↓
Task + Outbox 同事务
  ↓
WorkManager（网络约束）
  ↓
TaskSyncCoordinator
```

当前实现特性：

- Task 独立 scope cursor；
- Snapshot 分页状态；
- Push 批处理；
- Pull cursor 原子持久化；
- accepted 更新 serverVersion；
- duplicate 按成功处理；
- conflict 写 `sync_conflicts`；
- 同一实体冲突后阻塞后续 Outbox；
- rejected 标记 blocked，禁止无限重试；
- tombstone 下行删除；
- 网络/API 失败保留 Outbox；
- 同一实体离线连续多次编辑只发送队头；
- accepted 后下一条未尝试 change rebase 到最新 serverVersion；
- 本地写入触发 OneTimeWork；
- 周期兜底同步；
- 登录后首次同步自动排队。

尚未完成的正式验收：

- 两台真实设备；
- 同 changeId 重放；
- A/B 并发修改；
- 离线删除 / 旧设备重新上线；
- cursor expired；
- snapshot required；
- 100+ changes；
- 实际断网 → 修改 → 恢复网络自动同步。

## 5. LifeTrace Cloud 对齐

主仓库：`zhouxingxing1279/LifeTrace`

### 已完成

LifeTrace Cloud 已存在：

- Rust + Axum + PostgreSQL；
- Auth v1；
- Device / Session；
- Sync v1 capabilities / push / pull / snapshot；
- changeId 幂等；
- server cursor；
- optimistic conflict；
- tombstone；
- execution 核心实体注册。

已完成的 Execute 对齐：

- `AppId::EXECUTE_ANDROID = lifetrace-execute-android`；
- Execute Android `supported_app()` 授权；
- Execute Android 最小必要 Scope：account / devices / sync / execution / habits / reviews / files；
- `execution.important_date` 已加入 EntityType / Registry；
- `execution.focus_session` 已加入 EntityType / Registry；
- 两个新实体当前使用 RegisteredJson 接入通用 Sync v1 payload dispatch；
- registry contract tests 已通过。

### 仍需处理

1. `AuthService::capabilities()` 的信息性 `supportedApps` 列表仍需显式加入 Execute Android。
2. execution 域目前多数仍使用 RegisteredJson，正式 1.0 前要升级为强类型 DTO / Schema。
3. 为 `execution.important_date` / `execution.focus_session` 定义强类型字段与 schema tests。
4. 完成主仓库本轮 Cloud / Clippy / Docker / PostgreSQL smoke 全套 CI 结果确认。

## 6. CI / 构建状态

Android CI：

```text
.github/workflows/android-ci.yml
JDK 17
Gradle 8.10.2
:app:assembleDebug
:app:testDebugUnitTest
:app:lintDebug
```

最新已确认代码 Gate：

```text
commit: b139424b855f6ac0bacde9e6728ddd1cdd8ac87e
run:    33134713967

assembleDebug       PASS
testDebugUnitTest   PASS
lintDebug           PASS
workflow            SUCCESS
```

因此 Android 当前状态可以明确标记为：**真实 CI 编译 / 单测 / Lint 已通过。**

仓库仍没有 Gradle Wrapper；CI 使用固定 Gradle 8.10.2 保证可重复构建。

LifeTrace Cloud 新实体提交已观察到 contract tests 通过；完整主仓 workflow 仍在运行，未提前标记全套 Gate 完成。

## 7. 需求状态

| 需求 | 浏览器 | Android | Cloud |
| --- | --- | --- | --- |
| 一级导航 | 已完成 | 已完成 | N/A |
| 功能保护 | 已确认 | 持续约束 | N/A |
| 基础 Task CRUD | 已设计 | 基础 CRUD/编辑已接正式数据层 | execution.task 已有 |
| Task Sync | N/A | Coordinator + WorkManager 已实现，E2E 待验收 | 服务端协议已有 |
| 番茄时钟 | 已设计 | 待正式实现 | focus_session 已注册，typed DTO 待补 |
| 重要日期 | 已设计 | 待正式实现 | important_date 已注册，typed DTO 待补 |
| Project | 已设计 | UI/Mock | execution.project 已有 |
| Calendar | 已设计 | UI/Mock | calendar_event 已有 |
| Collection | 已设计 | UI/Mock | memo/file 能力已有 |
| Review | 已设计 | UI/Mock | review.daily / weekly_review 可复用 |

## 8. 下一批执行顺序

1. 增加 Task 冲突列表与“接受云端 / 保留本地”处理。
2. 完成 Reminder、Project 归属、重复任务与 occurrence。
3. 修 LifeTrace `AuthService::capabilities()` Execute AppId 展示。
4. 为 execution 域建立强类型 DTO / Schema，优先 task / project / important_date / focus_session。
5. 将 Project 迁移到 Room / Repository / Outbox / Sync。
6. 将 Calendar + ImportantDate 迁移到同一 Local-first 链路。
7. 实现 Android 番茄后台可靠计时、通知、进程恢复与 FocusSession。
8. 迁移 Collection / Review / Profile 数据。
9. 完成双设备、离线、冲突、删除、Snapshot E2E。
10. Release Gate、签名 APK 与发布流程。

## 9. 当前判定

当前项目还不能称为“完成”或“可发布”。

但关键基础设施已经形成：

**Task 已进入 Domain → Room → Transactional Outbox → Sync Coordinator → WorkManager → LifeTrace Cloud 的正式生产架构，且 Android 编译、单测和 Lint 已由 CI 验证通过。**
