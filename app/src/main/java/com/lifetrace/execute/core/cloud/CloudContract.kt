package com.lifetrace.execute.core.cloud

object CloudContract {
    const val APP_ID = "lifetrace-execute-android"
    const val PLATFORM = "android"
    const val PROTOCOL_VERSION = 1
    const val SCHEMA_VERSION = 1

    val REQUESTED_SCOPES = listOf(
        "account:read",
        "account:write",
        "devices:read",
        "devices:write",
        "sync:read",
        "sync:write",
        "execution:read",
        "execution:write",
        "habits:read",
        "habits:write",
        "reviews:read",
        "reviews:write",
        "files:read",
        "files:write",
    )

    val REQUIRED_SYNC_ENTITY_TYPES = setOf(
        "execution.task",
        "execution.project",
        "execution.calendar_event",
        "execution.memo",
        "execution.reminder",
    )
}

data class CloudAuthCapabilities(
    val registrationMode: String,
    val supportedApps: Set<String>,
)

data class CloudSyncCapabilities(
    val protocolVersion: Int,
    val schemaVersion: Int,
    val supportedEntityTypes: Set<String>,
)

data class CloudUser(
    val id: String,
    val email: String,
    val displayName: String?,
)

data class CloudAuthResult(
    val accessToken: String,
    val refreshToken: String?,
    val expiresInSeconds: Long,
    val refreshExpiresInSeconds: Long?,
    val user: CloudUser,
    val sessionId: String,
    val scopes: List<String>,
)

class CloudApiException(
    val statusCode: Int,
    val code: String?,
    val retryable: Boolean,
    message: String,
) : java.io.IOException(message)
