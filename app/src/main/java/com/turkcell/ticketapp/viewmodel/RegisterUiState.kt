package com.turkcell.ticketapp.viewmodel
/*
    -register ekranındaki geçici durumları tutuyor:
    -email
    -password
    -loading
   - başarı mesajı
    -hata mesajı
 */

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
