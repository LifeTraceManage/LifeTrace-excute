package com.lifetrace.execute.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifetrace.execute.domain.task.ExecutionTask
import com.lifetrace.execute.domain.task.ExecutionTaskPriority
import com.lifetrace.execute.domain.task.ExecutionTaskStatus
import com.lifetrace.execute.presentation.tasks.TasksUiState
import com.lifetrace.execute.presentation.tasks.TasksViewModel
import com.lifetrace.execute.ui.components.DateTimePickerField
import com.lifetrace.execute.ui.components.ScreenHeader
import com.lifetrace.execute.ui.components.SectionTitle
import com.lifetrace.execute.ui.theme.LifeBlue
import com.lifetrace.execute.ui.theme.LifeBlueSoft
import com.lifetrace.execute.ui.theme.LifeBorder
import com.lifetrace.execute.ui.theme.LifeMuted
import com.lifetrace.execute.ui.theme.LifeSurface
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TasksScreen(
    contentPadding: PaddingValues,
    onProfile: () -> Unit,
    onCloudConnection: () -> Unit = {},
    viewModel: TasksViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshSession()
    }

    TasksContent(
        contentPadding = contentPadding,
        state = state,
        onProfile = onProfile,
        onCloudConnection = onCloudConnection,
        onCreateTask = viewModel::createTask,
        onUpdateTask = viewModel::updateTask,
        onToggleTask = viewModel::toggleCompleted,
        onDeleteTask = viewModel::deleteTask,
        onSync = viewModel::syncNow,
        onClearFeedback = viewModel::clearFeedback,
    )
}

