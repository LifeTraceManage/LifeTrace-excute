package com.lifetrace.execute.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_outbox",
    indices = [
        Index(value = ["userId", "createdAt"]),
        Index(value = ["entityType", "entityId"]),
    ],
)
data class SyncOutboxEntity(
    @PrimaryKey val changeId: String,
    val userId: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val baseServerVersion: String,
    val entitySchemaVersion: Int,
    val clientModifiedAt: String,
    val payloadJson: String?,
    val atomicGroupId: String?,
    val dependenciesJson: String,
    val attemptCount: Int = 0,
    val lastErrorCode: String? = null,
    val lastErrorMessage: String? = null,
    val createdAt: String,
)

@Entity(
    tableName = "sync_state",
    primaryKeys = ["userId", "scopeKey"],
)
data class SyncStateEntity(
    val userId: String,
    val scopeKey: String,
    val cursor: String?,
    val lastSyncAt: String?,
    val snapshotId: String?,
    val snapshotPageToken: String?,
    val snapshotCursor: String?,
)

@Entity(
    tableName = "sync_conflicts",
    indices = [Index(value = ["userId", "createdAt"])],
)
data class SyncConflictEntity(
    @PrimaryKey val conflictId: String,
    val userId: String,
    val changeId: String,
    val entityType: String,
    val entityId: String,
    val clientBaseServerVersion: String,
    val currentServerVersion: String,
    val serverEntityJson: String?,
    val serverDeleted: Boolean,
    val reason: String,
    val createdAt: String,
)
