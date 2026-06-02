package com.turkcell.data.remote

import com.turkcell.data.dto.CheckinResultDto
import com.turkcell.data.dto.CheckinScanRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckinApi {
    @POST("/checkin/scan")
    suspend fun scan(
        @Body body: CheckinScanRequestDto
    ): CheckinResultDto
}
