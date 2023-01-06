package dev.tcode.thinmp.view.nav

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.tcode.thinmp.view.screen.*

@ExperimentalFoundationApi
@Composable
fun Nav() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("artists") { ArtistsScreen(navController) }
        composable("albums") { AlbumsScreen(navController) }
        composable("songs") { SongsScreen(navController) }
        composable(
            "albumDetail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        )
        { backStackEntry ->
            AlbumDetailScreen(navController, backStackEntry.arguments?.getString("id").let(::requireNotNull))
        }
        composable(
            "artistDetail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        )
        { backStackEntry ->
            ArtistDetailScreen(navController, backStackEntry.arguments?.getString("id").let(::requireNotNull))
        }
        composable("player") { PlayerScreen(navController) }
    }
}
