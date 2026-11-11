package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.repository.FavoriteSongRepository

class FavoriteSongsService(
    val context: Context,
    private val favoriteSongRepository: FavoriteSongRepository = FavoriteSongRepository(),
    private val songRepository: SongRepository = SongRepository(context)
) {
    suspend fun findAll(): List<SongModel> {
        val songIds = favoriteSongRepository.findAll()
        val songs = songRepository.findByIds(songIds)
        val found = songIds.mapNotNull { id ->
            songs.find { it.songId == id }
        }

        removeMissing(songIds, songs)

        return found
    }

    /**
     * MediaStoreから消えた曲のお気に入りを削除する。
     * findAll()は呼び直さない。
     * 見つからなかった行を消しても見つかった曲の一覧は変わらないので、
     * 呼び出し側はすでに正しい結果を持っている。
     * idの件数と行数が一致するまで取り直すと、削除で解消できないずれがあったときに終わらなくなる。
     *
     * MediaStoreの問い合わせが失敗したときは、findByIds()がMediaStoreQueryExceptionを投げるのでここには来ない。
     * 失敗を空の一覧にすると、すべてのidが消えたと判断して全件削除してしまう。
     */
    private suspend fun removeMissing(songIds: List<SongId>, songs: List<SongModel>) {
        val deleteIds = songIds.filter { id ->
            songs.none { it.songId == id }
        }

        if (deleteIds.isEmpty()) return

        favoriteSongRepository.deleteByIds(deleteIds)
    }
}