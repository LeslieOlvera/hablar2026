package com.example.tsh.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.R
import com.example.tsh.RetrofitClient
import com.example.tsh.models.ProgresoItemResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PremiosScreen() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)

    val idPaciente = sharedPref.getInt("id_paciente", 0)
    val token = sharedPref.getString("token", "") ?: ""

    var nombrePaciente by remember {
        mutableStateOf(sharedPref.getString("nombreP", "Paciente") ?: "Paciente")
    }

    var cargandoPerfil by remember {
        mutableStateOf(false)
    }

    var cargandoProgreso by remember {
        mutableStateOf(false)
    }

    var listaPremios by remember {
        mutableStateOf<List<ProgresoItemResponse>>(emptyList())
    }

    val colorMorado = Color(0xFF7C5CFC)
    val colorTarjeta = Color(0xFFDED6FE)

    val calendar = remember { Calendar.getInstance() }
    val mesActual = remember { calendar.get(Calendar.MONTH) + 1 }
    val anioActual = remember { calendar.get(Calendar.YEAR) }

    val nombreMes = remember {
        calendar.getDisplayName(
            Calendar.MONTH,
            Calendar.LONG,
            Locale("es", "ES")
        )?.replaceFirstChar { it.uppercase() } ?: ""
    }

    /*
        Filtramos los ejercicios válidos:
        - Solo se muestran ejercicios que tengan porcentaje mayor a 1.
        - Esto evita mostrar ejercicios que existen en la respuesta,
          pero no tienen avance real suficiente para generar estrellas.
    */
    val listaPremiosFiltrada = listaPremios.filter { progreso ->
        progreso.porcentaje > 1.0
    }

    val totalEstrellasCalculadas = listaPremiosFiltrada.sumOf { progreso ->
        convertirPorcentajeANumeroEstrellas(progreso.porcentaje)
    }

    LaunchedEffect(idPaciente, token) {
        if (idPaciente == 0 || token.isBlank()) {
            return@LaunchedEffect
        }

        try {
            cargandoPerfil = true

            val response = withContext(Dispatchers.IO) {
                RetrofitClient.instance
                    .getPacienteById("Bearer $token", idPaciente)
                    .execute()
            }

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    nombrePaciente = data.nombreP

                    sharedPref.edit()
                        .putString("nombreP", data.nombreP)
                        .putInt("estrella", data.estrella)
                        .apply()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cargandoPerfil = false
        }
    }

    LaunchedEffect(idPaciente, token) {
        if (idPaciente == 0 || token.isBlank()) {
            return@LaunchedEffect
        }

        cargandoProgreso = true

        try {
            val progresoDelMes = withContext(Dispatchers.IO) {
                val responseDias = RetrofitClient.instance
                    .getHistorialMensual(
                        "Bearer $token",
                        idPaciente,
                        mesActual,
                        anioActual
                    )
                    .execute()

                val diasConActividad = if (responseDias.isSuccessful) {
                    responseDias.body() ?: emptyList()
                } else {
                    emptyList()
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val resultado = mutableListOf<ProgresoItemResponse>()

                diasConActividad.forEach { dia ->
                    try {
                        val tempCal = calendar.clone() as Calendar
                        tempCal.set(Calendar.DAY_OF_MONTH, dia)

                        val fechaFormateada = sdf.format(tempCal.time)

                        val responseProgreso = RetrofitClient.instance
                            .getProgresoDia(
                                "Bearer $token",
                                idPaciente,
                                fechaFormateada
                            )
                            .execute()

                        if (responseProgreso.isSuccessful) {
                            resultado.addAll(responseProgreso.body() ?: emptyList())
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                resultado
            }

            listaPremios = progresoDelMes

        } catch (e: Exception) {
            listaPremios = emptyList()
            e.printStackTrace()
        } finally {
            cargandoProgreso = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Premios",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colorMorado
        )

        Spacer(modifier = Modifier.height(20.dp))

        // TARJETA DE PERFIL
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorTarjeta
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.panda),
                        contentDescription = "Foto de perfil Panda",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = nombrePaciente,
                    fontWeight = FontWeight.Bold,
                    color = colorMorado,
                    fontSize = 20.sp
                )

                Text(
                    text = "Paciente",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.size(92.dp),
                    color = colorMorado,
                    shape = CircleShape
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = totalEstrellasCalculadas.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "⭐",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        cargandoPerfil || cargandoProgreso -> "Calculando estrellas..."
                        totalEstrellasCalculadas == 0 -> "Aún no hay estrellas este mes"
                        else -> "Estrellas obtenidas este mes"
                    },
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ESTRELLAS POR EJERCICIO
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorTarjeta
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Estrellas por ejercicio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorMorado
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Resumen de ejercicios realizados en $nombreMes $anioActual.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    cargandoProgreso -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colorMorado,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    listaPremiosFiltrada.isEmpty() -> {
                        Text(
                            text = "Aún no hay ejercicios con avance para mostrar este mes.",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    else -> {
                        listaPremiosFiltrada.forEachIndexed { index, progreso ->
                            val estrellasEjercicio =
                                convertirPorcentajeANumeroEstrellas(progreso.porcentaje)

                            PremioEjercicioItem(
                                nivel = obtenerTextoNivelPremio(progreso.nivel),
                                ejercicio = obtenerNombreEjercicioPremio(progreso.id_ejercicio),
                                estrellasTexto = dibujarEstrellas(estrellasEjercicio)
                            )

                            if (index < listaPremiosFiltrada.lastIndex) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // REGLAS DE ESTRELLAS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorTarjeta
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "¿Cómo ganas estrellas?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorMorado
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Después de cada ejercicio, tu puntaje se convierte en estrellas.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                ReglaEstrellaItem(
                    rango = "90% a 100%",
                    estrellas = "⭐⭐⭐⭐⭐",
                    descripcion = "Excelente"
                )

                ReglaEstrellaItem(
                    rango = "70% a 89%",
                    estrellas = "⭐⭐⭐⭐",
                    descripcion = "Muy bien"
                )

                ReglaEstrellaItem(
                    rango = "50% a 69%",
                    estrellas = "⭐⭐⭐",
                    descripcion = "Bien"
                )

                ReglaEstrellaItem(
                    rango = "30% a 49%",
                    estrellas = "⭐⭐",
                    descripcion = "Sigue practicando"
                )

                ReglaEstrellaItem(
                    rango = "10% a 29%",
                    estrellas = "⭐",
                    descripcion = "Buen intento"
                )

                ReglaEstrellaItem(
                    rango = "0% a 9%",
                    estrellas = "Sin estrellas",
                    descripcion = "Intenta de nuevo"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PremioEjercicioItem(
    nivel: String,
    ejercicio: String,
    estrellasTexto: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = Color.White
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "⭐",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF7C5CFC),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = nivel,
                fontSize = 13.sp,
                color = Color(0xFF7C5CFC),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = ejercicio,
                fontSize = 16.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = estrellasTexto,
                fontSize = 17.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ReglaEstrellaItem(
    rango: String,
    estrellas: String,
    descripcion: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.width(100.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Text(
                text = rango,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7C5CFC)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = estrellas,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = descripcion,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

private fun convertirPorcentajeANumeroEstrellas(porcentaje: Double): Int {
    return when {
        porcentaje >= 90.0 -> 5
        porcentaje >= 70.0 -> 4
        porcentaje >= 50.0 -> 3
        porcentaje >= 30.0 -> 2
        porcentaje >= 10.0 -> 1
        else -> 0
    }
}

private fun dibujarEstrellas(cantidad: Int): String {
    return if (cantidad > 0) {
        "⭐".repeat(cantidad)
    } else {
        ""
    }
}

private fun obtenerTextoNivelPremio(nivel: String?): String {
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

private fun obtenerNombreEjercicioPremio(id: Int): String {
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