package com.turkcell.data.repository

import com.turkcell.core.domain.purchase.Purchase
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.data.dto.CreatePurchaseRequestDto
import com.turkcell.data.dto.PurchaseItemRequestDto
import com.turkcell.data.remote.PurchaseApi
import com.turkcell.data.util.runCatchingApi

class PurchaseRepositoryImpl(
    private val purchaseApi: PurchaseApi
) : PurchaseRepository {

    override suspend fun createPurchase(
        ticketTypeQuantities: Map<String, Int>
    ): Result<Purchase> =
        runCatchingApi {
            purchaseApi.createPurchase(
                CreatePurchaseRequestDto(
                    items = ticketTypeQuantities
                        .filterValues { quantity -> quantity > 0 }
                        .map { (ticketTypeId, quantity) ->
                            PurchaseItemRequestDto(
                                ticketTypeId = ticketTypeId,
                                quantity = quantity
                            )
                        }
                )
            )
        }.map { purchaseDto ->
            purchaseDto.toDomain()
        }

    override suspend fun payPurchase(id: String): Result<Purchase> =
        runCatchingApi {
            purchaseApi.payPurchase(id = id)
        }.map { purchaseDto ->
            purchaseDto.toDomain()
        }

    override suspend fun getPurchase(id: String): Result<Purchase> =
        runCatchingApi {
            purchaseApi.getPurchase(id = id)
        }.map { purchaseDto ->
            purchaseDto.toDomain()
        }
}
