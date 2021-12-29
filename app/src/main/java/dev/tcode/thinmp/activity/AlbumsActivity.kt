package dev.tcode.thinmp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.tcode.thinmp.activity.ui.theme.ThinMPTheme

class AlbumsActivity : ComponentActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThinMPTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting(arrayOf("Android", "ios", "web"))
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun Greeting(names: Array<String>) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(2)
    ) {
        items(names) { name ->
            Text(text = "Hello $name!")
        }
    }
}
