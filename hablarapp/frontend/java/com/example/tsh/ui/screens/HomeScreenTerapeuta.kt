package com.example.tsh.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.R
import com.example.tsh.RetrofitClient
import com.example.tsh.models.PacienteData
import com.example.tsh.models.DefaultResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// PANTALLA PRINCIPAL (CONTENEDOR)
@Composable
fun HomeScreenTerapeuta(
    onLogout: () -> Unit,
    onAsignarClick: (Int, String) -> Unit
) {
    var vistaInterna by remember { mutableStateOf("lista") }
    var nombreSeleccionado by remember { mutableStateOf("") }
    var idSeleccionado by remember { mutableIntStateOf(0) }

    when (vistaInterna) {
        "lista" -> {
            ListaPacientesScreen(
                onProgresoClick = { nombre, id ->
                    nombreSeleccionado = nombre
                    idSeleccionado = id
                    vistaInterna = "progreso"
                },
                onAsignarClick = { id, nombre ->
                    onAsignarClick(id, nombre)
                },
                onCerrarSesion = onLogout
            )
        }
        "progreso" -> {
            ProgresoPacienteTerapeutaScreen(
                idPaciente = idSeleccionado,
                nombrePaciente = nombreSeleccionado,
                onBack = { vistaInterna = "lista" }
            )
        }
    }
}

//  PANTALLA DE LISTADO
@Composable
fun ListaPacientesScreen(
    onProgresoClick: (String, Int) -> Unit,
    onAsignarClick: (Int, String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val menuAbierto = remember { mutableStateOf(false) }

    val sharedPref = context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
    val nombreTerapeuta = sharedPref.getString("nombreT", "Doctor") ?: "Doctor"
    val idCedula = sharedPref.getInt("idCedula", 0)
    val token = sharedPref.getString("token", null)

    var listaPacientes by remember { mutableStateOf<List<PacienteData>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val cargarPacientes = {
        cargando = true
        RetrofitClient.instance.getPacientesPorTerapeuta("Bearer $token", idCedula)
            .enqueue(object : Callback<List<PacienteData>> {
                override fun onResponse(call: Call<List<PacienteData>>, response: Response<List<PacienteData>>) {
                    if (response.isSuccessful) {
                        listaPacientes = response.body() ?: emptyList()
                    }
                    cargando = false
                }
                override fun onFailure(call: Call<List<PacienteData>>, t: Throwable) {
                    cargando = false
                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    LaunchedEffect(idCedula) {
        if (token == null) {
            onCerrarSesion()
            return@LaunchedEffect
        }
        cargarPacientes()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.panda),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hola, Bienvenido", fontSize = 14.sp, color = Color.Gray)
                    Text("Dr. $nombreTerapeuta", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Box {
                    IconButton(onClick = { menuAbierto.value = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = Color.Gray)
                    }
                    DropdownMenu(
                        expanded = menuAbierto.value,
                        onDismissRequest = { menuAbierto.value = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión", color = Color.Red) },
                            onClick = {
                                sharedPref.edit().clear().apply()
                                menuAbierto.value = false
                                onCerrarSesion()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { cargarPacientes() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A62FF)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Pacientes", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8A62FF))
                }
            } else if (listaPacientes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin pacientes registrados.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(listaPacientes) { paciente ->
                        PacienteCard(
                            nombre = paciente.nombreP,
                            onProgresoClick = { onProgresoClick(paciente.nombreP, paciente.id_paciente) },
                            onAsignarClick = { onAsignarClick(paciente.id_paciente, paciente.nombreP) },
                            onEliminarClick = {
                                RetrofitClient.instance.deletePaciente("Bearer $token", paciente.id_paciente)
                                    .enqueue(object : Callback<DefaultResponse> {
                                        override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                                            if (response.isSuccessful) {
                                                Toast.makeText(context, "Paciente eliminado", Toast.LENGTH_SHORT).show()
                                                cargarPacientes()
                                            } else {
                                                Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                                            Toast.makeText(context, "Error de red al eliminar", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PacienteCard(
    nombre: String,
    onProgresoClick: () -> Unit,
    onAsignarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDED6FE))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, modifier = Modifier.size(60.dp), color = Color.White) {
                Image(painter = painterResource(id = R.drawable.panda), contentDescription = null)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.Bold, color = Color(0xFF8A62FF))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = onProgresoClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text("Progreso", color = Color(0xFF8A62FF), fontSize = 10.sp)
                    }

                    Button(
                        onClick = onAsignarClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text("Asignar", color = Color(0xFF8A62FF), fontSize = 10.sp)
                    }

                    Button(
                        onClick = onEliminarClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.8f).height(40.dp)
                    ) {
                        Text("Eliminar", color = Color.Red, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}