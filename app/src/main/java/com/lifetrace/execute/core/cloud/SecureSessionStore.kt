package com.lifetrace.execute.core.cloud

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class StoredCloudSession(
    val baseUrl: String,
    val accessToken: String,
    val refreshToken: String?,
    val accessTokenExpiresAtEpochSeconds: Long,
    val userId: String,
    val email: String,
    val displayName: String?,
    val sessionId: String,
    val scopes: List<String>,
    val protocolVersion: Int,
    val schemaVersion: Int,
)

class SecureSessionStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(session: StoredCloudSession) {
        val json = JSONObject()
            .put("baseUrl", session.baseUrl)
            .put("accessToken", session.accessToken)
            .put("refreshToken", session.refreshToken ?: JSONObject.NULL)
            .put("accessTokenExpiresAtEpochSeconds", session.accessTokenExpiresAtEpochSeconds)
            .put("userId", session.userId)
            .put("email", session.email)
            .put("displayName", session.displayName ?: JSONObject.NULL)
            .put("sessionId", session.sessionId)
            .put("scopes", session.scopes.toJsonArray())
            .put("protocolVersion", session.protocolVersion)
            .put("schemaVersion", session.schemaVersion)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        preferences.edit()
            .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString(KEY_PAYLOAD, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .apply()
    }

    fun load(): StoredCloudSession? {
        val ivBase64 = preferences.getString(KEY_IV, null) ?: return null
        val payloadBase64 = preferences.getString(KEY_PAYLOAD, null) ?: return null
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(128, Base64.decode(ivBase64, Base64.NO_WRAP)),
            )
            val plain = cipher.doFinal(Base64.decode(payloadBase64, Base64.NO_WRAP))
            val root = JSONObject(String(plain, Charsets.UTF_8))
            StoredCloudSession(
                baseUrl = root.getString("baseUrl"),
                accessToken = root.getString("accessToken"),
                refreshToken = root.optNullableString("refreshToken"),
                accessTokenExpiresAtEpochSeconds = root.getLong("accessTokenExpiresAtEpochSeconds"),
                userId = root.getString("userId"),
                email = root.getString("email"),
                displayName = root.optNullableString("displayName"),
                sessionId = root.getString("sessionId"),
                scopes = root.optJSONArray("scopes").toStringList(),
                protocolVersion = root.getInt("protocolVersion"),
                schemaVersion = root.getInt("schemaVersion"),
            )
        }.getOrElse {
            clear()
            null
        }
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    companion object {
        private const val PREFS_NAME = "lifetrace_cloud_session"
        private const val KEY_IV = "session_iv"
        private const val KEY_PAYLOAD = "session_payload"
        private const val KEY_ALIAS = "lifetrace_execute_cloud_session_v1"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
