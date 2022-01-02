package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.tcode.thinmp.viewModel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = MainViewModel(), navigate: () -> Unit) {
    Column {
        Text("Main Screen!!")
        Button(onClick = navigate) {
            Text(text = "next albums")
        }
        LazyColumn{
            items(viewModel.uiState.menuList) { menu ->
                Text(menu.label)
            }
        }
    }
}
