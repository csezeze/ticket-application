package com.turkcell.data.repository

import com.turkcell.core.domain.AuthRepository
import com.turkcell.core.domain.AuthSession
import com.turkcell.core.domain.User
import com.turkcell.core.domain.UserRole
import com.turkcell.data.dto.CredentialsDto
import com.turkcell.data.dto.TokenPairDto
import com.turkcell.data.local.TokenStore
import com.turkcell.data.remote.AuthApi
import com.turkcell.data.util.runCatchingApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> =
        tokenStore.accessToken.map { token ->
            token != null
        }

    override val currentUserRole: Flow<UserRole?> =
        tokenStore.userRole.map { role ->
            role?.let { UserRole.fromApi(it) }
        }

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.login(CredentialsDto(email = email, password = password))
    }.onSuccess { tokenPair ->
        tokenStore.saveSession(tokenPair)
    }.map { tokenPair ->
        tokenPair.toAuthSession()
    }

    override suspend fun register(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.register(CredentialsDto(email = email, password = password))
    }.onSuccess { tokenPair ->
        tokenStore.saveSession(tokenPair)
    }.map { tokenPair ->
        tokenPair.toAuthSession()
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        tokenStore.clear()
    }

    private suspend fun TokenStore.saveSession(tokenPair: TokenPairDto) {
        save(
            access = tokenPair.accessToken,
            refresh = tokenPair.refreshToken,
            userId = tokenPair.user.id,
            userEmail = tokenPair.user.email,
            userRole = tokenPair.user.role
        )
    }

    private fun TokenPairDto.toAuthSession(): AuthSession {
        return AuthSession(
            user = User(
                id = user.id,
                email = user.email,
                role = UserRole.fromApi(user.role)
            ),
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}
