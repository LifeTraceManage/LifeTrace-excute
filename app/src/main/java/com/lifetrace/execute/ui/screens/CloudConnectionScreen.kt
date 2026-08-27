package com.lifetrace.execute.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifetrace.execute.presentation.cloud.CloudConnectionViewModel
import com.lifetrace.execute.ui.theme.LifeBlue
import com.lifetrace.execute.ui.theme.LifeBlueSoft
import com.lifetrace.execute.ui.theme.LifeMuted
import com.lifetrace.execute.ui.theme.LifeSurface

@Composable
fun CloudConnectionScreen(
    onBack: () -> Unit,
    viewModel: CloudConnectionViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    var serverUrl by rememberSaveable { mutableStateOf(state.baseUrl.ifBlank { "https://" }) }
    var email by rememberSaveable { mutableStateOf(state.email) }
    var password by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                }
                Column {
                    Text("LifeTrace Cloud", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "账号 · 设备 · 同步协议",
                        color = LifeMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.connected) LifeBlueSoft else LifeSurface,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (state.connected) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = if (state.connected) LifeBlue else LifeMuted,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(Modifier.size(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (state.connected) "已连接" else "尚未连接",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            if (state.connected) state.baseUrl else "连接后将启用 LifeTrace 统一账号与 Sync v1",
                            color = LifeMuted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        state.error?.let { error ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (!state.connected) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("连接云端", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "服务器地址必须是 HTTPS origin，例如 https://cloud.example.com。密码只用于登录请求，不会保存到本地。",
                        color = LifeMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = {
                            serverUrl = it
                            viewModel.clearError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Cloud 地址") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("邮箱") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                    Button(
                        onClick = { viewModel.connect(serverUrl, email, password) },
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.size(8.dp))
                        }
                        Text(if (state.loading) "正在验证 Cloud…" else "连接 LifeTrace Cloud")
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LifeSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(Icons.Outlined.Security, contentDescription = null, tint = LifeBlue)
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text("安全策略", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Refresh Token 使用 Android Keystore AES-GCM 加密保存；Access Token 不写入普通明文配置；正式连接拒绝 HTTP。",
                                color = LifeMuted,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LifeSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            state.displayName?.takeIf { it.isNotBlank() } ?: state.email,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        if (!state.displayName.isNullOrBlank()) {
                            Text(state.email, color = LifeMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.protocolVersion?.let {
                                AssistChip(onClick = {}, label = { Text("Sync v$it") })
                            }
                            state.schemaVersion?.let {
                                AssistChip(onClick = {}, label = { Text("Schema v$it") })
                            }
                        }
                        Text(
                            "已通过 Auth capabilities 与 Sync capabilities 兼容性检查。",
                            color = LifeMuted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("已授权范围", style = MaterialTheme.typography.titleMedium)
                    Text(
                        state.scopes.sorted().joinToString(" · ").ifBlank { "未返回 Scope" },
                        color = LifeMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = viewModel::disconnect,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.size(8.dp))
                    }
                    Text("退出当前 Cloud 会话")
                }
            }
        }
    }
}
