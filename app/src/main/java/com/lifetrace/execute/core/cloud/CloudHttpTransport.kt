package com.lifetrace.execute.core.cloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CloudHttpTransport {
    suspend fun requestJson(
        method: String,
        baseUrl: String,
        path: String,
        body: JSONObject? = null,
        accessToken: String? = null,
    ): JSONObject = withContext(Dispatchers.IO) {
        val connection = URL(endpoint(baseUrl, path)).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
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
                    message = error?.optNullableString("message")
                        ?: "LifeTrace Cloud 请求失败（HTTP $status）",
                )
            }
            if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            connection.disconnect()
        }
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
        require(url.path.isBlank() || url.path == "/") {
            "Cloud 地址必须填写服务 origin，不要附加 API 路径"
        }
        return "https://${url.authority}"
    }

    private fun endpoint(baseUrl: String, path: String): String = normalizeBaseUrl(baseUrl) + path

    companion object {
        private const val CONNECT_TIMEOUT_MS = 12_000
        private const val READ_TIMEOUT_MS = 20_000
    }
}

internal fun JSONObject.optNullableString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    return optString(name).takeIf { it.isNotBlank() }
}

internal fun JSONObject.optNullableLong(name: String): Long? {
    if (!has(name) || isNull(name)) return null
    return optLong(name)
}
