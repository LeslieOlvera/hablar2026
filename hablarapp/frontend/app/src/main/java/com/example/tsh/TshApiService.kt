package com.example.tsh

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TshApiService {
    // El nombre "send-reset-link" debe ser igual al @app.post de tu Python
    @POST("send-reset-link")
    suspend fun sendResetEmail(
        @Body request: ForgotPasswordRequest
    ): Response<Unit>
}