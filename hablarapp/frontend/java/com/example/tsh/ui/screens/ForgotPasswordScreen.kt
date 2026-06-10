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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.PurplePrimary
import com.example.tsh.RetrofitClient
import com.example.tsh.models.SendCodeRequest
import com.example.tsh.models.SendCodeTerapeutaRequest
import com.example.tsh.models.VerifyCodeRequest
import com.example.tsh.models.VerifyCodeTerapeutaRequest
import com.example.tsh.ui.components.CustomTextField
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    isTerapeuta: Boolean,
    onBack: () -> Unit,
    onNavigateToReset: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }

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
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Atrás",
                    tint = PurplePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "Olvide Mi Contraseña",
                color = PurplePrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = if (!isCodeSent) {
                "Ingresa el correo electrónico con el que ingresaste:"
            } else {
                "Ingresa el código de verificación enviado a tu correo:"
            },
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        CustomTextField(
            label = "Correo electrónico",
            value = email,
            onValueChange = { email = it },
            placeholder = "example@example.com",
            enabled = !isCodeSent
        )

        if (isCodeSent) {
            Spacer(modifier = Modifier.height(20.dp))

            CustomTextField(
                label = "Código de 6 dígitos",
                value = code,
                onValueChange = { code = it },
                placeholder = "000000"
            )
        }

        Spacer(modifier = Modifier.height(80.dp))

        Button(
            onClick = {
                val cleanEmail = email.trim()

                if (cleanEmail.isNotEmpty()) {
                    scope.launch {
                        try {
                            if (!isCodeSent) {
                                val response = if (isTerapeuta) {
                                    RetrofitClient.instance.sendCodeTerapeuta(
                                        SendCodeTerapeutaRequest(correoT = cleanEmail)
                                    )
                                } else {
                                    RetrofitClient.instance.sendCodePaciente(
                                        SendCodeRequest(correoP = cleanEmail)
                                    )
                                }

                                if (response.isSuccessful) {
                                    isCodeSent = true
                                    Toast.makeText(
                                        context,
                                        "Código enviado. Revisa tu correo.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error: El correo no existe",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                if (code.isNotEmpty()) {
                                    val response = if (isTerapeuta) {
                                        RetrofitClient.instance.verifyCodeTerapeuta(
                                            VerifyCodeTerapeutaRequest(
                                                correoT = cleanEmail,
                                                code = code
                                            )
                                        )
                                    } else {
                                        RetrofitClient.instance.verifyCodePaciente(
                                            VerifyCodeRequest(
                                                correoP = cleanEmail,
                                                code = code
                                            )
                                        )
                                    }

                                    if (response.isSuccessful) {
                                        onNavigateToReset(cleanEmail, code)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Código incorrecto",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Ingresa el código",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
                        "Ingresa un correo",
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
                text = if (!isCodeSent) "Enviar" else "Verificar Código",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}