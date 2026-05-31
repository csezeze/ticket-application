package com.turkcell.ticketapp.viewmodel

import com.turkcell.core.domain.Event
import com.turkcell.core.domain.purchase.Purchase

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val event: Event? = null,
    val quantities: Map<String, Int> = emptyMap(),
    val errorMessage: String? = null,
    val purchase: Purchase? = null,
    val isCreatingPurchase: Boolean = false,
    val isPaying: Boolean = false,
    val showPaymentDialog: Boolean = false,
    val purchaseErrorMessage: String? = null,
    val paymentSuccessMessage: String? = null,
    val isPaymentComplete: Boolean = false
) {
    val totalCents: Int
        get() = event
            ?.ticketTypes
            ?.sumOf { ticketType ->
                ticketType.priceCents * (quantities[ticketType.id] ?: 0)
            }
            ?: 0

    val canPurchase: Boolean
        get() = totalCents > 0 && !isLoading && !isCreatingPurchase && !isPaying
}
