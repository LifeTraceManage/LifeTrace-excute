package com.lifetrace.execute.core.cloud

import android.content.Context

class CloudSessionManager(
    context: Context,
    private val authClient: LifeTraceCloudClient = LifeTraceCloudClient(),
) {
    private val sessionStore = SecureSessionStore(context)
    private val identityStore = DeviceIdentityStore(context)

    fun currentSession(): StoredCloudSession? = sessionStore.load()

    suspend fun requireFreshSession(forceRefresh: Boolean = false): StoredCloudSession {
        val current = sessionStore.load() ?: throw IllegalStateException("尚未连接 LifeTrace Cloud")
        val now = System.currentTimeMillis() / 1000L
        if (!forceRefresh && current.accessTokenExpiresAtEpochSeconds > now + ACCESS_TOKEN_SKEW_SECONDS) {
            return current
        }
        return refresh(current)
    }

    suspend fun <T> authorized(block: suspend (StoredCloudSession) -> T): T {
        val session = requireFreshSession()
        return try {
            block(session)
        } catch (error: CloudApiException) {
            if (!error.shouldRefreshAccessToken()) throw error
            val refreshed = requireFreshSession(forceRefresh = true)
            block(refreshed)
        }
    }

    private suspend fun refresh(current: StoredCloudSession): StoredCloudSession {
        val refreshToken = current.refreshToken
            ?: throw IllegalStateException("Cloud 会话没有可用 Refresh Token，请重新登录")
        val refreshed = authClient.refresh(
            baseUrl = current.baseUrl,
            refreshToken = refreshToken,
            deviceId = identityStore.deviceId(),
        )
        val stored = current.copy(
            accessToken = refreshed.accessToken,
            refreshToken = refreshed.refreshToken ?: current.refreshToken,
            accessTokenExpiresAtEpochSeconds =
                System.currentTimeMillis() / 1000L + refreshed.expiresInSeconds,
            userId = refreshed.user.id,
            email = refreshed.user.email,
            displayName = refreshed.user.displayName,
            sessionId = refreshed.sessionId,
            scopes = refreshed.scopes,
        )
        sessionStore.save(stored)
        return stored
    }

    companion object {
        private const val ACCESS_TOKEN_SKEW_SECONDS = 60L
    }
}

private fun CloudApiException.shouldRefreshAccessToken(): Boolean =
    code == "LIFETRACE_AUTH_ACCESS_TOKEN_EXPIRED"
