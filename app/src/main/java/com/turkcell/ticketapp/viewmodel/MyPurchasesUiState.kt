package com.turkcell.ticketapp.viewmodel

import com.turkcell.core.domain.purchase.Purchase

data class MyPurchasesUiState(
    val isLoading: Boolean = false,
    val purchases: List<Purchase> = emptyList(),
    val payingPurchaseId: String? = null,
    val errorMessage: String? = null,
    val paymentSuccessMessage: String? = null,
    val isPaymentComplete: Boolean = false
)
