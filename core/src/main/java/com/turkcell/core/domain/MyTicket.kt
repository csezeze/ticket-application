package com.turkcell.core.domain

data class MyTicket(
    val id: String,
    val qrCode: String,
    val status: String,
    val usedAt: String?,
    val checkedInBy: String?,
    val ticketType: MyTicketType
)

data class MyTicketType(
    val id: String,
    val name: String,
    val priceCents: Int,
    val event: TicketEvent
)

data class TicketEvent(
    val id: String,
    val name: String,
    val venue: String,
    val startsAt: String
)
