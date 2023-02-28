package dev.tcode.thinmp.view.dropdownMenu

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.viewModel.FavoriteArtistRegisterViewModel

@Composable
fun FavoriteArtistDropdownMenuItemView(id: ArtistId, close: () -> Unit, viewModel: FavoriteArtistRegisterViewModel = viewModel()) {
    if (viewModel.exists(id)) {
        DropdownMenuItem(onClick = {
            viewModel.delete(id)
            close()
        }) {
            Text(stringResource(R.string.remove_favorite))
        }
    } else {
        DropdownMenuItem(onClick = {
            viewModel.add(id)
            close()
        }) {
            Text(stringResource(R.string.add_favorite))
        }
    }
}