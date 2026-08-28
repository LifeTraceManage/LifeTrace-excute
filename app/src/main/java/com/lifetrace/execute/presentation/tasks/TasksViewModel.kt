package com.lifetrace.execute.presentation.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifetrace.execute.core.cloud.DeviceIdentityStore
import com.lifetrace.execute.core.cloud.SecureSessionStore
import com.lifetrace.execute.data.local.LifeTraceExecuteDatabase
import com.lifetrace.execute.data.repository.TaskRepository
import com.lifetrace.execute.data.sync.SyncScheduler
import com.lifetrace.execute.data.sync.TaskSyncCoordinator
import com.lifetrace.execute.domain.task.ExecutionTask
import com.lifetrace.execute.domain.task.ExecutionTaskPriority
import com.lifetrace.execute.domain.task.ExecutionTaskStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TasksUiState(
    val connected: Boolean = false,
    val tasks: List<ExecutionTask> = emptyList(),
    val loading: Boolean = true,
    val syncing: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

class TasksViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val database = LifeTraceExecuteDatabase.get(application)
    private val repository = TaskRepository(database)
    private val sessionStore = SecureSessionStore(application)
    private val deviceIdentityStore = DeviceIdentityStore(application)
    private val syncCoordinator = TaskSyncCoordinator(application, database = database)

    private val _state = MutableStateFlow(TasksUiState())
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    private var observeJob: Job? = null
    private var activeUserId: String? = null

    init {
        refreshSession()
    }

    fun refreshSession() {
        val session = sessionStore.load()
        val userId = session?.userId
        if (userId == activeUserId && _state.value.loading.not()) return

        activeUserId = userId
        observeJob?.cancel()

        if (userId == null) {
            _state.value = TasksUiState(
                connected = false,
                tasks = emptyList(),
                loading = false,
            )
            return
        }

        _state.value = _state.value.copy(
            connected = true,
            loading = true,
            error = null,
        )
        observeJob = viewModelScope.launch {
            repository.observeTasks(userId).collect { tasks ->
                _state.value = _state.value.copy(
                    connected = true,
                    tasks = tasks,
                    loading = false,
                )
            }
        }
    }

    fun createTask(
        title: String,
        description: String?,
        priority: ExecutionTaskPriority,
        dueAt: String?,
        scheduledAt: String?,
    ) {
        val userId = activeUserId ?: run {
            _state.value = _state.value.copy(error = "请先连接 LifeTrace Cloud")
            return
        }
        viewModelScope.launch {
            try {
                repository.createTask(
                    userId = userId,
                    deviceId = deviceIdentityStore.deviceId(),
                    title = title,
                    description = description,
                    priority = priority,
                    dueAt = dueAt,
                    scheduledAt = scheduledAt,
                )
                SyncScheduler.enqueueAfterLocalChange(appContext)
                _state.value = _state.value.copy(
                    message = "任务已保存到本地，并进入待同步队列",
                    error = null,
                )
            } catch (error: Throwable) {
                _state.value = _state.value.copy(error = error.message ?: "新建任务失败")
            }
        }
    }

    fun updateTask(
        task: ExecutionTask,
        title: String,
        description: String?,
        status: ExecutionTaskStatus,
        priority: ExecutionTaskPriority,
        dueAt: String?,
        scheduledAt: String?,
    ) {
        viewModelScope.launch {
            try {
                repository.updateTask(
                    task = task,
                    deviceId = deviceIdentityStore.deviceId(),
                    title = title,
                    description = description,
                    status = status,
                    priority = priority,
                    dueAt = dueAt,
                    scheduledAt = scheduledAt,
                )
                SyncScheduler.enqueueAfterLocalChange(appContext)
                _state.value = _state.value.copy(
                    message = "任务修改已保存到本地，并进入待同步队列",
                    error = null,
                )
            } catch (error: Throwable) {
                _state.value = _state.value.copy(error = error.message ?: "保存任务失败")
            }
        }
    }

    fun toggleCompleted(task: ExecutionTask) {
        viewModelScope.launch {
            try {
                repository.updateTask(
                    task = task,
                    deviceId = deviceIdentityStore.deviceId(),
                    status = if (task.status == ExecutionTaskStatus.DONE) {
                        ExecutionTaskStatus.TODO
                    } else {
                        ExecutionTaskStatus.DONE
                    },
                )
                SyncScheduler.enqueueAfterLocalChange(appContext)
                _state.value = _state.value.copy(
                    message = if (task.status == ExecutionTaskStatus.DONE) "任务已恢复" else "任务已完成",
                    error = null,
                )
            } catch (error: Throwable) {
                _state.value = _state.value.copy(error = error.message ?: "更新任务失败")
            }
        }
    }

    fun deleteTask(task: ExecutionTask) {
        val userId = activeUserId ?: return
        viewModelScope.launch {
            try {
                repository.deleteTask(userId, task.id)
                SyncScheduler.enqueueAfterLocalChange(appContext)
                _state.value = _state.value.copy(message = "任务已删除，并进入待同步队列", error = null)
            } catch (error: Throwable) {
                _state.value = _state.value.copy(error = error.message ?: "删除任务失败")
            }
        }
    }

    fun syncNow() {
        if (!_state.value.connected || _state.value.syncing) return
        viewModelScope.launch {
            _state.value = _state.value.copy(syncing = true, message = null, error = null)
            try {
                val summary = syncCoordinator.syncNow()
                _state.value = _state.value.copy(
                    syncing = false,
                    message = buildString {
                        append("同步完成")
                        if (summary.snapshotItems > 0) append(" · 恢复 ${summary.snapshotItems}")
                        if (summary.pushed > 0) append(" · 上传 ${summary.pushed}")
                        if (summary.pulled > 0) append(" · 下载 ${summary.pulled}")
                        if (summary.conflicts > 0) append(" · 冲突 ${summary.conflicts}")
                        if (summary.rejected > 0) append(" · 拒绝 ${summary.rejected}")
                    },
                )
            } catch (error: Throwable) {
                _state.value = _state.value.copy(
                    syncing = false,
                    error = error.message ?: "同步失败，本地任务不会丢失",
                )
            }
        }
    }

    fun clearFeedback() {
        if (_state.value.message != null || _state.value.error != null) {
            _state.value = _state.value.copy(message = null, error = null)
        }
    }
}
