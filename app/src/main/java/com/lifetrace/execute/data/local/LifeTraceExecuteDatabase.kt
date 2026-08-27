package com.lifetrace.execute.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        SyncOutboxEntity::class,
        SyncStateEntity::class,
        SyncConflictEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class LifeTraceExecuteDatabase : RoomDatabase() {
    abstract fun dao(): LifeTraceExecuteDao

    companion object {
        @Volatile
        private var instance: LifeTraceExecuteDatabase? = null

        fun get(context: Context): LifeTraceExecuteDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LifeTraceExecuteDatabase::class.java,
                    "lifetrace-execute.db",
                ).build().also { instance = it }
            }
    }
}
