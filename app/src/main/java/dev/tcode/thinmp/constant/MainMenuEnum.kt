package dev.tcode.thinmp.constant

import dev.tcode.thinmp.R

enum class MainMenuEnum(val key: String, val id: Int) {
    ARTISTS(NavConstant.ARTISTS, R.string.artists),
    ALBUMS(NavConstant.ALBUMS, R.string.albums),
    SONGS(NavConstant.SONGS, R.string.songs),
    FAVORITE_ARTISTS(NavConstant.FAVORITE_ARTISTS, R.string.favorite_artists),
    FAVORITE_SONGS(NavConstant.FAVORITE_SONGS, R.string.favorite_songs);
}