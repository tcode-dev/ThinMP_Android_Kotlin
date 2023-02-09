package dev.tcode.thinmp.constant

import dev.tcode.thinmp.R

enum class MainMenuEnum(val key: String, val id: Int) {
    ARTISTS("artists", R.string.artists),
    ALBUMS("albums", R.string.albums),
    SONGS("songs", R.string.songs),
    FAVORITE_ARTISTS("favoriteArtists", R.string.favorite_artists),
    FAVORITE_SONGS("favoriteSongs", R.string.favorite_songs);
}