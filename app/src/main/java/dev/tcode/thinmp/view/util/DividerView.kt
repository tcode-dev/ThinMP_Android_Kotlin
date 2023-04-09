package dev.tcode.thinmp.view.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant

@Composable
fun DividerView() {
    Divider(
        color = MaterialTheme.colorScheme.secondary, modifier = Modifier
            .fillMaxWidth()
            .height(StyleConstant.DIVIDER_HEIGHT.dp)
    )
}