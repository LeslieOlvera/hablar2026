package com.example.tsh.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.content.ContextCompat
import com.example.tsh.R
import com.example.tsh.RetrofitClient
import com.example.tsh.models.DefaultResponse
import com.example.tsh.models.Ejercicio
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AccionEjercicioScreen(
    ejercicio: Ejercicio,
    esOrofacial: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)

    val nombrePaciente = sharedPref.getString("nombreP", "Paciente") ?: "Paciente"
    val token = "Bearer ${sharedPref.getString("token", "")}"

    var inicioGrabacionMillis by remember { mutableStateOf(0L) }
    var duracionGrabacion by remember { mutableStateOf("0s") }

    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var enviando by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var mostrarResultado by remember { mutableStateOf(false) }
    var porcentajeResultado by remember { mutableStateOf("0") }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {
            }

            try {
                mediaRecorder?.release()
            } catch (_: Exception) {
            }

            mediaPlayer = null
            mediaRecorder = null
        }
    }

    if (mostrarResultado) {
        ResultadoEjercicioScreen(
            porcentaje = porcentajeResultado,
            onNavigateBack = {
                mostrarResultado = false
                porcentajeResultado = "0"
                audioFile = null
                duracionGrabacion = "0s"
                onBack()
            }
        )
        return
    }

    val nombreLimpio = normalizarNombreEjercicio(nombre = ejercicio.nombre)

    val resId = when (nombreLimpio) {
        "ra" -> R.raw.ra
        "re" -> R.raw.re
        "ri" -> R.raw.ri
        "ro" -> R.raw.ro
        "ru" -> R.raw.ru

        "rra" -> R.raw.rra
        "rre" -> R.raw.rre
        "rri" -> R.raw.rri
        "rro" -> R.raw.rro
        "rru" -> R.raw.rru

        "ferrocarril" -> R.raw.ferrocarril
        "cigarro" -> R.raw.cigarro
        "barril" -> R.raw.barril
        "rita" -> R.raw.rita
        "ruso" -> R.raw.ruso
        "rama" -> R.raw.rama
        "rojo" -> R.raw.rojo
        "rosa" -> R.raw.rosa

        else -> R.raw.ra
    }

    fun reproducirGuia() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("Audio", "Error al reproducir guía", e)
            Toast.makeText(
                context,
                "No se pudo reproducir el audio",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "grabacion_${System.currentTimeMillis()}.m4a")
            audioFile = file

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            inicioGrabacionMillis = System.currentTimeMillis()
            duracionGrabacion = "0s"
            isRecording = true

        } catch (e: Exception) {
            Log.e("Audio", "Error al iniciar grabación", e)
            Toast.makeText(
                context,
                "Error al iniciar el micrófono",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.e("Audio", "Error al detener grabación", e)
        } finally {
            try {
                mediaRecorder?.release()
            } catch (_: Exception) {
            }

            mediaRecorder = null
            isRecording = false

            val finGrabacionMillis = System.currentTimeMillis()
            val segundos = ((finGrabacionMillis - inicioGrabacionMillis) / 1000).toInt()
            duracionGrabacion = formatearDuracionParaEnviar(segundos)

            mostrarConfirmacion = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecording()
        } else {
            Toast.makeText(
                context,
                "Debes permitir el uso del micrófono",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacion = false
            },
            title = {
                Text("¿Enviar grabación?")
            },
            text = {
                Text(
                    text = "Duración: $duracionGrabacion\n\n¿Deseas enviar este audio para calificarlo o prefieres repetirlo?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacion = false

                        audioFile?.let { file ->
                            enviando = true

                            enviarAudioAlServidor(
                                context = context,
                                file = file,
                                idEjercicio = ejercicio.id,
                                token = token,
                                duracion = duracionGrabacion,
                                onFinished = {
                                    enviando = false
                                },
                                onResultado = { porcentaje ->
                                    porcentajeResultado = porcentaje
                                    mostrarResultado = true
                                }
                            )
                        }
                    }
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        audioFile?.delete()
                        audioFile = null
                        duracionGrabacion = "0s"
                    }
                ) {
                    Text("Repetir")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color(0xFF9E82FE)
                )
            }

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDED6FE)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.panda),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Hola, Bienvenido",
                    fontSize = 10.sp,
                    color = Color(0xFF9E82FE)
                )

                Text(
                    text = nombrePaciente,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "¡Repite!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Presiona el icono\ndel micrófono al hablar",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF8A62FF)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Escuchar",
                    tint = Color.White,
                    modifier = Modifier
                        .size(55.dp)
                        .clickable {
                            reproducirGuia()
                        }
                )

                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    text = "“$nombreLimpio”",
                    color = Color.White,
                    fontSize = if (nombreLimpio.length > 8) 32.sp else 50.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp)
        ) {
            Text(
                text = when {
                    isRecording -> "Grabando..."
                    enviando -> "Calificando audio..."
                    else -> "Toca para grabar"
                },
                color = when {
                    isRecording -> Color.Red
                    enviando -> Color(0xFF7C5CFC)
                    else -> Color.Gray
                },
                fontWeight = FontWeight.Bold
            )

            if (duracionGrabacion != "0s" && !isRecording) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Duración: $duracionGrabacion",
                    color = Color(0xFF7C5CFC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clickable(enabled = !enviando) {
                        if (!isRecording) {
                            val permisoConcedido = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (permisoConcedido) {
                                startRecording()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        } else {
                            stopRecording()
                        }
                    },
                shape = CircleShape,
                color = if (isRecording) Color.Red else Color(0xFF8A62FF),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (enviando) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun normalizarNombreEjercicio(nombre: String): String {
    return nombre
        .substringAfter(":")
        .trim()
        .lowercase()
        .replace("“", "")
        .replace("”", "")
        .replace("\"", "")
}

private fun enviarAudioAlServidor(
    context: Context,
    file: File,
    idEjercicio: Int,
    token: String,
    duracion: String,
    onFinished: () -> Unit,
    onResultado: (String) -> Unit
) {
    val requestFile = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
    val body = MultipartBody.Part.createFormData("audio", file.name, requestFile)

    val idBody = idEjercicio.toString().toRequestBody("text/plain".toMediaTypeOrNull())
    val duracionBody = duracion.toRequestBody("text/plain".toMediaTypeOrNull())

    RetrofitClient.instance
        .subirFonetico(token, idBody, duracionBody, body)
        .enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(
                call: Call<DefaultResponse>,
                response: Response<DefaultResponse>
            ) {
                onFinished()

                if (response.isSuccessful) {
                    val porcentaje = response.body()?.porcentaje

                    if (porcentaje != null) {
                        val porcentajeTexto = String.format(Locale.US, "%.0f", porcentaje)
                        Toast.makeText(
                            context,
                            "Audio clasificado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        file.delete()
                        onResultado(porcentajeTexto)
                    } else {
                        Toast.makeText(
                            context,
                            "El audio se envió, pero no se recibió porcentaje",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Error del servidor: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<DefaultResponse>,
                t: Throwable
            ) {
                onFinished()
                Toast.makeText(
                    context,
                    "Fallo de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
}

private fun formatearDuracionParaEnviar(segundos: Int): String {
    val segundosSeguros = if (segundos <= 0) 1 else segundos

    return when {
        segundosSeguros < 60 -> "${segundosSeguros}s"

        else -> {
            val minutos = segundosSeguros / 60
            val segundosRestantes = segundosSeguros % 60

            if (segundosRestantes == 0) {
                "${minutos}m"
            } else {
                "${minutos}m ${segundosRestantes}s"
            }
        }
    }
}
