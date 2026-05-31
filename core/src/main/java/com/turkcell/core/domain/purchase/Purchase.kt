package com.turkcell.core.domain.purchase

data class Purchase(
    val id: String,
    val userId: String?,
    val status: PurchaseStatus,
    val totalCents: Int,
    val createdAt: String?,
    val paidAt: String?,
    val items: List<PurchaseItem>,
    val tickets: List<Ticket>
)

data class PurchaseItem(
    val id: String,
    val ticketTypeId: String,
    val quantity: Int,
    val unitPriceCents: Int
)

enum class PurchaseStatus {
    PENDING,
    PAID,
    UNKNOWN;

    companion object {
        fun fromApi(value: String): PurchaseStatus =
            entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}

data class Ticket(
    val id: String,
    val qrCode: String,
    val status: TicketStatus,
    val ticketTypeId: String
)

enum class TicketStatus {
    VALID,
    USED,
    UNKNOWN;

    companion object {
        fun fromApi(value: String): TicketStatus =
            entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}
