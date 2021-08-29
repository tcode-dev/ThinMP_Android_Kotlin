package dev.tcode.thinmp.viewModelCreator

import android.content.Context
import dev.tcode.thinmp.repository.media.SongRepository
import dev.tcode.thinmp.viewModel.SongsViewModel

class SongsViewModelCreator {
    fun create(context: Context): SongsViewModel {
        val repository = SongRepository(context)

        return SongsViewModel(repository.findAll())
    }
}