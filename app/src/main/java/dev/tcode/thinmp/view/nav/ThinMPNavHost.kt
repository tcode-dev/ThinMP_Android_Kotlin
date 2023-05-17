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
fun ThinMPNavHost() {
    val navController = rememberNavController()

    CompositionLocalProvider(LocalNavigator provides Navigator(navController)) {
        NavHost(navController = navController, startDestination = NavConstant.MAIN) {
            composable(NavConstant.MAIN) { MainScreen(navController) }
            composable(NavConstant.MAIN_EDIT) { MainEditScreen() }
            composable(NavConstant.ARTISTS) { ArtistsScreen() }
            composable(NavConstant.ALBUMS) { AlbumsScreen() }
            composable(NavConstant.SONGS) { SongsScreen() }
            composable(NavConstant.FAVORITE_ARTISTS) { FavoriteArtistsScreen() }
            composable(NavConstant.FAVORITE_ARTISTS_EDIT) { FavoriteArtistsEditScreen() }
            composable(NavConstant.FAVORITE_SONGS) { FavoriteSongsScreen() }
            composable(NavConstant.FAVORITE_SONGS_EDIT) { FavoriteSongsEditScreen() }
            composable(NavConstant.PLAYLISTS) { PlaylistsScreen() }
            composable(
                "${NavConstant.ALBUM_DETAIL}/{${NavConstant.ID}}", arguments = listOf(navArgument(NavConstant.ID) { type = NavType.StringType })
            ) { backStackEntry ->
                AlbumDetailScreen(backStackEntry.arguments?.getString(NavConstant.ID).let(::requireNotNull))
            }
            composable(
                "${NavConstant.ARTIST_DETAIL}/{${NavConstant.ID}}", arguments = listOf(navArgument(NavConstant.ID) { type = NavType.StringType })
            ) { backStackEntry ->
                ArtistDetailScreen(backStackEntry.arguments?.getString(NavConstant.ID).let(::requireNotNull))
            }
            composable(
                "${NavConstant.PLAYLIST_DETAIL}/{${NavConstant.ID}}", arguments = listOf(navArgument(NavConstant.ID) { type = NavType.StringType })
            ) { backStackEntry ->
                PlaylistDetailScreen(backStackEntry.arguments?.getString(NavConstant.ID).let(::requireNotNull))
            }
            composable(
                "${NavConstant.PLAYLIST_DETAIL_EDIT}/{${NavConstant.ID}}", arguments = listOf(navArgument(NavConstant.ID) { type = NavType.StringType })
            ) { backStackEntry ->
                PlaylistDetailEditScreen(backStackEntry.arguments?.getString(NavConstant.ID).let(::requireNotNull))
            }
            composable(NavConstant.PLAYER) { PlayerScreen() }
        }
    }
}