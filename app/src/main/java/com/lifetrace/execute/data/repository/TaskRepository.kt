package com.lifetrace.execute.data.repository

import androidx.room.withTransaction
import com.lifetrace.execute.data.local.LifeTraceExecuteDatabase
import com.lifetrace.execute.data.local.SyncOutboxEntity
import com.lifetrace.execute.data.local.toDomain
import com.lifetrace.execute.data.local.toEntity
import com.lifetrace.execute.domain.task.ExecutionTask
import com.lifetrace.execute.domain.task.ExecutionTaskPriority
import com.lifetrace.execute.domain.task.ExecutionTaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

class TaskRepository(
    private val database: LifeTraceExecuteDatabase,
) {
    private val dao = database.dao()

    fun observeTasks(userId: String): Flow<List<ExecutionTask>> =
        dao.observeTasks(userId).map { tasks -> tasks.map { it.toDomain() } }

    suspend fun createTask(
        userId: String,
        deviceId: String,
        title: String,
        description: String? = null,
        projectId: String? = null,
        priority: ExecutionTaskPriority = ExecutionTaskPriority.NORMAL,
        dueAt: String? = null,
        scheduledAt: String? = null,
    ): ExecutionTask {
        require(title.isNotBlank()) { "任务标题不能为空" }
        val now = Instant.now().toString()
        val task = ExecutionTask(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            projectId = projectId,
            priority = priority,
            dueAt = dueAt,
            scheduledAt = scheduledAt,
            createdAt = now,
            updatedAt = now,
            localVersion = 1,
            serverVersion = null,
            modifiedByDevice = deviceId,
        )
        writeLocalChange(task)
        return task
    }

    suspend fun updateTask(
        task: ExecutionTask,
        deviceId: String,
        title: String = task.title,
        description: String? = task.description,
        projectId: String? = task.projectId,
        status: ExecutionTaskStatus = task.status,
        priority: ExecutionTaskPriority = task.priority,
        dueAt: String? = task.dueAt,
        scheduledAt: String? = task.scheduledAt,
    ): ExecutionTask {
        require(title.isNotBlank()) { "任务标题不能为空" }
        val now = Instant.now().toString()
        val completedAt = when {
            status == ExecutionTaskStatus.DONE && task.status != ExecutionTaskStatus.DONE -> now
            status != ExecutionTaskStatus.DONE -> null
            else -> task.completedAt
        }
        val updated = task.copy(
            title = title.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            projectId = projectId,
            status = status,
            priority = priority,
            dueAt = dueAt,
            scheduledAt = scheduledAt,
            completedAt = completedAt,
            updatedAt = now,
            localVersion = task.localVersion + 1,
            modifiedByDevice = deviceId,
        )
        writeLocalChange(updated)
        return updated
    }

    suspend fun deleteTask(userId: String, taskId: String) {
        val existing = dao.getTask(userId, taskId)?.toDomain() ?: return
        val now = Instant.now().toString()
        database.withTransaction {
            dao.insertOutbox(
                SyncOutboxEntity(
                    changeId = UUID.randomUUID().toString(),
                    userId = userId,
                    entityType = ENTITY_TYPE,
                    entityId = taskId,
                    operation = "delete",
                    baseServerVersion = existing.serverVersion ?: "0",
                    entitySchemaVersion = 1,
                    clientModifiedAt = now,
                    payloadJson = null,
                    atomicGroupId = null,
                    dependenciesJson = "[]",
                    createdAt = now,
                )
            )
            dao.deleteTask(userId, taskId)
        }
    }

    suspend fun applyRemoteUpsert(payloadJson: String, serverVersion: String) {
        val task = TaskWireMapper.fromPayload(payloadJson, serverVersion)
        database.withTransaction {
            dao.upsertTask(task.toEntity())
        }
    }

    suspend fun applyRemoteDelete(userId: String, taskId: String) {
        database.withTransaction {
            dao.deleteTask(userId, taskId)
        }
    }

    private suspend fun writeLocalChange(task: ExecutionTask) {
        val payload = TaskWireMapper.toPayload(task)
        val dependencies = JSONArray().also { array ->
            task.projectId?.let { projectId ->
                array.put(
                    JSONObject()
                        .put("entityType", "execution.project")
                        .put("entityId", projectId)
                )
            }
        }.toString()

        database.withTransaction {
            dao.upsertTask(task.toEntity())
            dao.insertOutbox(
                SyncOutboxEntity(
                    changeId = UUID.randomUUID().toString(),
                    userId = task.userId,
                    entityType = ENTITY_TYPE,
                    entityId = task.id,
                    operation = "upsert",
                    baseServerVersion = task.serverVersion ?: "0",
                    entitySchemaVersion = 1,
                    clientModifiedAt = task.updatedAt,
                    payloadJson = payload,
                    atomicGroupId = null,
                    dependenciesJson = dependencies,
                    createdAt = task.updatedAt,
                )
            )
        }
    }

    companion object {
        const val ENTITY_TYPE = "execution.task"
    }
}
