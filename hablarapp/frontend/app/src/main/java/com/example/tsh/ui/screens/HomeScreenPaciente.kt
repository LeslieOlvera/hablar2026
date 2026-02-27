package com.example.tsh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.models.Nivel      // Asegúrate de que el package coincida con tu Models.kt
import com.example.tsh.models.Ejercicio


// --- COLORES ---
val PurplePrimary = Color(0xFF7C5CFC)
val PurpleLight = Color(0xFFDED6FE)
val GrayInput = Color(0xFFF3F3F3)

@Composable
fun HomeScreenPaciente() {
    // 1. ESTADOS DE NAVEGACIÓN
    var isOrofacialSelected by remember { mutableStateOf(false) }
    var nivelSeleccionado by remember { mutableStateOf<Nivel?>(null) }
    var ejercicioSeleccionado by remember { mutableStateOf<Ejercicio?>(null) }

    // 2. DATOS (Basados en tus imágenes de Niveles y Fonemas)
    val nivelesFonemas = listOf(
        Nivel(1, "\"ra\", \"re\", \"ri\", \"ro\", \"ru\"", "Vibrante simple",
            ejercicios = listOf(
                Ejercicio("Repetir ra-re", "Pronuncia de forma pausada"),
                Ejercicio("Palabra Rosa", "Enfócate en la R inicial")
            )),
        Nivel(2, "\"ra\", \"re\", \"ri\", \"ro\", \"ru\"", "Repetición intermedia",
            ejercicios = listOf(Ejercicio("Rima con R", "Lee la rima en voz alta"))),
        Nivel(3, "\"rra\", \"rre\", \"rri\", \"rro\", \"rru\"", "Vibrante múltiple",
            ejercicios = listOf(Ejercicio("Ferrocarril", "Vibra fuerte la lengua")))
    )

    val nivelesOrofaciales = listOf(
        Nivel(1, "Lengua", "Movilidad vertical",
            ejercicios = listOf(
                Ejercicio("Lengua arriba", "Toca el paladar con la punta"),
                Ejercicio("Lengua abajo", "Toca detrás de los dientes inferiores")
            )),
        Nivel(2, "Labios", "D",
            ejercicios = listOf(Ejercicio("Beso y Sonrisa", "Alterna rápido entre ambos"))),
        Nivel(3, "RR", "Fuerza lingual",
            ejercicios = listOf(Ejercicio("Vibración labial", "Haz vibrar tus labios con aire")))
    )

    // 3. ESTRUCTURA DE LA PANTALLA
    Scaffold(
        bottomBar = {
            // Ocultamos la barra si el niño está haciendo el ejercicio
            if (ejercicioSeleccionado == null) BottomNavigationBar()
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                // VISTA 3: ACCIÓN (CÁMARA / PRÁCTICA)
                ejercicioSeleccionado != null -> {
                    PantallaAccionEjercicio(
                        ejercicio = ejercicioSeleccionado!!,
                        onBack = { ejercicioSeleccionado = null }
                    )
                }
                // VISTA 2: LISTADO DE EJERCICIOS
                nivelSeleccionado != null -> {
                    EjerciciosView(
                        nivel = nivelSeleccionado!!,
                        onBack = { nivelSeleccionado = null },
                        onExerciseClick = { ejercicioSeleccionado = it }
                    )
                }
                // VISTA 1: LISTADO DE NIVELES (INICIO)
                else -> {
                    NivelesView(
                        isOrofacial = isOrofacialSelected,
                        onTabChange = { isOrofacialSelected = it },
                        niveles = if (isOrofacialSelected) nivelesOrofaciales else nivelesFonemas,
                        onLevelClick = { nivelSeleccionado = it }
                    )
                }
            }
        }
    }
}

// --- SUB-VISTAS ---

@Composable
fun NivelesView(isOrofacial: Boolean, onTabChange: (Boolean) -> Unit, niveles: List<Nivel>, onLevelClick: (Nivel) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        HeaderSection(userName = "Maria", doctorName = "Oscar Lopez Martinez")
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp)).background(Color(0xFFF3F1FF)).padding(4.dp)) {
            TabButton("Fonemas", !isOrofacial, Modifier.weight(1f)) { onTabChange(false) }
            TabButton("Orofaciales", isOrofacial, Modifier.weight(1f)) { onTabChange(true) }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(niveles) { nivel ->
                LevelCard(nivel, onClick = { onLevelClick(nivel) })
            }
        }
    }
}

@Composable
fun EjerciciosView(nivel: Nivel, onBack: () -> Unit, onExerciseClick: (Ejercicio) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onBack() }) {
            Icon(Icons.Default.ArrowBack, null, tint = PurplePrimary)
            Text(" Volver a Niveles", color = PurplePrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Nivel ${nivel.id}: ${nivel.titulo}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
        Text("Toca un ejercicio para empezar", color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(nivel.ejercicios) { ejercicio ->
                ExerciseCard(ejercicio, onClick = { onExerciseClick(ejercicio) })
            }
        }
    }
}

@Composable
fun PantallaAccionEjercicio(ejercicio: Ejercicio, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = Color.Red) }
            Text("Práctica en vivo", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(48.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(ejercicio.nombre, fontSize = 26.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
        Text(ejercicio.descripcion, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(30.dp))

        // Placeholder para la Cámara/IA de Roboflow
        Box(modifier = Modifier.fillMaxWidth().height(400.dp).clip(RoundedCornerShape(30.dp)).background(Color.DarkGray), contentAlignment = Alignment.Center) {
            Text("Aquí se activará tu cámara", color = Color.White)
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)) {
            Text("¡Terminar!", fontWeight = FontWeight.Bold)
        }
    }
}

// --- COMPONENTES REUTILIZABLES ---

@Composable
fun LevelCard(nivel: Nivel, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E9FF)), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Text("👦", fontSize = 35.sp) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Nivel ${nivel.id}", color = PurplePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(nivel.titulo, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PurplePrimary)
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(30.dp).clip(RoundedCornerShape(15.dp)).background(PurplePrimary).clickable { onClick() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(ejercicio: Ejercicio, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GrayInput)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayArrow, null, tint = PurplePrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(ejercicio.nombre, fontWeight = FontWeight.Bold)
                Text(ejercicio.descripcion, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(if (isSelected) PurplePrimary else Color.Transparent).clickable { onClick() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(text = text, color = if (isSelected) Color.White else PurplePrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HeaderSection(userName: String, doctorName: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(PurpleLight), contentAlignment = Alignment.Center) { Text("🐼", fontSize = 20.sp) }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Hola, Bienvenido", fontSize = 11.sp, color = Color.Gray)
                Text(userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Settings, null, tint = Color.Gray)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("Doctor: $doctorName", fontSize = 13.sp, color = PurplePrimary)
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, null) })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Person, null) })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.DateRange, null) })
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHome() {
    HomeScreenPaciente()
}

