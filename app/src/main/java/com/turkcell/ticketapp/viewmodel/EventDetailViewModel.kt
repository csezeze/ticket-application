package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.EventRepository
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.data.network.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventDetailViewModel(
    private val eventRepository: EventRepository,
    private val purchaseRepository: PurchaseRepository
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
                    errorMessage = null,
                    purchase = null,
                    isCreatingPurchase = false,
                    isPaying = false,
                    showPaymentDialog = false,
                    purchaseErrorMessage = null,
                    paymentSuccessMessage = null,
                    isPaymentComplete = false
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
                }
        }
    }
}

private fun Throwable.toPurchaseUserMessage(): String = when (this) {
    is ApiException -> when (errorCode) {
        "capacity_exceeded" -> "Stok yetersiz, yenile"
        "already_paid" -> "Bu satin alma zaten odenmis"
        "not_purchase_owner" -> "Bu satin alma sana ait degil"
        else -> when (code) {
            401, 403 -> "Bu islem icin tekrar giris yapman gerekebilir"
            in 500..599 -> "Sunucu su anda cevap veremiyor"
            else -> errorMessage ?: "Satin alma sirasinda hata olustu"
        }
    }
    else -> toUserMessage()
}
