package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import dev.tcode.thinmp.viewModel.MainViewModel

@Composable
fun MainScreen(navController: NavController) {
    val viewModel = MainViewModel()

    Column {
        Text("Main Screen!!")
        viewModel.uiState.menuList.forEach { menu ->
            Button(onClick = { navController.navigate(menu.key) }) {
                Text(text = menu.label)
            }
        }
    }
}
