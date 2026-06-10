package com.example.tsh.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import com.example.tsh.models.AsignacionRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AsignarEjerciciosScreen(
    nombrePaciente: String,
    pacienteId: Int,
    onBack: () -> Unit
) {
    var isOrofacial by remember { mutableStateOf(false) }
    val colorMoradoPrincipal = Color(0xFF8A62FF)
    val colorMoradoClaroFondo = Color(0xFFDED6FE)
    val context = LocalContext.current


    val ejerciciosSeleccionados = remember { mutableStateListOf<Int>() }
    val nivelesFonemas = remember {
        listOf(
            Nivel(1, "ra, re, ri, ro, ru", "Vibrante simple", listOf(
                Ejercicio(1, 1, "ra", "Pronuncia ra"),
                Ejercicio(2, 1, "re", "Pronuncia re"),
                Ejercicio(3, 1, "ri", "Pronuncia ri"),
                Ejercicio(4, 1, "ro", "Pronuncia ro"),
                Ejercicio(5, 1, "ru", "Pronuncia ru")
            )),
            Nivel(2, "rra, rre, rri, rro, rru", "Vibrante múltiple", listOf(
                Ejercicio(6, 2, "rra", "Pronuncia rra"),
                Ejercicio(7, 2, "rre", "Pronuncia rre"),
                Ejercicio(8, 2, "rri", "Pronuncia rri"),
                Ejercicio(9, 2, "rro", "Pronuncia rro"),
                Ejercicio(10, 2, "rru", "Pronuncia rru")
            )),
            Nivel(3, "Palabras", "Articulación completa", listOf(
                Ejercicio(11, 3, "Ferrocarril", "Ferrocarril"),
                Ejercicio(12, 3, "Cigarro", "Cigarro"),
                Ejercicio(13, 3, "Barril", "Barril"),
                Ejercicio(14, 3, "Rita", "Rita"),
                Ejercicio(15, 3, "Ruso", "Ruso"),
                Ejercicio(16, 3, "Rama", "Rama"),
                Ejercicio(17, 3, "Rojo", "Rojo"),
                Ejercicio(18, 3, "Rosa", "Rosa")
            ))
        )
    }

    val nivelesOrofaciales = remember {
        listOf(
            Nivel(1, "Lengua Vertical", "Movilidad Vertical", listOf(
                Ejercicio(19, 1, "Lengua arriba", "Lengua arriba"),
                Ejercicio(20, 1, "Lengua abajo", "Lengua abajo")
            )),
            Nivel(2, "Lengua Horizontal", "Movilidad Lateral", listOf(
                Ejercicio(21, 2, "Lengua izquierda", "Mueve a la izquierda"),
                Ejercicio(22, 2, "Lengua derecha", "Mueve a la derecha")
            )),
            Nivel(3, "Labios", "Labios", listOf(
                Ejercicio(23, 3, "Morder labio superior", "Morder labio superior"),
                Ejercicio(24, 3, "Morder labio inferior", "Morder labio inferior")
            ))
        )
    }

    val listaMostrar = if (isOrofacial) nivelesOrofaciales else nivelesFonemas


    fun guardarAsignaciones() {
        val sharedPref = context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""
        val idTerapeuta = sharedPref.getInt("idCedula", 0)


        val idPacienteActual = pacienteId
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val listaParaEnviar = ejerciciosSeleccionados.map { idEj ->
            AsignacionRequest(
                fecha = fechaHoy,
                fk_terapeutaA = idTerapeuta,
                fk_paciente = idPacienteActual,
                fk_idEjercicio = idEj
            )
        }

        RetrofitClient.instance.asignarEjercicios("Bearer $token", listaParaEnviar)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Asignación exitosa ✅", Toast.LENGTH_SHORT).show()
                        onBack()
                    } else {
                        Toast.makeText(context, "Error en servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(context, "Error de red: Conecta el servidor", Toast.LENGTH_SHORT).show()
                }
            })
    }

    Scaffold(
        floatingActionButton = {
            if (ejerciciosSeleccionados.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { guardarAsignaciones() },
                    containerColor = colorMoradoPrincipal,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Check, null) },
                    text = { Text("Confirmar Asignación") }
                )
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                // CABECERA
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.KeyboardArrowLeft, "Volver", tint = colorMoradoPrincipal, modifier = Modifier.size(35.dp))
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Asignar Ejercicios", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colorMoradoPrincipal)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(androidx.compose.ui.res.painterResource(id = com.example.tsh.R.drawable.panda), null, tint = colorMoradoPrincipal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(nombrePaciente, fontSize = 14.sp, color = colorMoradoPrincipal)
                        }
                    }
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(30.dp))

                // SELECTOR FONEMAS / OROFACIALES
                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp).background(colorMoradoClaroFondo.copy(alpha = 0.5f), RoundedCornerShape(30.dp)).padding(6.dp)
                ) {
                    Button(
                        onClick = { isOrofacial = false },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isOrofacial) colorMoradoPrincipal else Color.Transparent),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Fonemas", color = if (!isOrofacial) Color.White else colorMoradoPrincipal, fontSize = 18.sp)
                    }
                    Button(
                        onClick = { isOrofacial = true },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isOrofacial) colorMoradoPrincipal else Color.Transparent),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Orofaciales", color = if (isOrofacial) Color.White else colorMoradoPrincipal, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))

                // LISTA DE NIVELES Y EJERCICIOS
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(listaMostrar) { nivel ->
                        AsignarNivelCard(
                            nivel = nivel,
                            colorPrincipal = colorMoradoPrincipal,
                            colorFondo = colorMoradoClaroFondo,
                            onEjercicioToggled = { id, seleccionado ->
                                if (seleccionado) {
                                    if (!ejerciciosSeleccionados.contains(id)) ejerciciosSeleccionados.add(id)
                                } else {
                                    ejerciciosSeleccionados.remove(id)
                                }
                            },
                            ejerciciosSeleccionados = ejerciciosSeleccionados
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AsignarNivelCard(
    nivel: Nivel,
    colorPrincipal: Color,
    colorFondo: Color,
    onEjercicioToggled: (Int, Boolean) -> Unit,
    ejerciciosSeleccionados: List<Int>
) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expandido = !expandido },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.White) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Favorite, null, tint = colorPrincipal, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Nivel ${nivel.id}", fontSize = 12.sp, color = colorPrincipal, fontWeight = FontWeight.Bold)
                    Text(nivel.titulo, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorPrincipal)
                }
            }

            AnimatedVisibility(visible = expandido) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    nivel.ejercicios.forEach { ejercicio ->
                        ItemEjercicioCheck(
                            ejercicio = ejercicio,
                            colorPrincipal = colorPrincipal,
                            estaSeleccionado = ejerciciosSeleccionados.contains(ejercicio.id),
                            onCheckedChange = { seleccionado -> onEjercicioToggled(ejercicio.id, seleccionado) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemEjercicioCheck(
    ejercicio: Ejercicio,
    colorPrincipal: Color,
    estaSeleccionado: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCheckedChange(!estaSeleccionado) },
        color = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(ejercicio.nombre, modifier = Modifier.weight(1f), fontSize = 14.sp, color = colorPrincipal)

            Surface(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(6.dp),
                color = if (estaSeleccionado) Color.Black else Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(2.dp, if (estaSeleccionado) Color.Black else Color.Gray)
            ) {
                if (estaSeleccionado) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.padding(2.dp))
                }
            }
        }
    }
}