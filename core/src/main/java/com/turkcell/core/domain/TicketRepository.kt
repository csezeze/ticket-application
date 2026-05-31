package com.turkcell.core.domain

interface TicketRepository {
    suspend fun getMyTickets(): Result<List<MyTicket>>
    suspend fun getTicket(id: String): Result<MyTicket>
}
