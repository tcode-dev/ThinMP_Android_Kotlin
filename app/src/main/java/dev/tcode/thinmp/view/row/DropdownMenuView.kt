package dev.tcode.thinmp.view.row

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun DropdownMenuView(dropdownContent: @Composable ColumnScope.(callback: () -> Unit) -> Unit, content: @Composable BoxScope.(callback: () -> Unit) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
    ) {
        val expanded = remember { mutableStateOf(false) }
        val callback = { expanded.value = !expanded.value }

        Box(Modifier.pointerInput(UUID.randomUUID()) {
            detectTapGestures(onLongPress = { callback() }, onTap = { callback() })
        }) {
            content(callback)
            DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), modifier = Modifier.background(MaterialTheme.colorScheme.onBackground), onDismissRequest = callback) {
                dropdownContent(callback)
            }
        }
    }
}