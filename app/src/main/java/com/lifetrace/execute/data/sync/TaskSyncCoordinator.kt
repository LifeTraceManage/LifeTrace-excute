package com.lifetrace.execute.data.sync

import android.content.Context
import androidx.room.withTransaction
import com.lifetrace.execute.BuildConfig
import com.lifetrace.execute.core.cloud.CloudApiException
import com.lifetrace.execute.core.cloud.CloudSessionManager
import com.lifetrace.execute.core.cloud.DeviceIdentityStore
import com.lifetrace.execute.core.cloud.LifeTraceSyncClient
import com.lifetrace.execute.core.cloud.OutgoingSyncChange
import com.lifetrace.execute.core.cloud.PushChangeResult
import com.lifetrace.execute.core.cloud.SyncClientContext
import com.lifetrace.execute.core.cloud.SyncEntityRef
import com.lifetrace.execute.data.local.LifeTraceExecuteDatabase
import com.lifetrace.execute.data.local.SyncConflictEntity
import com.lifetrace.execute.data.local.SyncOutboxEntity
import com.lifetrace.execute.data.local.SyncStateEntity
import com.lifetrace.execute.data.local.toDomain
import com.lifetrace.execute.data.local.toEntity
import com.lifetrace.execute.data.repository.TaskRepository
import com.lifetrace.execute.data.repository.TaskWireMapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import java.time.Instant

