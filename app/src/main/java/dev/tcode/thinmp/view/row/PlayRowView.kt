package dev.tcode.thinmp.view.row

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer

@Composable
fun PlayRowView(song: SongModel, content: @Composable RowScope.() -> Unit) {
    Row(content = content,
        modifier = Modifier.clickable {
        val musicPlayer = MusicPlayer()
//        musicPlayer.start(song)
    })
}
