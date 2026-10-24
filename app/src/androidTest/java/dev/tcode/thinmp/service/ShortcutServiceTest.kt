package dev.tcode.thinmp.service

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.constant.ItemType
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.AlbumRepository
import dev.tcode.thinmp.repository.AppDatabase
import dev.tcode.thinmp.repository.ArtistRepository
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.ShortcutRepository
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.repository.createTestDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TITLE_PREFIX = "thinmp_test_compilation_"
private const val ALBUM = "thinmp_test_compilation"

/**
 * アーティストのショートカットはそのアーティストの最初のアルバムから画像を取るが、
 * アルバムがないこともある。
 * アルバムのテーブルはアルバムごとにartist_idを1つしか持たないので、
 * コンピレーションの参加アーティストのうち1人以外はアルバムを持たない。
 * それでもアーティスト一覧には出るのでショートカットにできる。
 * findAll()はメイン画面の読み込みで呼ばれるので、ここで例外が出ると画面全体が落ちる。
 *
 * リポジトリを差し替えず、MediaStoreに実際にその状態を作って確かめる。
 * 2人のどちらがアルバムのアーティストになるかはMediaStoreが決めるので、決め打ちせずに問い合わせる。
 */
@RunWith(AndroidJUnit4::class)
class ShortcutServiceTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var shortcuts: ShortcutRepository
    private var withoutAlbum: String = ""
    private var withAlbum: String = ""

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, "android.permission.READ_MEDIA_AUDIO")
        db = createTestDatabase()
        shortcuts = ShortcutRepository(db)

        deleteTracks()
        insertTrack("${TITLE_PREFIX}a", "thinmp_test_artist_a")
        insertTrack("${TITLE_PREFIX}b", "thinmp_test_artist_b")

        val artistIds = SongRepository(context).findAll().filter { it.name.startsWith(TITLE_PREFIX) }.map { it.artistId.id }.distinct()
        assumeTrue("MediaStore did not keep the two tracks apart", artistIds.size == 2)

        val albums = AlbumRepository(context)
        withoutAlbum = artistIds.firstOrNull { albums.findByArtistId(it).isEmpty() } ?: ""
        withAlbum = artistIds.firstOrNull { albums.findByArtistId(it).isNotEmpty() } ?: ""
        assumeTrue("MediaStore gave both artists an album of their own", withoutAlbum.isNotEmpty() && withAlbum.isNotEmpty())
    }

    @After
    fun tearDown() {
        deleteTracks()
        db.close()
    }

    @Test
    fun anArtistWithNoAlbumFallsBackToAnEmptyImage() = runBlocking {
        shortcuts.add(ArtistId(withoutAlbum))

        val models = ShortcutService(context, context.resources, shortcuts, ArtistRepository(context), AlbumRepository(context), SongRepository(context), PlaylistRepository(db)).findAll()

        assertEquals(1, models.size)
        assertEquals(ItemType.ARTIST, models.first().type)
        assertEquals(Uri.EMPTY, models.first().imageUri)
    }

    /** アルバムがなくてもショートカットは有効なので、不要な行として削除されないこと。 */
    @Test
    fun anArtistWithNoAlbumIsKept() = runBlocking {
        shortcuts.add(ArtistId(withoutAlbum))

        ShortcutService(context, context.resources, shortcuts, ArtistRepository(context), AlbumRepository(context), SongRepository(context), PlaylistRepository(db)).findAll()

        assertEquals(1, shortcuts.findAll().size)
    }

    /** アルバムがあるアーティストは、そのアルバムの画像を使うこと。 */
    @Test
    fun anArtistWithAnAlbumKeepsItsImage() = runBlocking {
        shortcuts.add(ArtistId(withAlbum))

        val models = ShortcutService(context, context.resources, shortcuts, ArtistRepository(context), AlbumRepository(context), SongRepository(context), PlaylistRepository(db)).findAll()

        assertEquals(1, models.size)
        assertNotEquals(Uri.EMPTY, models.first().imageUri)
    }

    /**
     * 2曲を同じアルバムに入れるので、アルバムのアーティストは2人のうち1人になり、
     * もう1人にはアルバムがなくなる。
     * 再生はしないうえ、WAVにはアーティストのタグも入れられないので、ファイルの中身は空にしている。
     */
    private fun insertTrack(title: String, artist: String) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$title.mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Audio.Media.TITLE, title)
            put(MediaStore.Audio.Media.ARTIST, artist)
            put(MediaStore.Audio.Media.ALBUM, ALBUM)
            put(MediaStore.Audio.Media.IS_MUSIC, 1)
        }

        context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
    }

    /** 前回の実行がtearDownの前に止まっていても影響しないよう、挿入の前にも呼ぶ。 */
    private fun deleteTracks() {
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media.TITLE} LIKE ?", arrayOf("$TITLE_PREFIX%")
        )
    }
}
