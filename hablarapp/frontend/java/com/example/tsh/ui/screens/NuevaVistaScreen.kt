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
import androidx.compose.ui.Alignment
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
import com.example.tsh.models.ResetPasswordRequest
import com.example.tsh.models.ResetTerapeutaRequest
import com.example.tsh.ui.components.EyeIcon
import kotlinx.coroutines.launch

@Composable
fun NuevaVistaScreen(
    email: String,
    code: String,
    isTerapeuta: Boolean,
    onBack: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var v1 by remember { mutableStateOf(false) }
    var v2 by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Atrás",
                    tint = PurplePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "Restablecer",
                color = PurplePrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Crea una nueva contraseña segura para tu cuenta.",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Nueva Contraseña",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("**********") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (v1) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { v1 = !v1 }) {
                    EyeIcon(visible = v1)
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Confirmar Nueva Contraseña",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("**********") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (v2) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { v2 = !v2 }) {
                    EyeIcon(visible = v2)
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

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (password.isNotEmpty() && password == confirmPassword) {
                    scope.launch {
                        try {
                            val response = if (isTerapeuta) {
                                RetrofitClient.instance.resetPasswordTerapeuta(
                                    ResetTerapeutaRequest(
                                        correoT = email,
                                        code = code,
                                        nuevaContrasena = password
                                    )
                                )
                            } else {
                                RetrofitClient.instance.resetPasswordPaciente(
                                    ResetPasswordRequest(
                                        correoP = email,
                                        code = code,
                                        nuevaContrasena = password
                                    )
                                )
                            }

                            if (response.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Contraseña actualizada con éxito",
                                    Toast.LENGTH_LONG
                                ).show()
                                onBack()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error al actualizar",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error de conexión",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Las contraseñas no coinciden",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Actualizar Contraseña",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}