package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.tcode.thinmp.viewModel.ArtistDetailViewModel

@ExperimentalFoundationApi
@Composable
fun ArtistDetailScreen(
    id: String,
    viewModel: ArtistDetailViewModel = ArtistDetailViewModel(
        LocalContext.current,
        id
    )
) {
    val uiState = viewModel.uiState

    Text(uiState.primaryText)
}
