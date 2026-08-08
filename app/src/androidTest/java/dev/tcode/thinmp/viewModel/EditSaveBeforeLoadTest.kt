package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.FavoriteSongRepository
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.SongRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The done button is composed from the first frame - EditTopAppBarView only animates the bar's
 * background, not the row the button sits in - while the list behind it arrives asynchronously.
 * save() applies uiState as it stands, and its initial state is empty, so a tap that beat the load
 * wrote that emptiness back. Both reorder() and replaceAll() read "not in the list" as "delete",
 * which is how a list emptied by swiping is saved, so the whole table went.
 *
 * The button is disabled until the load lands, and save() refuses on its own as well, which is what
 * these tests drive: they call save() directly, the way a tap that slipped through would.
 *
 * Calling save() in the same main-thread block as the constructor gets close to a cold start but
 * does not pin the ordering: withContext returns without suspending at all when the work on the
 * other dispatcher finishes before the caller reaches its suspension check, so with the database
 * warm and the table tiny the whole load can land inside the constructor. The two tests below
 * assert the invariant that holds either way - opening the screen and pressing done immediately
 * never destroys anything - and savingWithoutLoadedStateReportsNothing pins the guard itself on a
 * load that cannot finish, rather than on winning a race.
 *
 * Unlike the repository tests these view models build their own repositories, so this exercises the
 * app's real database on the device rather than an in-memory one. Every test clears what it touches
 * before and after itself.
 */
@RunWith(AndroidJUnit4::class)
class EditSaveBeforeLoadTest {
    private val timeoutMs = 10_000L
    private val quietMs = 1_000L

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
    fun savingBeforeTheListLoadsKeepsThePlaylists() = runBlocking {
        val repository = PlaylistRepository()
        repository.create(SongId(songIdValue), "first")
        repository.create(SongId(songIdValue), "second")

        val viewModel = onMain { PlaylistsEditViewModel(application).also { it.save() } }
        awaitLoaded { viewModel.uiState.value.loaded }

        assertEquals(listOf("first", "second"), repository.findAll().map { it.name })
    }

    @Test
    fun savingBeforeTheListLoadsKeepsTheFavourites() = runBlocking {
        val repository = FavoriteSongRepository()
        repository.add(SongId(songIdValue))

        val viewModel = onMain { FavoriteSongsEditViewModel(application).also { it.save() } }
        awaitLoaded { viewModel.uiState.value.loaded }

        assertEquals(listOf(songIdValue), repository.findAll().map { it.id })
    }

    /**
     * The tap does nothing at all. Reporting saved would take the user back to the previous screen
     * off a tap whose effect they cannot see, which reads as "saved" when nothing was.
     *
     * A playlist id that resolves to nothing gives a load that runs to completion without ever
     * filling uiState - findById returns null and load() returns early - so `loaded` stays false
     * however long this waits. That is the state the guard exists for, held still.
     */
    @Test
    fun savingWithoutLoadedStateReportsNothing() = runBlocking {
        val viewModel = onMain { PlaylistDetailEditViewModel(application, SavedStateHandle(mapOf("id" to "gone"))) }

        delay(quietMs)
        assertFalse(viewModel.uiState.value.loaded)

        onMain { viewModel.save() }

        assertNull(withTimeoutOrNull(quietMs) { viewModel.saved.flow.first() })
    }

    /** Saving after the load is what it always was: the list on screen is written back. */
    @Test
    fun savingAfterTheListLoadsAppliesTheList() = runBlocking {
        val repository = PlaylistRepository()
        repository.create(SongId(songIdValue), "first")
        repository.create(SongId(songIdValue), "second")

        val viewModel = onMain { PlaylistsEditViewModel(application) }
        awaitLoaded { viewModel.uiState.value.loaded }
        onMain { viewModel.removePlaylist(0) }
        onMain { viewModel.save() }
        withTimeout(timeoutMs) { viewModel.saved.flow.first() }

        assertEquals(listOf("second"), repository.findAll().map { it.name })
    }

    /** An empty list the user made by swiping is still saved: emptying everything must be possible. */
    @Test
    fun savingAnEmptiedListDeletesEverything() = runBlocking {
        val repository = PlaylistRepository()
        repository.create(SongId(songIdValue), "first")

        val viewModel = onMain { PlaylistsEditViewModel(application) }
        awaitLoaded { viewModel.uiState.value.loaded }
        onMain { viewModel.removePlaylist(0) }
        onMain { viewModel.save() }
        withTimeout(timeoutMs) { viewModel.saved.flow.first() }

        assertEquals(emptyList<String>(), repository.findAll().map { it.name })
    }

    private suspend fun awaitLoaded(loaded: () -> Boolean) {
        withTimeout(timeoutMs) {
            while (!loaded()) {
                kotlinx.coroutines.delay(20)
            }
        }
    }

    /**
     * Everything that touches a view model goes through here: viewModelScope dispatches with
     * Dispatchers.Main.immediate, so calling from anywhere else posts the work instead of running
     * it, and the constructor would return before load() had even started.
     */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        return result!!
    }

    private suspend fun clear() {
        val playlists = PlaylistRepository()

        playlists.findAll().forEach { playlists.delete(PlaylistId(it.id)) }

        val favourites = FavoriteSongRepository()

        favourites.deleteByIds(favourites.findAll())
    }
}
