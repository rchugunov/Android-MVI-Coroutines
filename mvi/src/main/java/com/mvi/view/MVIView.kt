package com.mvi.view

interface MVIView<ViewState : MVIView.ViewState> {

    fun render(viewState: ViewState)

    interface ViewState
}
