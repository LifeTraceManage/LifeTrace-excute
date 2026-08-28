package com.lifetrace.execute.data.sync

import androidx.room.withTransaction
import com.lifetrace.execute.data.local.LifeTraceExecuteDatabase
import com.lifetrace.execute.data.local.SyncOutboxEntity
import com.lifetrace.execute.data.local.toDomain
import com.lifetrace.execute.data.local.toEntity
import com.lifetrace.execute.data.repository.TaskRepository
import com.lifetrace.execute.data.repository.TaskWireMapper
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

class TaskConflictResolver(
    private val database: LifeTraceExecuteDatabase,
) {
    private val dao = database.dao()

    suspend fun keepServer(conflictId: String) {
        database.withTransaction {
            val conflict = dao.getConflict(conflictId) ?: return@withTransaction
            require(conflict.entityType == TaskRepository.ENTITY_TYPE) {
                "仅支持处理任务冲突"
            }

            dao.deleteOutboxForEntity(
                userId = conflict.userId,
                entityType = conflict.entityType,
                entityId = conflict.entityId,
            )

            if (conflict.serverDeleted) {
                dao.deleteTask(conflict.userId, conflict.entityId)
            } else {
                val payload = requireNotNull(conflict.serverEntityJson) {
                    "服务器冲突结果缺少任务内容"
                }
                dao.upsertTask(
                    TaskWireMapper.fromPayload(
                        payloadJson = payload,
                        serverVersion = conflict.currentServerVersion,
                    ).toEntity()
                )
            }

            dao.deleteConflictsForEntity(
                userId = conflict.userId,
                entityType = conflict.entityType,
                entityId = conflict.entityId,
            )
        }
    }

    suspend fun keepLocal(conflictId: String, deviceId: String) {
        database.withTransaction {
            val conflict = dao.getConflict(conflictId) ?: return@withTransaction
            require(conflict.entityType == TaskRepository.ENTITY_TYPE) {
                "仅支持处理任务冲突"
            }

            val conflictedChange = dao.firstOutboxForEntity(
                userId = conflict.userId,
                entityType = conflict.entityType,
                entityId = conflict.entityId,
            ) ?: error("冲突缺少对应的本地待同步变更")

            val now = Instant.now().toString()
            val currentTask = dao.getTask(conflict.userId, conflict.entityId)?.toDomain()

            dao.deleteOutboxForEntity(
                userId = conflict.userId,
                entityType = conflict.entityType,
                entityId = conflict.entityId,
            )

            when (conflictedChange.operation) {
                "upsert" -> {
                    val localTask = requireNotNull(currentTask) {
                        "本地任务已不存在，无法选择保留本地版本"
                    }
                    val rebasedTask = localTask.copy(
                        updatedAt = now,
                        localVersion = localTask.localVersion + 1,
                        serverVersion = conflict.currentServerVersion,
                        modifiedByDevice = deviceId,
                    )
                    dao.upsertTask(rebasedTask.toEntity())
                    dao.insertOutbox(
                        SyncOutboxEntity(
                            changeId = UUID.randomUUID().toString(),
                            userId = conflict.userId,
                            entityType = conflict.entityType,
                            entityId = conflict.entityId,
                            operation = "upsert",
                            baseServerVersion = conflict.currentServerVersion,
                            entitySchemaVersion = conflictedChange.entitySchemaVersion,
                            clientModifiedAt = now,
                            payloadJson = TaskWireMapper.toPayload(rebasedTask),
                            atomicGroupId = null,
                            dependenciesJson = rebasedTask.dependenciesJson(),
                            createdAt = now,
                        )
                    )
                }

                "delete" -> {
                    dao.deleteTask(conflict.userId, conflict.entityId)
                    dao.insertOutbox(
                        SyncOutboxEntity(
                            changeId = UUID.randomUUID().toString(),
                            userId = conflict.userId,
                            entityType = conflict.entityType,
                            entityId = conflict.entityId,
                            operation = "delete",
                            baseServerVersion = conflict.currentServerVersion,
                            entitySchemaVersion = conflictedChange.entitySchemaVersion,
                            clientModifiedAt = now,
                            payloadJson = null,
                            atomicGroupId = null,
                            dependenciesJson = "[]",
                            createdAt = now,
                        )
                    )
                }

                else -> error("不支持的冲突变更类型：${conflictedChange.operation}")
            }

            dao.deleteConflictsForEntity(
                userId = conflict.userId,
                entityType = conflict.entityType,
                entityId = conflict.entityId,
            )
        }
    }
}

private fun com.lifetrace.execute.domain.task.ExecutionTask.dependenciesJson(): String =
    JSONArray().also { array ->
        projectId?.let { projectId ->
            array.put(
                JSONObject()
                    .put("entityType", "execution.project")
                    .put("entityId", projectId)
            )
        }
    }.toString()
