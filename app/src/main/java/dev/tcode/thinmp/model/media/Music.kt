package dev.tcode.thinmp.model.media

abstract class Music {
    protected open lateinit var id: String
    protected open lateinit var name: String

    abstract fun getTrackList(): List<SongModel>

    fun getIdList(): List<String?> {
        return getTrackList().map(Music::id).toList();
    }

    companion object {
        const val ID = "Id"
        const val TYPE = "type"
        const val TYPE_TRACK = 1
        const val TYPE_ALBUM = 2
    }
}