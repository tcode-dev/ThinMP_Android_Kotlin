package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel

interface MusicPlayerListener : MusicServiceListener {
    fun onBind() {}
}

class MusicPlayer {
    private var musicService: MusicService? = null
    private lateinit var connection: ServiceConnection
    private var playingList: List<SongModel> = emptyList()
    private var startIndex: Int = 0
    private var listener: MusicPlayerListener? = null
    private var isConnecting = false
    private var bound = false

    fun isServiceRunning(): Boolean {
        return MusicService.isServiceRunning
    }

    fun isPlaying(): Boolean {
        return musicService?.isPlaying() ?: false
    }

    fun start(context: Context, songs: List<SongModel>, index: Int) {
        if (songs.isEmpty()) return

        playingList = songs
        startIndex = index

        if (isConnecting) return

        if (!isServiceRunning()) {
            context.startForegroundService(Intent(context, MusicService::class.java))
            bindService(context)

            return
        }

        if (!bound) {
            bindService(context)

            return
        }

        musicService?.start(playingList, startIndex)
    }

    fun play() {
        musicService?.play()
    }

    fun pause() {
        musicService?.pause()
    }

    fun prev() {
        musicService?.prev()
    }

    fun next() {
        musicService?.next()
    }

    fun seekTo(ms: Long) {
        musicService?.seekTo(ms)
    }

    fun getRepeat(): RepeatState {
        return musicService?.getRepeat() ?: RepeatState.OFF
    }

    fun changeRepeat() {
        musicService?.changeRepeat()
    }

    fun getShuffle(): Boolean {
        return musicService?.getShuffle() ?: false
    }

    fun changeShuffle() {
        musicService?.changeShuffle()
    }

    fun getCurrentSong(): SongModel? {
        return musicService?.getCurrentSong()
    }

    fun addEventListener(listener: MusicPlayerListener) {
        this.listener = listener
        musicService?.addEventListener(listener)
    }

    fun getCurrentPosition(): Long {
        return musicService?.getCurrentPosition() ?: 0
    }

    fun destroy(context: Context) {
        removeEventListener()
        unbindService(context)
    }

    fun bindService(context: Context) {
        if (isConnecting || bound) return

        isConnecting = true
        connection = createConnection()
        context.bindService(
            Intent(context, MusicService::class.java), connection, Context.BIND_AUTO_CREATE
        )
    }

    private fun removeEventListener() {
        this.listener = null
        musicService?.removeEventListener()
    }

    private fun unbindService(context: Context) {
        if (!bound) return

        context.unbindService(connection)
        musicService = null
        bound = false
    }

    private fun createConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
                musicService = binder.getService()
                listener?.let { musicService!!.addEventListener(it) }
                listener?.onBind()
                musicService?.start(playingList, startIndex)
                isConnecting = false
                bound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }
    }
}