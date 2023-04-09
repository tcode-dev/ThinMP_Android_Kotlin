package dev.tcode.thinmp.view.dropdownMenu

import androidx.compose.foundation.background
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId
import dev.tcode.thinmp.viewModel.ShortcutViewModel

@Composable
fun ShortcutDropdownMenuItemView(id: ShortcutItemId, callback: () -> Unit, viewModel: ShortcutViewModel = viewModel()) {
    if (viewModel.exists(id)) {
        DropdownMenuItem(onClick = {
            viewModel.delete(id)
            callback()
        }) {
            Text(stringResource(R.string.remove_shortcut), color = MaterialTheme.colorScheme.primary)
        }
    } else {
        DropdownMenuItem(onClick = {
            viewModel.add(id)
            callback()
        }) {
            Text(stringResource(R.string.add_shortcut), color = MaterialTheme.colorScheme.primary)
        }
    }
}