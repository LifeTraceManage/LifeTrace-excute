package com.lifetrace.execute.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lifetrace.execute.domain.task.ExecutionTask
import com.lifetrace.execute.domain.task.ExecutionTaskPriority
import com.lifetrace.execute.domain.task.ExecutionTaskStatus

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["userId", "updatedAt"]),
        Index(value = ["userId", "status"]),
        Index(value = ["userId", "projectId"]),
    ],
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val projectId: String?,
    val status: String,
    val priority: String,
    val dueAt: String?,
    val scheduledAt: String?,
    val completedAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val localVersion: Long,
    val serverVersion: String?,
    val modifiedByDevice: String?,
)

fun TaskEntity.toDomain(): ExecutionTask = ExecutionTask(
    id = id,
    userId = userId,
    title = title,
    description = description,
    projectId = projectId,
    status = ExecutionTaskStatus.fromWire(status),
    priority = ExecutionTaskPriority.fromWire(priority),
    dueAt = dueAt,
    scheduledAt = scheduledAt,
    completedAt = completedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    localVersion = localVersion,
    serverVersion = serverVersion,
    modifiedByDevice = modifiedByDevice,
)

fun ExecutionTask.toEntity(): TaskEntity = TaskEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    projectId = projectId,
    status = status.wireValue,
    priority = priority.wireValue,
    dueAt = dueAt,
    scheduledAt = scheduledAt,
    completedAt = completedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    localVersion = localVersion,
    serverVersion = serverVersion,
    modifiedByDevice = modifiedByDevice,
)
