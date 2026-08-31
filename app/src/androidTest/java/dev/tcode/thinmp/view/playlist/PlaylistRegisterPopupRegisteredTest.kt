package dev.tcode.thinmp.view.playlist

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.PlaylistRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A playlist the song is already in cannot be added to, so the popup marks it as registered and
 * stops the row responding. Without that the row looked like every other one and a tap closed the
 * popup having written nothing.
 *
 * Closing is what the tap is asserted on: the row's click handler adds the song and then invokes
 * the popup's callback, and the write itself is a no-op either way - the primary key already
 * ignores the duplicate - so the callback is the only thing that says whether the row responded.
 *
 * The popup builds its own PlaylistsViewModel, so this runs against the app's real database rather
 * than an in-memory one, and clears the playlists around itself. The song id is never resolved
 * against MediaStore here - only the playlist rows are read - so no audio file is needed.
 */
@RunWith(AndroidJUnit4::class)
class PlaylistRegisterPopupRegisteredTest {
    private val timeoutMs = 10_000L
    private val registeredName = "registered"
    private val otherName = "other"
    private val songId = SongId("1")

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var registeredText: String

    @Before
    fun setUp() = runBlocking {
        application = ApplicationProvider.getApplicationContext()
        registeredText = application.getString(R.string.already_added)
        clear()
        // create() seeds the playlist with the song, so the first playlist is one songId is in and
        // the second is one it is not.
        val repository = PlaylistRepository()
        repository.create(songId, registeredName)
        repository.create(SongId("2"), otherName)
    }

    @After
    fun tearDown() = runBlocking {
        clear()
    }

    @Test
    fun marksThePlaylistTheSongIsAlreadyIn() {
        showPopup()

        awaitRegisteredMark()

        composeTestRule.onNodeWithText(registeredText).assertExists()
    }

    @Test
    fun doesNotRespondToATapOnThePlaylistTheSongIsAlreadyIn() {
        var closed = false

        showPopup { closed = true }

        awaitRegisteredMark()

        composeTestRule.onNodeWithText(registeredName).performClick()
        composeTestRule.waitForIdle()

        assertFalse(closed)
    }

    @Test
    fun stillAddsToAPlaylistTheSongIsNotIn() {
        var closed = false

        showPopup { closed = true }

        composeTestRule.onNodeWithText(otherName).performClick()
        composeTestRule.waitForIdle()

        assertTrue(closed)
    }

    private fun showPopup(callback: () -> Unit = {}) {
        composeTestRule.setContent {
            PlaylistRegisterPopupView(songId, callback = callback)
        }

        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(otherName).fetchSemanticsNodes().isNotEmpty() }
    }

    /**
     * The playlists the song is already in are loaded for the song the popup was opened for, which
     * is a second load after the one the view model runs on its own - the list is on screen before
     * the marks are. A tap has to land on a row that has been marked.
     */
    private fun awaitRegisteredMark() {
        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(registeredText).fetchSemanticsNodes().isNotEmpty() }
    }

    private suspend fun clear() {
        val repository = PlaylistRepository()

        repository.findAll().forEach { repository.delete(PlaylistId(it.id)) }
    }
}
