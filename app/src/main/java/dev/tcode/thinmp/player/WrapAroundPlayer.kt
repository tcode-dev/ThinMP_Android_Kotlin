package dev.tcode.thinmp.player

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
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

    /** Passes every callback through and replaces only the command set with this player's own. */
    private inner class CommandsListener(private val listener: Player.Listener) : Player.Listener by listener {
        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            listener.onAvailableCommandsChanged(this@WrapAroundPlayer.getAvailableCommands())
        }
    }
}
