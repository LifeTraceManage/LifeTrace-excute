package com.lifetrace.execute.domain.task

enum class ExecutionTaskStatus(val wireValue: String) {
    TODO("todo"),
    IN_PROGRESS("in_progress"),
    WAITING("waiting"),
    DONE("done");

    companion object {
        fun fromWire(value: String): ExecutionTaskStatus =
            entries.firstOrNull { it.wireValue == value } ?: TODO
    }
}

enum class ExecutionTaskPriority(val wireValue: String) {
    LOW("low"),
    NORMAL("normal"),
    HIGH("high"),
    URGENT("urgent");

    companion object {
        fun fromWire(value: String): ExecutionTaskPriority =
            entries.firstOrNull { it.wireValue == value } ?: NORMAL
    }
}

data class ExecutionTask(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val projectId: String? = null,
    val status: ExecutionTaskStatus = ExecutionTaskStatus.TODO,
    val priority: ExecutionTaskPriority = ExecutionTaskPriority.NORMAL,
    val dueAt: String? = null,
    val scheduledAt: String? = null,
    val completedAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val localVersion: Long,
    val serverVersion: String? = null,
    val modifiedByDevice: String? = null,
)
