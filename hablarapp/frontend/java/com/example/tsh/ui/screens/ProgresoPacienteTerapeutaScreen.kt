package com.example.tsh.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.RetrofitClient
import com.example.tsh.models.ProgresoItemResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ProgresoPacienteTerapeutaScreen(
    idPaciente: Int,
    nombrePaciente: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)

    val token = sharedPref.getString("token", "") ?: ""

    val colorMoradoPrincipal = Color(0xFF8A62FF)
    val colorFondoTarjetas = Color(0xFFDED6FE)
    val colorVerdeActivo = Color(0xFF4CAF50)
    val colorRojoInactivo = Color(0xFFFF1744)

    var mesOffset by remember { mutableIntStateOf(0) }

    val calendarBase = remember(mesOffset) {
        Calendar.getInstance().apply { add(Calendar.MONTH, mesOffset) }
    }

    val mesActual = remember(calendarBase) {
        calendarBase.get(Calendar.MONTH) + 1
    }

    val anio = remember(calendarBase) {
        calendarBase.get(Calendar.YEAR)
    }

    val hoyDia = remember {
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }

    val hoyMes = remember {
        Calendar.getInstance().get(Calendar.MONTH) + 1
    }

    val hoyAnio = remember {
        Calendar.getInstance().get(Calendar.YEAR)
    }

    val nombreMes = remember(calendarBase) {
        calendarBase.getDisplayName(
            Calendar.MONTH,
            Calendar.LONG,
            Locale("es", "ES")
        )?.replaceFirstChar { it.uppercase() } ?: ""
    }

    val diasDelMes = remember(calendarBase) {
        calendarBase.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    var selectedDay by remember(mesOffset) {
        mutableIntStateOf(
            if (mesOffset == 0) Calendar.getInstance().get(Calendar.DAY_OF_MONTH) else 1
        )
    }

    var listaProgreso by remember {
        mutableStateOf<List<ProgresoItemResponse>>(emptyList())
    }

    var diasConActividad by remember {
        mutableStateOf<List<Int>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val primerDiaSemana = remember(calendarBase) {
        val tempCal = calendarBase.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)

        val rawDay = tempCal.get(Calendar.DAY_OF_WEEK)

        if (rawDay == Calendar.SUNDAY) {
            6
        } else {
            rawDay - 2
        }
    }

    LaunchedEffect(idPaciente, token, mesActual, anio) {
        if (idPaciente == 0 || token.isBlank()) {
            diasConActividad = emptyList()
            return@LaunchedEffect
        }

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.instance
                    .getHistorialMensual(
                        "Bearer $token",
                        idPaciente,
                        mesActual,
                        anio
                    )
                    .execute()
            }

            diasConActividad = if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }

        } catch (e: Exception) {
            diasConActividad = emptyList()
            e.printStackTrace()
        }
    }

    LaunchedEffect(selectedDay, idPaciente, token, mesActual, anio) {
        if (idPaciente == 0 || token.isBlank()) {
            listaProgreso = emptyList()
            return@LaunchedEffect
        }

        isLoading = true

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val tempCal = calendarBase.clone() as Calendar
            tempCal.set(Calendar.DAY_OF_MONTH, selectedDay)

            val fechaFormateada = sdf.format(tempCal.time)

            val response = withContext(Dispatchers.IO) {
                RetrofitClient.instance
                    .getProgresoDia(
                        "Bearer $token",
                        idPaciente,
                        fechaFormateada
                    )
                    .execute()
            }

            listaProgreso = if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }

        } catch (e: Exception) {
            listaProgreso = emptyList()
            Toast.makeText(
                context,
                "Error al cargar progreso",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Volver",
                        tint = colorMoradoPrincipal,
                        modifier = Modifier.size(35.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Progreso",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorMoradoPrincipal
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            color = colorMoradoPrincipal,
                            shape = CircleShape
                        ) {}

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = nombrePaciente,
                            fontSize = 14.sp,
                            color = colorMoradoPrincipal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorFondoTarjetas
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // --- NAVEGACIÓN DE MESES ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { mesOffset-- }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Mes anterior",
                                tint = colorMoradoPrincipal
                            )
                        }

                        Text(
                            text = "< ${nombreMes.uppercase()} $anio >",
                            textAlign = TextAlign.Center,
                            color = colorMoradoPrincipal,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = { mesOffset++ }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Mes siguiente",
                                tint = colorMoradoPrincipal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM").forEach { dia ->
                            Text(
                                text = dia,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorMoradoPrincipal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(240.dp),
                        userScrollEnabled = false
                    ) {
                        items(primerDiaSemana) {
                            Spacer(modifier = Modifier.size(40.dp))
                        }

                        items(diasDelMes) { diaNum ->
                            val dia = diaNum + 1
                            val tieneEjercicio = diasConActividad.contains(dia)
                            val esHoy = dia == hoyDia && mesActual == hoyMes && anio == hoyAnio
                            val esPasado = if (anio < hoyAnio || (anio == hoyAnio && mesActual < hoyMes)) {
                                true
                            } else anio == hoyAnio && mesActual == hoyMes && dia < hoyDia
                            val esSeleccionado = dia == selectedDay

                            val colorFondo = when {
                                esSeleccionado -> colorMoradoPrincipal
                                esHoy -> colorMoradoPrincipal.copy(alpha = 0.6f)
                                tieneEjercicio -> colorVerdeActivo
                                esPasado -> colorRojoInactivo
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(colorFondo)
                                    .clickable {
                                        selectedDay = dia
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dia.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (esSeleccionado || tieneEjercicio || esHoy) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    color = if (colorFondo != Color.Transparent) {
                                        Color.White
                                    } else {
                                        colorMoradoPrincipal
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = colorMoradoPrincipal,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.Start)
            ) {
                val nombreDiaSeleccionado = remember(selectedDay, calendarBase) {
                    val tempCal = calendarBase.clone() as Calendar
                    tempCal.set(Calendar.DAY_OF_MONTH, selectedDay)

                    tempCal.getDisplayName(
                        Calendar.DAY_OF_WEEK,
                        Calendar.LONG,
                        Locale("es", "ES")
                    )?.replaceFirstChar { it.uppercase() } ?: ""
                }

                Text(
                    text = "$nombreDiaSeleccionado $selectedDay, $anio",
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 6.dp
                    ),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorFondoTarjetas
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = colorMoradoPrincipal
                            )
                        }

                        listaProgreso.isEmpty() -> {
                            Text(
                                text = "Sin actividad para esta fecha.",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }

                        else -> {
                            val niveles = listaProgreso.groupBy { item ->
                                obtenerTextoNivelTerapeuta(item.nivel)
                            }

                            niveles.forEach { (nivel, ejercicios) ->
                                Text(
                                    text = nivel,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorMoradoPrincipal
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                ejercicios.forEach { ej ->
                                    ProgresoDetalleTerapeutaItem(
                                        nombre = obtenerNombreEjercicioTerapeuta(ej.id_ejercicio),
                                        porcentaje = formatearPorcentajeTerapeuta(ej.porcentaje),
                                        tiempo = ej.duracion?.ifBlank { "0s" } ?: "0s"
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProgresoDetalleTerapeutaItem(
    nombre: String,
    porcentaje: String,
    tiempo: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(35.dp),
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color(0xFF8A62FF))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = tiempo,
                    fontSize = 9.sp,
                    color = Color(0xFF8A62FF),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = nombre,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )

        Surface(
            color = Color(0xFF8A62FF),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = porcentaje,
                color = Color.White,
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 4.dp
                ),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun obtenerTextoNivelTerapeuta(nivel: String?): String {
    if (nivel.isNullOrBlank()) {
        return "Sin nivel"
    }

    val numero = nivel.filter { it.isDigit() }

    return if (numero.isNotBlank()) {
        "Nivel $numero"
    } else {
        nivel
    }
}

private fun obtenerNombreEjercicioTerapeuta(id: Int): String {
    return when (id) {
        1 -> "ra"
        2 -> "re"
        3 -> "ri"
        4 -> "ro"
        5 -> "ru"
        6 -> "rra"
        7 -> "rre"
        8 -> "rri"
        9 -> "rro"
        10 -> "rru"
        11 -> "Ferrocarril"
        12 -> "Cigarro"
        13 -> "Barril"
        14 -> "Rita"
        15 -> "Ruso"
        16 -> "Rama"
        17 -> "Rojo"
        18 -> "Rosa"
        19 -> "Lengua arriba"
        20 -> "Lengua abajo"
        21 -> "Lengua izquierda"
        22 -> "Lengua derecha"
        23 -> "Morder labio superior"
        24 -> "Morder labio inferior"
        else -> "Ejercicio $id"
    }
}

private fun formatearPorcentajeTerapeuta(valor: Double): String {
    return if (valor % 1.0 == 0.0) {
        "${valor.toInt()}%"
    } else {
        "${String.format(Locale.getDefault(), "%.1f", valor)}%"
    }
}
