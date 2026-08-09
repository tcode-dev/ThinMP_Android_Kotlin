package dev.tcode.thinmp.view.layout

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.view.nav.INavigator
import dev.tcode.thinmp.view.nav.LocalNavigator
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CommonLayoutView holds the song id the playlist popup is opened for. It used to hold it in a
 * plain local, which every recomposition of CommonLayoutView reset to SongId(""), and the popup
 * then wrote a playlist_songs row that resolves to no song in MediaStore - the song looks added
 * and is gone by the next load.
 *
 * isVisibleMiniPlayer is the recomposition: it comes from the screen's uiState and flips while the
 * popup is open, for instance when playback starts. The two tests are the same flow with and
 * without that flip, so a failure says whether adding is broken outright or only after a
 * recomposition.
 *
 * The popup builds its own PlaylistsViewModel, so this runs against the app's real database rather
 * than an in-memory one, and clears the playlists around itself. A real MediaStore song id is used
 * so the row written is one the app would keep; skipped when the device has no audio
 * (tools/push-test-audio.sh).
 */
@RunWith(AndroidJUnit4::class)
class PlaylistRegisterPopupSongIdTest {
    private val timeoutMs = 10_000L
    private val pollMs = 50L
    private val playlistName = "popup"

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private var songIdValue: String = ""

    @Before
    fun setUp() = runBlocking {
        application = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(application.packageName, "android.permission.READ_MEDIA_AUDIO")
        clear()

        val songs = SongRepository(application).findAll()
        assumeTrue("needs at least one audio file in MediaStore", songs.isNotEmpty())
        songIdValue = songs.first().songId.id
    }

    @After
    fun tearDown() = runBlocking {
        clear()
    }

    @Test
    fun addsTheSongThePopupWasOpenedFor() {
        assertEquals(listOf(songIdValue), addFromPopup(recompose = false))
    }

    @Test
    fun addsTheSongThePopupWasOpenedForAfterTheLayoutRecomposes() {
        assertEquals(listOf(songIdValue), addFromPopup(recompose = true))
    }

    /**
     * Opens the popup for songId on an empty playlist, optionally flips isVisibleMiniPlayer while
     * it is open, taps the playlist and returns what landed in playlist_songs.
     */
    private fun addFromPopup(recompose: Boolean): List<String> {
        val songId = SongId(songIdValue)
        val playlistId = runBlocking {
            val repository = PlaylistRepository()
            repository.create(songId, playlistName)
            val playlist = repository.findAll().first { it.name == playlistName }
            // create() seeds the playlist with the song, which would hide an empty id written on
            // top of it. The row is removed so the popup's write is the only one left.
            repository.delete(PlaylistId(playlist.id), listOf(songId))
            PlaylistId(playlist.id)
        }
        val isVisibleMiniPlayer = mutableStateOf(false)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigator provides NoNavigator) {
                CommonLayoutView(isVisibleMiniPlayer.value) { showPlaylistRegisterPopup ->
                    Text("open", modifier = Modifier.clickable { showPlaylistRegisterPopup(songId) })
                }
            }
        }

        composeTestRule.onNodeWithText("open").performClick()
        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(playlistName).fetchSemanticsNodes().isNotEmpty() }

        if (recompose) {
            composeTestRule.runOnUiThread { isVisibleMiniPlayer.value = true }
            composeTestRule.waitForIdle()
        }

        composeTestRule.onNodeWithText(playlistName).performClick()

        return awaitSongs(playlistId)
    }

    /** addSong() runs in viewModelScope, so the row arrives after the tap returns. */
    private fun awaitSongs(playlistId: PlaylistId): List<String> {
        val deadline = System.currentTimeMillis() + timeoutMs

        while (System.currentTimeMillis() < deadline) {
            val songs = runBlocking { PlaylistRepository().findSongsByPlaylistId(playlistId) }

            if (songs.isNotEmpty()) {
                return songs.map { it.songId }
            }

            Thread.sleep(pollMs)
        }

        return emptyList()
    }

    private suspend fun clear() {
        val repository = PlaylistRepository()

        repository.findAll().forEach { repository.delete(PlaylistId(it.id)) }
    }

    private object NoNavigator : INavigator {
        override fun back() {}
        override fun mainEdit() {}
        override fun artistDetail(id: String) {}
        override fun albumDetail(id: String) {}
        override fun favoriteArtistsEdit() {}
        override fun favoriteSongsEdit() {}
        override fun playlistsEdit() {}
        override fun playlistDetail(id: String) {}
        override fun playlistDetailEdit(id: String) {}
        override fun player() {}
    }
}
