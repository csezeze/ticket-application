package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventDetailViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    private var loadedEventId: String? = null

    fun loadEvent(id: String) {
        if (loadedEventId == id && _state.value.event != null) return
        loadedEventId = id

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    event = null,
                    quantities = emptyMap(),
                    errorMessage = null
                )
            }

            eventRepository.getEvent(id)
                .onSuccess { event ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            event = event,
                            quantities = event.ticketTypes.associate { ticketType ->
                                ticketType.id to 0
                            }
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

    fun increaseQuantity(ticketTypeId: String) {
        changeQuantity(ticketTypeId, step = 1)
    }

    fun decreaseQuantity(ticketTypeId: String) {
        changeQuantity(ticketTypeId, step = -1)
    }

    private fun changeQuantity(ticketTypeId: String, step: Int) {
        _state.update { current ->
            val ticketType = current.event
                ?.ticketTypes
                ?.firstOrNull { it.id == ticketTypeId }
                ?: return@update current

            val currentQuantity = current.quantities[ticketTypeId] ?: 0
            val maxQuantity = minOf(20, ticketType.remaining)
            val newQuantity = (currentQuantity + step).coerceIn(0, maxQuantity)

            current.copy(
                quantities = current.quantities + (ticketTypeId to newQuantity)
            )
        }
    }
}
