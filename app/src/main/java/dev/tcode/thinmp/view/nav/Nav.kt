package dev.tcode.thinmp.view.nav

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.view.screen.*

@ExperimentalFoundationApi
@Composable
fun Nav() {
    val navController = rememberNavController()

    CompositionLocalProvider(LocalNavigator provides Navigator(navController)) {
        NavHost(navController = navController, startDestination = NavConstant.MAIN) {
            composable(NavConstant.MAIN) { MainScreen(navController) }
            composable(NavConstant.MAIN_EDIT) { MainEditScreen(navController) }
            composable(NavConstant.ARTISTS) { ArtistsScreen(navController) }
            composable(NavConstant.ALBUMS) { AlbumsScreen() }
            composable(NavConstant.SONGS) { SongsScreen(navController) }
            composable(NavConstant.FAVORITE_ARTISTS) { FavoriteArtistsScreen(navController) }
            composable(NavConstant.FAVORITE_ARTISTS_EDIT) { FavoriteArtistsEditScreen(navController) }
            composable(NavConstant.FAVORITE_SONGS) { FavoriteSongsScreen(navController) }
            composable(NavConstant.FAVORITE_SONGS_EDIT) { FavoriteSongsEditScreen(navController) }
            composable(NavConstant.PLAYLISTS) { PlaylistsScreen(navController) }
            composable(
                "${NavConstant.ALBUM_DETAIL}/{${NavConstant.ID}}", arguments = listOf(navArgument(NavConstant.ID) { type = NavType.StringType })
            ) { backStackEntry ->
                AlbumDetailScreen(navController, backStackEntry.arguments?.getString(NavConstant.ID).let(::requireNotNull))
            }
            composable(
                "${NavConstant.ARTIST_DETAIL}/{${NavConstant.ID}}", arguments = listOf(navArgument(NavConstant.ID) { type = NavType.StringType })
            ) { backStackEntry ->
                ArtistDetailScreen(navController, backStackEntry.arguments?.getString(NavConstant.ID).let(::requireNotNull))
            }
            composable(
                "${NavConstant.PLAYLIST_DETAIL}/{${NavConstant.ID}}", arguments = listOf(navArgument(NavConstant.ID) { type = NavType.StringType })
            ) { backStackEntry ->
                PlaylistDetailScreen(navController, backStackEntry.arguments?.getString(NavConstant.ID).let(::requireNotNull))
            }
            composable(NavConstant.PLAYER) { PlayerScreen(navController) }
        }
    }
}