@Composable
fun TasksContent(
    contentPadding: PaddingValues,
    state: TasksUiState,
    onProfile: () -> Unit,
    onCloudConnection: () -> Unit,
    onCreateTask: (String, String?, ExecutionTaskPriority, String?, String?) -> Unit,
    onUpdateTask: (
        ExecutionTask,
        String,
        String?,
        ExecutionTaskStatus,
        ExecutionTaskPriority,
        String?,
        String?,
    ) -> Unit,
    onToggleTask: (ExecutionTask) -> Unit,
    onDeleteTask: (ExecutionTask) -> Unit,
    onSync: () -> Unit,
    onClearFeedback: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("全部") }
    var showNewTask by rememberSaveable { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<ExecutionTask?>(null) }
    val filters = listOf("全部", "进行中", "等待", "已完成")

    val visibleTasks = remember(state.tasks, query, filter) {
        state.tasks.filter { task ->
            val statusMatches = when (filter) {
                "进行中" -> task.status == ExecutionTaskStatus.IN_PROGRESS || task.status == ExecutionTaskStatus.TODO
                "等待" -> task.status == ExecutionTaskStatus.WAITING
                "已完成" -> task.status == ExecutionTaskStatus.DONE
                else -> true
            }
            val queryMatches = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.orEmpty().contains(query, ignoreCase = true)
            statusMatches && queryMatches
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = contentPadding.calculateBottomPadding()),
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (state.connected) showNewTask = true else onCloudConnection()
                },
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text(if (state.connected) "新建任务" else "连接 Cloud") },
            )
        },
    ) { scaffoldPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + scaffoldPadding.calculateTopPadding() + 18.dp,
                bottom = scaffoldPadding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ScreenHeader("任务", "本地优先 · 云端同步", onProfile)
                Spacer(Modifier.height(16.dp))

                if (!state.connected) {
                    CloudRequiredCard(onConnect = onCloudConnection)
                    Spacer(Modifier.height(14.dp))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Room 本地任务库", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "修改先落本地与 Outbox，网络恢复后再同步",
                                color = LifeMuted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        OutlinedButton(
                            onClick = onSync,
                            enabled = !state.syncing,
                        ) {
                            if (state.syncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(Icons.Outlined.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.size(6.dp))
                            Text(if (state.syncing) "同步中" else "同步")
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    SyncHealthCard(state)
                    Spacer(Modifier.height(14.dp))
                }

                state.message?.let { message ->
                    AssistChip(
                        onClick = onClearFeedback,
                        label = { Text(message) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
                state.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onClearFeedback),
                    ) {
                        Text(
                            error,
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text("搜索任务") },
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    filters.forEach { label ->
                        FilterChip(
                            selected = filter == label,
                            onClick = { filter = label },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                SectionTitle("任务 · ${visibleTasks.size}")
            }

            if (state.loading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                }
            } else if (state.connected && visibleTasks.isEmpty()) {
                item {
                    EmptyTasksCard(
                        hasQueryOrFilter = query.isNotBlank() || filter != "全部",
                        onCreate = { showNewTask = true },
                    )
                }
            } else {
                items(visibleTasks, key = { it.id }) { task ->
                    ExecutionTaskRow(
                        task = task,
                        onOpen = { editingTask = task },
                        onToggle = { onToggleTask(task) },
                        onDelete = { onDeleteTask(task) },
                    )
                }
            }
        }
    }

    if (showNewTask) {
        NewTaskSheet(
            onDismiss = { showNewTask = false },
            onCreate = { title, description, priority, dueAt, scheduledAt ->
                onCreateTask(title, description, priority, dueAt, scheduledAt)
                showNewTask = false
            },
        )
    }

    editingTask?.let { task ->
        EditTaskSheet(
            task = task,
            onDismiss = { editingTask = null },
            onSave = { title, description, status, priority, dueAt, scheduledAt ->
                onUpdateTask(task, title, description, status, priority, dueAt, scheduledAt)
                editingTask = null
            },
        )
    }
}

@Composable
private fun SyncHealthCard(state: TasksUiState) {
    val hasProblem = state.blockedSyncCount > 0 || state.conflictCount > 0
    val containerColor = if (hasProblem) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val primaryColor = if (hasProblem) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                if (hasProblem) "同步需要处理" else "同步状态",
                style = MaterialTheme.typography.labelLarge,
                color = primaryColor,
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                SyncHealthMetric(
                    label = "待同步",
                    value = state.pendingSyncCount,
                    modifier = Modifier.weight(1f),
                    color = primaryColor,
                )
                SyncHealthMetric(
                    label = "已阻塞",
                    value = state.blockedSyncCount,
                    modifier = Modifier.weight(1f),
                    color = primaryColor,
                )
                SyncHealthMetric(
                    label = "冲突",
                    value = state.conflictCount,
                    modifier = Modifier.weight(1f),
                    color = primaryColor,
                )
            }
            if (hasProblem) {
                Text(
                    "本地数据不会丢失；阻塞或冲突需要在后续冲突处理界面确认后才能继续上传。",
                    color = primaryColor,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else if (state.pendingSyncCount > 0) {
                Text(
                    "已进入 Outbox，网络可用时由后台同步自动上传。",
                    color = primaryColor,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text(
                    "当前没有等待上传或需要人工处理的任务变更。",
                    color = primaryColor,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun SyncHealthMetric(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(modifier = modifier) {
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = color,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
private fun CloudRequiredCard(onConnect: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LifeBlueSoft),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = LifeBlue)
                Spacer(Modifier.size(10.dp))
                Text("连接 LifeTrace Cloud", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "当前正式任务库按 Cloud 用户隔离。连接账号后，任务仍然先写入本机 Room，断网也可以继续执行。",
                color = LifeMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onConnect) {
                Text("前往连接")
            }
        }
    }
}

@Composable
private fun EmptyTasksCard(
    hasQueryOrFilter: Boolean,
    onCreate: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LifeSurface),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LifeBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                if (hasQueryOrFilter) "没有匹配的任务" else "任务列表还是空的",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                if (hasQueryOrFilter) "调整搜索词或筛选条件" else "创建第一条任务，它会立即保存到本地数据库",
                color = LifeMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!hasQueryOrFilter) {
                TextButton(onClick = onCreate) { Text("新建任务") }
            }
        }
    }
}

@Composable
private fun ExecutionTaskRow(
    task: ExecutionTask,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LifeSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LifeBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = task.status == ExecutionTaskStatus.DONE,
                onCheckedChange = { onToggle() },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
            ) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.status == ExecutionTaskStatus.DONE) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    color = if (task.status == ExecutionTaskStatus.DONE) LifeMuted else MaterialTheme.colorScheme.onSurface,
                )
                task.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        description,
                        color = LifeMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        task.status.label(),
                        color = LifeMuted,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        task.priority.label(),
                        color = priorityColor(task.priority),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    task.dueAt?.let { dueAt ->
                        Text(
                            "截止 ${formatDateTime(dueAt)}",
                            color = LifeMuted,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = "删除任务",
                    tint = LifeMuted,
                )
            }
        }
    }
}

