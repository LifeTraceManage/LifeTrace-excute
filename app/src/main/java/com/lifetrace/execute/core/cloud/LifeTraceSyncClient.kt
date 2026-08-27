package com.lifetrace.execute.core.cloud

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LifeTraceSyncClient(
    private val transport: CloudHttpTransport = CloudHttpTransport(),
) {
    suspend fun push(
        baseUrl: String,
        accessToken: String,
        client: SyncClientContext,
        changes: List<OutgoingSyncChange>,
    ): PushBatchResult {
        val requestId = UUID.randomUUID().toString()
        val body = JSONObject()
            .put("requestId", requestId)
            .put("client", client.toJson())
            .put("changes", JSONArray().also { array ->
                changes.forEach { change -> array.put(change.toJson()) }
            })

        val root = transport.requestJson(
            method = "POST",
            baseUrl = baseUrl,
            path = "/api/v1/sync/push",
            body = body,
            accessToken = accessToken,
        )
        return PushBatchResult(
            requestId = root.getString("requestId"),
            serverTime = root.getString("serverTime"),
            latestCursor = root.getString("latestCursor"),
            results = root.getJSONArray("results").mapObjects(::parsePushResult),
        )
    }

    suspend fun pull(
        baseUrl: String,
        accessToken: String,
        client: SyncClientContext,
        afterCursor: String?,
        limit: Int,
        entityTypes: List<String>? = null,
    ): PullBatchResult {
        require(limit > 0) { "pull limit must be positive" }
        val requestId = UUID.randomUUID().toString()
        val body = JSONObject()
            .put("requestId", requestId)
            .put("client", client.toJson())
            .putNullable("afterCursor", afterCursor)
            .put("limit", limit)
            .putNullableStringArray("entityTypes", entityTypes)

        val root = transport.requestJson(
            method = "POST",
            baseUrl = baseUrl,
            path = "/api/v1/sync/pull",
            body = body,
            accessToken = accessToken,
        )
        return PullBatchResult(
            requestId = root.getString("requestId"),
            serverTime = root.getString("serverTime"),
            changes = root.getJSONArray("changes").mapObjects { item ->
                PulledChange(
                    cursor = item.getString("cursor"),
                    entityType = item.getString("entityType"),
                    entityId = item.getString("entityId"),
                    operation = item.getString("operation"),
                    serverVersion = item.getString("serverVersion"),
                    serverModifiedAt = item.getString("serverModifiedAt"),
                    payloadJson = item.optObjectJson("payload"),
                    tombstoneJson = item.optObjectJson("tombstone"),
                    originDeviceId = item.optNullableString("originDeviceId"),
                )
            },
            nextCursor = root.getString("nextCursor"),
            hasMore = root.getBoolean("hasMore"),
        )
    }

    suspend fun snapshot(
        baseUrl: String,
        accessToken: String,
        client: SyncClientContext,
        snapshotId: String? = null,
        pageToken: String? = null,
        entityTypes: List<String>? = null,
        pageSize: Int = 200,
    ): SnapshotPageResult {
        require(pageSize > 0) { "snapshot pageSize must be positive" }
        val requestId = UUID.randomUUID().toString()
        val body = JSONObject()
            .put("requestId", requestId)
            .put("client", client.toJson())
            .putNullable("snapshotId", snapshotId)
            .putNullable("pageToken", pageToken)
            .putNullableStringArray("entityTypes", entityTypes)
            .put("pageSize", pageSize)

        val root = transport.requestJson(
            method = "POST",
            baseUrl = baseUrl,
            path = "/api/v1/sync/snapshot",
            body = body,
            accessToken = accessToken,
        )
        return SnapshotPageResult(
            requestId = root.getString("requestId"),
            snapshotId = root.getString("snapshotId"),
            snapshotCursor = root.getString("snapshotCursor"),
            items = root.getJSONArray("items").mapObjects { item ->
                SnapshotItem(
                    entityType = item.getString("entityType"),
                    entityId = item.getString("entityId"),
                    serverVersion = item.getString("serverVersion"),
                    payloadJson = item.getJSONObject("payload").toString(),
                )
            },
            nextPageToken = root.optNullableString("nextPageToken"),
            completed = root.getBoolean("completed"),
            serverTime = root.getString("serverTime"),
        )
    }

    private fun parsePushResult(item: JSONObject): PushChangeResult {
        return when (val status = item.getString("status")) {
            "accepted", "duplicate" -> PushChangeResult.Accepted(
                changeId = item.getString("changeId"),
                entityType = item.getString("entityType"),
                entityId = item.getString("entityId"),
                serverVersion = item.getString("serverVersion"),
                cursor = item.getString("cursor"),
                serverModifiedAt = item.getString("serverModifiedAt"),
                duplicate = status == "duplicate",
            )
            "conflict" -> PushChangeResult.Conflict(
                changeId = item.getString("changeId"),
                entityType = item.getString("entityType"),
                entityId = item.getString("entityId"),
                conflictId = item.getString("conflictId"),
                clientBaseServerVersion = item.getString("clientBaseServerVersion"),
                currentServerVersion = item.getString("currentServerVersion"),
                serverEntityJson = item.optObjectJson("serverEntity"),
                serverDeleted = item.getBoolean("serverDeleted"),
                reason = item.getString("reason"),
            )
            "rejected" -> PushChangeResult.Rejected(
                changeId = item.getString("changeId"),
                entityType = item.getString("entityType"),
                entityId = item.getString("entityId"),
                code = item.getString("code"),
                message = item.getString("message"),
            )
            else -> throw IllegalStateException("Unknown push result status: $status")
        }
    }
}

private fun SyncClientContext.toJson(): JSONObject = JSONObject()
    .put("appId", CloudContract.APP_ID)
    .put("clientVersion", clientVersion)
    .put("platform", CloudContract.PLATFORM)
    .put("protocolVersion", CloudContract.PROTOCOL_VERSION)
    .put("schemaVersion", schemaVersion)
    .put("deviceId", deviceId)

private fun OutgoingSyncChange.toJson(): JSONObject {
    require(operation == "upsert" || operation == "delete") { "unsupported sync operation: $operation" }
    require(operation != "upsert" || payloadJson != null) { "upsert requires a full entity payload" }
    require(operation != "delete" || payloadJson == null) { "delete must not carry a payload" }

    return JSONObject()
        .put("changeId", changeId)
        .put("entityType", entityType)
        .put("entityId", entityId)
        .put("operation", operation)
        .put("baseServerVersion", baseServerVersion)
        .put("entitySchemaVersion", entitySchemaVersion)
        .put("clientModifiedAt", clientModifiedAt)
        .put("payload", payloadJson?.let(::JSONObject) ?: JSONObject.NULL)
        .put("atomicGroupId", atomicGroupId ?: JSONObject.NULL)
        .put("dependencies", JSONArray().also { array ->
            dependencies.forEach { dependency ->
                array.put(
                    JSONObject()
                        .put("entityType", dependency.entityType)
                        .put("entityId", dependency.entityId)
                )
            }
        })
}

private fun JSONObject.putNullable(name: String, value: String?): JSONObject =
    put(name, value ?: JSONObject.NULL)

private fun JSONObject.putNullableStringArray(name: String, values: List<String>?): JSONObject =
    put(name, values?.toJsonArray() ?: JSONObject.NULL)

private fun JSONObject.optObjectJson(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return getJSONObject(name).toString()
}

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> = buildList {
    for (index in 0 until length()) {
        add(transform(getJSONObject(index)))
    }
}
