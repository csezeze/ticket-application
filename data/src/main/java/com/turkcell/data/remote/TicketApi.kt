package com.turkcell.data.remote

import com.turkcell.data.dto.MyTicketDto
import retrofit2.http.GET
import retrofit2.http.Path

interface TicketApi {
    @GET("/me/tickets")
    suspend fun getMyTickets(): List<MyTicketDto>

    @GET("/me/tickets/{id}")
    suspend fun getTicket(
        @Path("id") id: String
    ): MyTicketDto
}
