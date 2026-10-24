package dev.tcode.thinmp.viewModel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * 「保存が終わったので画面を閉じてよい」のような、view modelから画面への1回きりの通知。
 * StateFlowと違って再送されないので、recompositionで2回処理されることはない。
 */
class OneShotEvent<T> {
    private val channel = Channel<T>(Channel.CONFLATED)
    val flow: Flow<T> = channel.receiveAsFlow()

    suspend fun emit(value: T) {
        channel.send(value)
    }
}
