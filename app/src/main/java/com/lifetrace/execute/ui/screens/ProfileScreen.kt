package com.lifetrace.execute.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lifetrace.execute.ui.theme.LifeBlue
import com.lifetrace.execute.ui.theme.LifeBlueSoft
import com.lifetrace.execute.ui.theme.LifeMuted
import com.lifetrace.execute.ui.theme.LifeSurface

private data class SettingItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onCloud: () -> Unit = {},
) {
    val settings = listOf(
        SettingItem(Icons.Outlined.Person, "个人资料", "头像、昵称与个人信息"),
        SettingItem(Icons.Outlined.Lock, "账号与安全", "登录方式、密码与安全验证"),
        SettingItem(Icons.Outlined.Devices, "设备管理", "查看已登录的设备"),
        SettingItem(Icons.Outlined.Notifications, "通知设置", "提醒、免打扰与优先级"),
        SettingItem(Icons.Outlined.Sync, "同步与数据", "同步状态、备份与恢复"),
        SettingItem(Icons.Outlined.Palette, "通用设置", "主题、语言与时区"),
        SettingItem(Icons.Outlined.Info, "关于 LifeTrace", "版本、反馈与隐私说明")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 18.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                }
                Column {
                    Text("我的", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "个人资料 · 账号 · 设置",
                        color = LifeMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = LifeSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(58.dp),
                        shape = CircleShape,
                        color = LifeBlueSoft
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.AccountCircle,
                                contentDescription = null,
                                tint = LifeBlue,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    Spacer(Modifier.size(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Alex", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "alex@example.com",
                            color = LifeMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = LifeMuted
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.clickable(onClick = onCloud),
                shape = RoundedCornerShape(16.dp),
                color = LifeBlueSoft
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "LifeTrace Cloud",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "统一账号、设备与多端 Sync v1",
                            color = LifeMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        "连接 / 管理",
                        color = LifeBlue,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        items(settings.size) { index ->
            val item = settings[index]
            Card(
                colors = CardDefaults.cardColors(containerColor = LifeSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(item.icon, contentDescription = null, tint = LifeBlue)
                    Spacer(Modifier.size(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            item.subtitle,
                            color = LifeMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = LifeMuted
                    )
                }
            }
        }
    }
}
