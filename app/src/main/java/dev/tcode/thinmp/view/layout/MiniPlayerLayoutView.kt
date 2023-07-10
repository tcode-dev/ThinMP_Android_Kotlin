package dev.tcode.thinmp.view.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.util.miniPlayerHeight

@Composable
fun MiniPlayerLayoutView(content: @Composable (() -> Unit)) {
    val miniPlayerHeight = miniPlayerHeight()

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        content()

        Box(modifier = Modifier.constrainAs(miniPlayer) {
            top.linkTo(parent.bottom, margin = (-miniPlayerHeight).dp)
        }) {
            MiniPlayerView()
        }
    }
}