package com.tech.app.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StatusEnum {
    @SerialName("allowed") ALLOWED,
    @SerialName("not_allowed") NOT_ALLOWED,
    @SerialName("unknown") UNKNOWN,
    @SerialName("invalid") INVALID;
}