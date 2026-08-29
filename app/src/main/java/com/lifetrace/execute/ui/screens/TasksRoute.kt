package com.lifetrace.execute.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifetrace.execute.presentation.tasks.TasksViewModel
import com.lifetrace.execute.ui.components.TaskConflictResolutionSheet

/**
 * Production route for the task module.
 *
 * TasksScreen remains the reusable task surface, while this route owns
 * cross-cutting task UI such as persisted sync conflict resolution.
 */
@Composable
fun TasksRoute(
    contentPadding: PaddingValues,
    onProfile: () -> Unit,
    onCloudConnection: () -> Unit,
    viewModel: TasksViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showConflicts by rememberSaveable { mutableStateOf(false) }
    var lastAutoOpenedConflictCount by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(state.conflictCount) {
        when {
            state.conflictCount == 0 -> {
                showConflicts = false
                lastAutoOpenedConflictCount = 0
            }

            state.conflictCount != lastAutoOpenedConflictCount -> {
                showConflicts = true
                lastAutoOpenedConflictCount = state.conflictCount
            }
        }
    }

    Box {
        TasksScreen(
            contentPadding = contentPadding,
            onProfile = onProfile,
            onCloudConnection = onCloudConnection,
            viewModel = viewModel,
        )

        if (state.conflictCount > 0 && !showConflicts) {
            AssistChip(
                onClick = { showConflicts = true },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 16.dp,
                        bottom = contentPadding.calculateBottomPadding() + 88.dp,
                    ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                    )
                },
                label = { Text("处理 ${state.conflictCount} 个同步冲突") },
            )
        }
    }

    if (showConflicts && state.conflicts.isNotEmpty()) {
        TaskConflictResolutionSheet(
            conflicts = state.conflicts,
            resolvingConflictId = state.resolvingConflictId,
            onDismiss = { showConflicts = false },
            onKeepServer = viewModel::keepServer,
            onKeepLocal = viewModel::keepLocal,
        )
    }
}
