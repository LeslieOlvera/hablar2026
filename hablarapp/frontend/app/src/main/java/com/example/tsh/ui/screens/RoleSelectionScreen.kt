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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.PurpleLight
import com.example.tsh.PurplePrimary
import com.example.tsh.ui.components.PandaIcon


@Composable
fun RoleSelectionScreen(onNavigate: (String, String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(40.dp)); PandaIcon(); Spacer(modifier = Modifier.height(16.dp))
        Text("HablaR", color = PurplePrimary, fontSize = 36.sp, fontWeight = FontWeight.ExtraLight)
        Text("TSH", color = PurplePrimary, fontSize = 36.sp, fontWeight = FontWeight.Normal)
        Spacer(modifier = Modifier.height(48.dp))
        RoleSection("Paciente", { onNavigate("login", "Paciente") }, { onNavigate("registro_paciente", "Paciente") })
        Spacer(modifier = Modifier.height(32.dp))
        RoleSection("Terapeuta", { onNavigate("login", "Terapeuta") }, { onNavigate("registro_terapeuta", "Terapeuta") })
    }
}

@Composable
fun RoleSection(title: String, onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(title, color = PurplePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary), shape = RoundedCornerShape(28.dp)) { Text("Iniciar Sesión") }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRegister, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = PurpleLight), shape = RoundedCornerShape(28.dp)) { Text("Registrarme", color = PurplePrimary) }
    }
}
