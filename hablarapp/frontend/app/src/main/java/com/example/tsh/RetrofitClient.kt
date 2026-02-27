package com.example.tsh

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // Faltaba el "Converter" en tu imagen

object RetrofitClient {
    // Pon la IP de tu PC aquí para que tu celular físico se conecte por Wi-Fi
// Cambia la línea 8 de RetrofitClient.kt a esto:
    private const val BASE_URL = "http://192.168.0.175:8000/"

    val instance: TshApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(TshApiService::class.java)
    }
}