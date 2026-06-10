package com.example.tsh.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.PurplePrimary
import com.example.tsh.RetrofitClient
import com.example.tsh.models.TerapeutaRequest
import com.example.tsh.ui.components.CustomTextField
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun RegistroTerapeutaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var n by remember { mutableStateOf("") }
    var e by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PurplePrimary)
        }

        Text(
            "Nueva Cuenta (Terapeuta)",
            color = PurplePrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField("Nombre completo", n, { n = it }, "Dr. Ejemplo")
        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField("Correo electrónico", e, { e = it }, "example@example.com")
        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField("Contraseña", p, { p = it }, "*******")
        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField("Cédula Profesional", c, { c = it }, "12345678")
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {

                val cedulaInt = c.toIntOrNull()

                if (n.isNotEmpty() && e.isNotEmpty() && p.isNotEmpty() && cedulaInt != null) {

                    val datosTerapeuta = TerapeutaRequest(
                        idCedula = cedulaInt, // Ahora se envía como Int
                        nombreT = n,
                        correoT = e,
                        contrasenaT = p
                    )

                    RetrofitClient.instance.registrarTerapeuta(datosTerapeuta).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else if (response.code() == 409) {
                                Toast.makeText(context, "La cédula o el correo ya existen", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error al registrar", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(context, "Sin conexión al servidor", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "Llena los campos correctamente (Cédula debe ser numérica)", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Registrar")
        }
    }
}