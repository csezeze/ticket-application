package com.turkcell.ticketapp.viewmodel

import com.turkcell.core.domain.Event

data class HomeUiState(
    val isLoadingEvents: Boolean = false,
    val events: List<Event> = emptyList(),
    val eventErrorMessage: String? = null,
    val isLoggingOut: Boolean = false,
    val logoutErrorMessage: String? = null,
    val isLoggedOut: Boolean = false
)