@Composable
private fun NewTaskSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String?, ExecutionTaskPriority, String?, String?) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(ExecutionTaskPriority.NORMAL) }
    var dueAt by rememberSaveable { mutableStateOf<String?>(null) }
    var scheduledAt by rememberSaveable { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("新建任务", style = MaterialTheme.typography.headlineSmall)
            Text(
                "保存后立即写入 Room 与同步 Outbox，不需要等待网络。",
                color = LifeMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("任务标题") },
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("描述") },
                minLines = 2,
                maxLines = 4,
            )
            PrioritySelector(
                selected = priority,
                onSelected = { priority = it },
            )
            DateTimePickerField(
                label = "安排时间",
                value = scheduledAt,
                onValueChange = { scheduledAt = it },
            )
            DateTimePickerField(
                label = "截止时间",
                value = dueAt,
                onValueChange = { dueAt = it },
            )
            Button(
                onClick = {
                    onCreate(
                        title.trim(),
                        description.trim().takeIf { it.isNotEmpty() },
                        priority,
                        dueAt,
                        scheduledAt,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank(),
            ) {
                Text("保存到本地")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditTaskSheet(
    task: ExecutionTask,
    onDismiss: () -> Unit,
    onSave: (
        String,
        String?,
        ExecutionTaskStatus,
        ExecutionTaskPriority,
        String?,
        String?,
    ) -> Unit,
) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description.orEmpty()) }
    var status by remember(task.id) { mutableStateOf(task.status) }
    var priority by remember(task.id) { mutableStateOf(task.priority) }
    var dueAt by remember(task.id) { mutableStateOf(task.dueAt) }
    var scheduledAt by remember(task.id) { mutableStateOf(task.scheduledAt) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("编辑任务", style = MaterialTheme.typography.headlineSmall)
            Text(
                "修改会先保存到本机，再通过 Outbox 增量同步。",
                color = LifeMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("任务标题") },
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("描述") },
                minLines = 2,
                maxLines = 5,
            )
            StatusSelector(
                selected = status,
                onSelected = { status = it },
            )
            PrioritySelector(
                selected = priority,
                onSelected = { priority = it },
            )
            DateTimePickerField(
                label = "安排时间",
                value = scheduledAt,
                onValueChange = { scheduledAt = it },
            )
            DateTimePickerField(
                label = "截止时间",
                value = dueAt,
                onValueChange = { dueAt = it },
            )
            Button(
                onClick = {
                    onSave(
                        title.trim(),
                        description.trim().takeIf { it.isNotEmpty() },
                        status,
                        priority,
                        dueAt,
                        scheduledAt,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank(),
            ) {
                Text("保存修改")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusSelector(
    selected: ExecutionTaskStatus,
    onSelected: (ExecutionTaskStatus) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("状态", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExecutionTaskStatus.entries.take(2).forEach { item ->
                FilterChip(
                    selected = selected == item,
                    onClick = { onSelected(item) },
                    label = { Text(item.label()) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExecutionTaskStatus.entries.drop(2).forEach { item ->
                FilterChip(
                    selected = selected == item,
                    onClick = { onSelected(item) },
                    label = { Text(item.label()) },
                )
            }
        }
    }
}

@Composable
private fun PrioritySelector(
    selected: ExecutionTaskPriority,
    onSelected: (ExecutionTaskPriority) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("优先级", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExecutionTaskPriority.entries.forEach { item ->
                FilterChip(
                    selected = selected == item,
                    onClick = { onSelected(item) },
                    label = { Text(item.label()) },
                )
            }
        }
    }
}

private fun ExecutionTaskStatus.label(): String = when (this) {
    ExecutionTaskStatus.TODO -> "待开始"
    ExecutionTaskStatus.IN_PROGRESS -> "进行中"
    ExecutionTaskStatus.WAITING -> "等待"
    ExecutionTaskStatus.DONE -> "已完成"
}

private fun ExecutionTaskPriority.label(): String = when (this) {
    ExecutionTaskPriority.LOW -> "低"
    ExecutionTaskPriority.NORMAL -> "普通"
    ExecutionTaskPriority.HIGH -> "高"
    ExecutionTaskPriority.URGENT -> "紧急"
}

@Composable
private fun priorityColor(priority: ExecutionTaskPriority) = when (priority) {
    ExecutionTaskPriority.LOW -> LifeMuted
    ExecutionTaskPriority.NORMAL -> LifeBlue
    ExecutionTaskPriority.HIGH -> MaterialTheme.colorScheme.tertiary
    ExecutionTaskPriority.URGENT -> MaterialTheme.colorScheme.error
}

private fun formatDateTime(value: String): String = runCatching {
    val instant = Instant.parse(value)
    DateTimeFormatter.ofPattern("M/d HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(instant)
}.getOrDefault(value)