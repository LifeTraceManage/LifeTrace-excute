package com.lifetrace.execute.core.cloud

data class SyncClientContext(
    val clientVersion: String,
    val deviceId: String,
    val schemaVersion: Int,
)

data class SyncEntityRef(
    val entityType: String,
    val entityId: String,
)

data class OutgoingSyncChange(
    val changeId: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val baseServerVersion: String,
    val entitySchemaVersion: Int = 1,
    val clientModifiedAt: String,
    val payloadJson: String? = null,
    val atomicGroupId: String? = null,
    val dependencies: List<SyncEntityRef> = emptyList(),
)

sealed interface PushChangeResult {
    val changeId: String
    val entityType: String
    val entityId: String

    data class Accepted(
        override val changeId: String,
        override val entityType: String,
        override val entityId: String,
        val serverVersion: String,
        val cursor: String,
        val serverModifiedAt: String,
        val duplicate: Boolean,
    ) : PushChangeResult

    data class Conflict(
        override val changeId: String,
        override val entityType: String,
        override val entityId: String,
        val conflictId: String,
        val clientBaseServerVersion: String,
        val currentServerVersion: String,
        val serverEntityJson: String?,
        val serverDeleted: Boolean,
        val reason: String,
    ) : PushChangeResult

    data class Rejected(
        override val changeId: String,
        override val entityType: String,
        override val entityId: String,
        val code: String,
        val message: String,
    ) : PushChangeResult
}

data class PushBatchResult(
    val requestId: String,
    val serverTime: String,
    val latestCursor: String,
    val results: List<PushChangeResult>,
)

data class PulledChange(
    val cursor: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val serverVersion: String,
    val serverModifiedAt: String,
    val payloadJson: String?,
    val tombstoneJson: String?,
    val originDeviceId: String?,
)

data class PullBatchResult(
    val requestId: String,
    val serverTime: String,
    val changes: List<PulledChange>,
    val nextCursor: String,
    val hasMore: Boolean,
)

data class SnapshotItem(
    val entityType: String,
    val entityId: String,
    val serverVersion: String,
    val payloadJson: String,
)

data class SnapshotPageResult(
    val requestId: String,
    val snapshotId: String,
    val snapshotCursor: String,
    val items: List<SnapshotItem>,
    val nextPageToken: String?,
    val completed: Boolean,
    val serverTime: String,
)
