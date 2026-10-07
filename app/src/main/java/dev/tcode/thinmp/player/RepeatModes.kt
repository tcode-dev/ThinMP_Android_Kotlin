package dev.tcode.thinmp.player

import androidx.media3.common.Player
import dev.tcode.thinmp.config.RepeatState

/** The order the repeat button steps through. */
fun RepeatState.next(): RepeatState {
    return when (this) {
        RepeatState.OFF -> RepeatState.ALL
        RepeatState.ALL -> RepeatState.ONE
        RepeatState.ONE -> RepeatState.OFF
    }
}

fun RepeatState.toRepeatMode(): Int {
    return when (this) {
        RepeatState.OFF -> Player.REPEAT_MODE_OFF
        RepeatState.ONE -> Player.REPEAT_MODE_ONE
        RepeatState.ALL -> Player.REPEAT_MODE_ALL
    }
}

fun repeatStateOf(repeatMode: Int): RepeatState {
    return when (repeatMode) {
        Player.REPEAT_MODE_ONE -> RepeatState.ONE
        Player.REPEAT_MODE_ALL -> RepeatState.ALL
        else -> RepeatState.OFF
    }
}
