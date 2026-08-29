# LifeTrace Execute 开发文档指南

本目录保存直接指导 LifeTrace Execute 开发的文档。

目标不是增加文档数量，而是保证后续开发始终围绕真实业务闭环推进，并让新的开发者、Codex 或 Agent 能快速判断：当前做到哪里、接下来做什么、什么标准才算完成。

## 推荐阅读顺序

每次开始新的开发批次时，按以下顺序阅读：

1. [`REQUIREMENTS.md`](REQUIREMENTS.md)
   - 确认长期需求和产品约束；
2. [`FOUNDATION_EXECUTION_PLAN.md`](FOUNDATION_EXECUTION_PLAN.md)
   - 确认当前基础版本 Phase、执行顺序和 Gate；
3. [`PROJECT_STATUS.md`](PROJECT_STATUS.md)
   - 确认哪些是真实实现、哪些仍是 Mock/UI 外壳；
4. [`IMPLEMENTATION_LOG.md`](IMPLEMENTATION_LOG.md)
   - 查看最近已经提交并验证的实现证据；
5. [`EXECUTION_PLAN.md`](EXECUTION_PLAN.md)
   - 需要理解 1.0 长期架构和最终 Release Gate 时阅读；
6. [`UI_SPEC.md`](UI_SPEC.md)
   - 涉及页面、导航、组件、视觉修改时阅读。

## 当前主执行文档

当前开发必须以：

[`FOUNDATION_EXECUTION_PLAN.md`](FOUNDATION_EXECUTION_PLAN.md)

为主要执行依据。

它的优先级高于长期 `EXECUTION_PLAN.md` 中较宽泛的阶段顺序，因为当前最需要解决的问题不是“继续增加页面”，而是让已有核心模块达到真实可用状态。

## 模块完成标准

任何业务模块只有同时满足下面的纵向链，才可以在 `PROJECT_STATUS.md` 中标记为已实现：

```text
Domain Model
    ↓
Room Entity / DAO
    ↓
Repository
    ↓
ViewModel / UI State
    ↓
Compose UI
    ↓
Offline behavior
    ↓
Sync（需要云同步的实体）
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

简化后的 Foundation 顺序：

```text
F0  Task 冲突闭环 + Unit Test 基线
 ↓
F1  Generic Sync Core
 ↓
F2  Project 完整纵向链
 ↓
F3  Today 真实聚合
 ↓
F4  Calendar + ImportantDate
 ↓
F5  Collection 最小真实闭环
 ↓
F6  Review 持久化
 ↓
F7  Reminder + Pomodoro
 ↓
F8  Profile / Device / Sync Observability
 ↓
F9  双设备 / Offline / Migration / Release Gate
```

具体任务和验收条件以 `FOUNDATION_EXECUTION_PLAN.md` 为准。

## 文档更新约定

### REQUIREMENTS.md

只记录产品意图和长期需求，不记录日常 commit 流水账。

### FOUNDATION_EXECUTION_PLAN.md

只在以下情况更新：

- Phase 顺序发生变化；
- 发现新的关键依赖；
- Definition of Done / Gate 需要调整；
- 当前基础版本范围明确变更。

### PROJECT_STATUS.md

这是“现在到底做到哪里”的实时文档。

每完成一批经过验证的功能后更新，不允许根据计划预填完成状态。

### IMPLEMENTATION_LOG.md

记录事实证据：

- 实现内容；
- 关键文件；
- commit SHA；
- CI run；
- 测试结果；
- 已知阻塞。

不要在这里写未来计划。

### EXECUTION_PLAN.md

保持为 1.0 长期路线，不需要每个小迭代都修改。

### UI_SPEC.md

只维护 UI/UX 规则，不把业务逻辑要求混进视觉规范。

## 每批开发完成检查表

提交一批功能前至少检查：

- [ ] 是否仍有生产路径引用 MockData；
- [ ] 是否存在空 `onClick = {}`；
- [ ] App 重启后数据是否保留；
- [ ] 断网情况下核心写入是否成功；
- [ ] 需要同步的数据是否进入 Outbox；
- [ ] 网络恢复后是否能自动同步；
- [ ] 是否有真实业务测试；
- [ ] 是否需要 Room migration；
- [ ] assembleDebug 是否通过；
- [ ] testDebugUnitTest 是否实际执行测试；
- [ ] lintDebug 是否通过；
- [ ] PROJECT_STATUS 是否和代码一致；
- [ ] IMPLEMENTATION_LOG 是否记录验证证据。

## 新 Agent 启动模板

后续可以直接把下面的约束交给开发 Agent：

```text
你正在继续开发 LifeTrace-execute。

开始前必须阅读：
1. docs/README.md
2. docs/development/README.md
3. docs/development/REQUIREMENTS.md
4. docs/development/FOUNDATION_EXECUTION_PLAN.md
5. docs/development/PROJECT_STATUS.md
6. docs/development/IMPLEMENTATION_LOG.md

执行当前 Foundation Phase，不要跳阶段去铺新的 UI 外壳。
一个模块只有完成 Domain → Room → Repository → ViewModel → UI → Offline/Sync → Tests 才算实现。
生产路径禁止新增 MockData 和空 onClick。

完成后：
- 运行对应测试/CI；
- 更新 PROJECT_STATUS.md；
- 把 commit/测试证据记录到 IMPLEMENTATION_LOG.md；
- 未验证的内容不得标记为已完成。
```
