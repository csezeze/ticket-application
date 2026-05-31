package com.turkcell.data.remote

import com.turkcell.data.dto.CreatePurchaseRequestDto
import com.turkcell.data.dto.PurchaseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PurchaseApi {
    @POST("/purchases")
    suspend fun createPurchase(
        @Body body: CreatePurchaseRequestDto
    ): PurchaseDto

    @POST("/purchases/{id}/pay")
    suspend fun payPurchase(
        @Path("id") id: String
    ): PurchaseDto

    @GET("/purchases/{id}")
    suspend fun getPurchase(
        @Path("id") id: String
    ): PurchaseDto
}
