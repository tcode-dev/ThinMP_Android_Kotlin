package dev.tcode.thinmp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.tcode.thinmp.R
import dev.tcode.thinmp.epoxy.controller.MainController
import dev.tcode.thinmp.viewModel.MainViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        val viewModel = MainViewModel()
        val listView = findViewById<RecyclerView>(R.id.list)
        val controller = MainController()
        val layout = GridLayoutManager(this, viewModel.layoutSpanSize)

        viewModel.load()
        controller.setData(viewModel)
        listView.adapter = controller.adapter
        controller.spanCount = viewModel.layoutSpanSize
        layout.spanSizeLookup = controller.spanSizeLookup
        listView.layoutManager = layout
    }
}