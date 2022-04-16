package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import dev.tcode.thinmp.view.list.AlbumListView
import dev.tcode.thinmp.view.permission.PermissionView
import dev.tcode.thinmp.viewModel.AlbumsViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel = AlbumsViewModel(context)

    PermissionView()
//        Column{
//            Text(text = "Albums")
//            AlbumListView(navController, viewModel.uiState.albums)
//        }
}
