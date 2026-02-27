package com.example.tsh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// Asegúrate de importar tu nueva pantalla
import com.example.tsh.ui.screens.HomeScreenPaciente
import com.example.tsh.ui.screens.ForgotPasswordScreen
import com.example.tsh.ui.screens.LoginScreen
import com.example.tsh.ui.screens.NuevaVistaScreen
import com.example.tsh.ui.screens.RegistroPacienteScreen
import com.example.tsh.ui.screens.RegistroTerapeutaScreen
import com.example.tsh.ui.screens.RoleSelectionScreen
import com.example.tsh.ui.screens.SplashScreen
val PurplePrimary = Color(0xFF7C5CFC)
val PurpleLight = Color(0xFFDED6FE)
val GrayInput = Color(0xFFF3F3F3)

class MainActivity : ComponentActivity() {
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
    MaterialTheme(content = content)
}

// --- NAVEGACIÓN PRINCIPAL ---
@Composable
fun MainNavigation() {
    // Iniciamos en splash para ver la animación inicial
    var currentScreen by remember { mutableStateOf("splash") }
    var selectedRole by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        when (currentScreen) {
            // CAMBIO: Al terminar el splash, vamos directo a "home_paciente"
            "splash" -> SplashScreen(onTimeout = { currentScreen = "home_paciente" })

            // TU NUEVA VISTA DE PRUEBA
            "home_paciente" -> HomeScreenPaciente()

            "roles" -> RoleSelectionScreen(onNavigate = { screen, role ->
                selectedRole = role
                currentScreen = screen
            })
            "login" -> LoginScreen(
                role = selectedRole,
                onBack = { currentScreen = "roles" },
                onForgotPassword = { currentScreen = "forgot_password" }
            )
            "registro_paciente" -> RegistroPacienteScreen(onBack = { currentScreen = "roles" })
            "registro_terapeuta" -> RegistroTerapeutaScreen(onBack = { currentScreen = "roles" })
            "forgot_password" -> ForgotPasswordScreen(onBack = { currentScreen = "login" })
            "reset_password" -> NuevaVistaScreen(onBack = { currentScreen = "login" })
        }
    }
}