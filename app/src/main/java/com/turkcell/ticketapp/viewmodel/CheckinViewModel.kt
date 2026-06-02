package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.checkin.CheckinRepository
import com.turkcell.ticketapp.util.toCheckinUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckinViewModel(
    private val checkinRepository: CheckinRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CheckinUiState())
    val state: StateFlow<CheckinUiState> = _state.asStateFlow()

    fun onQrCodeChange(qrCode: String) {
        _state.update {
            it.copy(
                qrCode = qrCode,
                errorMessage = null,
                result = null
            )
        }
    }

    fun toggleCamera() {
        _state.update {
            it.copy(isCameraVisible = !it.isCameraVisible)
        }
    }

    fun onQrCodeScanned(qrCode: String) {
        val scannedQrCode = qrCode.trim()
        if (scannedQrCode.isBlank() || state.value.isLoading) return

        _state.update {
            it.copy(
                qrCode = scannedQrCode,
                isCameraVisible = false,
                errorMessage = null,
                result = null
            )
        }
        scanTicket()
    }

    fun scanTicket() {
        val qrCode = state.value.qrCode.trim()
        if (qrCode.isBlank()) {
            _state.update {
                it.copy(errorMessage = "QR kod bos olamaz")
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    result = null
                )
            }

            checkinRepository.scan(qrCode)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            result = result
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toCheckinUserMessage()
                        )
                    }
                }
        }
    }
}
