package com.lifetrace.execute.data.repository

import com.lifetrace.execute.domain.task.ExecutionTask
import com.lifetrace.execute.domain.task.ExecutionTaskPriority
import com.lifetrace.execute.domain.task.ExecutionTaskStatus
import org.json.JSONObject

object TaskWireMapper {
    fun toPayload(task: ExecutionTask): String {
        val meta = JSONObject()
            .put("id", task.id)
            .put("userId", task.userId)
            .put("createdAt", task.createdAt)
            .put("updatedAt", task.updatedAt)
            .put("deletedAt", JSONObject.NULL)
            .put("localVersion", task.localVersion)
            .put("serverVersion", task.serverVersion ?: JSONObject.NULL)
            .put("modifiedByDevice", task.modifiedByDevice ?: JSONObject.NULL)

        return JSONObject()
            .put("meta", meta)
            .put("title", task.title)
            .put("description", task.description ?: JSONObject.NULL)
            .put("projectId", task.projectId ?: JSONObject.NULL)
            .put("status", task.status.wireValue)
            .put("priority", task.priority.wireValue)
            .put("dueAt", task.dueAt ?: JSONObject.NULL)
            .put("scheduledAt", task.scheduledAt ?: JSONObject.NULL)
            .put("completedAt", task.completedAt ?: JSONObject.NULL)
            .toString()
    }

    fun fromPayload(payloadJson: String, serverVersion: String): ExecutionTask {
        val root = JSONObject(payloadJson)
        val meta = root.getJSONObject("meta")
        return ExecutionTask(
            id = meta.getString("id"),
            userId = meta.getString("userId"),
            title = root.getString("title"),
            description = root.optNullableString("description"),
            projectId = root.optNullableString("projectId"),
            status = ExecutionTaskStatus.fromWire(root.optString("status", "todo")),
            priority = ExecutionTaskPriority.fromWire(root.optString("priority", "normal")),
            dueAt = root.optNullableString("dueAt"),
            scheduledAt = root.optNullableString("scheduledAt"),
            completedAt = root.optNullableString("completedAt"),
            createdAt = meta.getString("createdAt"),
            updatedAt = meta.getString("updatedAt"),
            localVersion = meta.optLong("localVersion", 1L),
            serverVersion = serverVersion,
            modifiedByDevice = meta.optNullableString("modifiedByDevice"),
        )
    }
}

private fun JSONObject.optNullableString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name).takeIf { it.isNotBlank() }
}
