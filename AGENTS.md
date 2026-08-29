# LifeTrace Execute Development Instructions

These instructions apply to the entire repository.

## Required reading before implementation

Before changing production code, read in this order:

1. `docs/README.md`
2. `docs/development/README.md`
3. `docs/development/REQUIREMENTS.md`
4. `docs/development/FOUNDATION_EXECUTION_PLAN.md`
5. `docs/development/PROJECT_STATUS.md`
6. `docs/development/IMPLEMENTATION_LOG.md`

For UI changes also read `docs/development/UI_SPEC.md`.
For long-term architecture decisions also read `docs/development/EXECUTION_PLAN.md`.

## Current priority

Follow the current Foundation Phase defined in `docs/development/FOUNDATION_EXECUTION_PLAN.md`.

Do not skip ahead to add new UI shells while the current Phase Gate is incomplete.

## Definition of implemented

A business module is not implemented merely because a screen exists.

The expected vertical slice is:

```text
Domain
→ Room Entity / DAO
→ Repository
→ ViewModel / UI State
→ Compose UI
→ Offline behavior
→ Sync when applicable
→ Automated tests
→ CI / smoke / E2E evidence
```

If important parts of this chain are missing, document the module as `UI 外壳`, `开发中`, or another accurate partial state instead of `已完成`.

## Production-code constraints

- Do not introduce new `MockData` dependencies into production flows.
- Do not leave core user actions as empty `onClick = {}` handlers.
- Do not use Compose `remember` as persistent business storage.
- Local-first business writes must persist locally before network sync.
- Synced entities must use the shared Outbox / Sync Core architecture instead of copying a separate sync engine for each entity.
- Room schema changes require migrations and migration tests.
- New business behavior requires meaningful automated tests.
- Do not mark a feature complete solely because `testDebugUnitTest` exits successfully; confirm real tests exist and execute.

## Documentation updates after implementation

After a verified development batch:

1. update `docs/development/PROJECT_STATUS.md` with the real current state;
2. update `docs/development/IMPLEMENTATION_LOG.md` with implementation facts, commit SHA and test/CI evidence;
3. update `docs/development/REQUIREMENTS.md` only if product requirements changed;
4. update `docs/development/FOUNDATION_EXECUTION_PLAN.md` only if phase order, dependencies or Gate criteria changed.

Never pre-mark planned work as complete.

## Validation

At minimum run the relevant unit tests plus:

```text
:app:assembleDebug
:app:testDebugUnitTest
:app:lintDebug
```

For sync, offline, database migration, notification, timer or multi-device behavior, also perform the specific smoke/E2E Gate defined by the Foundation plan.

## Source of truth

When documentation disagrees with verified code/CI behavior, verified implementation is the factual source. Correct the stale documentation immediately rather than reverting correct code to match stale text.
