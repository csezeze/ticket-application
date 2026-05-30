package com.turkcell.ticketapp.viewmodel

import com.turkcell.core.domain.Event

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val event: Event? = null,
    val quantities: Map<String, Int> = emptyMap(),
    val errorMessage: String? = null
) {
    val totalCents: Int
        get() = event
            ?.ticketTypes
            ?.sumOf { ticketType ->
                ticketType.priceCents * (quantities[ticketType.id] ?: 0)
            }
            ?: 0

    val canPurchase: Boolean
        get() = totalCents > 0 && !isLoading
}
