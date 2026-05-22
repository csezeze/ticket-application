package com.turkcell.data.remote

import com.turkcell.data.dto.EventDto
import retrofit2.http.GET
import retrofit2.http.Query

interface EventApi {
    @GET("/events")
    suspend fun getEvents(
        @Query("upcoming") upcoming: Boolean = true
    ): List<EventDto>
}
