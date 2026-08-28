# LifeTrace Execute 项目进度

更新时间：2026-08-28

## 1. 当前阶段

LifeTrace Execute 已从“纯 UI / Mock 原型”进入 **正式 Android Local-first 数据链实施阶段**。

当前两套载体仍保留：

- `web-preview/`：高保真设计和交互评审基线；
- `app/`：正式 Android Compose 客户端。

正式云端继续复用 `zhouxingxing1279/LifeTrace` 的 Rust + Axum + PostgreSQL Cloud，不建设第二套 Execute 后端。

当前工程阶段可概括为：

```text
浏览器设计：核心信息架构已建立
Android UI：核心页面已建立
Cloud Auth：已接入
Room / Repository：Task 已接入
Sync v1：Task 第一条纵向链已实现
全模块云端化：未完成
真实 Android 构建：待 CI 验证
```

## 2. 信息架构保护状态

以下能力全部继续保留：

- 底部：今天 / 任务 / 项目 / 日历 / 收集；
- 我的：右上角头像进入；
- 今日复盘：今天页进入；
- 番茄时钟：任务页；
- 重要日期：日历页。

本轮数据层重构没有删除 Project、Collection、Review、Profile 等原有页面和入口。

## 3. 浏览器高保真设计

状态：**主要设计基线已完成。**

已经覆盖：

- 今天；
- 任务；
- 项目；
- 日历；
- 收集；
- 我的；
- 今日复盘；
- 重要日期；
- 公历 / 农历输入；
- 番茄时钟；
- 25/5、50/10；
- 任务关联；
- 浏览器内真实倒计时。

浏览器版本仍只承担设计验证。农历正式换算、Android 后台番茄计时不能直接照搬浏览器原型实现。

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

状态：**开发中，核心登录链已实现。**

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
- 401 access-token-expired 时受控刷新；
- Scope 校验；
- Sync protocol / schema capabilities 校验；
- Cloud 页面显示账号、Scope、Sync/Schema 版本；
- Cloud 页面提供“立即同步任务”。

待完成：

- 设备列表与撤销 UI；
- Session 管理；
- 更完整的认证错误 UX；
- 自动网络恢复同步；
- WorkManager。

### 4.3 Task Domain / Room / Repository

状态：**第一条正式业务链已接通代码。**

已完成：

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
- 优先级；
- 完成 / 恢复；
- 删除；
- 搜索；
- 状态筛选；
- 空状态；
- 未登录引导 Cloud；
- 手动同步。

待完成：

- 任务详情；
- 编辑标题/描述；
- dueAt 输入；
- scheduledAt；
- Reminder；
- Project 选择；
- WAITING / IN_PROGRESS 显式状态操作；
- 重复任务 / occurrence；
- 冲突解决 UI。

### 4.4 Task Sync Coordinator

状态：**协议处理代码已实现，真实 Cloud E2E 待验证。**

已有流程：

```text
首次设备
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
```

当前实现特性：

- Task 独立 scope cursor；
- Snapshot 分页状态；
- Push 批处理；
- Pull cursor 原子持久化；
- accepted 更新 serverVersion；
- duplicate 按成功；
- conflict 写 `sync_conflicts`；
- 同一实体冲突后阻塞后续 Outbox；
- rejected 标记 blocked，禁止无限重试；
- tombstone 下行删除；
- 网络/API 失败保留 Outbox；
- 同一实体离线连续多次编辑只发送队头；
- accepted 后下一条未尝试 change rebase 到最新 serverVersion。

尚未完成的正式验收：

- 两台真实设备；
- 同 changeId 重放；
- A/B 并发修改；
- 离线删除 / 旧设备重新上线；
- cursor expired；
- snapshot required；
- 100+ changes；
- 网络恢复自动触发。

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
- execution 大部分核心实体注册。

2026-08-27 已新增：

- `AppId::EXECUTE_ANDROID = lifetrace-execute-android`；
- Execute Android `supported_app()`；
- Execute Android最小必要 Scope：account / devices / sync / execution / habits / reviews / files。

### 仍需处理

1. `AuthService::capabilities()` 的 `supportedApps` 信息列表仍需显式加入 Execute Android；登录授权本身已支持。
2. execution 域目前多数仍使用 RegisteredJson，正式 1.0 前要升级为强类型 DTO / Schema。
3. 注册：
   - `execution.important_date`
   - `execution.focus_session`
4. 执行域契约生成物与 contract tests。

## 6. CI / 构建状态

已添加：

```text
.github/workflows/android-ci.yml
```

目标 Gate：

```text
JDK 17
Gradle 8.10.2
:app:assembleDebug
:app:testDebugUnitTest
:app:lintDebug
```

说明：仓库当前仍没有 Gradle Wrapper，因此 CI 明确安装固定 Gradle 版本，不依赖开发机全局 Gradle。

截至 2026-08-28，GitHub API 尚未返回该仓库的 Actions workflow run，因此目前只能标记：

**CI 配置已提交 / 真实 Android 编译结果待确认。**

不能标记“构建通过”。

## 7. 需求状态

| 需求 | 浏览器 | Android | Cloud |
| --- | --- | --- | --- |
| 一级导航 | 已完成 | 已完成 | N/A |
| 功能保护 | 已确认 | 持续约束 | N/A |
| 基础 Task CRUD | 已设计 | 开发中，Local-first 已接 | execution.task 已有 |
| Task Sync | N/A | 开发中 | 服务端协议已有 |
| 番茄时钟 | 已设计 | 待实现 | focus_session 待注册 |
| 重要日期 | 已设计 | 待实现 | important_date 待注册 |
| Project | 已设计 | UI/Mock | execution.project 已有 |
| Calendar | 已设计 | UI/Mock | calendar_event 已有 |
| Collection | 已设计 | UI/Mock | memo/file 能力已有 |
| Review | 已设计 | UI/Mock | review.daily / weekly_review 可复用 |

## 8. 下一批执行顺序

1. 获取 Android CI 第一次真实构建结果并修复所有 compile/lint 错误。
2. 完善 Task 编辑、状态流转、截止日期和 Reminder。
3. 增加冲突列表与“保留云端 / 保留本地”处理。
4. 增加自动 Sync Coordinator 触发与 WorkManager。
5. 修 LifeTrace Auth capabilities Execute AppId 展示。
6. 建 execution typed contracts。
7. 新增 `execution.important_date` / `execution.focus_session`。
8. 将 Project 迁移到 Room / Repository / Outbox / Sync。
9. 将 Calendar + ImportantDate 迁移。
10. 实现 Android 番茄后台可靠计时。
11. 迁移 Collection / Review / Profile 数据。
12. 双设备与 Release Gate。

## 9. 当前判定

当前项目还不能称为“完成”或“可发布”。

但相比上一阶段，已经完成了关键架构转折：

**Task 不再只是 UI Mock，而是已经进入正式 Domain → Room → Outbox → Sync v1 → LifeTrace Cloud 的生产架构。**
