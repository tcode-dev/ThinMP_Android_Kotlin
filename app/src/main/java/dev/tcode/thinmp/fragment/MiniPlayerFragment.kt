package dev.tcode.thinmp.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dev.tcode.thinmp.R
import dev.tcode.thinmp.databinding.FragmentMiniPlayerBinding
import dev.tcode.thinmp.model.SongModel
import dev.tcode.thinmp.player.MiniPlayer
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.player.MusicService.MusicBinder

class MiniPlayerFragment : Fragment() {
    private var musicService: MusicService? = null
    private var miniPlayer: MiniPlayer? = null
    private var connection: ServiceConnection? = null
    private var bound = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connection = createConnection()
        bindMusicService()
        activity?.startService(
            Intent(
                activity,
                MusicService::class.java
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMiniPlayerBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_mini_player,
            container,
            false
        )
        miniPlayer = MiniPlayer.createInstance(binding)
        binding.miniPlayer = miniPlayer

        return binding.root
    }

    /**
     * ServiceConnection
     */
    private fun createConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder = service as MusicBinder
                musicService = binder.service

                bound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                bound = false
            }
        }
    }

    fun start(song: SongModel) {
        musicService?.initStart(song)
    }

    /**
     * bindMusicService
     */
    private fun bindMusicService() {
        val intent = Intent(activity, MusicService::class.java)
        activity?.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
    }

    /**
     * unbindMusicService
     */
    private fun unbindMusicService() {
        activity?.unbindService(connection!!)
    }
}