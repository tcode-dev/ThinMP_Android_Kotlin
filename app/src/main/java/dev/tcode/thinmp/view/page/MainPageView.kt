package dev.tcode.thinmp.view.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@ExperimentalFoundationApi
@Composable
fun MainPageView() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen{ navController.navigate("albums") } }
        composable("albums") { AlbumsPageView() }
    }
}

@Composable
fun MainScreen(navigate: () -> Unit) {
    Column {
        Text("Main Screen!!")
        Button(onClick = navigate) {
            Text(text = "next albums")
        }
    }
}
