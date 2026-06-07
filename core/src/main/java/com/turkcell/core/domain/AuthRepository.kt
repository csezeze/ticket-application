package com.turkcell.core.domain

import kotlinx.coroutines.flow.Flow

// Soyut Sözleşme: ne yapılacağını belirtir, nasıl yapılacağını değil.
interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUserRole: Flow<UserRole?>

    suspend fun login(email : String, password: String): Result<AuthSession>
    suspend fun register(email : String, password: String): Result<AuthSession>
    suspend fun logout(): Result<Unit>
}
