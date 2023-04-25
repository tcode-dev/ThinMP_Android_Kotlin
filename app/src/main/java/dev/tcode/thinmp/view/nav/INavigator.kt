package dev.tcode.thinmp.view.nav

interface INavigator {
    fun back()
    fun mainEdit()
    fun artistDetail(id: String)
    fun albumDetail(id: String)
    fun favoriteArtistsEdit()
    fun favoriteSongsEdit()
    fun playlistDetail(id: String)
    fun player()
}