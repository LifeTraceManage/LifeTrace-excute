package com.lifetrace.execute.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lifetrace.execute.ui.components.ScreenHeader
import com.lifetrace.execute.ui.components.SectionTitle
import com.lifetrace.execute.ui.model.MockData
import com.lifetrace.execute.ui.theme.LifeBlue
import com.lifetrace.execute.ui.theme.LifeBlueSoft
import com.lifetrace.execute.ui.theme.LifeMuted
import com.lifetrace.execute.ui.theme.LifeSurface

private data class QuickCapture(
    val label: String,
    val icon: ImageVector
)

@Composable
fun CollectionScreen(
    contentPadding: PaddingValues,
    onProfile: () -> Unit
) {
    val quickCaptures = listOf(
        QuickCapture("文本", Icons.Outlined.EditNote),
        QuickCapture("图片", Icons.Outlined.Image),
        QuickCapture("语音", Icons.Outlined.Mic),
        QuickCapture("链接", Icons.Outlined.Link),
        QuickCapture("文件", Icons.Outlined.InsertDriveFile),
        QuickCapture("想法", Icons.Outlined.Lightbulb)
    )

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
            ScreenHeader("收集", "随时捕捉，稍后整理", onProfile)
        }

        item {
            SectionTitle("快速收集")
            Spacer(Modifier.padding(4.dp))
            quickCaptures.chunked(3).forEach { rowItems ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LifeBlueSoft),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.size(30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.icon, contentDescription = null, tint = LifeBlue)
                                }
                                Spacer(Modifier.padding(3.dp))
                                Text(item.label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionTitle("收集箱")
            Spacer(Modifier.padding(4.dp))
            MockData.captureBuckets.forEach { bucket ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LifeSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            bucket.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "${bucket.count} ›",
                            color = LifeMuted,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
