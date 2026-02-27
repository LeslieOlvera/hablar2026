package com.example.tsh.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.tsh.ui.components.CustomTextField
import com.example.tsh.ui.components.EyeIcon


@Composable
fun LoginScreen(role: String, onBack: () -> Unit, onForgotPassword: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PurplePrimary) }
        Text("Inicio Sesión ($role)", color = PurplePrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(40.dp))
        CustomTextField("Correo electrónico", email, { email = it }, "example@example.com")
        Spacer(modifier = Modifier.height(24.dp))
        Text("Contraseña", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { EyeIcon(passwordVisible) } },
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = GrayInput, unfocusedContainerColor = GrayInput, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp)
        )
        Text("Olvide mi contraseña", color = PurplePrimary, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clickable { onForgotPassword() }, textAlign = TextAlign.End)
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary), shape = RoundedCornerShape(28.dp)) { Text("Iniciar Sesión") }
    }
}