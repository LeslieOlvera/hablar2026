

package com.example.tsh

/**
 * Esta clase empaqueta el correo para enviarlo al servidor.
 */
data class ForgotPasswordRequest(
    val email: String
)