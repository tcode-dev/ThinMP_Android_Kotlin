package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.constant.MainMenuItem
import dev.tcode.thinmp.constant.RecentlyAlbumConstant
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.repository.AlbumRepository
import dev.tcode.thinmp.repository.SongRepository

class MainService(val context: Context) {
    suspend fun getMenu(): List<MainMenuItem> {
        return MainMenuEnum.getList(context)
    }

    suspend fun getRecentlyAlbumsVisibility(): Boolean {
        val config = ConfigStore(context)

        return config.getRecentlyAlbumsVisibility()
    }

    /**
     * The albums an album's tracks were added most recently first.
     *
     * This used to sort the albums collection by its own _id, on the assumption that a larger id
     * means a newer album. MediaStore computes that id from the album's name instead: delete an
     * album and add it back and it returns with the id it had before, so the order it produces has
     * nothing to do with when anything was added.
     *
     * findByIds cannot preserve an order, so the ids drive it afterwards, the way the favourites
     * and shortcuts services do.
     */
    suspend fun getRecentlyAlbums(): List<AlbumModel> {
        val songRepository = SongRepository(context)
        val albumRepository = AlbumRepository(context)
        val albumIds = songRepository.findRecentlyAdded().map { it.albumId }.distinct().take(RecentlyAlbumConstant.DISPLAY_COUNT)
        val albums = albumRepository.findByIds(albumIds)

        return albumIds.mapNotNull { id -> albums.find { it.albumId == id } }
    }

    suspend fun getShortcutVisibility(): Boolean {
        val config = ConfigStore(context)

        return config.getShortcutVisibility()
    }

    suspend fun getShortcuts(): List<ShortcutModel> {
        val service = ShortcutService(context)

        return service.findAll()
    }
}