package com.example.tsh.models

// Define esto en un archivo nuevo llamado models/TherapyModels.kt
data class Ejercicio(
    val nombre: String,
    val descripcion: String,
    val imagenRes: Int? = null // Para el futuro icono del ejercicio
)

data class Nivel(
    val id: Int,
    val titulo: String,
    val subtitulo: String = "",
    val ejercicios: List<Ejercicio> // Aquí está la clave
)