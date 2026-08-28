package com.lifetrace.execute.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifetrace.execute.core.cloud.CloudApiException
import com.lifetrace.execute.core.cloud.SecureSessionStore
import java.io.IOException

class TaskSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (SecureSessionStore(applicationContext).load() == null) {
            return Result.success()
        }

        return try {
            TaskSyncCoordinator(applicationContext).syncNow()
            Result.success()
        } catch (error: CloudApiException) {
            when {
                error.statusCode == 401 || error.statusCode == 403 -> Result.failure()
                error.retryable || error.statusCode == 429 || error.statusCode >= 500 -> retryOrFail()
                else -> Result.failure()
            }
        } catch (_: IOException) {
            retryOrFail()
        } catch (_: Throwable) {
            Result.failure()
        }
    }

    private fun retryOrFail(): Result =
        if (runAttemptCount < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 6
    }
}
