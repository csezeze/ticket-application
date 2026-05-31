package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadEvents()
    }

    fun loadHomeData() {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingEvents = true,
                    eventErrorMessage = null
                )
            }

            eventRepository.getEvents(upcoming = false)
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
}
