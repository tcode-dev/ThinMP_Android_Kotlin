package dev.tcode.thinmp.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
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
import dev.tcode.thinmp.constant.NotificationConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * ExoPlayerとMediaSessionを1つずつ持つ。
 * 通知、ロック画面、フォアグラウンド化はMediaSessionServiceに任せる。
 * 再生が始まるとフォアグラウンドになり、止まると外れる。
 *
 * 操作はすべてセッション経由で届く。
 * アプリからはPlaybackControllerのMediaController、
 * 通知・ロック画面・ヘッドセットからはシステムのMediaControllerを通る。
 */
@OptIn(UnstableApi::class)
class MusicService : MediaSessionService() {
    companion object {
        /**
         * retry()が再生できない曲を外したときに、接続中のコントローラーへ送る。
         * 曲リストはこれを受けて再読み込みする。
         * キューの変化は新しい一覧の再生でも起きるので、それでは判断できない。
         */
        val SONG_REMOVED = SessionCommand("dev.tcode.thinmp.player.SONG_REMOVED", Bundle.EMPTY)
    }

    /**
     * onCreate()で作り、onDestroy()まで使い続ける。
     * 曲を変えるときはsetMediaItems()でキューを置き換えるので、プレーヤーがないことはないが、
     * キューが空のこと(最初のstart()の前、retry()が最後の曲を外した後)はある。
     * 操作はその状態でも動かなければならない。
     */
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var config: ConfigStore

    /**
     * ConfigStoreの読み書きをメインスレッドの外で行うためのスコープ。
     * onDestroy()でキャンセルする。
     */
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)

    /**
     * onCreate()は保存値の読み込みを待たない。
     * 待つとstartForegroundService()の5秒の制限内でメインスレッドをブロックしてしまう。
     * そのため読み込みが終わるまでプレーヤーは既定値で動く。
     *
     * その間もボタンは押せるので、ユーザーが操作した設定はフラグを立て、loadConfig()で上書きしない。
     * ユーザーの操作のほうが新しく、リスナーがすでに保存している。
     */
    private var repeatChangedByUser = false
    private var shuffleChangedByUser = false

    /**
     * loadConfig()が保存値を反映している間はtrue。
     * リスナーがそれを保存し直さないようにする。
     */
    private var applyingStoredConfig = false

    /**
     * setHandleAudioBecomingNoisyで、有線イヤホンやBluetoothが外れたら一時停止する。
     * 停止ではなく一時停止なので、そのあと再生ボタンでそのまま再開できる。
     */
    override fun onCreate() {
        super.onCreate()

        config = ConfigStore(baseContext)
        exoPlayer = ExoPlayer.Builder(applicationContext).setLooper(Looper.getMainLooper()).setHandleAudioBecomingNoisy(true).build()
        exoPlayer.addListener(PlayerEventListener())
        mediaSession = MediaSession.Builder(this, exoPlayer).setSessionActivity(sessionActivity()).setCallback(SessionCallback()).build()

        setMediaNotificationProvider(notificationProvider())
        // メディアボタンなど、コントローラー以外から起動された場合もセッションを持つよう、
        // onGetSession()を待たずに追加する。
        addSession(mediaSession)

        loadConfig()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    /**
     * ランチャーと同じIntentでアプリを開く。
     * MainActivityを直接指定すると、既存のActivityの上にもう1つ積まれてしまう。
     */
    private fun sessionActivity(): PendingIntent {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent(this, MainActivity::class.java)

        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * NotificationConstantのチャンネルを使い、チャンネル名とユーザーの通知設定を引き継ぐ。
     */
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
     * 保存値はonCreate()の後に届き、その間にユーザーが変えた設定には反映しない。
     *
     * 両方を読み終えてから反映するので、フラグの確認から代入までの間に中断はない。
     * 読むたびに代入すると、その間にシャッフルのタップが割り込んで上書きされることがある。
     */
    private fun loadConfig() {
        serviceScope.launch {
            val storedRepeat = config.getRepeat()
            val storedShuffle = config.getShuffle()

            applyingStoredConfig = true

            if (!repeatChangedByUser) exoPlayer.repeatMode = storedRepeat.toRepeatMode()
            if (!shuffleChangedByUser) exoPlayer.shuffleModeEnabled = storedShuffle

            applyingStoredConfig = false
        }
    }

    private fun onError() {
        retry()
        mediaSession.broadcastCustomCommand(SONG_REMOVED, Bundle.EMPTY)
    }

    /**
     * 失敗した曲を外し、同じ位置の次の曲を再生する。
     * 最後の曲だった場合は1つ前の曲を再生し、キューが空になったら止める。
     *
     * エラーでプレーヤーはidleになっているので、prepareし直してから再生する。
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

    /**
     * プロセスが終了した後に再起動されても、キューが空で何もできないのでSTART_NOT_STICKYにする。
     * 通知のメディアボタンのIntentはsuperが処理するので、superは呼ぶ。
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return START_NOT_STICKY
    }

    /**
     * アプリをスワイプで消したら再生を止める。
     * MediaSessionServiceの既定では再生中は動き続ける。
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
        override fun onPlaybackStateChanged(playbackState: Int) {
            // リピートがオフで最後の曲が終わるとSTATE_ENDEDになるので、先頭の曲に戻して止める。
            // 1曲だけのときはonMediaItemTransitionもEVENT_IS_PLAYING_CHANGEDも来ないので、ここで扱う。
            if (playbackState == Player.STATE_ENDED) {
                exoPlayer.pause()
                exoPlayer.seekTo(0, 0)
            }
        }

        /**
         * アプリ、ロック画面など、どのコントローラーからの変更もここに届くので、ここで保存する。
         * 保存時にプレーヤーの値を読むので、連打しても最後の状態が保存される。
         */
        override fun onRepeatModeChanged(repeatMode: Int) {
            if (applyingStoredConfig) return

            repeatChangedByUser = true
            serviceScope.launch { config.saveRepeat(repeatStateOf(exoPlayer.repeatMode)) }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            if (applyingStoredConfig) return

            shuffleChangedByUser = true
            serviceScope.launch { config.saveShuffle(exoPlayer.shuffleModeEnabled) }
        }

        override fun onPlayerError(error: PlaybackException) {
            // ファイルが削除された曲
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
                onError()
            }
        }
    }

    /**
     * PlaybackControllerはmedia idとメタデータだけを送り、
     * 車や腕時計などアプリ外からはidしか来ないこともあるので、再生用のURIはここでidから作る。
     *
     * ここでサービスをstartもする。
     * MediaSessionServiceがstartするのは再生が始まってからなので、それまではバインドされているだけで、
     * 読み込み中や読み込みに失敗したときに接続が切れるとサービスごと消えてしまう。
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
}
