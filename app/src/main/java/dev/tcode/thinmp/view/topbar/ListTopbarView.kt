package dev.tcode.thinmp.view.topbar

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ListTopbarView(title: String, offset: Int) {
    Box() {
        AnimatedVisibility(
            visible = offset > 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = MaterialTheme.colors.secondary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(50.dp)
                ) {}
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(50.dp)
        ) {
            Text(title, fontSize = 24.sp)
        }
    }
}
