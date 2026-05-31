package com.turkcell.core.domain.purchase

interface PurchaseRepository {
    suspend fun createPurchase(ticketTypeQuantities: Map<String, Int>): Result<Purchase>
    suspend fun payPurchase(id: String): Result<Purchase>
    suspend fun getPurchase(id: String): Result<Purchase>
}
