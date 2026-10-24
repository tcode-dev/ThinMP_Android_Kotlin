package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.FavoriteSongRepository

interface FavoriteSongRegister {
    suspend fun existsFavoriteSong(songId: SongId): Boolean {
        val repository = FavoriteSongRepository()

        return repository.exists(songId)
    }

    /**
     * お気に入りの切り替えはこれだけにし、追加と削除は公開しない。
     * existsFavoriteSong()で確かめてから書き込むと間に中断点ができ、ダブルタップで2回挿入されてしまう。
     */
    suspend fun toggleFavoriteSong(songId: SongId) {
        val repository = FavoriteSongRepository()

        repository.toggle(songId)
    }

    suspend fun replaceFavoriteSongs(songIds: List<SongId>) {
        val repository = FavoriteSongRepository()

        repository.replaceAll(songIds)
    }
}
