package dev.tcode.thinmp.view.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.util.miniPlayerHeight

@Composable
fun MiniPlayerLayoutView(isVisibleMiniPlayer: Boolean = true, content: @Composable (() -> Unit)) {
    val miniPlayerHeight = miniPlayerHeight()

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        content()

        if (isVisibleMiniPlayer) {
            Box(modifier = Modifier.constrainAs(miniPlayer) {
                top.linkTo(parent.bottom, margin = (-miniPlayerHeight).dp)
            }) {
                MiniPlayerView()
            }
        }
    }
}