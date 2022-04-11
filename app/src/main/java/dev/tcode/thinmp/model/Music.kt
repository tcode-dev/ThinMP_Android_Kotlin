package dev.tcode.thinmp.model

import android.net.Uri

abstract class Music {
    protected open lateinit var id: String
    protected open lateinit var name: String

    abstract fun getTrackList(): List<SongModel>
    abstract fun getUri(): Uri?

    val primaryText: String
        get() = name

    abstract val secondaryText: String?

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