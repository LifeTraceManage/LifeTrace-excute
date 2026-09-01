package com.lifetrace.execute.data.sync

import com.lifetrace.execute.data.local.LifeTraceExecuteDatabase
import com.lifetrace.execute.data.local.toDomain
import com.lifetrace.execute.data.local.toEntity
import com.lifetrace.execute.data.repository.TaskRepository
import com.lifetrace.execute.data.repository.TaskWireMapper

class TaskSyncEntityAdapter(
    private val database: LifeTraceExecuteDatabase,
) : SyncEntityAdapter {
    private val dao = database.dao()

    override val entityType: String = TaskRepository.ENTITY_TYPE
    override val priority: Int = 20

    override suspend fun applyRemoteUpsert(
        userId: String,
        entityId: String,
        payloadJson: String,
        serverVersion: String,
    ) {
        val task = TaskWireMapper.fromPayload(payloadJson, serverVersion)
        require(task.userId == userId) { "任务 payload 用户不匹配" }
        require(task.id == entityId) { "任务 payload id 不匹配" }
        dao.upsertTask(task.toEntity())
    }

    override suspend fun applyRemoteDelete(userId: String, entityId: String) {
        dao.deleteTask(userId, entityId)
    }

    override suspend fun updateServerVersion(userId: String, entityId: String, serverVersion: String) {
        dao.updateTaskServerVersion(userId, entityId, serverVersion)
    }

    override suspend fun payloadForRebase(
        userId: String,
        entityId: String,
        serverVersion: String,
    ): String? = dao.getTask(userId, entityId)
        ?.toDomain()
        ?.copy(serverVersion = serverVersion)
        ?.let(TaskWireMapper::toPayload)
}
