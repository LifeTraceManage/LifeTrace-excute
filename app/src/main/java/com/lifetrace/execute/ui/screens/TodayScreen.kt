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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifetrace.execute.ui.components.MetricCard
import com.lifetrace.execute.ui.components.ScreenHeader
import com.lifetrace.execute.ui.components.SectionTitle
import com.lifetrace.execute.ui.components.TaskRow
import com.lifetrace.execute.ui.components.TimelineRow
import com.lifetrace.execute.ui.model.MockData
import com.lifetrace.execute.ui.theme.LifeOrange
import com.lifetrace.execute.ui.theme.LifeOrangeSoft

@Composable
fun TodayScreen(
    contentPadding: PaddingValues,
    onProfile: () -> Unit,
    onReview: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 18.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            ScreenHeader(
                title = "早上好，Alex",
                subtitle = "8月27日 · 周四",
                onProfile = onProfile
            )
        }

        item {
            SectionTitle("今日概览")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard("8", "待完成", Modifier.weight(1f))
                MetricCard("3", "今日习惯", Modifier.weight(1f))
                MetricCard("2", "进行中项目", Modifier.weight(1f))
            }
        }

        item {
            SectionTitle("今日时间线", action = "全部")
            Spacer(Modifier.height(4.dp))
            MockData.timeline.forEach { TimelineRow(it) }
        }

        item {
            SectionTitle("今日任务", action = "查看全部")
            Spacer(Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                )
            ) {
                androidx.compose.foundation.layout.Column(Modifier.padding(horizontal = 14.dp)) {
                    MockData.todayTasks.take(2).forEach { TaskRow(it) }
                }
            }
        }

        item {
            Surface(
                onClick = onReview,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = LifeOrangeSoft,
                border = androidx.compose.foundation.BorderStroke(1.dp, LifeOrange.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = LifeOrange
                    )
                    Spacer(Modifier.padding(6.dp))
                    androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                        Text("今日复盘", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "回顾今天 · 记录收获 · 安排明日",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = LifeOrange
                    )
                }
            }
        }
    }
}
