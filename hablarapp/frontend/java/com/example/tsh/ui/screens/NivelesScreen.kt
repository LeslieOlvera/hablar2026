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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.models.Nivel
import com.example.tsh.ui.components.PurplePrimary
import com.example.tsh.ui.components.TabUnselected

@Composable
fun NivelesScreen(
    userName: String,
    doctorName: String,
    isOrofacial: Boolean,
    niveles: List<com.example.tsh.models.Nivel>,
    onTabChange: (Boolean) -> Unit,
    onLevelClick: (com.example.tsh.models.Nivel) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        com.example.tsh.ui.components.HeaderSection(userName, doctorName)

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.example.tsh.ui.components.TabButton("Fonemas", !isOrofacial, Modifier.weight(1f)) { onTabChange(false) }
            com.example.tsh.ui.components.TabButton("Orofaciales", isOrofacial, Modifier.weight(1f)) { onTabChange(true) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(niveles) { nivel ->
                LevelCard(nivel = nivel, onClick = { onLevelClick(nivel) })
            }
        }
    }
}


@Composable
fun LevelCard(nivel: Nivel, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = TabUnselected),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Color.White) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.tsh.R.drawable.nino_ejercicio),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = PurplePrimary, modifier = Modifier.size(14.dp))
                    Text(" Nivel ${nivel.id}", color = PurplePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text("“${nivel.titulo}”", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(32.dp).clip(CircleShape).background(PurplePrimary), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                }
            }
            Icon(Icons.Default.Favorite, null, tint = PurplePrimary)
        }
    }
}