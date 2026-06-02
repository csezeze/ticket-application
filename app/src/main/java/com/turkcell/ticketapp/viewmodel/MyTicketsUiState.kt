package com.turkcell.ticketapp.viewmodel

import com.turkcell.core.domain.MyTicket

data class MyTicketsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val tickets: List<MyTicket> = emptyList(),
    val errorMessage: String? = null
)
