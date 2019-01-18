package com.mvi.vm

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.mvi.view.MVIView
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
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


    init {
        bindIntentChannels()
    }

    /**
     * Return array of channels which represent intents coming from component using this viewmodel
     */
    protected abstract suspend fun bindIntentsActual(): Array<ReceiveChannel<StateChanges>>

    /**
     * Reduce logic for the viewState {@code previousState}
     * @param previousState the most recent viewState
     */
    protected abstract fun handleStateChanges(previousState: VS, stateChanges: StateChanges): VS

    @ExperimentalCoroutinesApi
    private fun bindIntentChannels() {
        launch(uiDispatcher) {
            mergeIntents(bindIntentsActual())
                    .foldChannel(initialViewState) { lastViewState, stateChanges ->
                        handleStateChanges(clearSingleEventsState(lastViewState), stateChanges)
                    }
                    .consumeEach { viewState ->
                        this@MviBaseViewModel.viewState.value = viewState
                    }
        }
    }

    /**
     * Clear such fields as exceptions, messages thrown in UI etc
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun clearSingleEventsState(vs : VS) : VS = vs

    @ExperimentalCoroutinesApi
    private fun mergeIntents(publishers: Array<ReceiveChannel<StateChanges>>) = produce {
        publishers.forEach { publisher ->
            launch(uiDispatcher) {
                publisher.consumeEach {
                    send(it)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private suspend inline fun <E, R> ReceiveChannel<E>.foldChannel(initial: R, crossinline operation: (acc: R, E) -> R): ReceiveChannel<R> =
            produce {
                var accumulator = initial
                consumeEach {
                    accumulator = operation(accumulator, it)
                    send(accumulator)
                }
            }

}