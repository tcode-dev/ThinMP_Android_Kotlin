package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.repository.media.AlbumRepository

class MainService(val context: Context) {
    fun getMenu(): List<MainMenuEnum> {
        return MainMenuEnum.values().toList()
    }

    fun getRecentlyAlbums(): List<AlbumModel> {
        val repository = AlbumRepository(context)

        return repository.findRecentlyAdded(20)
    }
}