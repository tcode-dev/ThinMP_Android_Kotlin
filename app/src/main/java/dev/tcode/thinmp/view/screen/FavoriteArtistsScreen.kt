package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.view.collapsingTopAppBar.MenuCollapsingTopAppBarView
import dev.tcode.thinmp.view.dropdownMenu.FavoriteArtistDropdownMenuItemView
import dev.tcode.thinmp.view.layout.MiniPlayerLayoutView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.EmptyTopAppBarView
import dev.tcode.thinmp.viewModel.FavoriteArtistsViewModel

@ExperimentalFoundationApi
@Composable
fun FavoriteArtistsScreen(viewModel: FavoriteArtistsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val navigator = LocalNavigator.current

    CustomLifecycleEventObserver(viewModel)

    MiniPlayerLayoutView {
        MenuCollapsingTopAppBarView(title = stringResource(R.string.favorite_artists), dropdownMenus = {
            DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, onClick = { navigator.favoriteArtistsEdit() })
        }) {
            item {
                EmptyTopAppBarView()
            }
            itemsIndexed(uiState.artists) { index, artist ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    val expanded = remember { mutableStateOf(false) }
                    val close = {
                        expanded.value = false
                        viewModel.load(context)
                    }

                    PlainRowView(artist.name, Modifier.pointerInput(artist.url) {
                        detectTapGestures(onLongPress = { expanded.value = true }, onTap = { navigator.artistDetail(artist.id) })
                    })
                    DropdownMenu(expanded = expanded.value,
                        offset = DpOffset((-1).dp, 0.dp),
                        modifier = Modifier.background(MaterialTheme.colorScheme.onBackground),
                        onDismissRequest = { expanded.value = false }) {
                        FavoriteArtistDropdownMenuItemView(artist.artistId, close)
                    }
                }
            }
            item {
                EmptyMiniPlayerView()
            }
        }
    }
}