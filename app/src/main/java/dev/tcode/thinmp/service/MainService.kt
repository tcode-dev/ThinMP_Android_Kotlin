package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.constant.MainMenuItem
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.repository.media.AlbumRepository

class MainService(val context: Context) {
    fun getMenu(): List<MainMenuItem> {
        return MainMenuEnum.getList(context)
    }

    fun getRecentlyAlbums(): List<AlbumModel> {
        val repository = AlbumRepository(context)

        return repository.findRecentlyAdded(20)
    }

    fun getShortcuts(): List<ShortcutModel> {
        val service = ShortcutService(context)

        return service.findAll()
    }
}