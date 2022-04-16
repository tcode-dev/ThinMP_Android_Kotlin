package dev.tcode.thinmp.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import dev.tcode.thinmp.activity.ui.theme.ThinMPTheme
import dev.tcode.thinmp.view.nav.Nav

class MainActivity : AppCompatActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 画面全体に表示
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // StatusBarを透過
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)

        setContent {
            ThinMPTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Nav()
                }
            }
        }
    }
}