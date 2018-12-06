package android.mvi.app.ui.main

import com.mvi.view.MVIView

interface MainView : MVIView<MainView.MainViewState> {
    data class MainViewState(
        val progress: Boolean = false,
        val result: String = ""
    ) : MVIView.ViewState
}