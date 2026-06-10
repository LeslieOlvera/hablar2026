package com.example.tsh.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.PurplePrimary
import com.example.tsh.ui.components.CustomTextField
import com.example.tsh.models.PacienteRequest

@Composable
fun RegistroPacienteScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var n by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var e by remember { mutableStateOf("") }
    var t by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState)) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PurplePrimary)
        }

        Text(
            "Nueva Cuenta (Paciente)",
            color = PurplePrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        CustomTextField("Nombre completo", n, { n = it }, "Juan Perez")
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField("Correo electrónico", e, { e = it }, "example@example.com")
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField("Contraseña", p, { p = it }, "*******")
        Spacer(modifier = Modifier.height(16.dp))


        CustomTextField("Cédula del Terapeuta", t, { t = it }, "12345678")

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val cedulaInt = t.toIntOrNull()

                if (n.isNotEmpty() && e.isNotEmpty() && p.isNotEmpty() && cedulaInt != null) {
                    val datos = PacienteRequest(
                        nombreP = n,
                        correoP = e,
                        contrasenaP = p,
                        estrella = 0,
                        fk_idCedula = cedulaInt
                    )

                    com.example.tsh.RetrofitClient.instance.registrarPaciente(datos).enqueue(object : retrofit2.Callback<Void> {
                        override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                // Error si la cédula no existe en la base de datos (Error 400 que configuramos en Node)
                                Toast.makeText(context, "La cédula del terapeuta no es válida", Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                            Toast.makeText(context, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "Por favor llena todos los campos correctamente", Toast.LENGTH_SHORT).show()
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