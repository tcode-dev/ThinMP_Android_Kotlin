package dev.tcode.thinmp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class HeadsetEventReceiver(var callback: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getIntExtra("state", AudioManager.SCO_AUDIO_STATE_ERROR) == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
            callback()
        }
    }
}