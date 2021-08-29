package dev.tcode.thinmp.epoxy.controller

import android.util.Log
import com.airbnb.epoxy.TypedEpoxyController
import dev.tcode.thinmp.epoxy.model.song
import dev.tcode.thinmp.listener.PlayClickListener
import dev.tcode.thinmp.model.SongModel
import dev.tcode.thinmp.viewModel.SongsViewModel

class SongsController: TypedEpoxyController<SongsViewModel>() {
    override fun buildModels(vm: SongsViewModel) {
        buildSongs(vm.songs)
    }

    private fun buildSongs(songs: List<SongModel>) {
        songs
            .forEach { song ->
                song {
                    id(song.id)
                    primaryText(song.name)
                    clickListener(PlayClickListener(song))
                }
            }
    }
}
