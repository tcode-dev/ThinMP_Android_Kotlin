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
     * 最近曲が追加されたアルバムを新しい順に返す。
     *
     * アルバムの_idでは並べられない。
     * MediaStoreはアルバム名からidを計算するので、削除して追加し直したアルバムも以前と同じidになり、
     * idの大小は追加日時と関係がない。
     *
     * 曲はアルバムごとに1曲、表示件数分だけ返るので、ここで絞り込む必要はない。
     * findByIdsは順序を保たないので、他のサービスと同じくidの順に並べ直す。
     */
    suspend fun getRecentlyAlbums(): List<AlbumModel> {
        val songRepository = SongRepository(context)
        val albumRepository = AlbumRepository(context)
        val albumIds = songRepository.findRecentlyAddedByAlbum(RecentlyAlbumConstant.DISPLAY_COUNT).map { it.albumId }
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