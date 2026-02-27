package com.example.tsh.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


// --- COLORES PERSONALIZADOS ---
val PurplePrimary = Color(0xFF7C5CFC)
val PurpleLight = Color(0xFFDED6FE)
val GrayInput = Color(0xFFF3F3F3)


@Composable
fun PandaIcon(isInverse: Boolean = false, size: Int = 120) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(if (isInverse) Color.White else PurplePrimary)
            .padding(if (isInverse) 4.dp else 0.dp)
            .clip(CircleShape)
            .background(if (isInverse) PurplePrimary else Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((size * 0.8).dp)) {
            drawCircle(Color.Black, radius = 40f, center = center.copy(x = center.x - 70f, y = center.y - 70f))
            drawCircle(Color.Black, radius = 40f, center = center.copy(x = center.x + 70f, y = center.y - 70f))
            drawCircle(Color.White, radius = 120f)
            drawCircle(Color.Black, radius = 120f, style = Stroke(width = 2f))
            drawOval(Color.Black, topLeft = center.copy(x = center.x - 85f, y = center.y - 45f), size = androidx.compose.ui.geometry.Size(60f, 80f))
            drawOval(Color.Black, topLeft = center.copy(x = center.x + 25f, y = center.y - 45f), size = androidx.compose.ui.geometry.Size(60f, 80f))
            drawCircle(Color.White, radius = 10f, center = center.copy(x = center.x - 55f, y = center.y - 15f))
            drawCircle(Color.White, radius = 10f, center = center.copy(x = center.x + 55f, y = center.y - 15f))
            drawCircle(Color.Black, radius = 15f, center = center.copy(y = center.y + 30f))
        }
    }
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

@Composable
fun CustomTextField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GrayInput,
                unfocusedContainerColor = GrayInput,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
