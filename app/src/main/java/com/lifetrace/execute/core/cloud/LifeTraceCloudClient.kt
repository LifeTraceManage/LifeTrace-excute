package com.lifetrace.execute.core.cloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LifeTraceCloudClient {
    suspend fun authCapabilities(baseUrl: String): CloudAuthCapabilities = withContext(Dispatchers.IO) {
        val root = requestJson(
            method = "GET",
            url = endpoint(baseUrl, "/api/v1/auth/capabilities"),
        )
        CloudAuthCapabilities(
            registrationMode = root.optString("registrationMode"),
            supportedApps = root.optJSONArray("supportedApps").toStringSet(),
        )
    }

    suspend fun syncCapabilities(baseUrl: String): CloudSyncCapabilities = withContext(Dispatchers.IO) {
        val root = requestJson(
            method = "GET",
            url = endpoint(baseUrl, "/api/v1/sync/capabilities"),
        )
        CloudSyncCapabilities(
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
    ): CloudAuthResult = withContext(Dispatchers.IO) {
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

        parseTokenResponse(
            requestJson(
                method = "POST",
                url = endpoint(baseUrl, "/api/v1/auth/login"),
                body = request,
            )
        )
    }

    suspend fun refresh(
        baseUrl: String,
        refreshToken: String,
        deviceId: String,
    ): CloudAuthResult = withContext(Dispatchers.IO) {
        val request = JSONObject()
            .put("refreshToken", refreshToken)
            .put("appId", CloudContract.APP_ID)
            .put("deviceId", deviceId)

        parseTokenResponse(
            requestJson(
                method = "POST",
                url = endpoint(baseUrl, "/api/v1/auth/refresh"),
                body = request,
            )
        )
    }

    suspend fun logout(baseUrl: String, accessToken: String) = withContext(Dispatchers.IO) {
        requestJson(
            method = "POST",
            url = endpoint(baseUrl, "/api/v1/auth/logout"),
            accessToken = accessToken,
        )
    }

    suspend fun me(baseUrl: String, accessToken: String): CloudUser = withContext(Dispatchers.IO) {
        val user = requestJson(
            method = "GET",
            url = endpoint(baseUrl, "/api/v1/auth/me"),
            accessToken = accessToken,
        )
        CloudUser(
            id = user.getString("id"),
            email = user.getString("email"),
            displayName = user.optNullableString("displayName"),
        )
    }

    fun normalizeBaseUrl(raw: String): String {
        val value = raw.trim().trimEnd('/')
        require(value.isNotBlank()) { "请输入 LifeTrace Cloud 地址" }
        val url = URL(value)
        require(url.protocol.equals("https", ignoreCase = true)) {
            "LifeTrace Cloud 必须使用 HTTPS"
        }
        require(url.host.isNotBlank()) { "Cloud 地址缺少有效主机名" }
        require(url.query == null && url.ref == null) { "Cloud 地址不能包含 query 或 fragment" }
        require(url.path.isBlank() || url.path == "/") { "Cloud 地址必须填写服务 origin，不要附加 API 路径" }
        return "https://${url.authority}"
    }

    private fun endpoint(baseUrl: String, path: String): String = normalizeBaseUrl(baseUrl) + path

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

    private fun requestJson(
        method: String,
        url: String,
        body: JSONObject? = null,
        accessToken: String? = null,
    ): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 12_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "LifeTrace-Execute-Android")
            if (!accessToken.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
            }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write(body.toString())
                }
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val error = runCatching { JSONObject(text) }.getOrNull()
                throw CloudApiException(
                    statusCode = status,
                    code = error?.optNullableString("code"),
                    retryable = error?.optBoolean("retryable", false) ?: false,
                    message = error?.optNullableString("message") ?: "LifeTrace Cloud 请求失败（HTTP $status）",
                )
            }
            return if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }
}

private fun Iterable<String>.toJsonArray(): JSONArray = JSONArray().also { array ->
    forEach { value -> array.put(value) }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val value = optString(index)
            if (value.isNotBlank()) add(value)
        }
    }
}

private fun JSONArray?.toStringSet(): Set<String> = toStringList().toSet()

private fun JSONObject.optNullableString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name).takeIf { it.isNotBlank() }
}

private fun JSONObject.optNullableLong(name: String): Long? {
    if (!has(name) || isNull(name)) return null
    return optLong(name)
}
