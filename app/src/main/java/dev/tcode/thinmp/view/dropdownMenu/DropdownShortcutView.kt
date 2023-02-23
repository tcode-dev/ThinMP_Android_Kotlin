package dev.tcode.thinmp.view.dropdownMenu

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.viewModel.ShortcutViewModel

@Composable
fun DropdownShortcutView(id: ArtistId, addText: Int, removeText: Int, close: () -> Unit, viewModel: ShortcutViewModel = viewModel()) {
    if (viewModel.existsShortcut(id)) {
        DropdownMenuItem(onClick = {
            viewModel.deleteShortcut(id)
            close()
        }) {
            Text(stringResource(removeText))
        }
    } else {
        DropdownMenuItem(onClick = {
            viewModel.addShortcut(id)
            close()
        }) {
            Text(stringResource(addText))
        }
    }
}