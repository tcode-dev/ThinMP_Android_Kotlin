package dev.tcode.thinmp.view.playlist

import android.app.Application
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.PlaylistRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The popup's done button had no check on the name field, so tapping it straight away created a
 * playlist named "", which every list then shows as a blank row.
 *
 * The empty name and the spaces-only name are both tried before a real one, and the assertion is
 * that the playlists table holds only the real one: a write from either of the first two taps
 * leaves a row behind that the last create cannot hide.
 *
 * The popup builds its own PlaylistsViewModel, so this runs against the app's real database rather
 * than an in-memory one, and clears the playlists around itself. The song id is never resolved
 * against MediaStore here - only the playlist rows are read - so no audio file is needed.
 */
@RunWith(AndroidJUnit4::class)
class PlaylistRegisterPopupNameTest {
    private val timeoutMs = 10_000L
    private val pollMs = 50L
    private val playlistName = "popup"
    private val songId = SongId("1")

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var doneText: String

    @Before
    fun setUp() = runBlocking {
        application = ApplicationProvider.getApplicationContext()
        doneText = application.getString(R.string.done)
        clear()
    }

    @After
    fun tearDown() = runBlocking {
        clear()
    }

    @Test
    fun doesNotCreateAPlaylistWithoutAName() {
        showPopup()

        composeTestRule.onNodeWithText(doneText).performClick()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("   ")
        composeTestRule.onNodeWithText(doneText).performClick()

        composeTestRule.onNode(hasSetTextAction()).performTextClearance()
        composeTestRule.onNode(hasSetTextAction()).performTextInput(playlistName)
        composeTestRule.onNodeWithText(doneText).performClick()

        assertEquals(listOf(playlistName), awaitPlaylists())
    }

    /** With no playlists in the table the popup opens on the name field rather than the list. */
    private fun showPopup() {
        composeTestRule.setContent {
            PlaylistRegisterPopupView(songId, callback = {})
        }

        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(doneText).fetchSemanticsNodes().isNotEmpty() }
    }

    /** create() runs in viewModelScope, so the row arrives after the tap returns. */
    private fun awaitPlaylists(): List<String> {
        val deadline = System.currentTimeMillis() + timeoutMs

        while (System.currentTimeMillis() < deadline) {
            val playlists = runBlocking { PlaylistRepository().findAll() }

            if (playlists.isNotEmpty()) {
                return playlists.map { it.name }
            }

            Thread.sleep(pollMs)
        }

        return emptyList()
    }

    private suspend fun clear() {
        val repository = PlaylistRepository()

        repository.findAll().forEach { repository.delete(PlaylistId(it.id)) }
    }
}
