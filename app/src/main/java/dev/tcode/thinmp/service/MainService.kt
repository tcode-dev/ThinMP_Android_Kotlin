package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.constant.MainMenuItem
import dev.tcode.thinmp.constant.RecentlyAlbumConstant
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.repository.AlbumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainService(val context: Context) {
    fun getMenu(): List<MainMenuItem> {
        return MainMenuEnum.getList(context)
    }

    // TODO: ConfigStore reads block on runBlocking internally; drop the wrapper once
    // ConfigStore exposes suspend accessors and MusicService no longer needs them synchronously.
    suspend fun getRecentlyAlbumsVisibility(): Boolean = withContext(Dispatchers.IO) {
        val config = ConfigStore(context)

        config.getRecentlyAlbumsVisibility()
    }

    suspend fun getRecentlyAlbums(): List<AlbumModel> {
        val repository = AlbumRepository(context)

        return repository.findRecentlyAdded(RecentlyAlbumConstant.DISPLAY_COUNT)
    }

    // TODO: see getRecentlyAlbumsVisibility().
    suspend fun getShortcutVisibility(): Boolean = withContext(Dispatchers.IO) {
        val config = ConfigStore(context)

        config.getShortcutVisibility()
    }

    suspend fun getShortcuts(): List<ShortcutModel> {
        val service = ShortcutService(context)

        return service.findAll()
    }
}