package com.turkcell.ticketapp.util

import com.turkcell.data.network.ApiException
import com.turkcell.data.network.NetworkException

fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> when (code) {
        401 -> "Email veya sifre hatali"
        403 -> "Bu islem icin yetkin yok"
        in 500..599 -> "Sunucu su anda cevap veremiyor"
        else -> errorMessage ?: "Beklenmeyen bir hata olustu"
    }
    is NetworkException -> "Internet baglantisi yok"
    else -> message ?: "Bilinmeyen bir hata olustu"
}

fun Throwable.toRegisterUserMessage(): String = when (this) {
    is ApiException -> when (errorCode) {
        "email_taken" -> "Bu email zaten kayitli"
        else -> when (code) {
            409 -> "Bu email zaten kayitli"
            in 500..599 -> "Sunucu su anda cevap veremiyor"
            else -> errorMessage ?: "Kayit sirasinda hata olustu"
        }
    }
    else -> toUserMessage()
}

fun Throwable.toPurchaseUserMessage(): String = when (this) {
    is ApiException -> when (errorCode) {
        "capacity_exceeded" -> "Stok yetersiz, yenile"
        "already_paid" -> "Bu satin alma zaten odenmis"
        "not_purchase_owner" -> "Bu satin alma sana ait degil"
        else -> when (code) {
            401, 403 -> "Bu islem icin tekrar giris yapman gerekebilir"
            in 500..599 -> "Sunucu su anda cevap veremiyor"
            else -> errorMessage ?: "Satin alma sirasinda hata olustu"
        }
    }
    else -> toUserMessage()
}

fun Throwable.toCheckinUserMessage(): String = when (this) {
    is ApiException -> when (errorCode) {
        "ticket_not_found" -> "Bilet bulunamadi veya QR gecersiz"
        "not_assigned" -> "Bu etkinlik icin gorevli degilsin"
        "already_used" -> "Bu bilet daha once kullanilmis"
        else -> when (code) {
            401, 403 -> "Bu islem icin STAFF veya ADMIN yetkisi gerekir"
            in 500..599 -> "Sunucu su anda cevap veremiyor"
            else -> errorMessage ?: "Bilet kontrolu sirasinda hata olustu"
        }
    }
    else -> toUserMessage()
}

fun Throwable.isCapacityExceeded(): Boolean =
    this is ApiException && errorCode == "capacity_exceeded"
