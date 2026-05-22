package com.turkcell.data.dto

import com.turkcell.core.domain.Event
import com.turkcell.core.domain.TicketType
import kotlinx.serialization.Serializable

@Serializable
data class EventDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val venue: String? = null,
    val place: String? = null,
    val startsAt: String,
    val endsAt: String? = null,
    val createdAt: String? = null,
    val ticketTypes: List<TicketTypeDto> = emptyList()
) {
    fun toDomain(): Event {
        return Event(
            id = id,
            name = name,
            description = description,
            venue = venue ?: place ?: "Konum belirtilmemiş",
            startsAt = startsAt,
            endsAt = endsAt,
            ticketTypes = ticketTypes.map { it.toDomain() }
        )
    }
}

@Serializable
data class TicketTypeDto(
    val id: String,
    val name: String,
    val priceCents: Int,
    val capacity: Int,
    val soldCount: Int,
    val remaining: Int
) {
    fun toDomain(): TicketType {
        return TicketType(
            id = id,
            name = name,
            priceCents = priceCents,
            capacity = capacity,
            soldCount = soldCount,
            remaining = remaining
        )
    }
}