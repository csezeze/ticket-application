package com.turkcell.data.repository

import com.turkcell.core.domain.Event
import com.turkcell.core.domain.EventRepository
import com.turkcell.data.remote.EventApi
import com.turkcell.data.util.runCatchingApi

class EventRepositoryImpl(
    private val eventApi: EventApi
) : EventRepository {

    override suspend fun getEvents(upcoming: Boolean): Result<List<Event>> =
        runCatchingApi {
            eventApi.getEvents(upcoming = upcoming)
        }.map { eventDtos ->
            eventDtos.map { it.toDomain() }
        }
}
