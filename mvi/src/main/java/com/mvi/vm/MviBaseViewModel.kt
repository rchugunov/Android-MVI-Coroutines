package com.mvi.vm

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.mvi.view.MVIView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Created by Kusraev Soslan on 10/30/18.
 */
abstract class MviBaseViewModel<VS : MVIView.ViewState, StateChanges>(
        protected val bgDispatcher: CoroutineDispatcher,
        protected val uiDispatcher: CoroutineDispatcher
): ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    val viewState: MutableLiveData<VS> = MutableLiveData()

    protected abstract val initialViewState: VS

    protected abstract suspend fun bindIntentsActual(): Array<ReceiveChannel<StateChanges>>
    protected abstract fun handleStateChanges(previousState: VS, stateChanges: StateChanges): VS

    init {
        bindIntentChannels()
    }

    private fun bindIntentChannels() {
        launch(uiDispatcher) {
            mergeIntents(bindIntentsActual())
                    .foldChannel(initialViewState) { lastViewState, stateChanges ->
                        handleStateChanges(lastViewState, stateChanges)
                    }
                    .consumeEach { viewState ->
                        this@MviBaseViewModel.viewState.value = viewState
                    }
        }
    }


    private fun mergeIntents(publishers: Array<ReceiveChannel<StateChanges>>) = produce {
        publishers.forEach { publisher ->
            launch(uiDispatcher) {
                publisher.consumeEach {
                    send(it)
                }
            }
        }
    }



    private suspend inline fun <E, R> ReceiveChannel<E>.foldChannel(initial: R, crossinline operation: (acc: R, E) -> R): ReceiveChannel<R> =
            produce {
                var accumulator = initial
                consumeEach {
                    accumulator = operation(accumulator, it)
                    send(accumulator)
                }
            }

}