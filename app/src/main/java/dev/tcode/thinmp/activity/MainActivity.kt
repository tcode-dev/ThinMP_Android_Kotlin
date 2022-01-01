package dev.tcode.thinmp.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import dev.tcode.thinmp.activity.ui.theme.ThinMPTheme
import dev.tcode.thinmp.view.nav.Nav

class MainActivity : AppCompatActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThinMPTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Nav()
                }
            }
        }
    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        init()
//    }
//
//    private fun init() {
//        val viewModel = MainViewModel()
//        val listView = findViewById<RecyclerView>(R.id.list)
//        val controller = MainController()
//        val layout = GridLayoutManager(this, viewModel.layoutSpanSize)
//
//        viewModel.load()
//        controller.setData(viewModel)
//        listView.adapter = controller.adapter
//        controller.spanCount = viewModel.layoutSpanSize
//        layout.spanSizeLookup = controller.spanSizeLookup
//        listView.layoutManager = layout
//    }
}