package dev.tcode.thinmp.view.dropdownMenu

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.tcode.thinmp.R

@Composable
fun PlaylistRemoveDropdownMenuItemView(callback: () -> Unit) {
    DropdownMenuItem(text = { Text(stringResource(R.string.remove_playlist), color = MaterialTheme.colorScheme.primary) }, onClick = callback)
}