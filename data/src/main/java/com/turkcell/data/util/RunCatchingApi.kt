package com.turkcell.data.util

import com.turkcell.data.network.ApiException
import com.turkcell.data.network.NetworkException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> runCatchingApi(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    val apiError = e.toApiError()
    Result.failure(
        ApiException(
            code = e.code(),
            errorMessage = apiError.message ?: e.message(),
            errorCode = apiError.code,
            cause = e
        )
    )
} catch (e: IOException) {
    Result.failure(NetworkException(e))
} catch (e: Exception) {
    Result.failure(e)
}

private data class ParsedApiError(
    val code: String?,
    val message: String?
)

private val errorJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private fun HttpException.toApiError(): ParsedApiError {
    val body = response()?.errorBody()?.string() ?: return ParsedApiError(null, null)

    return runCatching {
        val root = errorJson.parseToJsonElement(body).jsonObject
        val error = root["error"] as? JsonObject
        ParsedApiError(
            code = error?.get("code")?.jsonPrimitive?.contentOrNull,
            message = error?.get("message")?.jsonPrimitive?.contentOrNull
        )
    }.getOrDefault(ParsedApiError(null, null))
}
