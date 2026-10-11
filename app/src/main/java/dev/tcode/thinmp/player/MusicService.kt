package dev.tcode.thinmp.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dev.tcode.thinmp.R
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.constant.NotificationConstant
import dev.tcode.thinmp.model.media.SongModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

interface MusicServiceListener {
    fun onChange() {}
    fun onError() {}
}

/**
 * Owns the one ExoPlayer and the one MediaSession for the life of the service. The notification,
 * the lock screen and the foreground state are MediaSessionService's: it starts the service in the
 * foreground when playback starts and drops back out when it stops.
 *
 * The screens still reach the service through [MusicBinder], bound with [bindIntent]'s own action.
 * Every other bind - a MediaController, the system - goes to MediaSessionService.
 */
@OptIn(UnstableApi::class)
class MusicService : MediaSessionService() {
    companion object {
        private const val ACTION_BIND_MUSIC_PLAYER = "dev.tcode.thinmp.player.BIND_MUSIC_PLAYER"

        /**
         * Sent to every connected controller once retry() has dropped a song it could not play, so
         * the song lists can reload without it. The queue changing is not enough to tell: it also
         * changes whenever a new list is started.
         */
        val SONG_REMOVED = SessionCommand("dev.tcode.thinmp.player.SONG_REMOVED", Bundle.EMPTY)

        /** The intent that binds to [MusicBinder]. A plain intent reaches MediaSessionService instead, which answers it with nothing. */
        fun bindIntent(context: Context): Intent {
            return Intent(context, MusicService::class.java).setAction(ACTION_BIND_MUSIC_PLAYER)
        }
    }

    private val binder = MusicBinder()

    /**
     * Built once in onCreate() and kept until onDestroy(). A new song replaces the queue with
     * setMediaItems() rather than building a new player, so an empty queue - before the first
     * start(), or after retry() dropped the last song - is the state the controls have to tolerate,
     * not a missing player.
     */
    private lateinit var exoPlayer: ExoPlayer

    /** What the session is given, so its controls go back and forward the way the app's do. */
    private lateinit var player: WrapAroundPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var config: ConfigStore

