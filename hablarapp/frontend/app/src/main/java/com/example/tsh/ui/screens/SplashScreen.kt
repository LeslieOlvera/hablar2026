package com.example.tsh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsh.PurplePrimary
import com.example.tsh.ui.components.PandaIcon
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) { delay(3000); onTimeout() }
    Box(modifier = Modifier.fillMaxSize().background(PurplePrimary), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PandaIcon(isInverse = true)
            Spacer(modifier = Modifier.height(24.dp))
            Text("HablaR", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.ExtraLight, letterSpacing = 4.sp)
            Text("TSH", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Normal, letterSpacing = 4.sp)
        }
    }
}