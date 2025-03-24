package com.tech.app.model

import com.tech.app.model.enums.StatusEnum
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AuthenticateResponseDTO (
    val stationId: String,
    val driverToken: String,
    val status: StatusEnum
) {

    fun isValid(): Boolean {
        return isValidUUID(stationId) && isValidDriverToken(driverToken)
    }

    private fun isValidUUID(id: String): Boolean {
        return try {
            UUID.fromString(id) // Will throw an error if not a valid UUID
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun isValidDriverToken(token: String): Boolean {
        val regex = "^[A-Za-z0-9._~-]{20,80}$".toRegex()
        return token.matches(regex)
    }
}