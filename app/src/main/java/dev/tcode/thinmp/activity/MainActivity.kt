package dev.tcode.thinmp.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.ui.theme.ThinMPTheme
import dev.tcode.thinmp.view.nav.ThinMPNavHost
import dev.tcode.thinmp.view.permission.PermissionView

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 画面全体に表示
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // StatusBarを透過
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)

        // display cutout領域にも表示
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        setContent {
            ThinMPTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PermissionView {
                        ThinMPNavHost()
                    }
                }
            }
        }

//        println("Log: MainApplication startService")
//        startForeground(Intent(applicationContext, MusicService::class.java))
//        startForegroundService(Intent(applicationContext, MusicService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Log: MainActivity onDestroy")
    }
}