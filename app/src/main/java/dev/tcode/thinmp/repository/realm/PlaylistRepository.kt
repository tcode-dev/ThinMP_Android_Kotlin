package dev.tcode.thinmp.repository.realm

import android.text.TextUtils
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.realm.PlaylistRealmModel
import dev.tcode.thinmp.model.realm.PlaylistSongRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query

class PlaylistRepository {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(PlaylistRealmModel::class, PlaylistSongRealmModel::class))

        realm = Realm.open(config)
    }

    fun create(songId: SongId, name: String) {
        val playlist = PlaylistRealmModel()
        val song = PlaylistSongRealmModel()

        song.playlistId = playlist.id
        song.songId = songId.id

        playlist.name = name
        playlist.order = increment()
        playlist.songs.add(song)

        realm.writeBlocking {
            copyToRealm(playlist)
        }
    }

    fun add(playlistId: PlaylistId, songId: SongId) {
        realm.query<PlaylistRealmModel>("id == $0", playlistId.id).first().find()?.also { playlist ->
            realm.writeBlocking {
                val song = PlaylistSongRealmModel()

                song.playlistId = playlistId.id
                song.songId = songId.id
                findLatest(playlist)?.songs?.add(song)
            }
        }
    }

    fun delete(playlistId: PlaylistId, songIds: List<SongId>) {
        realm.writeBlocking {
            val playlist = findById(playlistId)

            playlist?.songs?.filter { song ->
                songIds.any { it.id == song.songId }
            }?.forEach { song ->
                findLatest(song)?.let { delete(it) }
            }
        }
    }

    fun updatePlaylist(playlistId: PlaylistId, name: String, songIds: List<SongId>) {
        findById(playlistId)?.also { playlist ->
            realm.writeBlocking {
                val songs = songIds.map { songId ->
                    val newSong = PlaylistSongRealmModel()

                    newSong.playlistId = playlistId.id
                    newSong.songId = songId.id
                    newSong
                }

                findLatest(playlist)?.let {
                    it.name = name
                    it.songs.clear()
                    it.songs.addAll(songs)
                }
            }
        }
    }

    fun updatePlaylists(playlistIds: List<PlaylistId>) {
        findAll().also { playlists ->
            val group = playlists.groupBy { playlist -> playlistIds.any { it.id == playlist.id } }
            val deletePlaylists = if (group[false] != null) group[false]!! else emptyList()
            val sortedPlaylists = if (group[true] != null) {
                playlistIds.mapNotNull { playlistId -> group[true]?.first { it.id == playlistId.id } }
            } else {
                emptyList()
            }

            realm.writeBlocking {
                deletePlaylists.forEach { playlist ->
                    findLatest(playlist)?.let { delete(it) }
                }

                for ((index, playlist) in sortedPlaylists.withIndex()) {
                    findLatest(playlist)?.let {
                        it.order = index + 1
                    }
                }
            }
        }
    }

    fun findAll(): List<PlaylistRealmModel> {
        return realm.query<PlaylistRealmModel>().find().sortedBy(PlaylistRealmModel::order)
    }

    fun findById(playlistId: PlaylistId): PlaylistRealmModel? {
        return realm.query<PlaylistRealmModel>("id == $0", playlistId.id).first().find()
    }

    fun findByIds(playlistIds: List<PlaylistId>): List<PlaylistRealmModel> {
        val values = TextUtils.join(", ", playlistIds.map { "'${it.id}'" })

        return realm.query<PlaylistRealmModel>("id in { $values }").find()
    }

    fun delete(playlistId: PlaylistId) {
        realm.writeBlocking {
            val playlist = realm.query<PlaylistRealmModel>("id == $0", playlistId.id).find().first()

            findLatest(playlist)?.let { delete(it) }
        }
    }

    private fun increment(): Int {
        val result = realm.query<PlaylistRealmModel>().find()

        if (result.isEmpty()) return 1

        return result.maxOf(PlaylistRealmModel::order) + 1
    }
}