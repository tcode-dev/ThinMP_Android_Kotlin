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
import android.view.ViewGroup.MarginLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dev.tcode.thinmp.R
import dev.tcode.thinmp.databinding.FragmentMiniPlayerBinding
import dev.tcode.thinmp.model.SongModel
import dev.tcode.thinmp.player.MiniPlayer
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.player.MusicService.MusicBinder
import dev.tcode.thinmp.player.MusicService.OnMusicServiceListener

class MiniPlayerFragment : Fragment() {
    private lateinit var musicService: MusicService
    private lateinit var miniPlayer: MiniPlayer
    private lateinit var musicServiceListener: OnMusicServiceListener
    private lateinit var connection: ServiceConnection
    private var bound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        musicServiceListener = createMusicServiceListener();
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
    ): View {
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

    fun start(song: SongModel) {
        musicService.initStart(song)
        this.view?.let { updateView(it.height) }
        update(song)
    }

    fun update() {
        update(musicService.song)
    }

    fun update(song: SongModel) {
        miniPlayer.update(song)
    }

    /**
     * ミニプレイヤー表示時に画面下に余白を確保する
     * 余白を設定するviewにはidにmainを設定しておく
     */
    private fun updateView(bottomMargin: Int) {
        val rootView =
            (activity?.findViewById(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        val mainView = rootView.findViewById<ViewGroup>(R.id.main)
        val mlp = mainView.layoutParams as MarginLayoutParams
        mlp.setMargins(
            mlp.leftMargin,
            mlp.topMargin,
            mlp.rightMargin,
            bottomMargin
        )
        mainView.layoutParams = mlp
    }

    /**
     * createMusicServiceListener
     */
    private fun createMusicServiceListener(): OnMusicServiceListener {
        return object : OnMusicServiceListener {
            override fun onChangeTrack(song: SongModel) {
                update(song)
            }

            override fun onStarted() {}
            override fun onFinished() {}
            override fun onForceFinished() {}
            override fun onScreenUpdate() {}
        }
    }

    /**
     * createConnection
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

    /**
     * bindMusicService
     */
    private fun bindMusicService() {
        val intent = Intent(activity, MusicService::class.java)

        activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    /**
     * unbindMusicService
     */
    private fun unbindMusicService() {
        activity?.unbindService(connection)
    }
}