package dev.tcode.thinmp.player

import androidx.databinding.ObservableField
import dev.tcode.thinmp.databinding.FragmentMiniPlayerBinding
import dev.tcode.thinmp.model.SongModel

/**
 * 画面下のミニプレイヤー
 * UIの変更を行う
 */
class MiniPlayer private constructor(
    private val binding: FragmentMiniPlayerBinding
) {
    var primaryText = ObservableField<String>()

    fun update(song: SongModel) {
        this.primaryText.set(song.name)
    }

    companion object {
        fun createInstance(
            binding: FragmentMiniPlayerBinding
        ): MiniPlayer {
            return MiniPlayer(binding)
        }
    }
}