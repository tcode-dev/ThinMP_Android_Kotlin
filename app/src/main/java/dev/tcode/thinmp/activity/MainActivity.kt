package dev.tcode.thinmp.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.core.content.ContextCompat
import dev.tcode.thinmp.activity.ui.theme.ThinMPTheme
import dev.tcode.thinmp.view.nav.Nav

class MainActivity : AppCompatActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // StatusBarを透過
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)

        setContent {
            ThinMPTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Nav()
                }
            }
        }
    }
}