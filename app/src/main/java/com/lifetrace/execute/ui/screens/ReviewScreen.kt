package com.lifetrace.execute.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifetrace.execute.ui.theme.LifeBlue
import com.lifetrace.execute.ui.theme.LifeBlueSoft
import com.lifetrace.execute.ui.theme.LifeMuted
import com.lifetrace.execute.ui.theme.LifeOrangeSoft
import com.lifetrace.execute.ui.theme.LifeSurface

@Composable
fun ReviewScreen(onBack: () -> Unit) {
    var reflection by remember { mutableStateOf("") }
    var learning by remember { mutableStateOf("") }
    var tomorrow by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                }
                Column {
                    Text("每日复盘", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "8月27日 · 周四",
                        color = LifeMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LifeSurface)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = LifeBlue)
                        Spacer(Modifier.padding(5.dp))
                        Text("完成情况", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.padding(5.dp))
                    Text("任务完成 5 / 8", style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { 0.625f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = LifeBlue,
                        trackColor = LifeBlueSoft
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = reflection,
                onValueChange = { reflection = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("今日回顾") },
                placeholder = { Text("今天过得怎么样？") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            OutlinedTextField(
                value = learning,
                onValueChange = { learning = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("收获与感悟") },
                placeholder = { Text("记录今天的收获、感悟和想法") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            OutlinedTextField(
                value = tomorrow,
                onValueChange = { tomorrow = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("明日计划") },
                placeholder = { Text("为明天设置 3 件最重要的事") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = LifeOrangeSoft),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Mood, contentDescription = null)
                        Spacer(Modifier.padding(5.dp))
                        Text("心情评分", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.padding(6.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..5).forEach { score ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.StarOutline, contentDescription = null)
                                Text(score.toString(), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("完成复盘")
            }
        }
    }
}
