package dev.tcode.thinmp.viewModel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.repository.media.SongRepository
import dev.tcode.thinmp.view.screen.PlayInterface

data class SongsUiState(
    var songs: List<SongModel> = emptyList()
)

class SongsViewModel(
    context: Context
) : PlayInterface {
    override lateinit var musicService: MusicService
    override lateinit var connection: ServiceConnection
    override var bound: Boolean = false
    var uiState by mutableStateOf(SongsUiState())
        private set

    init {
        bindService(context)
        load(context)
    }

    fun load(context: Context) {
        val repository = SongRepository(context)

        uiState.songs = repository.findAll()
    }
}
