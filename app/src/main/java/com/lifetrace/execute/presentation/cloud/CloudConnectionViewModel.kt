package com.lifetrace.execute.presentation.cloud

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lifetrace.execute.BuildConfig
import com.lifetrace.execute.core.cloud.CloudApiException
import com.lifetrace.execute.core.cloud.CloudContract
import com.lifetrace.execute.core.cloud.DeviceIdentityStore
import com.lifetrace.execute.core.cloud.LifeTraceCloudClient
import com.lifetrace.execute.core.cloud.SecureSessionStore
import com.lifetrace.execute.core.cloud.StoredCloudSession
import com.lifetrace.execute.data.sync.TaskSyncCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CloudConnectionUiState(
    val connected: Boolean = false,
    val loading: Boolean = false,
    val syncing: Boolean = false,
    val baseUrl: String = "",
    val email: String = "",
    val displayName: String? = null,
    val scopes: List<String> = emptyList(),
    val protocolVersion: Int? = null,
    val schemaVersion: Int? = null,
    val lastSyncMessage: String? = null,
    val error: String? = null,
)

class CloudConnectionViewModel(application: Application) : AndroidViewModel(application) {
    private val client = LifeTraceCloudClient()
    private val sessionStore = SecureSessionStore(application)
    private val identityStore = DeviceIdentityStore(application)
    private val taskSyncCoordinator = TaskSyncCoordinator(application)

    private val _state = MutableStateFlow(sessionStore.load().toUiState())
    val state: StateFlow<CloudConnectionUiState> = _state.asStateFlow()

    fun connect(baseUrl: String, email: String, password: String) {
        if (_state.value.loading) return
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "请输入邮箱和密码")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val normalizedBaseUrl = client.normalizeBaseUrl(baseUrl)

                // Probe the authentication service first. Some older Cloud builds hard-code
                // supportedApps in this informational response, so /auth/login remains the
                // authoritative decision for whether this AppId can authenticate.
                client.authCapabilities(normalizedBaseUrl)

                val auth = client.login(
                    baseUrl = normalizedBaseUrl,
                    email = email,
                    password = password,
                    deviceId = identityStore.deviceId(),
                    deviceName = identityStore.deviceName(),
                    clientVersion = BuildConfig.VERSION_NAME,
                )

                val missingScopes = CloudContract.REQUESTED_SCOPES.toSet() - auth.scopes.toSet()
                require(missingScopes.isEmpty()) {
                    "Cloud 未授予 Execute 必需权限：${missingScopes.sorted().joinToString()}"
                }

                val syncCapabilities = client.syncCapabilities(normalizedBaseUrl)
                require(syncCapabilities.protocolVersion == CloudContract.PROTOCOL_VERSION) {
                    "Cloud Sync 协议版本不兼容：server=${syncCapabilities.protocolVersion}, client=${CloudContract.PROTOCOL_VERSION}"
                }
                val missingEntities = CloudContract.REQUIRED_SYNC_ENTITY_TYPES - syncCapabilities.supportedEntityTypes
                require(missingEntities.isEmpty()) {
                    "Cloud 缺少 Execute 必需实体：${missingEntities.sorted().joinToString()}"
                }

                val stored = StoredCloudSession(
                    baseUrl = normalizedBaseUrl,
                    accessToken = auth.accessToken,
                    refreshToken = auth.refreshToken,
                    accessTokenExpiresAtEpochSeconds = System.currentTimeMillis() / 1000L + auth.expiresInSeconds,
                    userId = auth.user.id,
                    email = auth.user.email,
                    displayName = auth.user.displayName,
                    sessionId = auth.sessionId,
                    scopes = auth.scopes,
                    protocolVersion = syncCapabilities.protocolVersion,
                    schemaVersion = syncCapabilities.schemaVersion,
                )
                sessionStore.save(stored)
                _state.value = stored.toUiState()
            } catch (error: Throwable) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = error.toUserMessage(),
                )
            }
        }
    }

    fun syncTasks() {
        if (!_state.value.connected || _state.value.syncing) return
        viewModelScope.launch {
            _state.value = _state.value.copy(syncing = true, error = null, lastSyncMessage = null)
            try {
                val summary = taskSyncCoordinator.syncNow()
                _state.value = _state.value.copy(
                    syncing = false,
                    lastSyncMessage = buildString {
                        append("任务同步完成")
                        append(" · Snapshot ${summary.snapshotItems}")
                        append(" · Push ${summary.pushed}")
                        append(" · Pull ${summary.pulled}")
                        if (summary.conflicts > 0) append(" · 冲突 ${summary.conflicts}")
                        if (summary.rejected > 0) append(" · 拒绝 ${summary.rejected}")
                    },
                )
            } catch (error: Throwable) {
                _state.value = _state.value.copy(
                    syncing = false,
                    error = error.toUserMessage(),
                )
            }
        }
    }

    fun disconnect() {
        if (_state.value.loading || _state.value.syncing) return
        val stored = sessionStore.load()
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            if (stored != null) {
                try {
                    client.logout(stored.baseUrl, stored.accessToken)
                } catch (_: Throwable) {
                    // Local logout must remain possible when the network is unavailable.
                }
            }
            sessionStore.clear()
            _state.value = CloudConnectionUiState()
        }
    }

    fun clearError() {
        if (_state.value.error != null) {
            _state.value = _state.value.copy(error = null)
        }
    }
}

private fun StoredCloudSession?.toUiState(): CloudConnectionUiState {
    if (this == null) return CloudConnectionUiState()
    return CloudConnectionUiState(
        connected = true,
        baseUrl = baseUrl,
        email = email,
        displayName = displayName,
        scopes = scopes,
        protocolVersion = protocolVersion,
        schemaVersion = schemaVersion,
    )
}

private fun Throwable.toUserMessage(): String = when (this) {
    is CloudApiException -> buildString {
        append(message ?: "LifeTrace Cloud 请求失败")
        code?.let { append("\n").append(it) }
    }
    is IllegalArgumentException -> message ?: "Cloud 配置无效"
    else -> message ?: "无法连接 LifeTrace Cloud"
}
