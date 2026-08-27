package com.lifetrace.execute.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeTraceExecuteDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY completedAt IS NOT NULL, dueAt IS NULL, dueAt, updatedAt DESC")
    fun observeTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId AND userId = :userId LIMIT 1")
    suspend fun getTask(userId: String, taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId AND userId = :userId")
    suspend fun deleteTask(userId: String, taskId: String)

    @Query("UPDATE tasks SET serverVersion = :serverVersion WHERE id = :taskId AND userId = :userId")
    suspend fun updateTaskServerVersion(userId: String, taskId: String, serverVersion: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOutbox(change: SyncOutboxEntity)

    @Query("SELECT * FROM sync_outbox WHERE userId = :userId ORDER BY createdAt, changeId LIMIT :limit")
    suspend fun pendingOutbox(userId: String, limit: Int): List<SyncOutboxEntity>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE userId = :userId")
    fun observePendingOutboxCount(userId: String): Flow<Int>

    @Query("DELETE FROM sync_outbox WHERE changeId = :changeId")
    suspend fun deleteOutbox(changeId: String)

    @Query(
        "UPDATE sync_outbox SET attemptCount = attemptCount + 1, lastErrorCode = :code, lastErrorMessage = :message WHERE changeId = :changeId"
    )
    suspend fun markOutboxFailed(changeId: String, code: String?, message: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncState(state: SyncStateEntity)

    @Query("SELECT * FROM sync_state WHERE userId = :userId LIMIT 1")
    suspend fun getSyncState(userId: String): SyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConflict(conflict: SyncConflictEntity)

    @Query("SELECT * FROM sync_conflicts WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeConflicts(userId: String): Flow<List<SyncConflictEntity>>

    @Query("DELETE FROM sync_conflicts WHERE conflictId = :conflictId")
    suspend fun deleteConflict(conflictId: String)
}
