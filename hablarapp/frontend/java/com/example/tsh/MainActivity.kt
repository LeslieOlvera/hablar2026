package com.example.tsh

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.tsh.ui.screens.AsignarEjerciciosScreen
import com.example.tsh.ui.screens.ForgotPasswordScreen
import com.example.tsh.ui.screens.HomeScreenPaciente
import com.example.tsh.ui.screens.HomeScreenTerapeuta
import com.example.tsh.ui.screens.LoginScreen
import com.example.tsh.ui.screens.NuevaVistaScreen
import com.example.tsh.ui.screens.RegistroPacienteScreen
import com.example.tsh.ui.screens.RegistroTerapeutaScreen
import com.example.tsh.ui.screens.RoleSelectionScreen
import com.example.tsh.ui.screens.SplashScreen

// Colores globales
val PurplePrimary = Color(0xFF7C5CFC)
val PurpleLight = Color(0xFFDED6FE)
val GrayInput = Color(0xFFF3F3F3)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TshAppTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun TshAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigation() {
    // ESTADOS DE NAVEGACIÓN
    var currentScreen by rememberSaveable { mutableStateOf("splash") }
    var selectedRole by rememberSaveable { mutableStateOf("Paciente") }

    // ESTADOS PARA RECUPERACIÓN DE CONTRASEÑA
    var resetEmail by rememberSaveable { mutableStateOf("") }
    var resetCode by rememberSaveable { mutableStateOf("") }

    // ESTADOS PARA ASIGNACIÓN DE PACIENTES
    var selectedPacienteId by rememberSaveable { mutableStateOf(0) }
    var selectedPacienteNombre by rememberSaveable { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        when (currentScreen) {

            "splash" -> SplashScreen(
                onTimeout = {
                    currentScreen = "roles"
                }
            )

            "roles" -> RoleSelectionScreen(
                onNavigate = { screen, role ->
                    selectedRole = role
                    currentScreen = screen
                }
            )

            "login" -> LoginScreen(
                role = selectedRole,
                onBack = {
                    currentScreen = "roles"
                },
                onForgotPassword = {
                    currentScreen = "forgot_password"
                },
                onLoginClick = {
                    currentScreen = if (selectedRole == "Paciente") {
                        "home_paciente"
                    } else {
                        "home_terapeuta"
                    }
                }
            )

            "registro_paciente" -> RegistroPacienteScreen(
                onBack = {
                    currentScreen = "roles"
                }
            )

            "registro_terapeuta" -> RegistroTerapeutaScreen(
                onBack = {
                    currentScreen = "roles"
                }
            )

            "forgot_password" -> ForgotPasswordScreen(
                isTerapeuta = selectedRole == "Terapeuta",
                onBack = {
                    currentScreen = "login"
                },
                onNavigateToReset = { email, code ->
                    resetEmail = email
                    resetCode = code
                    currentScreen = "reset_password"
                }
            )

            "reset_password" -> NuevaVistaScreen(
                email = resetEmail,
                code = resetCode,
                isTerapeuta = selectedRole == "Terapeuta",
                onBack = {
                    currentScreen = "login"
                }
            )

            // --- PACIENTE ---
            "home_paciente" -> HomeScreenPaciente(
                onLogout = {
                    currentScreen = "login"
                }
            )

            // --- TERAPEUTA ---
            "home_terapeuta" -> HomeScreenTerapeuta(
                onLogout = {
                    currentScreen = "login"
                },
                onAsignarClick = { id, nombre ->
                    selectedPacienteId = id
                    selectedPacienteNombre = nombre
                    currentScreen = "asignar_ejercicios"
                }
            )

            "asignar_ejercicios" -> AsignarEjerciciosScreen(
                nombrePaciente = selectedPacienteNombre,
                pacienteId = selectedPacienteId,
                onBack = {
                    currentScreen = "home_terapeuta"
                }
            )

            else -> {
                currentScreen = "roles"
            }
        }
    }
}