package com.lifetrace.execute.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lifetrace.execute.domain.task.ExecutionTask
import com.lifetrace.execute.domain.task.ExecutionTaskPriority
import com.lifetrace.execute.domain.task.ExecutionTaskStatus
import com.lifetrace.execute.presentation.tasks.TasksUiState
import com.lifetrace.execute.ui.screens.CalendarScreen
import com.lifetrace.execute.ui.screens.CollectionScreen
import com.lifetrace.execute.ui.screens.ProfileScreen
import com.lifetrace.execute.ui.screens.ProjectsScreen
import com.lifetrace.execute.ui.screens.ReviewScreen
import com.lifetrace.execute.ui.screens.TasksContent
import com.lifetrace.execute.ui.screens.TodayScreen
import com.lifetrace.execute.ui.theme.LifeTraceExecuteTheme

private val previewPadding = PaddingValues(0.dp)

@Composable
private fun PreviewTheme(content: @Composable () -> Unit) {
    LifeTraceExecuteTheme(content)
}

@Preview(name = "Today", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TodayPreview() = PreviewTheme {
    TodayScreen(previewPadding, onProfile = {}, onReview = {})
}

@Preview(name = "Tasks", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TasksPreview() = PreviewTheme {
    TasksContent(
        contentPadding = previewPadding,
        state = TasksUiState(
            connected = true,
            loading = false,
            tasks = listOf(
                previewTask("1", "完善 LifeTrace Execute 同步", ExecutionTaskPriority.HIGH),
                previewTask("2", "检查 Android 构建", ExecutionTaskPriority.NORMAL, ExecutionTaskStatus.DONE),
            ),
        ),
        onProfile = {},
        onCloudConnection = {},
        onCreateTask = { _, _ -> },
        onUpdateTask = { _, _, _, _, _ -> },
        onToggleTask = {},
        onDeleteTask = {},
        onSync = {},
        onClearFeedback = {},
    )
}

@Preview(name = "Projects", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ProjectsPreview() = PreviewTheme {
    ProjectsScreen(previewPadding, onProfile = {})
}

@Preview(name = "Calendar", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CalendarPreview() = PreviewTheme {
    CalendarScreen(previewPadding, onProfile = {})
}

@Preview(name = "Collection", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionPreview() = PreviewTheme {
    CollectionScreen(previewPadding, onProfile = {})
}

@Preview(name = "Profile", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ProfilePreview() = PreviewTheme {
    ProfileScreen(onBack = {}, onCloud = {})
}

@Preview(name = "Review", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ReviewPreview() = PreviewTheme {
    ReviewScreen(onBack = {})
}

private fun previewTask(
    id: String,
    title: String,
    priority: ExecutionTaskPriority,
    status: ExecutionTaskStatus = ExecutionTaskStatus.TODO,
) = ExecutionTask(
    id = id,
    userId = "preview-user",
    title = title,
    status = status,
    priority = priority,
    completedAt = if (status == ExecutionTaskStatus.DONE) "2026-08-28T00:30:00Z" else null,
    createdAt = "2026-08-28T00:00:00Z",
    updatedAt = "2026-08-28T00:30:00Z",
    localVersion = 1,
)
