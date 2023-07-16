package dev.tcode.thinmp.constant

import android.content.Context
import androidx.compose.runtime.Immutable
import dev.tcode.thinmp.R
import dev.tcode.thinmp.config.ConfigStore

@Immutable
data class MainMenuItem(
    val id: Int,
    val key: String,
    val visibility: Boolean
)

enum class MainMenuEnum(val key: String, val id: Int) {
    ARTISTS(NavConstant.ARTISTS, R.string.artists),
    ALBUMS(NavConstant.ALBUMS, R.string.albums),
    SONGS(NavConstant.SONGS, R.string.songs),
    FAVORITE_ARTISTS(NavConstant.FAVORITE_ARTISTS, R.string.favorite_artists),
    FAVORITE_SONGS(NavConstant.FAVORITE_SONGS, R.string.favorite_songs),
    PLAYLISTS(NavConstant.PLAYLISTS, R.string.playlists);

    companion object {
        fun getList(context: Context): List<MainMenuItem> {
            val config = ConfigStore(context)

            return MainMenuEnum.values().toList().map {
                MainMenuItem(it.id, it.key, config.getMainMenuVisibility(it.key))
            }
        }
    }
}