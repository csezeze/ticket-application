package com.turkcell.data.dto

import com.turkcell.core.domain.purchase.Purchase
import com.turkcell.core.domain.purchase.PurchaseItem
import com.turkcell.core.domain.purchase.PurchaseStatus
import com.turkcell.core.domain.purchase.Ticket
import com.turkcell.core.domain.purchase.TicketStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreatePurchaseRequestDto(
    val items: List<PurchaseItemRequestDto>
)

@Serializable
data class PurchaseItemRequestDto(
    val ticketTypeId: String,
    val quantity: Int
)

@Serializable
data class PurchaseDto(
    val id: String,
    val userId: String? = null,
    val status: String,
    val totalCents: Int,
    val createdAt: String? = null,
    val paidAt: String? = null,
    val items: List<PurchaseItemDto> = emptyList(),
    val tickets: List<TicketDto> = emptyList()
) {
    fun toDomain(): Purchase {
        return Purchase(
            id = id,
            userId = userId,
            status = PurchaseStatus.fromApi(status),
            totalCents = totalCents,
            createdAt = createdAt,
            paidAt = paidAt,
            items = items.map { it.toDomain() },
            tickets = tickets.map { it.toDomain() }
        )
    }
}

@Serializable
data class PurchaseItemDto(
    val id: String,
    val ticketTypeId: String,
    val quantity: Int,
    val unitPriceCents: Int
) {
    fun toDomain(): PurchaseItem {
        return PurchaseItem(
            id = id,
            ticketTypeId = ticketTypeId,
            quantity = quantity,
            unitPriceCents = unitPriceCents
        )
    }
}

@Serializable
data class TicketDto(
    val id: String,
    val qrCode: String,
    val status: String,
    val ticketTypeId: String
) {
    fun toDomain(): Ticket {
        return Ticket(
            id = id,
            qrCode = qrCode,
            status = TicketStatus.fromApi(status),
            ticketTypeId = ticketTypeId
        )
    }
}
