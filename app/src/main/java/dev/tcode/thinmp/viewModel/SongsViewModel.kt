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

data class SongsUiState(
    var songs: List<SongModel> = emptyList()
)

class SongsViewModel(context: Context) {
    private var musicService: MusicService? = null
    private var connection: ServiceConnection? = null
    private var bound: Boolean = false
    var uiState by mutableStateOf(SongsUiState())
        private set

    init {
        connection = createConnection()
        context.bindService(
            Intent(context, MusicService::class.java),
            connection!!,
            Context.BIND_AUTO_CREATE
        )
        load(context)
    }

    fun load(context: Context) {
        val repository = SongRepository(context)

        uiState.songs = repository.findAll()
    }

    fun start(song: SongModel) {
        musicService?.start(song)
    }

    private fun createConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
                musicService = binder.getService()
//                musicService.setListener(musicServiceListener)
                bound = true;
            }

            override fun onServiceDisconnected(name: ComponentName) {
                bound = false;
            }
        }
    }
}
