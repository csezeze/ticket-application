package com.turkcell.ticketapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object Login
@Serializable
object Register
@Serializable
object Home

@Serializable
object MyTickets

@Serializable
data class EventDetail(val id: String)

@Serializable
data class TicketDetail(val id: String)
