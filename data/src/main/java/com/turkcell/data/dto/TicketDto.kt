package com.turkcell.data.dto

import com.turkcell.core.domain.MyTicket
import com.turkcell.core.domain.MyTicketType
import com.turkcell.core.domain.TicketEvent
import kotlinx.serialization.Serializable

@Serializable
data class MyTicketDto(
    val id: String,
    val qrCode: String,
    val status: String,
    val usedAt: String? = null,
    val checkedInBy: String? = null,
    val ticketType: MyTicketTypeDto
) {
    fun toDomain(): MyTicket {
        return MyTicket(
            id = id,
            qrCode = qrCode,
            status = status,
            usedAt = usedAt,
            checkedInBy = checkedInBy,
            ticketType = ticketType.toDomain()
        )
    }
}

@Serializable
data class MyTicketTypeDto(
    val id: String,
    val name: String,
    val priceCents: Int,
    val event: TicketEventDto
) {
    fun toDomain(): MyTicketType {
        return MyTicketType(
            id = id,
            name = name,
            priceCents = priceCents,
            event = event.toDomain()
        )
    }
}

@Serializable
data class TicketEventDto(
    val id: String,
    val name: String,
    val venue: String,
    val startsAt: String
) {
    fun toDomain(): TicketEvent {
        return TicketEvent(
            id = id,
            name = name,
            venue = venue,
            startsAt = startsAt
        )
    }
}
