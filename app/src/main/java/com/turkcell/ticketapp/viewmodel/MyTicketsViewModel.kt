package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyTicketsViewModel(
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyTicketsUiState())
    val state: StateFlow<MyTicketsUiState> = _state.asStateFlow()

    init {
        loadTickets()
    }

    fun refreshTickets() {
        loadTickets(isRefresh = true)
    }

    private fun loadTickets(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            ticketRepository.getMyTickets()
                .onSuccess { tickets ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            tickets = tickets
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }
}
