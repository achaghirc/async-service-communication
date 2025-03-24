package com.tech.app.model

import kotlinx.serialization.Serializable
import java.net.URL
import java.util.UUID


@Serializable
data class StartSessionDTO (
    val stationId: String,
    val driverId: String,
    val callbackUrl: String
) {
    companion object {
        private val DRIVER_ID_REGEX = Regex("^[A-Za-z0-9._~\\-]{20,80}\$")
    }

    fun isValid(): Boolean {
        return try {
            UUID.fromString(stationId)
            if (!DRIVER_ID_REGEX.matches(driverId)) return false
            URL(callbackUrl)
            true
        } catch (e: Exception) {
            false
        }
    }
}