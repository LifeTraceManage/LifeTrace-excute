package com.lifetrace.execute.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifetrace.execute.presentation.tasks.TaskConflictUi
import com.lifetrace.execute.ui.theme.LifeMuted

@Composable
fun TaskConflictResolutionSheet(
    conflicts: List<TaskConflictUi>,
    resolvingConflictId: String?,
    onDismiss: () -> Unit,
    onKeepServer: (String) -> Unit,
    onKeepLocal: (String) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("处理任务同步冲突", style = MaterialTheme.typography.headlineSmall)
            Text(
                "冲突不会自动采用最后写入覆盖。请为每条任务明确选择云端版本或本地版本。",
                color = LifeMuted,
                style = MaterialTheme.typography.bodyMedium,
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(conflicts, key = { it.conflictId }) { conflict ->
                    ConflictCard(
                        conflict = conflict,
                        resolving = resolvingConflictId == conflict.conflictId,
                        actionsEnabled = resolvingConflictId == null,
                        onKeepServer = { onKeepServer(conflict.conflictId) },
                        onKeepLocal = { onKeepLocal(conflict.conflictId) },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConflictCard(
    conflict: TaskConflictUi,
    resolving: Boolean,
    actionsEnabled: Boolean,
    onKeepServer: () -> Unit,
    onKeepLocal: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                conflict.localTitle ?: conflict.serverTitle ?: "任务 ${conflict.taskId.take(8)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                conflict.reason.toReasonLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            ConflictVersionRow(
                label = "本地",
                value = conflict.localTitle ?: "已删除",
            )
            ConflictVersionRow(
                label = "云端",
                value = if (conflict.serverDeleted) "已删除" else conflict.serverTitle ?: "云端任务",
            )

            Text(
                "接受云端会放弃这条任务尚未同步的本地修改；保留本地会以最新云端版本为基线重新生成 changeId。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            if (resolving) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onKeepServer,
                        enabled = actionsEnabled,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("接受云端")
                    }
                    Button(
                        onClick = onKeepLocal,
                        enabled = actionsEnabled,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("保留本地")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConflictVersionRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            modifier = Modifier.weight(0.24f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            value,
            modifier = Modifier.weight(0.76f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

private fun String.toReasonLabel(): String = when (this) {
    "base_version_mismatch" -> "云端版本已发生变化"
    "client_modified_server_deleted" -> "本地修改时云端已删除"
    "client_deleted_server_modified" -> "本地删除时云端已有新修改"
    "both_deleted" -> "本地与云端都已删除"
    else -> this
}
