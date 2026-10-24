package dev.tcode.thinmp.activity

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
import dev.tcode.thinmp.player.PlaybackController
import dev.tcode.thinmp.ui.theme.ThinMPTheme
import dev.tcode.thinmp.view.nav.ThinMPNavHost
import dev.tcode.thinmp.view.permission.PermissionView
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var playbackController: PlaybackController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // システムバーの下まで描画する
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)

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
    }

    /** Media3の推奨どおり、Activityが見えている間だけ接続する。 */
    override fun onStart() {
        super.onStart()
        playbackController.connect()
    }

    override fun onStop() {
        playbackController.release()
        super.onStop()
    }
}
