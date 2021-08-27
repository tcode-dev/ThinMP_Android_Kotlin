package dev.tcode.thinmp.model

class SongModel(
    override var id: String?,
    override var name: String?,
    val artistId: String?,
    val artistName: String?,
    val albumId: String?,
    val albumName: String?,
    val duration: Int?
): Music() {
    override fun getTrackList(): List<SongModel> {
        val trackList: MutableList<SongModel> = ArrayList()

        trackList.add(this)

        return trackList
    }
}
