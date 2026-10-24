package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.FavoriteArtistRepository

interface FavoriteArtistRegister {
    suspend fun existsFavoriteArtist(artistId: ArtistId): Boolean {
        val repository = FavoriteArtistRepository()

        return repository.exists(artistId)
    }

    /**
     * お気に入りの切り替えはこれだけにし、追加と削除は公開しない。
     * existsFavoriteArtist()で確かめてから書き込むと間に中断点ができ、
     * ダブルタップで2回挿入されてしまう。
     */
    suspend fun toggleFavoriteArtist(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.toggle(artistId)
    }

    suspend fun replaceFavoriteArtists(artistIds: List<ArtistId>) {
        val repository = FavoriteArtistRepository()

        repository.replaceAll(artistIds)
    }
}
