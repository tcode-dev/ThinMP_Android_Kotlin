package dev.tcode.thinmp.view.divider

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DividerView() {
    Divider(
        color = MaterialTheme.colors.secondary,
        modifier = Modifier.fillMaxWidth().height(1.dp)
    )
}
