# LifeTrace Execute 开发文档指南

本目录保存直接指导 LifeTrace Execute 开发的文档。

目标不是增加文档数量，而是保证后续开发始终围绕真实业务闭环推进，并让新的开发者、Codex 或 Agent 能快速判断：当前做到哪里、接下来做什么、什么标准才算完成。

## 推荐阅读顺序

每次开始新的开发批次时，按以下顺序阅读：

1. [`REQUIREMENTS.md`](REQUIREMENTS.md)
   - 确认长期需求和产品约束；
2. [`FOUNDATION_EXECUTION_PLAN.md`](FOUNDATION_EXECUTION_PLAN.md)
   - 确认当前全功能 1.0 Phase、执行顺序和 Gate；
3. [`PROJECT_STATUS.md`](PROJECT_STATUS.md)
   - 确认哪些是真实实现、哪些仍是 Mock/UI 外壳；
4. [`IMPLEMENTATION_LOG.md`](IMPLEMENTATION_LOG.md)
   - 查看最近已经提交并验证的实现证据；
5. [`EXECUTION_PLAN.md`](EXECUTION_PLAN.md)
   - 需要理解长期架构、Cloud 约束和最终 Release Gate 时阅读；
6. [`UI_SPEC.md`](UI_SPEC.md)
   - 涉及页面、导航、组件、视觉修改时阅读。

## 当前主执行文档

当前开发必须以：

[`FOUNDATION_EXECUTION_PLAN.md`](FOUNDATION_EXECUTION_PLAN.md)

为主要执行依据。

从 2026-08-30 起，该文件不再只是“基础可用版本”计划，而是 **LifeTrace Execute 当前规划内全部功能的 1.0 交付执行计划**。阶段可以分批实现，但最终 Release Gate 不允许裁剪已经确认或已经设计的功能。

## 模块完成标准

任何业务模块只有同时满足下面的纵向链，才可以在 `PROJECT_STATUS.md` 中标记为已实现：

```text
Requirement / Product Rule
    ↓
Domain Model
    ↓
Room Entity / DAO / Migration
    ↓
Repository / UseCase
    ↓
ViewModel / UI State
    ↓
Compose UI
    ↓
Offline behavior
    ↓
Sync / File / Notification（适用时）
    ↓
Automated Tests
    ↓
CI / Smoke / E2E evidence
```

不满足其中关键环节时，只能写成：

- UI 已设计；
- 数据层开发中；
- Sync 待接入；
- E2E 待验证；

不能直接写“已完成”。

## 状态定义

后续文档尽量统一使用以下状态：

| 状态 | 含义 |
| --- | --- |
| `未实现` | 没有正式业务代码 |
| `UI 外壳` | 只有 Compose/Web UI、MockData、静态值或空操作 |
| `开发中` | 已进入正式业务链，但纵向闭环尚未完成 |
| `基础可用` | 本地 CRUD/持久化/核心交互已完成，关键测试通过 |
| `同步可用` | Local-first + Cloud Sync 主链已打通 |
| `已验证` | 对应 CI / E2E / smoke Gate 已有证据 |
| `已完成` | 满足当前版本 Definition of Done，不存在已知阻断项 |

## 开发任务拆分规则

后续不要按“做一个页面”拆任务，优先按“做完一个业务纵向链”拆分。

错误示例：

```text
实现 Project 页面
实现 Calendar 页面
实现 Collection 页面
```

正确示例：

```text
Project Domain + Room + Repository
Project CRUD + Task projectId 归属
Project Local-first Outbox
Project Sync Handler
ProjectsScreen 接真实数据
Project Repository/Sync tests
Project offline/sync smoke
```

这样可以避免页面越来越多，但产品仍然不可使用。

## 当前执行顺序

全功能 1.0 Phase 顺序：

```text
F0   Task 冲突闭环 + 真实测试基线
 ↓
F1   Generic Sync Core + Execution Contracts
 ↓
F2   Project 完整纵向链
 ↓
F3   Task 高级能力
     recurrence / occurrence / waiting / reminder /
     dependency / completion / subtask
 ↓
F4   Calendar + ImportantDate + Reminder / Notification
 ↓
F5   Collection 六类入口 + Tags + Files + Voice
 ↓
F6   Daily Review + Weekly Review
 ↓
F7   Goal / Habit 正式接入
 ↓
F8   Pomodoro / FocusSession
 ↓
F9   Today 最终真实聚合
 ↓
F10  Profile / Devices / Settings / Data
 ↓
F11  全实体 Sync / Offline / E2E / Release
```

具体任务和验收条件以 `FOUNDATION_EXECUTION_PLAN.md` 为准。

## 文档更新约定

### REQUIREMENTS.md

记录产品意图和长期需求，不记录日常 commit 流水账。

### FOUNDATION_EXECUTION_PLAN.md

只在以下情况更新：

- Phase 顺序发生变化；
- 发现新的关键依赖；
- Definition of Done / Gate 需要调整；
- 当前 1.0 交付范围发生明确变化。

### PROJECT_STATUS.md

这是“现在到底做到哪里”的实时文档。

每完成一批经过验证的功能后更新，不允许根据计划预填完成状态。

### IMPLEMENTATION_LOG.md

记录事实证据：

- 实现内容；
- commit / PR；
- 测试与 CI；
- E2E / smoke；
- 已知限制。

### EXECUTION_PLAN.md

保留长期架构、Cloud、测试、性能、安全与 Release 设计。若它与当前具体 Phase 顺序不一致，当前执行顺序以 `FOUNDATION_EXECUTION_PLAN.md` 为准；产品范围冲突仍以 `REQUIREMENTS.md` 为最高依据。

### UI_SPEC.md

页面、导航、组件和视觉交互基线。已经确认的 UI 功能不得在 Android 实现时静默删除。

## 每个开发批次的固定闭环

每个 Batch 至少执行：

```text
代码实现
  ↓
相关 Unit / DB / UI Tests
  ↓
:app:assembleDebug
:app:testDebugUnitTest
:app:lintDebug
  ↓
Phase 特定 Smoke / E2E
  ↓
PROJECT_STATUS.md
  ↓
IMPLEMENTATION_LOG.md
```

只有 Gate 有真实证据后才能进入下一阶段。

## 禁止事项

后续开发禁止：

- 仅因为页面可以打开就标记模块完成；
- 用 Compose `remember` 代替业务数据持久化；
- 用 MockData 填充生产页面并计入完成度；
- 为每个实体复制一份独立 Sync Coordinator；
- 新增数据库字段却不提供 migration；
- CI 中没有真实业务测试却宣称“单测完成”；
- 将图片/文件/语音等已确认能力长期留作假入口；
- 将 recurrence、waiting、reminder、dependency、review、device 等已确认功能以“首版裁剪”跳过；
- 在文档中提前写“已完成”而代码或验证证据尚未满足 Gate。

## Agent / Codex 使用建议

给开发 Agent 分配任务时，应引用明确文档路径，例如：

```text
先阅读：
- docs/README.md
- docs/development/README.md
- docs/development/REQUIREMENTS.md
- docs/development/FOUNDATION_EXECUTION_PLAN.md
- docs/development/PROJECT_STATUS.md

然后从当前未完成的 Phase 开始执行。
不要跳阶段铺 Mock UI。
最终目标是 F0 → F11 全部 Gate 通过，而不是只完成 Foundation 外壳。
完成每个 Batch 后运行对应测试，并更新 PROJECT_STATUS.md 与 IMPLEMENTATION_LOG.md。
```

这样可以减少后续 Agent 因上下文不足而重新走回“先铺页面”或“先做最小版”的旧路线。