package com.lifetrace.execute.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val INITIAL_SYNC_WORK = "lifetrace-task-sync-after-cloud-login"
    private const val ONE_TIME_WORK = "lifetrace-task-sync-after-local-change"
    private const val PERIODIC_WORK = "lifetrace-task-sync-periodic"

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<TaskSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun enqueueInitialSync(context: Context) {
        enqueueOneTime(
            context = context,
            uniqueWorkName = INITIAL_SYNC_WORK,
            initialDelaySeconds = 0,
        )
    }

    fun enqueueAfterLocalChange(context: Context) {
        enqueueOneTime(
            context = context,
            uniqueWorkName = ONE_TIME_WORK,
            initialDelaySeconds = 3,
        )
    }

    private fun enqueueOneTime(
        context: Context,
        uniqueWorkName: String,
        initialDelaySeconds: Long,
    ) {
        val request = OneTimeWorkRequestBuilder<TaskSyncWorker>()
            .setConstraints(networkConstraints)
            .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
