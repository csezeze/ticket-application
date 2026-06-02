package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.ticketapp.util.toPurchaseUserMessage
import com.turkcell.ticketapp.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyPurchasesViewModel(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyPurchasesUiState())
    val state: StateFlow<MyPurchasesUiState> = _state.asStateFlow()

    init {
        loadPurchases()
    }

    fun loadPurchases() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    paymentSuccessMessage = null,
                    isPaymentComplete = false
                )
            }

            purchaseRepository.getMyPurchases()
                .onSuccess { purchases ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            purchases = purchases
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

    fun continuePayment(purchaseId: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    payingPurchaseId = purchaseId,
                    errorMessage = null,
                    paymentSuccessMessage = null,
                    isPaymentComplete = false
                )
            }

            purchaseRepository.payPurchase(purchaseId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            payingPurchaseId = null,
                            paymentSuccessMessage = "Odeme basarili. Biletlerine yonlendiriliyorsun.",
                            isPaymentComplete = true
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            payingPurchaseId = null,
                            errorMessage = error.toPurchaseUserMessage()
                        )
                    }
                }
        }
    }
}