class TaskSyncCoordinator(
    context: Context,
    private val database: LifeTraceExecuteDatabase = LifeTraceExecuteDatabase.get(context),
    private val sessionManager: CloudSessionManager = CloudSessionManager(context),
    private val syncClient: LifeTraceSyncClient = LifeTraceSyncClient(),
) {
    private val dao = database.dao()
    private val identityStore = DeviceIdentityStore(context.applicationContext)
    private val mutex = Mutex()

    suspend fun syncNow(): TaskSyncSummary = mutex.withLock {
        sessionManager.authorized { session ->
            val client = SyncClientContext(
                clientVersion = BuildConfig.VERSION_NAME,
                deviceId = identityStore.deviceId(),
                schemaVersion = session.schemaVersion,
            )
            var snapshotItems = 0
            var pushed = 0
            var conflicts = 0
            var rejected = 0

            var state = dao.getSyncState(session.userId, TASK_SCOPE_KEY)
            if (state?.cursor == null) {
                snapshotItems = restoreTaskSnapshot(
                    userId = session.userId,
                    baseUrl = session.baseUrl,
                    accessToken = session.accessToken,
                    client = client,
                    existingState = state,
                )
                state = dao.getSyncState(session.userId, TASK_SCOPE_KEY)
            }

            for (round in 0 until MAX_PUSH_ROUNDS) {
                val result = pushOneRound(
                    userId = session.userId,
                    baseUrl = session.baseUrl,
                    accessToken = session.accessToken,
                    client = client,
                )
                if (!result.hadWork) break
                pushed += result.accepted
                conflicts += result.conflicts
                rejected += result.rejected
            }

            val pulled = pullUntilCurrent(
                userId = session.userId,
                baseUrl = session.baseUrl,
                accessToken = session.accessToken,
                client = client,
                afterCursor = state?.cursor,
            )

            TaskSyncSummary(
                snapshotItems = snapshotItems,
                pushed = pushed,
                pulled = pulled,
                conflicts = conflicts,
                rejected = rejected,
            )
        }
    }

    private suspend fun restoreTaskSnapshot(
        userId: String,
        baseUrl: String,
        accessToken: String,
        client: SyncClientContext,
        existingState: SyncStateEntity?,
    ): Int {
        var state = existingState ?: SyncStateEntity(
            userId = userId,
            scopeKey = TASK_SCOPE_KEY,
            cursor = null,
            lastSyncAt = null,
            snapshotId = null,
            snapshotPageToken = null,
            snapshotCursor = null,
        )
        var total = 0

        while (true) {
            val page = syncClient.snapshot(
                baseUrl = baseUrl,
                accessToken = accessToken,
                client = client,
                snapshotId = state.snapshotId,
                pageToken = state.snapshotPageToken,
                entityTypes = listOf(TaskRepository.ENTITY_TYPE),
                pageSize = SNAPSHOT_PAGE_SIZE,
            )

            database.withTransaction {
                for (item in page.items) {
                    if (item.entityType != TaskRepository.ENTITY_TYPE) continue
                    val localChange = dao.firstOutboxForEntity(userId, item.entityType, item.entityId)
                    if (localChange == null) {
                        dao.upsertTask(TaskWireMapper.fromPayload(item.payloadJson, item.serverVersion).toEntity())
                    }
                }
                state = if (page.completed) {
                    SyncStateEntity(
                        userId = userId,
                        scopeKey = TASK_SCOPE_KEY,
                        cursor = page.snapshotCursor,
                        lastSyncAt = page.serverTime,
                        snapshotId = null,
                        snapshotPageToken = null,
                        snapshotCursor = null,
                    )
                } else {
                    state.copy(
                        snapshotId = page.snapshotId,
                        snapshotPageToken = page.nextPageToken,
                        snapshotCursor = page.snapshotCursor,
                    )
                }
                dao.upsertSyncState(state)
            }
            total += page.items.size
            if (page.completed) return total
        }
    }

    private suspend fun pushOneRound(
        userId: String,
        baseUrl: String,
        accessToken: String,
        client: SyncClientContext,
    ): PushRoundResult {
        val heads = dao.pendingOutboxHeads(userId, TaskRepository.ENTITY_TYPE, PUSH_BATCH_SIZE)
        if (heads.isEmpty()) return PushRoundResult(hadWork = false)

        database.withTransaction {
            heads.forEach { dao.markOutboxAttempted(it.changeId) }
        }

        val response = try {
            syncClient.push(
                baseUrl = baseUrl,
                accessToken = accessToken,
                client = client,
                changes = heads.map(SyncOutboxEntity::toOutgoingChange),
            )
        } catch (error: Throwable) {
            val cloud = error as? CloudApiException
            database.withTransaction {
                heads.forEach { item ->
                    dao.markOutboxRetryableFailure(
                        item.changeId,
                        cloud?.code,
                        error.message,
                    )
                }
            }
            throw error
        }

        var accepted = 0
        var conflicts = 0
        var rejected = 0
        for (result in response.results) {
            when (result) {
                is PushChangeResult.Accepted -> {
                    accepted++
                    handleAccepted(userId, result)
                }
                is PushChangeResult.Conflict -> {
                    conflicts++
                    val now = Instant.now().toString()
                    database.withTransaction {
                        dao.upsertConflict(
                            SyncConflictEntity(
                                conflictId = result.conflictId,
                                userId = userId,
                                changeId = result.changeId,
                                entityType = result.entityType,
                                entityId = result.entityId,
                                clientBaseServerVersion = result.clientBaseServerVersion,
                                currentServerVersion = result.currentServerVersion,
                                serverEntityJson = result.serverEntityJson,
                                serverDeleted = result.serverDeleted,
                                reason = result.reason,
                                createdAt = now,
                            )
                        )
                        dao.blockOutboxForEntity(
                            userId = userId,
                            entityType = result.entityType,
                            entityId = result.entityId,
                            code = "SYNC_CONFLICT",
                            message = result.reason,
                        )
                    }
                }
                is PushChangeResult.Rejected -> {
                    rejected++
                    dao.blockOutbox(result.changeId, result.code, result.message)
                }
            }
        }
        return PushRoundResult(
            hadWork = true,
            accepted = accepted,
            conflicts = conflicts,
            rejected = rejected,
        )
    }

    private suspend fun handleAccepted(userId: String, result: PushChangeResult.Accepted) {
        database.withTransaction {
            if (dao.getTask(userId, result.entityId) != null) {
                dao.updateTaskServerVersion(userId, result.entityId, result.serverVersion)
            }
            dao.deleteOutbox(result.changeId)

            val next = dao.firstOutboxForEntity(userId, result.entityType, result.entityId)
            if (next != null && !next.blocked && next.attemptCount == 0) {
                val currentTask = dao.getTask(userId, result.entityId)?.toDomain()
                val rebasedPayload = if (next.operation == "upsert" && currentTask != null) {
                    TaskWireMapper.toPayload(currentTask.copy(serverVersion = result.serverVersion))
                } else {
                    next.payloadJson
                }
                dao.rebaseUnattemptedOutbox(
                    changeId = next.changeId,
                    baseServerVersion = result.serverVersion,
                    payloadJson = rebasedPayload,
                )
            }
        }
    }

    private suspend fun pullUntilCurrent(
        userId: String,
        baseUrl: String,
        accessToken: String,
        client: SyncClientContext,
        afterCursor: String?,
    ): Int {
        var cursor = dao.getSyncState(userId, TASK_SCOPE_KEY)?.cursor ?: afterCursor
        var total = 0
        while (true) {
            val batch = syncClient.pull(
                baseUrl = baseUrl,
                accessToken = accessToken,
                client = client,
                afterCursor = cursor,
                limit = PULL_BATCH_SIZE,
                entityTypes = listOf(TaskRepository.ENTITY_TYPE),
            )
            database.withTransaction {
                for (change in batch.changes) {
                    if (change.entityType != TaskRepository.ENTITY_TYPE) continue
                    val localChange = dao.firstOutboxForEntity(userId, change.entityType, change.entityId)
                    if (localChange != null) continue

                    when (change.operation) {
                        "upsert" -> change.payloadJson?.let { payload ->
                            dao.upsertTask(TaskWireMapper.fromPayload(payload, change.serverVersion).toEntity())
                        }
                        "delete" -> dao.deleteTask(userId, change.entityId)
                    }
                }
                dao.upsertSyncState(
                    SyncStateEntity(
                        userId = userId,
                        scopeKey = TASK_SCOPE_KEY,
                        cursor = batch.nextCursor,
                        lastSyncAt = batch.serverTime,
                        snapshotId = null,
                        snapshotPageToken = null,
                        snapshotCursor = null,
                    )
                )
            }
            total += batch.changes.size
            cursor = batch.nextCursor
            if (!batch.hasMore) return total
        }
    }

    private data class PushRoundResult(
        val hadWork: Boolean,
        val accepted: Int = 0,
        val conflicts: Int = 0,
        val rejected: Int = 0,
    )

    companion object {
        private const val TASK_SCOPE_KEY = "entities:execution.task"
        private const val PUSH_BATCH_SIZE = 100
        private const val PULL_BATCH_SIZE = 100
        private const val SNAPSHOT_PAGE_SIZE = 100
        private const val MAX_PUSH_ROUNDS = 20
    }
}

data class TaskSyncSummary(
    val snapshotItems: Int,
    val pushed: Int,
    val pulled: Int,
    val conflicts: Int,
    val rejected: Int,
)

private fun SyncOutboxEntity.toOutgoingChange(): OutgoingSyncChange = OutgoingSyncChange(
    changeId = changeId,
    entityType = entityType,
    entityId = entityId,
    operation = operation,
    baseServerVersion = baseServerVersion,
    entitySchemaVersion = entitySchemaVersion,
    clientModifiedAt = clientModifiedAt,
    payloadJson = payloadJson,
    atomicGroupId = atomicGroupId,
    dependencies = dependenciesJson.toEntityRefs(),
)

private fun String.toEntityRefs(): List<SyncEntityRef> {
    val array = runCatching { JSONArray(this) }.getOrElse { return emptyList() }
    return buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val entityType = item.optString("entityType")
            val entityId = item.optString("entityId")
            if (entityType.isNotBlank() && entityId.isNotBlank()) {
                add(SyncEntityRef(entityType, entityId))
            }
        }
    }
}
