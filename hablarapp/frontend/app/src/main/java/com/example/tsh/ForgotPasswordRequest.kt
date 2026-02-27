

package com.example.tsh

/**
 * Esta clase empaqueta el correo para enviarlo al servidor.
 * El nombre del parámetro "email" debe ser idéntico al que definiste en Python.
 */
data class ForgotPasswordRequest(
    val email: String
)