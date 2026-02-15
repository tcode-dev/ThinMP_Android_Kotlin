package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.constant.MainMenuItem
import dev.tcode.thinmp.constant.RecentlyAlbumConstant
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.repository.AlbumRepository

class MainService(val context: Context) {
    fun getMenu(): List<MainMenuItem> {
        return MainMenuEnum.getList(context)
    }

    fun getRecentlyAlbumsVisibility(): Boolean {
        val config = ConfigStore(context)

        return config.getRecentlyAlbumsVisibility()
    }

    fun getRecentlyAlbums(): List<AlbumModel> {
        val repository = AlbumRepository(context)

        return repository.findRecentlyAdded(RecentlyAlbumConstant.DISPLAY_COUNT)
    }

    fun getShortcutVisibility(): Boolean {
        val config = ConfigStore(context)

        return config.getShortcutVisibility()
    }

    fun getShortcuts(): List<ShortcutModel> {
        val service = ShortcutService(context)

        return service.findAll()
    }
}