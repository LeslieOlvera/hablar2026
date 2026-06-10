package com.example.tsh.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import java.io.FileOutputStream
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AccionOrofacialScreen(
    ejercicio: Ejercicio,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val sharedPref = context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)
    val nombrePaciente = sharedPref.getString("nombreP", "Paciente") ?: "Paciente"
    val token = "Bearer ${sharedPref.getString("token", "")}"

    var cameraActiva by remember { mutableStateOf(false) }
    var enviando by remember { mutableStateOf(false) }
    var mensajeProceso by remember { mutableStateOf("") }

    var mostrarResultado by remember { mutableStateOf(false) }
    var porcentajeResultado by remember { mutableStateOf("0") }

    var inicioEjercicioMillis by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setJpegQuality(50)
            .build()
    }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val reproducirInstruccionOrofacial = {
        mediaPlayer?.stop()
        mediaPlayer?.release()

        val audioResId = when (ejercicio.nombre.lowercase()) {
            "lengua arriba" -> R.raw.lengua_arriba
            "lengua abajo" -> R.raw.lengua_abajo
            "lengua izquierda" -> R.raw.lengua_izquierda
            "lengua derecha" -> R.raw.lengua_derecha
            "morder labio superior" -> R.raw.morder_superior
            "morder labio inferior" -> R.raw.morder_inferior
            else -> R.raw.ra
        }

        try {
            mediaPlayer = MediaPlayer.create(context, audioResId)
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("Audio", "Error al reproducir", e)

            Toast.makeText(
                context,
                "No se pudo reproducir el audio",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {
            }

            mediaPlayer = null
        }
    }

    val imagenAMostrar = when (ejercicio.nombre.lowercase()) {
        "lengua arriba" -> R.drawable.arriba
        "lengua abajo" -> R.drawable.abajo
        "lengua izquierda" -> R.drawable.izquierda
        "lengua derecha" -> R.drawable.derecha
        "morder labio superior" -> R.drawable.labio_superior
        "morder labio inferior" -> R.drawable.labio_inferior
        else -> R.drawable.nino_ejercicio
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            inicioEjercicioMillis = System.currentTimeMillis()
            cameraActiva = true
        } else {
            Toast.makeText(
                context,
                "Debes permitir el uso de la cámara",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    if (mostrarResultado) {
        ResultadoEjercicioScreen(
            porcentaje = porcentajeResultado,
            onNavigateBack = {
                mostrarResultado = false
                mensajeProceso = ""
                onBack()
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        contentDescription = "Perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Hola, Bienvenido",
                        fontSize = 10.sp,
                        color = Color(0xFF9E82FE)
                    )

                    Text(
                        text = nombrePaciente,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }

        Text(
            text = "¡Imita!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF8A62FF)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Image(
                        painter = painterResource(id = imagenAMostrar),
                        contentDescription = ejercicio.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Escuchar instrucción",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(20.dp)
                        .size(45.dp)
                        .align(Alignment.TopStart)
                        .clickable {
                            reproducirInstruccionOrofacial()
                        }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (cameraActiva) Color.Black else Color(0xFFB89FFF)
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (!cameraActiva) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cámara",
                        tint = Color.White,
                        modifier = Modifier
                            .size(60.dp)
                            .clickable {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                    )
                } else {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()

                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(this.surfaceProvider)
                                    }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_FRONT_CAMERA,
                                            preview,
                                            imageCapture
                                        )
                                    } catch (e: Exception) {
                                        Log.e("Camera", "Error al iniciar cámara", e)

                                        Toast.makeText(
                                            context,
                                            "No se pudo iniciar la cámara",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(40.dp))
                    )
                }
            }
        }

        if (cameraActiva) {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!enviando) {
                        val segundos =
                            ((System.currentTimeMillis() - inicioEjercicioMillis) / 1000).toInt()

                        val duracionTexto = formatearDuracionOrofacialParaEnviar(segundos)

                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            idEjercicio = ejercicio.id,
                            token = token,
                            duracion = duracionTexto,
                            onUploadingChange = { enviando = it },
                            onMensajeChange = { mensajeProceso = it },
                            onResultado = { porcentaje ->
                                porcentajeResultado = porcentaje
                                mostrarResultado = true
                            }
                        )
                    }
                },
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8A62FF)
                ),
                contentPadding = PaddingValues(0.dp),
                enabled = !enviando
            ) {
                if (enviando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Tomar foto",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            if (mensajeProceso.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = mensajeProceso,
                    color = Color(0xFF7C5CFC),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    idEjercicio: Int,
    token: String,
    duracion: String,
    onUploadingChange: (Boolean) -> Unit,
    onMensajeChange: (String) -> Unit,
    onResultado: (String) -> Unit
) {
    val jpgFile = File(context.cacheDir, "${System.currentTimeMillis()}_temp.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(jpgFile).build()

    onUploadingChange(true)
    onMensajeChange("Tomando foto...")

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                try {
                    onMensajeChange("Foto tomada")

                    val bitmap = BitmapFactory.decodeFile(jpgFile.absolutePath)

                    if (bitmap == null) {
                        jpgFile.delete()
                        onUploadingChange(false)
                        onMensajeChange("")

                        Toast.makeText(
                            context,
                            "No se pudo procesar la imagen",
                            Toast.LENGTH_LONG
                        ).show()

                        return
                    }

                    val pngFile = File(context.cacheDir, "${System.currentTimeMillis()}.png")

                    FileOutputStream(pngFile).use { outputStream ->
                        bitmap.compress(
                            android.graphics.Bitmap.CompressFormat.PNG,
                            100,
                            outputStream
                        )
                    }

                    jpgFile.delete()

                    onMensajeChange("Calificando imagen...")

                    enviarImagenAlServidor(
                        context = context,
                        file = pngFile,
                        idEjercicio = idEjercicio,
                        token = token,
                        duracion = duracion,
                        onFinished = {
                            onUploadingChange(false)
                            pngFile.delete()
                        },
                        onMensajeChange = onMensajeChange,
                        onResultado = onResultado
                    )
                } catch (e: Exception) {
                    jpgFile.delete()
                    onUploadingChange(false)
                    onMensajeChange("")

                    Log.e("Camera", "Error al convertir imagen a PNG", e)

                    Toast.makeText(
                        context,
                        "Error al convertir imagen: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onUploadingChange(false)
                onMensajeChange("")

                Log.e("Camera", "Error al tomar foto", exception)

                Toast.makeText(
                    context,
                    "Error al tomar foto: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )
}

private fun enviarImagenAlServidor(
    context: Context,
    file: File,
    idEjercicio: Int,
    token: String,
    duracion: String,
    onFinished: () -> Unit,
    onMensajeChange: (String) -> Unit,
    onResultado: (String) -> Unit
) {
    val requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())

    val body = MultipartBody.Part.createFormData(
        "imagen",
        file.name,
        requestFile
    )

    val idBody = idEjercicio.toString().toRequestBody("text/plain".toMediaTypeOrNull())
    val duracionBody = duracion.toRequestBody("text/plain".toMediaTypeOrNull())

    RetrofitClient.instance
        .subirOrofacial(
            token,
            idBody,
            duracionBody,
            body
        )
        .enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(
                call: Call<DefaultResponse>,
                response: Response<DefaultResponse>
            ) {
                onFinished()

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val porcentaje = responseBody?.porcentaje

                    if (porcentaje != null) {
                        val porcentajeTexto = String.format(Locale.US, "%.0f", porcentaje)

                        onMensajeChange("Resultado listo")

                        Toast.makeText(
                            context,
                            "Imagen clasificada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        onResultado(porcentajeTexto)
                    } else {
                        onMensajeChange("")

                        Toast.makeText(
                            context,
                            "La imagen se envió, pero no se recibió porcentaje",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    onMensajeChange("")

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
                onMensajeChange("")

                Toast.makeText(
                    context,
                    "Fallo de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
}

private fun formatearDuracionOrofacialParaEnviar(segundos: Int): String {
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
