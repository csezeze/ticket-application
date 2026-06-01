package com.turkcell.core.domain.checkin

data class CheckinResult(
    val ticketId: String,
    val ticketType: String,
    val event: CheckinEvent,
    val checkedInAt: String
)

data class CheckinEvent(
    val id: String,
    val name: String,
    val venue: String,
    val startsAt: String
)
