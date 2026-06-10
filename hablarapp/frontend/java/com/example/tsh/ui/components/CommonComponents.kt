package com.example.tsh.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.tsh.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.ArrowForward

val PurplePrimary = Color(0xFF7C5CFC)
val PurpleLight = Color(0xFFDED6FE)
val GrayInput = Color(0xFFF3F3F3)

@Composable
fun PandaIcon() {
    Image(
        painter = painterResource(id = R.drawable.panda),
        contentDescription = null,
        modifier = Modifier.size(120.dp)
    )
}

@Composable
fun EyeIcon(visible: Boolean) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(2f, 12f)
            quadraticBezierTo(12f, 2f, 22f, 12f)
            quadraticBezierTo(12f, 22f, 2f, 12f)
        }
        drawPath(path, color = Color.Gray, style = Stroke(width = 2f))
        drawCircle(color = Color.Gray, radius = 4f, center = center)

        if (!visible) {
            drawLine(
                color = Color.Gray,
                start = androidx.compose.ui.geometry.Offset(4f, 4f),
                end = androidx.compose.ui.geometry.Offset(20f, 20f),
                strokeWidth = 2f
            )
        }
    }
}

// --- COMPONENTE CORREGIDO ---
@Composable
fun ImagenEjercicioDinamica(nombreEjercicio: String, modifier: Modifier = Modifier) {
    val nombreBuscado = nombreEjercicio.lowercase()

    val imagenRes = when {
        nombreBuscado.contains("arriba") -> R.drawable.arriba
        nombreBuscado.contains("abajo") -> R.drawable.abajo
        nombreBuscado.contains("derecha") -> R.drawable.derecha
        nombreBuscado.contains("izquierda") -> R.drawable.izquierda
        nombreBuscado.contains("superior") -> R.drawable.labio_superior
        nombreBuscado.contains("inferior") -> R.drawable.labio_inferior
        else -> R.drawable.nino_ejercicio
    }

    Image(
        painter = painterResource(id = imagenRes),
        contentDescription = nombreEjercicio,
        modifier = modifier
            .size(200.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Fit // Cambiado a Fit para que no se corte el dibujo técnico
    )
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GrayInput,
                unfocusedContainerColor = GrayInput,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledContainerColor = GrayInput,
                disabledBorderColor = Color.Transparent,
                disabledLabelColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

val TabUnselected = Color(0xFFE8E9FF)

@Composable
fun HeaderSection(userName: String, doctorName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFDED6FE)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.panda),
                contentDescription = "Perfil Panda",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Hola, Bienvenido",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = userName,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Dr. $doctorName",
                fontSize = 13.sp,
                color = Color(0xFF7C5CFC)
            )
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(55.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) PurplePrimary else TabUnselected),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, color = if (isSelected) Color.White else PurplePrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CustomBottomBar(onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF7C5CFC),
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(40.dp)),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null, tint = Color.White) },
            selected = false,
            onClick = { onTabSelected("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, null, tint = Color.White) },
            selected = false,
            onClick = { onTabSelected("profile") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.DateRange, null, tint = Color.White) },
            selected = false,
            onClick = { onTabSelected("calendar") }
        )
    }
}

@Composable
fun NivelCard(
    nivelId: Int,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDED6FE))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Color.White) {
                Image(
                    painter = painterResource(id = R.drawable.nino_ejercicio),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Nivel $nivelId", color = Color(0xFF7C5CFC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("“$titulo”", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun PacienteCard(
    nombre: String,
    onProgresoClick: () -> Unit,
    onAsignarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDED6FE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.panda),
                    contentDescription = "Foto Paciente",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = Color(0xFF8A62FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF8A62FF))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onProgresoClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(45.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Progreso", color = Color(0xFF8A62FF), fontSize = 12.sp)
                    }
                    Button(
                        onClick = onAsignarClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(45.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Asignar\nEjercicios", color = Color(0xFF8A62FF), fontSize = 10.sp, lineHeight = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onProgresoClick,
                        modifier = Modifier.weight(1f).height(30.dp).background(Color(0xFF8A62FF), RoundedCornerShape(15.dp))
                    ) { Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp)) }

                    IconButton(
                        onClick = onAsignarClick,
                        modifier = Modifier.weight(1f).height(30.dp).background(Color(0xFF8A62FF), RoundedCornerShape(15.dp))
                    ) { Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
}