package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.realm.FavoriteSongRepository

interface FavoriteSongRegister {
    fun exists(songId: SongId): Boolean {
        val repository = FavoriteSongRepository()

        return repository.exists(songId)
    }

    fun add(songId: SongId) {
        val repository = FavoriteSongRepository()

        repository.add(songId)
    }

    fun delete(songId: SongId) {
        val repository = FavoriteSongRepository()

        repository.delete(songId)
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("updateSongs")
    fun update(songIds: List<SongId>) {
        val repository = FavoriteSongRepository()

        repository.update(songIds)
    }
}