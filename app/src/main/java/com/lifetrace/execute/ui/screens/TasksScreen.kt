package com.lifetrace.execute.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifetrace.execute.ui.components.ScreenHeader
import com.lifetrace.execute.ui.components.SectionTitle
import com.lifetrace.execute.ui.components.TaskRow
import com.lifetrace.execute.ui.model.MockData

@Composable
fun TasksScreen(
    contentPadding: PaddingValues,
    onProfile: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("全部") }
    val filters = listOf("全部", "进行中", "等待", "已完成")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = contentPadding.calculateBottomPadding()),
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {},
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("新建任务") }
            )
        }
    ) { scaffoldPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + scaffoldPadding.calculateTopPadding() + 18.dp,
                bottom = scaffoldPadding.calculateBottomPadding() + 96.dp
            )
        ) {
            item {
                ScreenHeader("任务", "专注执行", onProfile)
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text("搜索任务") },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { label ->
                        FilterChip(
                            selected = filter == label,
                            onClick = { filter = label },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                SectionTitle("今天 · 5")
            }

            items(MockData.todayTasks.size) { index ->
                TaskRow(MockData.todayTasks[index])
            }
        }
    }
}
