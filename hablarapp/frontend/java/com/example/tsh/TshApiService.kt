package com.example.tsh

import com.example.tsh.models.AsignacionRequest
import com.example.tsh.models.DefaultResponse
import com.example.tsh.models.Ejercicio
import com.example.tsh.models.LoginResponse
import com.example.tsh.models.LoginTerapeutaResponse
import com.example.tsh.models.PacienteData
import com.example.tsh.models.PacienteRequest
import com.example.tsh.models.ProgresoItemResponse
import com.example.tsh.models.ProgresoResponse
import com.example.tsh.models.ProgresoSimpleRequest
import com.example.tsh.models.ResetPasswordRequest
import com.example.tsh.models.ResetTerapeutaRequest
import com.example.tsh.models.SendCodeRequest
import com.example.tsh.models.SendCodeTerapeutaRequest
import com.example.tsh.models.TerapeutaRequest
import com.example.tsh.models.VerifyCodeRequest
import com.example.tsh.models.VerifyCodeTerapeutaRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TshApiService {

    // ===============================
    // OLVIDO DE CONTRASEÑA PACIENTE
    // ===============================

    @POST("auth/paciente/send-code")
    suspend fun sendCodePaciente(
        @Body request: SendCodeRequest
    ): Response<DefaultResponse>

    @POST("auth/paciente/verify-code")
    suspend fun verifyCodePaciente(
        @Body request: VerifyCodeRequest
    ): Response<DefaultResponse>

    @POST("auth/paciente/reset-password")
    suspend fun resetPasswordPaciente(
        @Body request: ResetPasswordRequest
    ): Response<DefaultResponse>

    // ===============================
    // OLVIDO DE CONTRASEÑA TERAPEUTA
    // ===============================

    @POST("auth/terapeuta/send-code")
    suspend fun sendCodeTerapeuta(
        @Body request: SendCodeTerapeutaRequest
    ): Response<DefaultResponse>

    @POST("auth/terapeuta/verify-code")
    suspend fun verifyCodeTerapeuta(
        @Body request: VerifyCodeTerapeutaRequest
    ): Response<DefaultResponse>

    @POST("auth/terapeuta/reset-password")
    suspend fun resetPasswordTerapeuta(
        @Body request: ResetTerapeutaRequest
    ): Response<DefaultResponse>

    // ===============================
    // AUTENTICACIÓN
    // ===============================

    @POST("auth/paciente/signup")
    fun registrarPaciente(
        @Body paciente: PacienteRequest
    ): Call<Void>

    @POST("auth/paciente/login")
    fun loginPaciente(
        @Body credentials: Map<String, String>
    ): Call<LoginResponse>

    @POST("auth/terapeuta/signup")
    fun registrarTerapeuta(
        @Body terapeuta: TerapeutaRequest
    ): Call<Void>

    @POST("auth/terapeuta/login")
    fun loginTerapeuta(
        @Body credentials: Map<String, String>
    ): Call<LoginTerapeutaResponse>

    // ===============================
    // PACIENTES
    // ===============================

    @GET("pacientes/{id}")
    fun getPacienteById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<PacienteData>

    @DELETE("pacientes/{id}")
    fun deletePaciente(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<DefaultResponse>

    @GET("pacientes")
    fun getPacientesPorTerapeuta(
        @Header("Authorization") token: String,
        @Query("fk_idCedula") idCedula: Int
    ): Call<List<PacienteData>>

    // ===============================
    // EJERCICIOS Y ASIGNACIONES
    // ===============================

    @GET("pacientes/{id}/asignados")
    fun getEjerciciosAsignados(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<List<Ejercicio>>

    @POST("asignaciones/asignar")
    fun asignarEjercicios(
        @Header("Authorization") token: String,
        @Body asignaciones: List<AsignacionRequest>
    ): Call<ResponseBody>

    @POST("pacientes/guardar-progreso")
    fun guardarProgresoSimple(
        @Body request: ProgresoSimpleRequest
    ): Call<ProgresoResponse>

    // ===============================
    // CLASIFICACIÓN OROFACIAL Y FONÉTICA
    // ===============================

    @Multipart
    @POST("pacientes/subir-fonetico")
    fun subirFonetico(
        @Header("Authorization") token: String,
        @Part("id_ejercicio") idEjercicio: RequestBody,
        @Part("duracion") duracion: RequestBody,
        @Part audio: MultipartBody.Part
    ): Call<DefaultResponse>

    @Multipart
    @POST("pacientes/subir-orofacial")
    fun subirOrofacial(
        @Header("Authorization") token: String,
        @Part("id_ejercicio") idEjercicio: RequestBody,
        @Part("duracion") duracion: RequestBody,
        @Part imagen: MultipartBody.Part
    ): Call<DefaultResponse>

    // ===============================
    // PROGRESO Y CALENDARIO
    // ===============================

    @GET("pacientes/{id}/progreso-dia")
    fun getProgresoDia(
        @Header("Authorization") token: String,
        @Path("id") idPaciente: Int,
        @Query("fecha") fecha: String
    ): Call<List<ProgresoItemResponse>>

    @GET("pacientes/progreso/historial/{id}/{mes}/{anio}")
    fun getHistorialMensual(
        @Header("Authorization") token: String,
        @Path("id") idPaciente: Int,
        @Path("mes") mes: Int,
        @Path("anio") anio: Int
    ): Call<List<Int>>
}
