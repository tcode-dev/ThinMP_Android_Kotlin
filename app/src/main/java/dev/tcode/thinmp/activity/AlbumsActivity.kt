package dev.tcode.thinmp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import dev.tcode.thinmp.activity.ui.theme.ThinMPTheme
import dev.tcode.thinmp.view.page.AlbumsPageView

class AlbumsActivity : ComponentActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThinMPTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    AlbumsPageView(arrayOf("Android", "ios", "web"))
                }
            }
        }
    }
}
