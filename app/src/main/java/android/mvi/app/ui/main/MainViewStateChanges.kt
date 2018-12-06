package android.mvi.app.ui.main

sealed class MainViewStateChanges {
    class Progress(val isLoading : Boolean) : MainViewStateChanges()
    class Data(val value : String) : MainViewStateChanges()
    class Error(val error : Throwable) : MainViewStateChanges()
}