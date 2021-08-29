package dev.tcode.thinmp.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.tcode.thinmp.R
import dev.tcode.thinmp.epoxy.controller.SongsController
import dev.tcode.thinmp.viewModelCreator.SongsViewModelCreator

class SongsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)

        initWithPermissionCheck()
    }

    override fun init() {
        val listView = findViewById<RecyclerView>(R.id.list)
        val viewModelCreator = SongsViewModelCreator()
        val viewModel = viewModelCreator.create(this)
        val controller = SongsController()
        val layout = LinearLayoutManager(this)

        controller.setData(viewModel)
        listView.adapter = controller.adapter
        listView.layoutManager = layout
    }
}