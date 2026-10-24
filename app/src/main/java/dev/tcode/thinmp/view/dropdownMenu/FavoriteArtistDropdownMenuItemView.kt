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
    // idをkeyにして、メニューを開いた行の状態を表示する。
    // 結果が返るまでは項目を無効にして残し、メニューの高さが変わらないようにする。
    //
    // このlintは中身に関係なくproduceStateの呼び出しを警告するので(ラムダの書き方を変えても消えない)、
    // プロジェクト全体ではなくこの行だけで抑制する。
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
