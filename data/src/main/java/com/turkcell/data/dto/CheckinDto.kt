package com.turkcell.data.dto

import com.turkcell.core.domain.checkin.CheckinEvent
import com.turkcell.core.domain.checkin.CheckinResult
import kotlinx.serialization.Serializable

@Serializable
data class CheckinScanRequestDto(
    val qrCode: String
)

@Serializable
data class CheckinResultDto(
    val ticketId: String,
    val ticketType: String,
    val event: CheckinEventDto,
    val checkedInAt: String
) {
    fun toDomain(): CheckinResult {
        return CheckinResult(
            ticketId = ticketId,
            ticketType = ticketType,
            event = event.toDomain(),
            checkedInAt = checkedInAt
        )
    }
}

@Serializable
data class CheckinEventDto(
    val id: String,
    val name: String,
    val venue: String? = null,
    val place: String? = null,
    val startsAt: String = ""
) {
    fun toDomain(): CheckinEvent {
        return CheckinEvent(
            id = id,
            name = name,
            venue = venue ?: place ?: "Konum belirtilmemis",
            startsAt = startsAt
        )
    }
}