    /**
     * Owns the ConfigStore reads and writes, so none of them run on the main thread. Cancelled in
     * onDestroy().
     */
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)

    /**
     * Defaults until the stored values arrive. onCreate() used to block the main thread on two
     * DataStore reads, inside the five second window startForegroundService() allows.
     *
     * The buttons are live during that window, so each of the two carries a flag saying the user
     * has already worked it. loadConfig() skips the ones that are set: what the user just chose is
     * newer than what was on disk, and the player listener has saved it already.
     */
    private var repeat: RepeatState = RepeatState.OFF
    private var repeatChangedByUser = false
    private var shuffle = false
    private var shuffleChangedByUser = false

    /** Set while loadConfig() applies the stored values, so the listener does not save them back. */
    private var applyingStoredConfig = false
    private var listeners: MutableList<MusicServiceListener> = mutableListOf()
    private var isPlaying = false

    /**
     * setHandleAudioBecomingNoisy replaces a HEADSET_PLUG receiver this service used to register in
     * onCreate(). That receiver stopped rather than paused - which leaves ExoPlayer idle, so the play
     * button afterwards did nothing until the queue was rebuilt - and it read the wired headset's
     * state extra with AudioManager's Bluetooth SCO constants, which only lined up because both
     * happen to be 0. ACTION_AUDIO_BECOMING_NOISY is the intent meant for this, it covers Bluetooth
     * going away as well as the wired jack, and the player enables and disables its own receiver
     * around playback.
     */
    override fun onCreate() {
        super.onCreate()

        config = ConfigStore(baseContext)
        exoPlayer = ExoPlayer.Builder(applicationContext).setLooper(Looper.getMainLooper()).setHandleAudioBecomingNoisy(true).build()
        exoPlayer.addListener(PlayerEventListener())
        player = WrapAroundPlayer(exoPlayer)
        mediaSession = MediaSession.Builder(this, player).setSessionActivity(sessionActivity()).setCallback(SessionCallback()).build()

        setMediaNotificationProvider(notificationProvider())
        // The screens reach the service through the binder, not through a MediaController, so
        // nothing would add the session through onGetSession(). Without this the service never
        // learns about it and posts no notification.
        addSession(mediaSession)

        loadConfig()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    fun addEventListener(listener: MusicServiceListener) {
        listeners.add(listener)
    }

    fun removeEventListener(listener: MusicServiceListener) {
        listeners.remove(listener)
    }

    /** Read back from the item itself, so a queue a MediaController set is as readable as one set here. */
    fun getCurrentSong(): SongModel? {
        return exoPlayer.currentMediaItem?.toSongModel()
    }

    fun start(songs: List<SongModel>, index: Int) {
        if (songs.isEmpty()) return

        // MediaSessionService starts itself only once playback is under way. Until then the service
        // is merely bound and goes away with the last screen unbinding - before the song has even
        // loaded, or for good when it fails to load.
        startService(Intent(this, MusicService::class.java))
        exoPlayer.setMediaItems(songs.map { it.toMediaItem(withUri = true) }, index, 0)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun play() {
        if (exoPlayer.mediaItemCount == 0) return

        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    /**
     * A seek back to the start of the same song is not a media item transition, so nothing else
     * would tell the screens about it.
     */
    fun prev() {
        player.seekToPrevious()
        onChange()
    }

    fun next() {
        player.seekToNext()
    }

    fun getRepeat(): RepeatState {
        return repeat
    }

    /** The player listener saves the new value; see onRepeatModeChanged(). */
    fun changeRepeat() {
        repeatChangedByUser = true
        repeat = repeat.next()
        exoPlayer.repeatMode = repeat.toRepeatMode()
    }

    fun getShuffle(): Boolean {
        return shuffle
    }

    /** The player listener saves the new value; see onShuffleModeEnabledChanged(). */
    fun changeShuffle() {
        shuffleChangedByUser = true
        shuffle = !shuffle
        exoPlayer.shuffleModeEnabled = shuffle
    }

    fun seekTo(ms: Long) {
        if (exoPlayer.mediaItemCount == 0) return

        try {
            exoPlayer.seekTo(ms)
        } catch (e: Exception) {
            onError()
        }
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun getCurrentPosition(): Long {
        if (exoPlayer.mediaItemCount == 0) return 0

        return exoPlayer.currentPosition
    }

    /**
     * Brings the app back the way the launcher does. An intent naming MainActivity alone would
     * stack a second instance on top of the one already in the task.
     */
    private fun sessionActivity(): PendingIntent {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent(this, MainActivity::class.java)

        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /** Keeps the channel the app's own notification used, so its name and the user's settings for it carry over. */
    private fun notificationProvider(): DefaultMediaNotificationProvider {
        val provider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(NotificationConstant.CHANNEL_ID)
            .setChannelName(R.string.channel_name)
            .setNotificationId(NotificationConstant.NOTIFICATION_ID)
            .build()

        provider.setSmallIcon(R.drawable.round_audiotrack_24)

        return provider
    }

    /**
     * The stored values arrive after onCreate() has returned and are applied to the player as they
     * come, unless the user has changed that setting in the meantime.
     *
     * Both are read before either is applied, so nothing suspends between the two flag checks and
     * the fields they guard: a tap either lands before this whole block, and is kept, or after it,
     * and wins on its own. Assigning as each read returned left a gap between them where a tap on
     * shuffle was still overwritten however the repeat flag came out.
     */
    private fun loadConfig() {
        serviceScope.launch {
            val storedRepeat = config.getRepeat()
            val storedShuffle = config.getShuffle()

            applyingStoredConfig = true

            if (!repeatChangedByUser) {
                repeat = storedRepeat
                exoPlayer.repeatMode = storedRepeat.toRepeatMode()
            }

            if (!shuffleChangedByUser) {
                shuffle = storedShuffle
                exoPlayer.shuffleModeEnabled = storedShuffle
            }

            applyingStoredConfig = false
            onChange()
        }
    }

    private fun onChange() {
        listeners.forEach {
            it.onChange()
        }
    }

    private fun onError() {
        retry()
        mediaSession.broadcastCustomCommand(SONG_REMOVED, Bundle.EMPTY)
        listeners.forEach {
            it.onError()
        }
    }

    /**
     * Drops the song that failed and carries on with the one that took its place, or with the one
     * before it when the failed song was the last. An empty queue stops there.
     *
     * The player has gone idle with the error, so it has to be prepared again before it plays.
     */
    private fun retry() {
        val count = exoPlayer.mediaItemCount

        if (count == 0) return

        val currentIndex = exoPlayer.currentMediaItemIndex

        exoPlayer.removeMediaItem(currentIndex)

        if (exoPlayer.mediaItemCount == 0) {
            exoPlayer.stop()

            return
        }

        val nextIndex = if (count == currentIndex + 1) currentIndex - 1 else currentIndex

        exoPlayer.seekTo(nextIndex, 0)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (intent?.action == ACTION_BIND_MUSIC_PLAYER) return binder

        return super.onBind(intent)
    }

    /**
     * MediaSessionService asks to be restarted after the process is killed, which would bring back
     * a service with an empty queue and nothing to show. super still has to run: it is what
     * delivers the notification's media button intents.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return START_NOT_STICKY
    }

    /**
     * Swiping the app away stops playback, as closing the app always has. MediaSessionService's own
     * default keeps the service running while it plays.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        serviceJob.cancel()
        mediaSession.release()
        exoPlayer.release()
        super.onDestroy()
    }

    inner class PlayerEventListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) return

            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                isPlaying = player.isPlaying
                onChange()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            onChange()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            // ループ再生していない場合最後の曲の再生が終了すると呼ばれる
            // 曲が1曲の場合、onMediaItemTransition、events.contains(Player.EVENT_IS_PLAYING_CHANGED)は呼ばれない
            if (playbackState == Player.STATE_ENDED) {
                isPlaying = false
                exoPlayer.pause()
                exoPlayer.seekTo(0, 0)
                onChange()
            }
        }

        /**
         * Saved here rather than in changeRepeat(), so a change from the lock screen or any other
         * controller is saved too. The save reads the field when it runs rather than capturing it,
         * so rapid taps all persist the state the user actually ended on.
         */
        override fun onRepeatModeChanged(repeatMode: Int) {
            repeat = repeatStateOf(repeatMode)

            if (!applyingStoredConfig) {
                repeatChangedByUser = true
                serviceScope.launch { config.saveRepeat(repeat) }
            }

            onChange()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            shuffle = shuffleModeEnabled

            if (!applyingStoredConfig) {
                shuffleChangedByUser = true
                serviceScope.launch { config.saveShuffle(shuffle) }
            }

            onChange()
        }

        override fun onPlayerError(error: PlaybackException) {
            // 曲が削除されている場合
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
                onError()
            }
        }
    }

    /**
     * PlaybackController sends only each song's media id and metadata, and an item from outside the
     * app - a car, a watch - may carry nothing but the id, so the playable URI is rebuilt from the id
     * here. The service is started for the same reason start() starts it.
     */
    private inner class SessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession, controller: MediaSession.ControllerInfo, mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            startService(Intent(this@MusicService, MusicService::class.java))

            val items = mediaItems.map { it.buildUpon().setUri(songMediaUri(it.mediaId)).build() }

            return Futures.immediateFuture(items.toMutableList())
        }
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}
