package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.EventRepository
import com.turkcell.core.domain.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        loadEvents()
        loadTickets()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingEvents = true,
                    eventErrorMessage = null
                )
            }

            eventRepository.getEvents(upcoming = true)
                .onSuccess { events ->
                    _state.update {
                        it.copy(
                            isLoadingEvents = false,
                            events = events
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingEvents = false,
                            eventErrorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    private fun loadTickets() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingTickets = true,
                    ticketErrorMessage = null
                )
            }

            ticketRepository.getMyTickets()
                .onSuccess { tickets ->
                    _state.update {
                        it.copy(
                            isLoadingTickets = false,
                            tickets = tickets
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingTickets = false,
                            ticketErrorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }
}