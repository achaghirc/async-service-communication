package com.tech.app.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticateUserSessionDTO (
    val sessionId: String,
    val startSession: StartSessionDTO
)