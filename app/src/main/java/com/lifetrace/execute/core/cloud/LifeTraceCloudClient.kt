package com.lifetrace.execute.core.cloud

import org.json.JSONArray
import org.json.JSONObject

class LifeTraceCloudClient(
    private val transport: CloudHttpTransport = CloudHttpTransport(),
) {
    suspend fun authCapabilities(baseUrl: String): CloudAuthCapabilities {
        val root = transport.requestJson(
            method = "GET",
            baseUrl = baseUrl,
            path = "/api/v1/auth/capabilities",
        )
        return CloudAuthCapabilities(
            registrationMode = root.optString("registrationMode"),
            supportedApps = root.optJSONArray("supportedApps").toStringSet(),
        )
    }

    suspend fun syncCapabilities(baseUrl: String): CloudSyncCapabilities {
        val root = transport.requestJson(
            method = "GET",
            baseUrl = baseUrl,
            path = "/api/v1/sync/capabilities",
        )
        return CloudSyncCapabilities(
            protocolVersion = root.optInt("protocolVersion", -1),
            schemaVersion = root.optInt("schemaVersion", -1),
            supportedEntityTypes = root.optJSONArray("supportedEntityTypes").toStringSet(),
        )
    }

    suspend fun login(
        baseUrl: String,
        email: String,
        password: String,
        deviceId: String,
        deviceName: String,
        clientVersion: String,
    ): CloudAuthResult {
        val request = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .put("appId", CloudContract.APP_ID)
            .put("deviceId", deviceId)
            .put("deviceName", deviceName)
            .put("platform", CloudContract.PLATFORM)
            .put("clientVersion", clientVersion)
            .put("requestedScopes", CloudContract.REQUESTED_SCOPES.toJsonArray())
            .put("publicDevice", false)

        return parseTokenResponse(
            transport.requestJson(
                method = "POST",
                baseUrl = baseUrl,
                path = "/api/v1/auth/login",
                body = request,
            )
        )
    }

    suspend fun refresh(
        baseUrl: String,
        refreshToken: String,
        deviceId: String,
    ): CloudAuthResult {
        val request = JSONObject()
            .put("refreshToken", refreshToken)
            .put("appId", CloudContract.APP_ID)
            .put("deviceId", deviceId)

        return parseTokenResponse(
            transport.requestJson(
                method = "POST",
                baseUrl = baseUrl,
                path = "/api/v1/auth/refresh",
                body = request,
            )
        )
    }

    suspend fun logout(baseUrl: String, accessToken: String) {
        transport.requestJson(
            method = "POST",
            baseUrl = baseUrl,
            path = "/api/v1/auth/logout",
            accessToken = accessToken,
        )
    }

    suspend fun me(baseUrl: String, accessToken: String): CloudUser {
        val user = transport.requestJson(
            method = "GET",
            baseUrl = baseUrl,
            path = "/api/v1/auth/me",
            accessToken = accessToken,
        )
        return CloudUser(
            id = user.getString("id"),
            email = user.getString("email"),
            displayName = user.optNullableString("displayName"),
        )
    }

    fun normalizeBaseUrl(raw: String): String = transport.normalizeBaseUrl(raw)

    private fun parseTokenResponse(root: JSONObject): CloudAuthResult {
        val user = root.getJSONObject("user")
        val session = root.getJSONObject("session")
        return CloudAuthResult(
            accessToken = root.getString("accessToken"),
            refreshToken = root.optNullableString("refreshToken"),
            expiresInSeconds = root.optLong("expiresIn", 0L),
            refreshExpiresInSeconds = root.optNullableLong("refreshExpiresIn"),
            user = CloudUser(
                id = user.getString("id"),
                email = user.getString("email"),
                displayName = user.optNullableString("displayName"),
            ),
            sessionId = session.getString("id"),
            scopes = root.optJSONArray("scopes").toStringList(),
        )
    }
}

internal fun Iterable<String>.toJsonArray(): JSONArray = JSONArray().also { array ->
    forEach { value -> array.put(value) }
}

internal fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val value = optString(index)
            if (value.isNotBlank()) add(value)
        }
    }
}

internal fun JSONArray?.toStringSet(): Set<String> = toStringList().toSet()
