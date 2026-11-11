package dev.tcode.thinmp.viewModel

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.repository.SongRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ViewModelの読み込みは`viewModelScope.launch { mediaStoreFailure.catching { ... } }`の形をしている。
 * それと同じ形で、MediaStoreQueryExceptionだけを捕まえて失敗の状態にし、
 * ほかの例外はコルーチンの外へ伝えることを確かめる。
 *
 * ViewModelは自分でサービスとリポジトリを作るので、失敗するリポジトリを渡せない。
 * そのため、ViewModelと同じ形でリポジトリを呼ぶコルーチンで確かめる。
 */
@RunWith(AndroidJUnit4::class)
class MediaStoreFailureTest {
    private val missingUri: Uri = Uri.parse("content://dev.tcode.thinmp.test.missing")
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun aFailedQueryMarksTheFailure() = runBlocking {
        val failure = MediaStoreFailure()
        var uncaught: Throwable? = null
        var reachedTheEnd = false
        val scope = CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { _, e -> uncaught = e })

        scope.launch {
            failure.catching {
                SongRepository(context, missingUri).findAll()
                reachedTheEnd = true
            }
        }.join()

        assertTrue(failure.failed.value)
        assertFalse(reachedTheEnd)
        assertNull(uncaught)
    }

    @Test
    fun otherExceptionsAreNotCaught() = runBlocking {
        val failure = MediaStoreFailure()
        var uncaught: Throwable? = null
        val scope = CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { _, e -> uncaught = e })

        scope.launch {
            failure.catching {
                throw IllegalStateException("not a MediaStore failure")
            }
        }.join()

        assertFalse(failure.failed.value)
        assertEquals(IllegalStateException::class.java, uncaught?.javaClass)
    }

    @Test
    fun resetClearsTheFailure() {
        val failure = MediaStoreFailure()

        failure.report()
        failure.reset()

        assertFalse(failure.failed.value)
    }
}
