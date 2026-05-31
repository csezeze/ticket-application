package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketDetailViewModel(
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TicketDetailUiState())
    val state: StateFlow<TicketDetailUiState> = _state.asStateFlow()

    private var loadedTicketId: String? = null

    fun loadTicket(id: String) {
        if (loadedTicketId == id && _state.value.ticket != null) return
        loadedTicketId = id

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    ticket = null,
                    errorMessage = null
                )
            }

            ticketRepository.getTicket(id)
                .onSuccess { ticket ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            ticket = ticket
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }
}
