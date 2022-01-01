package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun MainScreen(navigate: () -> Unit) {
    Column {
        Text("Main Screen!!")
        Button(onClick = navigate) {
            Text(text = "next albums")
        }
    }
}
