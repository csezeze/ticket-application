package com.turkcell.ticketapp.viewmodel

import com.turkcell.core.domain.Event
import com.turkcell.core.domain.MyTicket

data class HomeUiState(
    val isLoadingEvents: Boolean = false,
    val isLoadingTickets: Boolean = false,
    val events: List<Event> = emptyList(),
    val tickets: List<MyTicket> = emptyList(),
    val eventErrorMessage: String? = null,
    val ticketErrorMessage: String? = null
)