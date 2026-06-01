package com.turkcell.data.di

import com.turkcell.core.domain.AuthRepository
import com.turkcell.core.domain.checkin.CheckinRepository
import com.turkcell.core.domain.EventRepository
import com.turkcell.core.domain.TicketRepository
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.data.local.TokenStore
import com.turkcell.data.network.AuthInterceptor
import com.turkcell.data.network.TokenAuthenticator
import com.turkcell.data.remote.AuthApi
import com.turkcell.data.remote.CheckinApi
import com.turkcell.data.remote.EventApi
import com.turkcell.data.remote.PurchaseApi
import com.turkcell.data.remote.TicketApi
import com.turkcell.data.repository.AuthRepositoryImpl
import com.turkcell.data.repository.CheckinRepositoryImpl
import com.turkcell.data.repository.EventRepositoryImpl
import com.turkcell.data.repository.PurchaseRepositoryImpl
import com.turkcell.data.repository.TicketRepositoryImpl
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val BASE_URL = "https://tickets-api.halitkalayci.com/"

private val REFRESH_CLIENT = named("refresh_client")
private val REFRESH_RETROFIT = named("refresh_retrofit")
private val REFRESH_API = named("refresh_api")

val dataModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        TokenStore(context = get())
    }

    single {
        AuthInterceptor(tokenStore = get())
    }

    single(REFRESH_CLIENT) {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single(REFRESH_RETROFIT) {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get(REFRESH_CLIENT))
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    single<AuthApi>(REFRESH_API) {
        get<Retrofit>(REFRESH_RETROFIT).create(AuthApi::class.java)
    }

    single {
        TokenAuthenticator(
            tokenStore = get(),
            refreshApiProvider = {
                get<AuthApi>(REFRESH_API)
            }
        )
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .authenticator(get<TokenAuthenticator>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    single<AuthApi> {
        get<Retrofit>().create(AuthApi::class.java)
    }

    single<EventApi> {
        get<Retrofit>().create(EventApi::class.java)
    }

    single<TicketApi> {
        get<Retrofit>().create(TicketApi::class.java)
    }

    single<PurchaseApi> {
        get<Retrofit>().create(PurchaseApi::class.java)
    }

    single<CheckinApi> {
        get<Retrofit>().create(CheckinApi::class.java)
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get(),
            tokenStore = get()
        )
    }

    single<EventRepository> {
        EventRepositoryImpl(
            eventApi = get()
        )
    }

    single<TicketRepository> {
        TicketRepositoryImpl(
            ticketApi = get()
        )
    }

    single<PurchaseRepository> {
        PurchaseRepositoryImpl(
            purchaseApi = get()
        )
    }

    single<CheckinRepository> {
        CheckinRepositoryImpl(
            checkinApi = get()
        )
    }
}
