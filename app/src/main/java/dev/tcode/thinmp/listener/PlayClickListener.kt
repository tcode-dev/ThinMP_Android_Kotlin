package dev.tcode.thinmp.listener

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.tcode.thinmp.R
import dev.tcode.thinmp.fragment.MiniPlayerFragment
import dev.tcode.thinmp.model.SongModel

class PlayClickListener(private val song: SongModel) : View.OnClickListener {
    override fun onClick(view: View) {
        val fragment: Fragment? =
            (view.context as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.includeMiniPlayer)
        if (fragment is MiniPlayerFragment) {
            (fragment as MiniPlayerFragment?)!!.start(song)
        }
    }
}