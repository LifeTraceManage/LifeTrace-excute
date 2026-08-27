package com.lifetrace.execute.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifetrace.execute.ui.model.ProjectStatus
import com.lifetrace.execute.ui.model.ProjectUi
import com.lifetrace.execute.ui.model.TaskUi
import com.lifetrace.execute.ui.model.TimelineUi
import com.lifetrace.execute.ui.theme.LifeBlue
import com.lifetrace.execute.ui.theme.LifeBlueSoft
import com.lifetrace.execute.ui.theme.LifeBorder
import com.lifetrace.execute.ui.theme.LifeGreen
import com.lifetrace.execute.ui.theme.LifeGreenSoft
import com.lifetrace.execute.ui.theme.LifeMuted
import com.lifetrace.execute.ui.theme.LifeOrange
import com.lifetrace.execute.ui.theme.LifeOrangeSoft
import com.lifetrace.execute.ui.theme.LifeSurface
import com.lifetrace.execute.ui.theme.LifeSurfaceMuted

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    onProfile: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (onProfile != null) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clickable(onClick = onProfile),
                shape = CircleShape,
                color = LifeBlueSoft
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "我的",
                        tint = LifeBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LifeSurfaceMuted),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                color = LifeBlue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                color = LifeMuted,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium
        )
        if (action != null) {
            Text(
                action,
                color = LifeBlue,
                style = MaterialTheme.typography.labelLarge,
                modifier = if (onAction != null) Modifier.clickable(onClick = onAction) else Modifier
            )
        }
    }
}

@Composable
fun TimelineRow(item: TimelineUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            item.time,
            color = LifeMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.size(width = 52.dp, height = 24.dp)
        )
        Surface(
            shape = CircleShape,
            color = if (item.completed) LifeGreen else LifeBlueSoft,
            modifier = Modifier.size(10.dp)
        ) {}
        Spacer(Modifier.size(12.dp))
        Text(
            item.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        TagPill(item.tag)
    }
}

@Composable
fun TagPill(text: String) {
    Surface(
        color = LifeSurfaceMuted,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text,
            color = LifeMuted,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun TaskRow(
    task: TaskUi,
    onToggle: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(20.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(1.dp, LifeBorder)
        ) {}
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(task.title, style = MaterialTheme.typography.bodyLarge)
            if (task.project != null) {
                Text(
                    task.project,
                    color = LifeMuted,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        Text(
            task.time,
            color = LifeMuted,
            style = MaterialTheme.typography.labelMedium
        )
    }
    HorizontalDivider(color = LifeBorder.copy(alpha = 0.75f))
}

@Composable
fun ProjectCard(
    project: ProjectUi,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LifeSurface),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LifeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    project.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                val statusText = when (project.status) {
                    ProjectStatus.ACTIVE -> "进行中"
                    ProjectStatus.PAUSED -> "已暂停"
                    ProjectStatus.DONE -> "已完成"
                }
                val statusColor = when (project.status) {
                    ProjectStatus.ACTIVE -> LifeGreen
                    ProjectStatus.PAUSED -> LifeOrange
                    ProjectStatus.DONE -> LifeMuted
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (project.status == ProjectStatus.ACTIVE) LifeGreenSoft else LifeOrangeSoft
                ) {
                    Text(
                        statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            LinearProgressIndicator(
                progress = { project.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp),
                color = LifeBlue,
                trackColor = LifeBlueSoft,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "截止 ${project.deadline}",
                    color = LifeMuted,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = LifeMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    project.memberCount.toString(),
                    color = LifeMuted,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color = LifeBlue,
    background: Color = LifeBlueSoft,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(13.dp),
                color = LifeSurface.copy(alpha = 0.82f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = tint)
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    color = LifeMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = LifeMuted)
        }
    }
}
