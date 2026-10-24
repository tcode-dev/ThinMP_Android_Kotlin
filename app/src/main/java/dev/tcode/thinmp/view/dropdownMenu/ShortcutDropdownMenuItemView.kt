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
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId
import dev.tcode.thinmp.viewModel.ShortcutViewModel

@Composable
fun ShortcutDropdownMenuItemView(id: ShortcutItemId, callback: () -> Unit, viewModel: ShortcutViewModel = viewModel()) {
    // idをkeyにして、メニューを開いた行の状態を表示する。
    // 結果が返るまでは項目を無効にして残し、メニューの高さが変わらないようにする。
    // lintを抑制する理由はFavoriteArtistDropdownMenuItemViewを参照。
    @Suppress("ProduceStateDoesNotAssignValue")
    val isShortcut by produceState<Boolean?>(initialValue = null, id) { value = viewModel.isShortcut(id) }

    DropdownMenuItem(
        enabled = isShortcut != null,
        text = { Text(stringResource(if (isShortcut == true) R.string.remove_shortcut else R.string.add_shortcut), color = MaterialTheme.colorScheme.primary) },
        onClick = {
            viewModel.toggle(id)
            callback()
        },
    )
}
