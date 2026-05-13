package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = null,
            successMessage = null
        )
    }

    fun register() {
        val currentState = _uiState.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Email ve şifre boş bırakılamaz."
            )
            return
        }

        if (currentState.password.length < 8) {
            _uiState.value = currentState.copy(
                errorMessage = "Şifre en az 8 karakter olmalıdır."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            val result = authRepository.register(
                email = currentState.email,
                password = currentState.password
            )

            _uiState.value = result.fold(
                onSuccess = { session ->
                    currentState.copy(
                        isLoading = false,
                        successMessage = "Kayıt başarılı: ${session.user.email}",
                        errorMessage = null
                    )
                },
                onFailure = { throwable ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Kayıt sırasında hata oluştu.",
                        successMessage = null
                    )
                }
            )
        }
    }
}