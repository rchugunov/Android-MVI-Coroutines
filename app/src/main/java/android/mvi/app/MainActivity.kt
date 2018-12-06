package android.mvi.app

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.mvi.app.ui.main.MainView
import android.mvi.app.ui.main.MainViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), MainView {

    private val viewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }

    private val btnLoad by lazy { findViewById<Button>(R.id.btn_load) }
    private val tvLabel by lazy { findViewById<TextView>(R.id.tv_label) }
    private val progressBar by lazy { findViewById<ProgressBar>(R.id.progress) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        viewModel.viewState.observe(this, Observer { vs -> vs?.let { render(it) } })
        btnLoad.setOnClickListener { viewModel.startLoading() }
    }

    override fun render(viewState: MainView.MainViewState) {
        progressBar.visibility = if (viewState.progress) View.VISIBLE else View.GONE
        btnLoad.isEnabled = !viewState.progress
        tvLabel.text = viewState.result
    }
}
