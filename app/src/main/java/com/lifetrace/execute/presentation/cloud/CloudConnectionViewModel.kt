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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

 data class CloudConnectionUiState(
    val connected: Boolean = false,
    val loading: Boolean = false,
    val baseUrl: String = "",
    val email: String = "",
    val displayName: String? = null,
    val scopes: List<String> = emptyList(),
    val protocolVersion: Int? = null,
    val schemaVersion: Int? = null,
    val error: String? = null,
)

class CloudConnectionViewModel(application: Application) : AndroidViewModel(application) {
    private val client = LifeTraceCloudClient()
    private val sessionStore = SecureSessionStore(application)
    private val identityStore = DeviceIdentityStore(application)

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
                val authCapabilities = client.authCapabilities(normalizedBaseUrl)
                require(CloudContract.APP_ID in authCapabilities.supportedApps) {
                    "当前 LifeTrace Cloud 尚未启用 ${CloudContract.APP_ID}"
                }

                val auth = client.login(
                    baseUrl = normalizedBaseUrl,
                    email = email,
                    password = password,
                    deviceId = identityStore.deviceId(),
                    deviceName = identityStore.deviceName(),
                    clientVersion = BuildConfig.VERSION_NAME,
                )

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

    fun disconnect() {
        if (_state.value.loading) return
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
