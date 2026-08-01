package dev.tcode.thinmp.viewModel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A one-off signal from a view model to its screen, such as "the save finished, you may navigate
 * away". Unlike StateFlow it is not replayed, so a recomposition cannot act on it twice.
 */
class OneShotEvent<T> {
    private val channel = Channel<T>(Channel.CONFLATED)
    val flow: Flow<T> = channel.receiveAsFlow()

    suspend fun emit(value: T) {
        channel.send(value)
    }
}
