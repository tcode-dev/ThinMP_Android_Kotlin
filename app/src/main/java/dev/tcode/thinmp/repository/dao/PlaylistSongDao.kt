package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.tcode.thinmp.constant.SqliteConstant
import dev.tcode.thinmp.model.room.PlaylistSongEntity

@Dao
interface PlaylistSongDao {
    /**
     * プレイリストの曲順は挿入順なので、rowidで並べる。
     * 指定しないと、SQLiteが選んだインデックスの順で返ってくる。
     */
    @Query("SELECT * FROM playlist_songs WHERE playlist_id = :playlistId ORDER BY rowid")
    suspend fun findByPlaylistId(playlistId: String): List<PlaylistSongEntity>

    /**
     * その曲が登録されているプレイリスト。
     * song_idだけで絞り込むので、playlist_songsはsong_idにインデックスを持つ。
     */
    @Query("SELECT playlist_id FROM playlist_songs WHERE song_id = :songId")
    suspend fun findPlaylistIdsBySongId(songId: String): List<String>

    /**
     * 登録済みの曲は主キーで重複になるが、IGNOREなので例外にならず、何もしない。
     * 戻り値は新しいrowidで、すでにあった場合は-1。
     * 例外が出ないので、書き込まなかったことは戻り値でしか分からない。
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: PlaylistSongEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<PlaylistSongEntity>)

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId AND song_id IN (:songIds)")
    suspend fun deleteByPlaylistIdAndSongIdsChunk(playlistId: String, songIds: List<String>)

    /**
     * FavoriteSongDao.deleteBySongIdsを参照。
     * playlistIdもホストパラメーターを1つ使うので、チャンクを1つ小さくする。
     */
    @Transaction
    suspend fun deleteByPlaylistIdAndSongIds(playlistId: String, songIds: List<String>) {
        songIds.chunked(SqliteConstant.MAX_VARIABLES - 1).forEach {
            deleteByPlaylistIdAndSongIdsChunk(playlistId, it)
        }
    }

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: String)
}
