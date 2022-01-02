package dev.tcode.thinmp.view.nav

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.tcode.thinmp.view.screen.AlbumsScreen
import dev.tcode.thinmp.view.screen.MainScreen
import dev.tcode.thinmp.view.screen.SongsScreen

@ExperimentalFoundationApi
@Composable
fun Nav() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("albums") { AlbumsScreen() }
        composable("songs") { SongsScreen() }
    }
}
