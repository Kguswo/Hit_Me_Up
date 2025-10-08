package com.hitmeup.backend.model

import java.time.LocalDateTime

sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String, val timestamp: LocalDateTime = LocalDateTime.now()) :
        ApiResponse<Nothing>()

    companion object {
        fun <T> success(data: T): Success<T> = Success(data)
        fun error(code: Int, message: String): Error = Error(code, message)
    }
}