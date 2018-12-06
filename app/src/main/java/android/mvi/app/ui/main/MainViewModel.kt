package android.mvi.app.ui.main

import com.mvi.vm.MviBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
class MainViewModel : MviBaseViewModel<MainView.MainViewState, MainViewStateChanges>(Dispatchers.IO, Dispatchers.Main) {
    override val initialViewState = MainView.MainViewState()

    private val loadDataIntent = ConflatedBroadcastChannel<Unit>()

    override suspend fun bindIntentsActual(): Array<ReceiveChannel<MainViewStateChanges>> {
        return arrayOf(
            produce {
                loadDataIntent.consumeEach {
                    send(
                        MainViewStateChanges.Data(
                            "Operation started at ${
                            SimpleDateFormat("hh-mm-ss", Locale.getDefault()).format(Date())
                            }"
                        )
                    )
                    send(MainViewStateChanges.Progress(true))
                    delay(5000)
                    send(MainViewStateChanges.Progress(false))
                    send(
                        MainViewStateChanges.Data(
                            "Operation completed at ${
                            SimpleDateFormat("hh-mm-ss", Locale.getDefault()).format(Date())
                            }"
                        )
                    )
                }
            }
        )
    }

    override fun handleStateChanges(previousState: MainView.MainViewState, stateChanges: MainViewStateChanges) =
        when (stateChanges) {
            is MainViewStateChanges.Progress -> previousState.copy(progress = stateChanges.isLoading)
            is MainViewStateChanges.Data -> previousState.copy(result = stateChanges.value)
            is MainViewStateChanges.Error -> previousState
        }

    fun startLoading() {
        loadDataIntent.offer(Unit)
    }
}
