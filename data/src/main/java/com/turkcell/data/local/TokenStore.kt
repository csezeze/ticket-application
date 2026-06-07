package com.turkcell.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Single Source garantilemek.
private val Context.authDataStore by preferencesDataStore(name="auth_prefs")

class TokenStore(private val context: Context)
{
    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    // UI tarafından collect edilmek için
    val accessToken: Flow<String?> = context.authDataStore.data.map { it[Keys.ACCESS] }
    val refreshToken: Flow<String?> = context.authDataStore.data.map { it[Keys.REFRESH] }
    val userRole: Flow<String?> = context.authDataStore.data.map { it[Keys.USER_ROLE] }


    suspend fun save(
        access:String,
        refresh: String,
        userId: String? = null,
        userEmail: String? = null,
        userRole: String? = null
    ) {
        context.authDataStore.edit {
            prefs ->
                prefs[Keys.ACCESS] = access
                prefs[Keys.REFRESH] = refresh
                userId?.let { prefs[Keys.USER_ID] = it }
                userEmail?.let { prefs[Keys.USER_EMAIL] = it }
                userRole?.let { prefs[Keys.USER_ROLE] = it }
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS)
            prefs.remove(Keys.REFRESH)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USER_EMAIL)
            prefs.remove(Keys.USER_ROLE)
        }
    }

    fun accessTokenBlocking(): String? = runBlocking { accessToken.first() }
    fun refreshTokenBlocking(): String? = runBlocking { refreshToken.first() }
    fun saveBlocking(
        access: String,
        refresh: String,
        userId: String? = null,
        userEmail: String? = null,
        userRole: String? = null
    ) = runBlocking { save(access, refresh, userId, userEmail, userRole) }
    fun clearBlocking() = runBlocking { clear() }
}
