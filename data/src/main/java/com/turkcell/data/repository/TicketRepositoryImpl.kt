package com.turkcell.data.repository

import com.turkcell.core.domain.MyTicket
import com.turkcell.core.domain.TicketRepository
import com.turkcell.data.remote.TicketApi
import com.turkcell.data.util.runCatchingApi

class TicketRepositoryImpl(
    private val ticketApi: TicketApi
) : TicketRepository {

    override suspend fun getMyTickets(): Result<List<MyTicket>> =
        runCatchingApi {
            ticketApi.getMyTickets()
        }.map { ticketDtos ->
            ticketDtos.map { it.toDomain() }
        }
}
