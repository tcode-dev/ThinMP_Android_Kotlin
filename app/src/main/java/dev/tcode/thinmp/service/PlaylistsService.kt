package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.realm.PlaylistRealmModel
import dev.tcode.thinmp.repository.realm.PlaylistRepository

class PlaylistsService(val context: Context) {
    fun findAll(): List<PlaylistRealmModel> {
        val repository = PlaylistRepository()

        return repository.findAll()
    }
}