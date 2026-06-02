package com.turkcell.ticketapp.viewmodel

import com.turkcell.core.domain.checkin.CheckinResult

data class CheckinUiState(
    val qrCode: String = "",
    val isCameraVisible: Boolean = false,
    val isLoading: Boolean = false,
    val result: CheckinResult? = null,
    val errorMessage: String? = null
)
