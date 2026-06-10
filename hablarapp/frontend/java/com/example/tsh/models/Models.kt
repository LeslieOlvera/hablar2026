package com.example.tsh.models

// ==========================================
// 1. MODELOS DE AUTENTICACIÓN (PACIENTE)
// ==========================================

data class PacienteRequest(
    val nombreP: String,
    val correoP: String,
    val contrasenaP: String,
    val estrella: Int = 0,
    val fk_idCedula: Int
)

data class LoginResponse(
    val message: String,
    val role: String,
    val token: String,
    val paciente: PacienteData
)

data class PacienteData(
    val id_paciente: Int,
    val nombreP: String,
    val correoP: String,
    val estrella: Int,
    val fk_idCedula: Int
)

// ==========================================
// 2. MODELOS DE AUTENTICACIÓN (TERAPEUTA)
// ==========================================

data class TerapeutaRequest(
    val idCedula: Int,
    val nombreT: String,
    val correoT: String,
    val contrasenaT: String
)

data class LoginTerapeutaResponse(
    val message: String,
    val role: String,
    val token: String,
    val terapeuta: TerapeutaData
)

data class TerapeutaData(
    val idCedula: Int,
    val nombreT: String,
    val correoT: String
)

// ==========================================
// 3. MODELOS DE EJERCICIOS Y ESTRUCTURA
// ==========================================

data class Ejercicio(
    val id: Int,
    val nivel: Int,
    val nombre: String,
    val descripcion: String? = null,
    val imagenRes: Int? = null
)

data class Nivel(
    val id: Int,
    val titulo: String,
    val subtitulo: String = "",
    val ejercicios: List<Ejercicio>
)

// ==========================================
// 4. MODELOS DE PROGRESO
// ==========================================

data class ProgresoRequest(
    val id_paciente: Int,
    val id_ejercicio: Int,
    val porcentaje: Double,
    val duracion: String
)

data class ProgresoResponse(
    val message: String = "",
    val estrellasGanadas: Int = 0
)

data class ProgresoItemResponse(
    val id_realizar: Int = 0,
    val id_paciente: Int = 0,
    val id_ejercicio: Int = 0,
    val porcentaje: Double = 0.0,
    val duracion: String? = "",
    val nivel: String? = null,
    val fechaRealiza: String? = null
)

data class ProgresoSimpleRequest(
    val id_paciente: Int,
    val id_ejercicio: Int,
    val porcentaje: Double,
    val duracion: String
)

// ==========================================
// 5. ASIGNACIONES Y GESTIÓN
// ==========================================

data class AsignacionRequest(
    val fecha: String,
    val fk_terapeutaA: Int,
    val fk_paciente: Int,
    val fk_idEjercicio: Int
)

data class DefaultResponse(
    val success: Boolean? = null,
    val ok: Boolean? = null,
    val message: String? = null,
    val porcentaje: Double? = null,
    val clasificacion: String? = null,
    val confianza: Double? = null,
    val estado: String? = null,
    val id_realizar: Int? = null
)

// ==========================================
// 6. RECUPERACIÓN DE CONTRASEÑA
// ==========================================

data class SendCodeRequest(
    val correoP: String? = null,
    val correoT: String? = null
)

data class VerifyCodeRequest(
    val correoP: String? = null,
    val correoT: String? = null,
    val code: String
)

data class ResetPasswordRequest(
    val correoP: String? = null,
    val correoT: String? = null,
    val code: String,
    val nuevaContrasena: String
)

data class SendCodeTerapeutaRequest(
    val correoT: String
)

data class VerifyCodeTerapeutaRequest(
    val correoT: String,
    val code: String
)

data class ResetTerapeutaRequest(
    val correoT: String,
    val code: String,
    val nuevaContrasena: String
)

data class BoundingBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float,
    val className: String
)