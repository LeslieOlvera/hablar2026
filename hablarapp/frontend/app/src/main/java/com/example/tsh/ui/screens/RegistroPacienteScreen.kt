package com.example.tsh.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.PurplePrimary
import com.example.tsh.ui.components.CustomTextField

@Composable
fun RegistroPacienteScreen(onBack: () -> Unit) {
    var n by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var e by remember { mutableStateOf("") }; var t by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PurplePrimary) }
        Text("Nueva Cuenta (Paciente)", color = PurplePrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        CustomTextField("Nombre completo", n, { n = it }, "Juan Perez")
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField("Correo electrónico", e, { e = it }, "example@example.com")
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField("Contraseña", p, { p = it }, "*******")
        Spacer(modifier = Modifier.height(16.dp))
        CustomTextField("Terapeuta", t, { t = it }, "Dr. Ejemplo")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary), shape = RoundedCornerShape(28.dp)) { Text("Registrar") }
    }
}
