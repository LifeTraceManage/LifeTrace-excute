package com.lifetrace.execute.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifetrace.execute.ui.components.ScreenHeader
import com.lifetrace.execute.ui.components.SectionTitle
import com.lifetrace.execute.ui.components.TimelineRow
import com.lifetrace.execute.ui.model.MockData
import com.lifetrace.execute.ui.theme.LifeBlue
import com.lifetrace.execute.ui.theme.LifeBlueSoft
import com.lifetrace.execute.ui.theme.LifeMuted

@Composable
fun CalendarScreen(
    contentPadding: PaddingValues,
    onProfile: () -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(27) }
    val cells = (1..31).toList()

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
            ScreenHeader("日历", "2026年8月", onProfile)
        }
        item {
            androidx.compose.material3.Card(
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                ),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                androidx.compose.foundation.layout.Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        listOf("一", "二", "三", "四", "五", "六", "日").forEach {
                            Text(
                                it,
                                color = LifeMuted,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.padding(4.dp))
                    cells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable { selectedDay = day },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedDay == day) {
                                        Box(
                                            Modifier
                                                .size(34.dp)
                                                .background(LifeBlueSoft, CircleShape)
                                        )
                                    }
                                    Text(
                                        day.toString(),
                                        color = if (selectedDay == day) LifeBlue else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            repeat(7 - week.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionTitle("8月${selectedDay}日")
            MockData.timeline.drop(1).take(3).forEach { TimelineRow(it) }
        }
    }
}
