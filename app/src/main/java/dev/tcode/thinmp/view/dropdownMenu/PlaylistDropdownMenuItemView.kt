package dev.tcode.thinmp.view.dropdownMenu

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.tcode.thinmp.R

@Composable
fun PlaylistDropdownMenuItemView(callback: () -> Unit) {
    DropdownMenuItem(text = { Text(stringResource(R.string.add_playlist), color = MaterialTheme.colorScheme.primary) }, onClick = {
        callback()
    })
}