package dev.tcode.thinmp.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.tcode.thinmp.R
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.tcode.thinmp.epoxy.controller.MainController
import dev.tcode.thinmp.viewModelCreator.MainViewModelCreator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        val listView = findViewById<RecyclerView>(R.id.list)
        val viewModelCreator = MainViewModelCreator()
        val viewModel = viewModelCreator.create()
        val controller = MainController()
        val layout = GridLayoutManager(this, viewModel.layoutSpanSize)

        controller.setData(viewModel)
        listView.adapter = controller.adapter
        controller.spanCount = viewModel.layoutSpanSize
        layout.spanSizeLookup = controller.spanSizeLookup
        listView.layoutManager = layout
    }
}