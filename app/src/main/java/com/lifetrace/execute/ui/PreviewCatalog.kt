package com.lifetrace.execute.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lifetrace.execute.ui.screens.CalendarScreen
import com.lifetrace.execute.ui.screens.CollectionScreen
import com.lifetrace.execute.ui.screens.ProfileScreen
import com.lifetrace.execute.ui.screens.ProjectsScreen
import com.lifetrace.execute.ui.screens.ReviewScreen
import com.lifetrace.execute.ui.screens.TasksScreen
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
    TasksScreen(previewPadding, onProfile = {})
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
    ProfileScreen(onBack = {})
}

@Preview(name = "Review", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ReviewPreview() = PreviewTheme {
    ReviewScreen(onBack = {})
}
