package com.lifetrace.execute.data.sync

/**
 * Bridges the generic Sync v1 engine to one local Room/domain implementation.
 *
 * Entity-specific wire parsing and persistence live behind this interface so
 * push/pull/snapshot/conflict orchestration is implemented exactly once.
 */
interface SyncEntityAdapter {
    val entityType: String
    val schemaVersion: Int get() = 1
    val priority: Int get() = 100

    suspend fun applyRemoteUpsert(
        userId: String,
        entityId: String,
        payloadJson: String,
        serverVersion: String,
    )

    suspend fun applyRemoteDelete(userId: String, entityId: String)

    suspend fun updateServerVersion(
        userId: String,
        entityId: String,
        serverVersion: String,
    )

    /**
     * Returns a full snapshot payload rebased to [serverVersion].
     * Null means the local entity no longer exists (for example a queued delete).
     */
    suspend fun payloadForRebase(
        userId: String,
        entityId: String,
        serverVersion: String,
    ): String?
}
