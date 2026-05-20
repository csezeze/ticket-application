package com.turkcell.data.repository

import com.turkcell.core.domain.AuthRepository
import com.turkcell.core.domain.AuthSession
import com.turkcell.core.domain.User
import com.turkcell.core.domain.UserRole
import com.turkcell.data.dto.CredentialsDto
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

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.login(CredentialsDto(email = email, password = password))
    }.onSuccess { tokenPair ->
        tokenStore.save(
            access = tokenPair.accessToken,
            refresh = tokenPair.refreshToken
        )
    }.map { tokenPair ->
        AuthSession(
            user = User(
                tokenPair.user.id,
                tokenPair.user.email,
                UserRole.fromApi(tokenPair.user.role)
            ),
            accessToken = tokenPair.accessToken,
            refreshToken = tokenPair.refreshToken
        )
    }

    override suspend fun register(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.register(CredentialsDto(email = email, password = password))
    }.onSuccess { tokenPair ->
        tokenStore.save(
            access = tokenPair.accessToken,
            refresh = tokenPair.refreshToken
        )
    }.map { tokenPair ->
        AuthSession(
            user = User(
                tokenPair.user.id,
                tokenPair.user.email,
                UserRole.fromApi(tokenPair.user.role)
            ),
            accessToken = tokenPair.accessToken,
            refreshToken = tokenPair.refreshToken
        )
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        tokenStore.clear()
    }
}
