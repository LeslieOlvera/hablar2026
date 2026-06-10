package com.example.tsh.ui.screens

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.RetrofitClient
import com.example.tsh.models.Nivel
import com.example.tsh.models.Ejercicio
import com.example.tsh.models.PacienteData
import com.example.tsh.ui.components.CustomBottomBar
import com.example.tsh.ui.components.NivelCard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenPaciente(onLogout: () -> Unit) {
    val context = LocalContext.current

    val sharedPref = context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
    val nombrePaciente = sharedPref.getString("nombreP", "Paciente") ?: "Paciente"
    val cedulaTerapeuta = sharedPref.getInt("fk_idCedula", 0)
    val idPaciente = sharedPref.getInt("id_paciente", 0)
    val token = sharedPref.getString("token", "") ?: ""

    var selectedTab by remember { mutableStateOf("home") }
    var isOrofacial by remember { mutableStateOf(false) }
    var nivelSelected by remember { mutableStateOf<Nivel?>(null) }
    var ejercicioSelected by remember { mutableStateOf<Ejercicio?>(null) }
    var menuAbierto by remember { mutableStateOf(false) }

    var ejerciciosAsignados by remember { mutableStateOf<List<Ejercicio>>(emptyList()) }
    var cargandoTareas by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        RetrofitClient.instance.getPacienteById("Bearer $token", idPaciente).enqueue(object : Callback<PacienteData> {
            override fun onResponse(call: Call<PacienteData>, response: Response<PacienteData>) {
                if (!response.isSuccessful || response.body() == null) {
                    sharedPref.edit().clear().apply()
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Aviso de HablaR")
                        .setMessage("Su terapeuta ha terminado su tratamiento con usted, gracias por usar HablaR.")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar") { _, _ -> onLogout() }
                        .show()
                }
            }
            override fun onFailure(call: Call<PacienteData>, t: Throwable) {}
        })
    }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.getEjerciciosAsignados("Bearer $token", idPaciente)
            .enqueue(object : Callback<List<Ejercicio>> {
                override fun onResponse(call: Call<List<Ejercicio>>, response: Response<List<Ejercicio>>) {
                    if (response.isSuccessful) {
                        ejerciciosAsignados = response.body() ?: emptyList()
                    }
                    cargandoTareas = false
                }
                override fun onFailure(call: Call<List<Ejercicio>>, t: Throwable) {
                    cargandoTareas = false
                }
            })
    }

    val idsAsignados = ejerciciosAsignados.map { it.id }.toSet()

    val nivelesFonemasFiltrados = remember(ejerciciosAsignados) {
        listOf(
            Nivel(1, "ra, re, ri, ro, ru", "Vibrante simple", listOf(
                Ejercicio(1, 1, "ra", "Pronuncia ra"),
                Ejercicio(2, 1, "re", "Pronuncia re"),
                Ejercicio(3, 1, "ri", "Pronuncia ri"),
                Ejercicio(4, 1, "ro", "Pronuncia ro"),
                Ejercicio(5, 1, "ru", "Pronuncia ru")
            ).filter { it.id in idsAsignados }),

            Nivel(2, "rra, rre, rri, rro, rru", "Vibrante múltiple", listOf(
                Ejercicio(6, 2, "rra", "Pronuncia rra"),
                Ejercicio(7, 2, "rre", "Pronuncia rre"),
                Ejercicio(8, 2, "rri", "Pronuncia rri"),
                Ejercicio(9, 2, "rro", "Pronuncia rro"),
                Ejercicio(10, 2, "rru", "Pronuncia rru")
            ).filter { it.id in idsAsignados }),

            Nivel(3, "Palabras", "Articulación completa", listOf(
                Ejercicio(11, 3, "Ferrocarril", "Ferrocarril"),
                Ejercicio(12, 3, "Cigarro", "Cigarro"),
                Ejercicio(13, 3, "Barril", "Barril"),
                Ejercicio(14, 3, "Rita", "Rita"),
                Ejercicio(15, 3, "Ruso", "Ruso"),
                Ejercicio(16, 3, "Rama", "Rama"),
                Ejercicio(17, 3, "Rojo", "Rojo"),
                Ejercicio(18, 3, "Rosa", "Rosa")
            ).filter { it.id in idsAsignados })
        ).filter { it.ejercicios.isNotEmpty() }
    }

    val nivelesOrofacialesFiltrados = remember(ejerciciosAsignados) {
        listOf(
            Nivel(1, "Lengua Vertical", "Movilidad Vertical", listOf(
                Ejercicio(19, 1, "Lengua arriba", "Lengua arriba"),
                Ejercicio(20, 1, "Lengua abajo", "Lengua abajo")
            ).filter { it.id in idsAsignados }),

            Nivel(2, "Lengua Horizontal", "Movilidad Lateral", listOf(
                Ejercicio(21, 2, "Lengua izquierda", "Lengua izquierda"),
                Ejercicio(22, 2, "Lengua derecha", "Lengua derecha")
            ).filter { it.id in idsAsignados }),

            Nivel(3, "Labios", "Labios", listOf(
                Ejercicio(23, 3, "Morder labio superior", "Morder labio superior"),
                Ejercicio(24, 3, "Morder labio inferior", "Morder labio inferior")
            ).filter { it.id in idsAsignados })
        ).filter { it.ejercicios.isNotEmpty() }
    }
    val listaActual = if (isOrofacial) nivelesOrofacialesFiltrados else nivelesFonemasFiltrados

    Scaffold(
        bottomBar = { CustomBottomBar(onTabSelected = { selectedTab = it }) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                "home" -> {
                    when {
                        ejercicioSelected != null -> {
                            if (isOrofacial) {
                                AccionOrofacialScreen(
                                    ejercicio = ejercicioSelected!!,
                                    onBack = { ejercicioSelected = null }
                                )
                            } else {
                                AccionEjercicioScreen(
                                    ejercicio = ejercicioSelected!!,
                                    esOrofacial = false,
                                    onBack = { ejercicioSelected = null }
                                )
                            }
                        }
                        nivelSelected != null -> {
                            EjerciciosDelNivelScreen(
                                nivel = nivelSelected!!,
                                isOrofacial = isOrofacial,
                                onEjercicioClick = { ejercicioSelected = it },
                                onBack = { nivelSelected = null }
                            )
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    NivelesScreen(
                                        userName = nombrePaciente,
                                        doctorName = "Cédula Profesional: $cedulaTerapeuta",
                                        isOrofacial = isOrofacial,
                                        niveles = listaActual,
                                        onTabChange = { nuevoValor -> isOrofacial = nuevoValor },
                                        onLevelClick = { nuevoNivel -> nivelSelected = nuevoNivel }
                                    )

                                    if (cargandoTareas) {
                                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = Color(0xFF7C5CFC), strokeWidth = 2.dp)
                                        }
                                    } else if (ejerciciosAsignados.isEmpty()) {
                                        Text(
                                            "No hay tareas asignadas para hoy.",
                                            modifier = Modifier.padding(20.dp),
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Box {
                                        IconButton(onClick = { menuAbierto = true }) {
                                            Icon(Icons.Default.Settings, "Config", tint = Color.Gray)
                                        }
                                        DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Cerrar Sesión", color = Color.Red) },
                                                onClick = {
                                                    sharedPref.edit().clear().apply()
                                                    menuAbierto = false
                                                    onLogout()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "profile" -> PremiosScreen()
                "calendar" -> ProgresoScreen(
                    onBack = { selectedTab = "home" }
                )

            }
        }
    }
}

@Composable
fun EjerciciosDelNivelScreen(
    nivel: Nivel,
    isOrofacial: Boolean,
    onEjercicioClick: (Ejercicio) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = Color(0xFF7C5CFC))
            }
            Text(
                text = if(nivel.id == 3 && !isOrofacial) "Palabras" else "Nivel ${nivel.id}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        LazyColumn {
            items(nivel.ejercicios) { ejercicio ->
                NivelCard(
                    nivelId = nivel.id,
                    titulo = ejercicio.nombre,
                    subtitulo = "Presiona para practicar",
                    onClick = { onEjercicioClick(ejercicio) }
                )
            }
        }
    }
}