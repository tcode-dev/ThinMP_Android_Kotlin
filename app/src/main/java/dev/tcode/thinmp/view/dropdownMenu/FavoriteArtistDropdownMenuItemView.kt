package dev.tcode.thinmp.view.dropdownMenu

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.viewModel.FavoriteArtistRegisterViewModel

@Composable
fun FavoriteArtistDropdownMenuItemView(id: ArtistId, close: () -> Unit, viewModel: FavoriteArtistRegisterViewModel = viewModel()) {
    // Keyed by id so the row that opened the menu is the row this reflects. Until the query
    // returns the item stays in place but disabled, so the menu does not change height.
    //
    // The assignment lint asks for is on this very line. The check reports the call whatever the
    // body does: a multi-statement lambda, a plain constant with no suspend call, .value instead
    // of by, a positional first argument - all four still report it, so there is nothing to write
    // differently. Suppressed at the one line it fires on rather than disabled for the project,
    // so a produceState that really does forget to assign is still caught.
    @Suppress("ProduceStateDoesNotAssignValue")
    val isFavorite by produceState<Boolean?>(initialValue = null, id) { value = viewModel.isFavorite(id) }

    DropdownMenuItem(
        enabled = isFavorite != null,
        text = { Text(stringResource(if (isFavorite == true) R.string.remove_favorite else R.string.add_favorite), color = MaterialTheme.colorScheme.primary) },
        onClick = {
            viewModel.toggle(id)
            close()
        },
    )
}
