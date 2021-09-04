package dev.tcode.thinmp.player

import dev.tcode.thinmp.databinding.FragmentMiniPlayerBinding

/**
 * 画面下のミニプレイヤー
 * UIの変更を行う
 */
class MiniPlayer private constructor(
    private val binding: FragmentMiniPlayerBinding
) {
    companion object {
        fun createInstance(
            binding: FragmentMiniPlayerBinding
        ): MiniPlayer {
            return MiniPlayer(binding)
        }
    }
}