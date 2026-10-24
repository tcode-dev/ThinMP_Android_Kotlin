package dev.tcode.thinmp.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaylistRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: PlaylistRepository

    @Before
    fun setUp() {
        db = createTestDatabase()
        repository = PlaylistRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createNumbersPlaylistsFromOne() = runTest {
        repository.create(SongId("1"), "first")
        repository.create(SongId("2"), "second")

        val playlists = repository.findAll()

        assertEquals(listOf("first", "second"), playlists.map { it.name })
        assertEquals(listOf(1, 2), playlists.map { it.order })
    }

    @Test
    fun createAddsTheSeedSong() = runTest {
        repository.create(SongId("1"), "first")

        val playlist = repository.findAll().first()

        assertEquals(listOf("1"), repository.findSongsByPlaylistId(PlaylistId(playlist.id)).map { it.songId })
    }

    @Test
    fun updatePlaylistReplacesNameAndSongs() = runTest {
        repository.create(SongId("1"), "first")
        val id = PlaylistId(repository.findAll().first().id)

        repository.updatePlaylist(id, "renamed", listOf(SongId("2"), SongId("3")))

        assertEquals("renamed", repository.findById(id)?.name)
        assertEquals(listOf("2", "3"), repository.findSongsByPlaylistId(id).map { it.songId })
    }

    /**
     * 同じ曲を同じプレイリストに2回は登録できない。
     * 主キーが2行目を拒否し、1行目はそのまま残る。
     * add()が結果を返すので、ポップアップはそれを表示できる。
     */
    @Test
    fun addIgnoresASongAlreadyInThePlaylist() = runTest {
        repository.create(SongId("1"), "first")
        val id = PlaylistId(repository.findAll().first().id)

        assertTrue(repository.add(id, SongId("2")))
        assertFalse(repository.add(id, SongId("1")))

        assertEquals(listOf("1", "2"), repository.findSongsByPlaylistId(id).map { it.songId })
    }

    /** 登録ポップアップがグレーアウトする、その曲を含むプレイリストだけを返すこと。 */
    @Test
    fun findPlaylistIdsBySongIdReturnsThePlaylistsHoldingTheSong() = runTest {
        repository.create(SongId("1"), "first")
        repository.create(SongId("2"), "second")
        repository.create(SongId("1"), "third")
        val ids = repository.findAll().map { PlaylistId(it.id) }

        assertEquals(listOf(ids[0], ids[2]).sortedBy { it.id }, repository.findPlaylistIdsBySongId(SongId("1")).sortedBy { it.id })
        assertEquals(listOf(ids[1]), repository.findPlaylistIdsBySongId(SongId("2")))
        assertTrue(repository.findPlaylistIdsBySongId(SongId("3")).isEmpty())
    }

    @Test
    fun updatePlaylistStoresARepeatedIdOnce() = runTest {
        repository.create(SongId("1"), "first")
        val id = PlaylistId(repository.findAll().first().id)

        repository.updatePlaylist(id, "renamed", listOf(SongId("2"), SongId("3"), SongId("2")))

        assertEquals(listOf("2", "3"), repository.findSongsByPlaylistId(id).map { it.songId })
    }

    /**
     * プレイリストの曲は追加した順で返り、idの順ではない。
     * 文字列としてソートすると"1"、"10"、"5"になる。
     */
    @Test
    fun songsComeBackInTheOrderTheyWereAdded() = runTest {
        repository.create(SongId("5"), "first")
        val id = PlaylistId(repository.findAll().first().id)

        repository.add(id, SongId("10"))
        repository.add(id, SongId("1"))

        assertEquals(listOf("5", "10", "1"), repository.findSongsByPlaylistId(id).map { it.songId })
    }

    @Test
    fun deleteRemovesThePlaylistAndItsSongs() = runTest {
        repository.create(SongId("1"), "first")
        val id = PlaylistId(repository.findAll().first().id)

        repository.delete(id)

        assertNull(repository.findById(id))
        assertTrue(repository.findSongsByPlaylistId(id).isEmpty())
    }

    @Test
    fun reorderRenumbersSurvivorsAndDropsTheRest() = runTest {
        repository.create(SongId("1"), "first")
        repository.create(SongId("2"), "second")
        repository.create(SongId("3"), "third")
        val ids = repository.findAll().map { PlaylistId(it.id) }

        // 3番目と1番目をこの順で残し、2番目は消す。
        repository.reorder(listOf(ids[2], ids[0]))

        val playlists = repository.findAll()

        assertEquals(listOf("third", "first"), playlists.map { it.name })
        assertEquals(listOf(1, 2), playlists.map { it.order })
        assertTrue(repository.findSongsByPlaylistId(ids[1]).isEmpty())
    }

    /**
     * 呼び出し側は以前に読んだリストで並び替えるので、保存時にはなくなっているidが含まれうる。
     * それで例外を投げるとトランザクション全体が戻って何も保存されないので、読み飛ばす。
     */
    @Test
    fun reorderIgnoresAnIdThatIsNoLongerThere() = runTest {
        repository.create(SongId("1"), "first")
        repository.create(SongId("2"), "second")
        val ids = repository.findAll().map { PlaylistId(it.id) }

        repository.reorder(listOf(ids[1], PlaylistId("gone"), ids[0]))

        val playlists = repository.findAll()

        assertEquals(listOf("second", "first"), playlists.map { it.name })
        assertEquals(listOf(1, 2), playlists.map { it.order })
    }
}
