package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.EventRepository
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.ticketapp.util.isCapacityExceeded
import com.turkcell.ticketapp.util.toPurchaseUserMessage
import com.turkcell.ticketapp.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    private val eventId: String = savedStateHandle["id"] ?: ""
    private var loadedEventId: String? = null

    init {
        if (eventId.isBlank()) {
            _state.update {
                it.copy(errorMessage = "Etkinlik bulunamadi.")
            }
        } else {
            loadEvent(forceRefresh = false)
        }
    }

    private fun loadEvent(
        forceRefresh: Boolean,
        purchaseErrorMessage: String? = null
    ) {
        if (!forceRefresh && loadedEventId == eventId && _state.value.event != null) return
        loadedEventId = eventId

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    event = null,
                    quantities = emptyMap(),
                    errorMessage = null,
                    purchase = null,
                    isCreatingPurchase = false,
                    isPaying = false,
                    showPaymentDialog = false,
                    purchaseErrorMessage = purchaseErrorMessage,
                    paymentSuccessMessage = null,
                    isPaymentComplete = false
                )
            }

            eventRepository.getEvent(eventId)
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
                quantities = current.quantities + (ticketTypeId to newQuantity),
                purchaseErrorMessage = null,
                paymentSuccessMessage = null
            )
        }
    }

    fun createPurchase() {
        val selectedItems = _state.value.quantities.filterValues { quantity -> quantity > 0 }
        if (selectedItems.isEmpty()) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isCreatingPurchase = true,
                    purchaseErrorMessage = null,
                    paymentSuccessMessage = null,
                    purchase = null,
                    showPaymentDialog = false,
                    isPaymentComplete = false
                )
            }

            purchaseRepository.createPurchase(selectedItems)
                .onSuccess { purchase ->
                    _state.update {
                        it.copy(
                            isCreatingPurchase = false,
                            purchase = purchase,
                            showPaymentDialog = true
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isCreatingPurchase = false,
                            purchaseErrorMessage = error.toPurchaseUserMessage()
                        )
                    }
                    if (error.isCapacityExceeded()) {
                        loadEvent(
                            forceRefresh = true,
                            purchaseErrorMessage = error.toPurchaseUserMessage()
                        )
                    }
                }
        }
    }

    fun dismissPaymentDialog() {
        _state.update {
            it.copy(showPaymentDialog = false)
        }
    }

    fun payPurchase() {
        val purchaseId = _state.value.purchase?.id ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isPaying = true,
                    purchaseErrorMessage = null,
                    paymentSuccessMessage = null
                )
            }

            purchaseRepository.payPurchase(purchaseId)
                .onSuccess { paidPurchase ->
                    _state.update { current ->
                        current.copy(
                            isPaying = false,
                            purchase = paidPurchase,
                            showPaymentDialog = false,
                            quantities = current.quantities.mapValues { 0 },
                            paymentSuccessMessage = "Odeme basarili. Biletlerim ekranina yonlendiriliyorsun.",
                            isPaymentComplete = true
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isPaying = false,
                            showPaymentDialog = false,
                            purchaseErrorMessage = error.toPurchaseUserMessage()
                        )
                    }
                    if (error.isCapacityExceeded()) {
                        loadEvent(
                            forceRefresh = true,
                            purchaseErrorMessage = error.toPurchaseUserMessage()
                        )
                    }
                }
        }
    }
}
