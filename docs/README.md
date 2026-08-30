# LifeTrace Execute 文档中心

本目录是 LifeTrace Execute 的统一文档入口。

后续开发、Agent 执行、代码评审和版本验收都应先从本文档进入，避免直接依赖聊天记录或记忆中的旧路径。

## 目录结构

```text
docs/
├── README.md
└── development/
    ├── README.md
    ├── REQUIREMENTS.md
    ├── FOUNDATION_EXECUTION_PLAN.md
    ├── PROJECT_STATUS.md
    ├── IMPLEMENTATION_LOG.md
    ├── EXECUTION_PLAN.md
    └── UI_SPEC.md
```

当前文档规模还不大，因此暂时把所有直接指导开发的文档放在同一个 `development/` 目录，避免过度分层导致相对链接、Agent 检索和维护成本增加。

如果后续出现独立 API 契约、ADR、测试规范、发布手册，再新增 `architecture/`、`adr/`、`testing/`、`release/` 等目录。

## 当前开发入口

**当前最高优先级：** [`development/FOUNDATION_EXECUTION_PLAN.md`](development/FOUNDATION_EXECUTION_PLAN.md)

从 2026-08-30 起，该文件定义的是 **LifeTrace Execute 全功能 1.0 的执行顺序和 Release Gate**，不再只以“基础可用版本”为最终目标。

核心原则：

> 一个功能只有完成 Requirement → Domain → Room → Repository → ViewModel → UI → Offline/Sync → Tests → CI/E2E 的纵向闭环，才计为“已实现”。Mock 页面、静态数字、空 `onClick` 和仅浏览器原型不计入正式完成度。

> 阶段可以分批交付，但 `REQUIREMENTS.md`、`UI_SPEC.md`、`EXECUTION_PLAN.md` 中已经确认或已经设计的 1.0 功能，最终不得以“首版先不做”为理由裁剪。

## 文档职责

| 文档 | 职责 | 什么时候读 | 什么时候更新 |
| --- | --- | --- | --- |
| `REQUIREMENTS.md` | 长期产品需求 Source of Truth | 判断“要做什么”时 | 新增/调整/取消需求时 |
| `FOUNDATION_EXECUTION_PLAN.md` | 当前全功能 1.0 执行总纲 | 开始任何新开发任务前 | 1.0 范围、Phase、依赖或 Gate 调整时 |
| `PROJECT_STATUS.md` | 当前真实完成度 | 开发前确认现状 | 一批功能经过验证后 |
| `IMPLEMENTATION_LOG.md` | 已提交实现事实、commit/CI 证据 | 排查历史和确认已做内容时 | 功能提交并验证后 |
| `EXECUTION_PLAN.md` | 长期完整架构计划 | 评估 Cloud、Sync、安全、测试和最终 Release 架构时 | 长期路线或架构调整时 |
| `UI_SPEC.md` | 导航、视觉、功能入口和组件规范 | 修改 Compose/UI 前 | UI 规则发生变化时 |

## 文档优先级

当文档之间出现不一致时，按以下规则处理：

1. **产品范围与需求定义**：`REQUIREMENTS.md` 优先；
2. **当前开发顺序与 1.0 Phase Gate**：`FOUNDATION_EXECUTION_PLAN.md` 优先；
3. **当前实现事实**：以代码 + CI 为最终事实，随后更新 `PROJECT_STATUS.md` / `IMPLEMENTATION_LOG.md`；
4. **长期方向与架构约束**：`EXECUTION_PLAN.md`；
5. **视觉实现与已确认交互入口**：`UI_SPEC.md`。

任何文档都不能覆盖已经验证的代码事实；发现文档过期时，应修正文档，而不是按旧文档回退正确实现。

## 后续开发固定流程

### 开发开始前

至少阅读：

1. `development/README.md`
2. `development/REQUIREMENTS.md`
3. `development/FOUNDATION_EXECUTION_PLAN.md`
4. `development/PROJECT_STATUS.md`

确认当前 Phase、依赖和 Gate 后再修改代码。

### 开发过程中

- 不新增只有 UI 的“假完成功能”；
- 生产路径不得继续引入 `MockData`；
- 核心按钮不得保留空操作；
- 新业务实体优先完成 Domain / Room / Repository，再接 UI；
- 需要同步的实体必须走共享 Sync Core / Outbox，不复制独立同步协议；
- 数据库结构变化必须提供 migration 与 migration test；
- 业务逻辑必须补自动化测试；
- 已确认的图片/文件/语音、recurrence、waiting、reminder、review、pomodoro、device/settings 等能力不能长期留在“以后补”。

### 一批功能完成后

必须同步：

1. 代码；
2. Tests；
3. `PROJECT_STATUS.md`；
4. `IMPLEMENTATION_LOG.md`；
5. 若需求状态变化，再更新 `REQUIREMENTS.md`；
6. 若执行顺序或 Gate 变化，再更新 `FOUNDATION_EXECUTION_PLAN.md`。

只有 CI / E2E / smoke 等对应 Gate 有证据后，才可以把状态写为“已完成”。

## 当前全功能阶段

```text
F0   Task 冲突闭环 + 测试基线
F1   Generic Sync Core + Execution Contracts
F2   Project
F3   Task Advanced
F4   Calendar + ImportantDate + Reminder
F5   Collection + Tags + Files + Voice
F6   Daily Review + Weekly Review
F7   Goal / Habit
F8   Pomodoro / FocusSession
F9   Today Aggregation
F10  Profile / Devices / Settings / Data
F11  Full Sync / Offline / E2E / Release
```

详细任务、依赖和 Gate 只以 `development/FOUNDATION_EXECUTION_PLAN.md` 为准。

## 禁止事项

后续开发禁止：

- 仅因为页面可以打开就标记模块完成；
- 用 Compose `remember` 代替业务数据持久化；
- 用 MockData 填充生产页面并计入完成度；
- 为每个实体复制一份独立 Sync Coordinator；
- 新增数据库字段却不提供 migration；
- CI 中没有真实业务测试却宣称“单测完成”；
- 在文档中提前写“已完成”而代码或验证证据尚未满足 Gate；
- 为了赶进度静默删除或裁剪已经确认的功能。

## Agent / Codex 使用建议

给开发 Agent 分配任务时，应引用明确文档路径，例如：

```text
先阅读：
- docs/README.md
- docs/development/README.md
- docs/development/REQUIREMENTS.md
- docs/development/FOUNDATION_EXECUTION_PLAN.md
- docs/development/PROJECT_STATUS.md

从当前未完成 Phase 开始执行。
按纵向闭环实现，不要创建只有 UI 外壳的功能。
一个 Phase Gate 未通过时不要跳阶段。
最终目标是 F0 → F11 全部完成并通过全功能 Release Gate。
完成后运行对应测试，并更新 PROJECT_STATUS.md 与 IMPLEMENTATION_LOG.md。
```

这样可以减少后续 Agent 因上下文不足而重新走回“先铺页面”或“先做最小版”的旧路线。