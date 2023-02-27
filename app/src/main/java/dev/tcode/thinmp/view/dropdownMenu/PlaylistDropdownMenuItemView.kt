package dev.tcode.thinmp.view.dropdownMenu

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.valueObject.SongId

@Composable
fun PlaylistDropdownMenuItemView(id: SongId, close: () -> Unit) {
    DropdownMenuItem(onClick = {
        close()
    }) {
        Text(stringResource(R.string.add_playlist))
    }
}