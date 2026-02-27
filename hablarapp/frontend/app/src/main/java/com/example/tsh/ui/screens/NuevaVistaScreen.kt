package com.example.tsh.ui.screens

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.GrayInput
import com.example.tsh.PurplePrimary
import com.example.tsh.ui.components.EyeIcon

// --- NUEVA PANTALLA: RESTABLECER CONTRASEÑA ---
@Composable
fun NuevaVistaScreen(onBack: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var v1 by remember { mutableStateOf(false) }
    var v2 by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PurplePrimary, modifier = Modifier.size(32.dp))
            }
            Text("Restablecer", color = PurplePrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text("Crea una nueva contraseña segura para tu cuenta.", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 32.dp))

        Text("Nueva Contraseña", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("**********") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (v1) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton(onClick = { v1 = !v1 }) { EyeIcon(visible = v1) } },
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = GrayInput, unfocusedContainerColor = GrayInput, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Confirmar Nueva Contraseña", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("**********") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (v2) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton(onClick = { v2 = !v2 }) { EyeIcon(visible = v2) } },
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = GrayInput, unfocusedContainerColor = GrayInput, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { /* Lógica de actualización */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Actualizar Contraseña", fontSize = 18.sp, color = Color.White)
        }
    }
}