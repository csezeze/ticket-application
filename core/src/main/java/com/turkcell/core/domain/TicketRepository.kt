package com.turkcell.core.domain

interface TicketRepository {
    suspend fun getMyTickets(): Result<List<MyTicket>>
}
