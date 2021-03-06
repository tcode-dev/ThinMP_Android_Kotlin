package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import dev.tcode.thinmp.viewModel.MainViewModel

@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel = MainViewModel()) {
    val uiState = viewModel.uiState

    Column {
        Text("Main Screen!!")
        uiState.menuList.forEach { menu ->
            Button(onClick = { navController.navigate(menu.key) }) {
                Text(text = menu.label)
            }
        }
    }
}
