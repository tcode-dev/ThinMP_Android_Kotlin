package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.FavoriteSongRepository
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.SongRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The done button is composed and clickable from the first frame - EditTopAppBarView only animates
 * the bar's background, not the row the button sits in - while the list behind it arrives
 * asynchronously. save() applies uiState as it stands, and its initial state is empty, so a tap
 * that beat the load wrote that emptiness back. Both reorder() and replaceAll() read "not in the
 * list" as "delete", which is how a list emptied by swiping is saved, so the whole table went.
 *
 * Calling save() in the same main-thread block as the constructor is what makes this deterministic
 * rather than a race: viewModelScope dispatches with Dispatchers.Main.immediate, so load() runs
 * inline until its first database call suspends, and save() then runs while uiState is still empty.
 *
 * Unlike the repository tests these view models build their own repositories, so this exercises the
 * app's real database on the device rather than an in-memory one. Both tests clear what they touch
 * before and after themselves.
 */
@RunWith(AndroidJUnit4::class)
class EditSaveBeforeLoadTest {
    private val timeoutMs = 10_000L

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
        awaitSaved(viewModel.saved)

        assertEquals(listOf("first", "second"), repository.findAll().map { it.name })
    }

    @Test
    fun savingBeforeTheListLoadsKeepsTheFavourites() = runBlocking {
        val repository = FavoriteSongRepository()
        repository.add(SongId(songIdValue))

        val viewModel = onMain { FavoriteSongsEditViewModel(application).also { it.save() } }
        awaitSaved(viewModel.saved)

        assertEquals(listOf(songIdValue), repository.findAll().map { it.id })
    }

    /** The done tap still has to take the user off the screen, loaded or not. */
    @Test
    fun savingBeforeTheListLoadsStillReportsSaved() = runBlocking {
        val viewModel = onMain { PlaylistsEditViewModel(application).also { it.save() } }

        awaitSaved(viewModel.saved)
    }

    /** Saving after the load is what it always was: the list on screen is written back. */
    @Test
    fun savingAfterTheListLoadsAppliesTheList() = runBlocking {
        val repository = PlaylistRepository()
        repository.create(SongId(songIdValue), "first")
        repository.create(SongId(songIdValue), "second")

        val viewModel = onMain { PlaylistsEditViewModel(application) }
        waitUntilLoaded(viewModel)
        onMain { viewModel.removePlaylist(0) }
        onMain { viewModel.save() }
        awaitSaved(viewModel.saved)

        assertEquals(listOf("second"), repository.findAll().map { it.name })
    }

    private suspend fun waitUntilLoaded(viewModel: PlaylistsEditViewModel) {
        withTimeout(timeoutMs) {
            viewModel.uiState.first { it.playlists.isNotEmpty() }
        }
    }

    private suspend fun awaitSaved(saved: OneShotEvent<Unit>) {
        withTimeout(timeoutMs) { saved.flow.first() }
    }

    /**
     * The constructor and the save have to happen on the main thread and in one block, or
     * Dispatchers.Main.immediate posts them instead of running them inline and the ordering the
     * test depends on is gone.
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
