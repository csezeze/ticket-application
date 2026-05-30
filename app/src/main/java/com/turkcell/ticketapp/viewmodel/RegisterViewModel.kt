package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.AuthRepository
import com.turkcell.data.network.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                errorMessage = null,
                successMessage = null,
                isRegistered = false
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                errorMessage = null,
                successMessage = null,
                isRegistered = false
            )
        }
    }

    fun register() {
        val currentState = _uiState.value

        if (!currentState.canSubmit) {
            _uiState.update {
                it.copy(errorMessage = "Email ve sifre bos birakilamaz.")
            }
            return
        }

        if (!isValidEmail(currentState.email)) {
            _uiState.update {
                it.copy(errorMessage = "Gecerli bir email adresi gir.")
            }
            return
        }

        if (currentState.password.length !in 8..128) {
            _uiState.update {
                it.copy(errorMessage = "Sifre 8 ile 128 karakter arasinda olmali.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null,
                    isRegistered = false
                )
            }

            authRepository.register(
                email = currentState.email,
                password = currentState.password
            ).onSuccess { session ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRegistered = true,
                        successMessage = "Kayit basarili: ${session.user.email}",
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRegistered = false,
                        errorMessage = throwable.toRegisterUserMessage(),
                        successMessage = null
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$").matches(email)
    }
}

private fun Throwable.toRegisterUserMessage(): String = when (this) {
    is ApiException -> when (code) {
        409 -> "Bu email zaten kayitli"
        in 500..599 -> "Sunucu su anda cevap veremiyor"
        else -> "Kayit sirasinda hata olustu"
    }
    else -> toUserMessage()
}
