package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        LazyColumn{
            items(viewModel.uiState.menuList) { menu ->
                Text(menu.label)
                Button(onClick = { navController.navigate(menu.key) }) {
                    Text(text = menu.label)
                }
            }
        }
    }
}
