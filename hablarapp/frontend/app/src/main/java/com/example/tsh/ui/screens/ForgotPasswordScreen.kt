package com.example.tsh.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.ForgotPasswordRequest
import com.example.tsh.PurplePrimary
import com.example.tsh.RetrofitClient
import com.example.tsh.ui.components.CustomTextField
import kotlinx.coroutines.launch

// --- PANTALLA: OLVIDÉ MI CONTRASEÑA ---
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PurplePrimary, modifier = Modifier.size(32.dp))
            }
            Text("Olvide Mi Contraseña", color = PurplePrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.width(48.dp))
        }
        Spacer(modifier = Modifier.height(60.dp))
        Text("Ingresa el correo electrónico con el que ingresaste:", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 32.dp))

        CustomTextField(label = "Correo electrónico", value = email, onValueChange = { email = it }, placeholder = "example@example.com")

        Spacer(modifier = Modifier.height(80.dp))

        Button(
            onClick = {
                if (email.isNotEmpty()) {
                    scope.launch {
                        try {
                            val response = RetrofitClient.instance.sendResetEmail(ForgotPasswordRequest(email))
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Correo enviado. Revisa tu bandeja.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Ingresa un correo", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Enviar", fontSize = 18.sp, color = Color.White)
        }
    }
}