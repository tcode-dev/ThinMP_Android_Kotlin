package dev.tcode.thinmp.player

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.DeviceInfo
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi

/**
 * The player the MediaSession is given, so the notification, the lock screen and a headset's
 * buttons go back and forward the same way the app's own buttons do: back to the start of the song
 * after the first three seconds, otherwise to the previous song, and round from the first song to
 * the last and from the last to the first.
 *
 * The media notification sends seekToPreviousMediaItem() / seekToNextMediaItem() and the platform
 * session sends seekToPrevious() / seekToNext(), so all four are routed through the same two paths.
 *
 * ExoPlayer reports no "next" command on the last song when repeat is off, which greys the button
 * out wherever the session is shown. The wrap-around makes it meaningful there, so the four seek
 * commands are reported as available whenever there is a queue - both from getAvailableCommands()
 * and in the onAvailableCommandsChanged() callbacks the session listens to, which carry ExoPlayer's
 * own set unless they are rewritten on the way through.
 */
@OptIn(UnstableApi::class)
class WrapAroundPlayer(private val player: Player) : ForwardingPlayer(player) {
    private val PREV_MS = 3000
    private val listeners = mutableMapOf<Player.Listener, Player.Listener>()

    override fun seekToPrevious() {
        seekBackOrWrap()
    }

    override fun seekToPreviousMediaItem() {
        seekBackOrWrap()
    }

    override fun seekToNext() {
        seekForwardOrWrap()
    }

    override fun seekToNextMediaItem() {
        seekForwardOrWrap()
    }

    override fun getAvailableCommands(): Player.Commands {
        val commands = super.getAvailableCommands()

        if (player.mediaItemCount == 0) return commands

        return commands.buildUpon().addAll(
            Player.COMMAND_SEEK_TO_PREVIOUS, Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM, Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
        ).build()
    }

    override fun isCommandAvailable(command: Int): Boolean {
        return getAvailableCommands().contains(command)
    }

    override fun addListener(listener: Player.Listener) {
        if (listeners.containsKey(listener)) return

        val commandsListener = CommandsListener(listener)

        listeners[listener] = commandsListener
        super.addListener(commandsListener)
    }

    override fun removeListener(listener: Player.Listener) {
        val commandsListener = listeners.remove(listener) ?: return

        super.removeListener(commandsListener)
    }

    private fun seekBackOrWrap() {
        if (player.mediaItemCount == 0) return

        if (player.currentPosition > PREV_MS) {
            player.seekTo(0)
        } else if (player.currentMediaItemIndex == 0) {
            player.seekTo(player.mediaItemCount - 1, 0)
        } else {
            player.seekToPrevious()
        }
    }

    private fun seekForwardOrWrap() {
        if (player.mediaItemCount == 0) return

        if (player.currentMediaItemIndex == player.mediaItemCount - 1) {
            player.seekTo(0, 0)
        } else {
            player.seekToNext()
        }
    }

    /**
     * Passes every callback through and replaces only the command set with this player's own.
     *
     * Every method of Player.Listener is spelled out, the way Media3's own ForwardingListener does
     * it, because every one of them is a Java default method: `Player.Listener by listener` compiles
     * but generates no delegation for them, so the wrapped listener hears nothing but the one
     * callback written below. The session is the listener this wraps, and with the rest silenced it
     * never learned that playback had started.
     */
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    private inner class CommandsListener(private val listener: Player.Listener) : Player.Listener {
        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            listener.onAvailableCommandsChanged(this@WrapAroundPlayer.availableCommands)
        }

        override fun onEvents(player: Player, events: Player.Events) {
            listener.onEvents(player, events)
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            listener.onTimelineChanged(timeline, reason)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            listener.onMediaItemTransition(mediaItem, reason)
        }

        override fun onTracksChanged(tracks: Tracks) {
            listener.onTracksChanged(tracks)
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            listener.onMediaMetadataChanged(mediaMetadata)
        }

        override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
            listener.onPlaylistMetadataChanged(mediaMetadata)
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            listener.onIsLoadingChanged(isLoading)
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            listener.onLoadingChanged(isLoading)
        }

        override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
            listener.onTrackSelectionParametersChanged(parameters)
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            listener.onPlayerStateChanged(playWhenReady, playbackState)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            listener.onPlaybackStateChanged(playbackState)
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            listener.onPlayWhenReadyChanged(playWhenReady, reason)
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
            listener.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            listener.onIsPlayingChanged(isPlaying)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            listener.onRepeatModeChanged(repeatMode)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            listener.onShuffleModeEnabledChanged(shuffleModeEnabled)
        }

        override fun onPlayerError(error: PlaybackException) {
            listener.onPlayerError(error)
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            listener.onPlayerErrorChanged(error)
        }

        override fun onPositionDiscontinuity(reason: Int) {
            listener.onPositionDiscontinuity(reason)
        }

        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
            listener.onPositionDiscontinuity(oldPosition, newPosition, reason)
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            listener.onPlaybackParametersChanged(playbackParameters)
        }

        override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
            listener.onSeekBackIncrementChanged(seekBackIncrementMs)
        }

        override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
            listener.onSeekForwardIncrementChanged(seekForwardIncrementMs)
        }

        override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
            listener.onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs)
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            listener.onVideoSizeChanged(videoSize)
        }

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            listener.onSurfaceSizeChanged(width, height)
        }

        override fun onRenderedFirstFrame() {
            listener.onRenderedFirstFrame()
        }

        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            listener.onAudioSessionIdChanged(audioSessionId)
        }

        override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
            listener.onAudioAttributesChanged(audioAttributes)
        }

        override fun onVolumeChanged(volume: Float) {
            listener.onVolumeChanged(volume)
        }

        override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
            listener.onSkipSilenceEnabledChanged(skipSilenceEnabled)
        }

        override fun onCues(cues: MutableList<Cue>) {
            listener.onCues(cues)
        }

        override fun onCues(cueGroup: CueGroup) {
            listener.onCues(cueGroup)
        }

        override fun onMetadata(metadata: Metadata) {
            listener.onMetadata(metadata)
        }

        override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
            listener.onDeviceInfoChanged(deviceInfo)
        }

        override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
            listener.onDeviceVolumeChanged(volume, muted)
        }
    }
}
