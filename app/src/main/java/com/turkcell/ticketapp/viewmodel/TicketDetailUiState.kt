package com.turkcell.ticketapp.viewmodel

import com.turkcell.core.domain.MyTicket

data class TicketDetailUiState(
    val isLoading: Boolean = false,
    val ticket: MyTicket? = null,
    val errorMessage: String? = null
)
