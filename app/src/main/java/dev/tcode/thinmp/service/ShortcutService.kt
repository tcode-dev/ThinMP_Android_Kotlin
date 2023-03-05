package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.realm.ShortcutRealmModel
import dev.tcode.thinmp.repository.media.AlbumRepository
import dev.tcode.thinmp.repository.media.ArtistRepository
import dev.tcode.thinmp.repository.realm.ItemType
import dev.tcode.thinmp.repository.realm.ShortcutRepository

class ShortcutService(val context: Context) {
    fun findAll(): List<ShortcutModel> {
        val shortcutRepository = ShortcutRepository()
        val artistRepository = ArtistRepository(context)
        val albumRepository = AlbumRepository(context)
        val shortcuts = shortcutRepository.findAll()
        val group = shortcuts.groupBy { it.type }
        var shortcutArtists: List<ShortcutModel> = emptyList()

        if (group.containsKey(ItemType.ARTIST.ordinal)) {
            val artistIds = group[ItemType.ARTIST.ordinal]?.map { ArtistId(it.itemId) }

            shortcutArtists = artistIds?.let { artistRepository.findByIds(it) }?.map {
                val albums = albumRepository.findByArtistId(it.id)
                ShortcutModel(it.artistId, it.name, "artist", albums.first().getImageUri())
            }!!
        }

        return shortcutArtists

//        if (group.containsKey(ItemType.ALBUM.ordinal)) {
//
//
//        }
//
//        shortcuts.groupBy { it.type }.map { it ->
//
//            if (it.key == ItemType.ALBUM.ordinal) {
//                val albumIds = it.value.map { AlbumId(it.itemId) }
//                val albumRepository = AlbumRepository(context)
//                val albums = albumRepository.findByIds(albumIds)
//            }
//        }
    }

    fun ar(model: ShortcutRealmModel): ShortcutRealmModel {
        return model
     }
}