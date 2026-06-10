package com.example.tsh.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.GrayInput
import com.example.tsh.PurplePrimary
import com.example.tsh.RetrofitClient
import com.example.tsh.models.LoginResponse
import com.example.tsh.models.LoginTerapeutaResponse
import com.example.tsh.ui.components.CustomTextField
import com.example.tsh.ui.components.EyeIcon
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(
    role: String,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PurplePrimary)
        }

        Text(
            text = "Inicio Sesión ($role)",
            color = PurplePrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        CustomTextField("Correo electrónico", email, { email = it }, "example@example.com")

        Spacer(modifier = Modifier.height(24.dp))

        Text("Contraseña", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    EyeIcon(passwordVisible)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GrayInput,
                unfocusedContainerColor = GrayInput,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Text(
            text = "Olvide mi contraseña",
            color = PurplePrimary,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable { onForgotPassword() },
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    val sharedPref = context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)

                    if (role == "Terapeuta") {
                        // Credenciales con llaves para Terapeuta (correoT, contrasenaT)
                        val credencialesT = mapOf(
                            "correoT" to email,
                            "contrasenaT" to password
                        )
                        RetrofitClient.instance.loginTerapeuta(credencialesT).enqueue(object : Callback<LoginTerapeutaResponse> {
                            override fun onResponse(call: Call<LoginTerapeutaResponse>, response: Response<LoginTerapeutaResponse>) {
                                if (response.isSuccessful) {
                                    val body = response.body()
                                    with(sharedPref.edit()) {
                                        putString("token", body?.token)
                                        putString("rol", "terapeuta")
                                        putInt("idCedula", body?.terapeuta?.idCedula ?: 0)
                                        putString("nombreT", body?.terapeuta?.nombreT)
                                        apply()
                                    }
                                    onLoginClick()
                                } else {
                                    Toast.makeText(context, "Credenciales de terapeuta incorrectas", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<LoginTerapeutaResponse>, t: Throwable) {
                                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        // Credenciales con llaves para Paciente (correoP, contrasenaP)
                        val credencialesP = mapOf(
                            "correoP" to email,
                            "contrasenaP" to password
                        )
                        RetrofitClient.instance.loginPaciente(credencialesP).enqueue(object : Callback<LoginResponse> {
                            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                if (response.isSuccessful) {
                                    val body = response.body()
                                    with(sharedPref.edit()) {
                                        putString("token", body?.token)
                                        putString("rol", "paciente")
                                        putInt("id_paciente", body?.paciente?.id_paciente ?: 0)
                                        putString("nombreP", body?.paciente?.nombreP)
                                        putInt("fk_idCedula", body?.paciente?.fk_idCedula ?: 0)
                                        putInt("estrella", body?.paciente?.estrella ?: 0)
                                        apply()
                                    }
                                    onLoginClick()
                                } else {
                                    if (response.code() == 404) {
                                        Toast.makeText(context, "Tratamiento finalizado o usuario no encontrado", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                } else {
                    Toast.makeText(context, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Iniciar Sesión", color = Color.White, fontSize = 18.sp)
        }
    }
}