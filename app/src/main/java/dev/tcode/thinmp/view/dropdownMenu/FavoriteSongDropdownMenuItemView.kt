package dev.tcode.thinmp.view.dropdownMenu

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.viewModel.FavoriteSongRegisterViewModel

@Composable
fun FavoriteSongDropdownMenuItemView(id: SongId, callback: () -> Unit, viewModel: FavoriteSongRegisterViewModel = viewModel()) {
    if (viewModel.exists(id)) {
        DropdownMenuItem(text = { Text(stringResource(R.string.remove_favorite), color = MaterialTheme.colorScheme.primary) }, onClick = {
            viewModel.delete(id)
            callback()
        })
    } else {
        DropdownMenuItem(text = { Text(stringResource(R.string.add_favorite), color = MaterialTheme.colorScheme.primary) }, onClick = {
            viewModel.add(id)
            callback()
        })
    }
}