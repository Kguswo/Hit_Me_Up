package com.hitmeup.backend.util

import com.hitmeup.backend.model.ApiResponse
import org.springframework.http.ResponseEntity

fun <T> T.toSuccessResponse(): ResponseEntity<ApiResponse<T>> {
    return ResponseEntity.ok(ApiResponse.success(this))
}