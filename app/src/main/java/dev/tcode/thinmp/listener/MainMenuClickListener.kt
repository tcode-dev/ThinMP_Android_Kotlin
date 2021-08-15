package dev.tcode.thinmp.listener

import android.content.Context
import android.content.Intent
import android.view.View

class MainMenuClickListener(private val link: Class<*>) : View.OnClickListener {
    override fun onClick(view: View) {
        val context: Context = view.context
        val intent = Intent(context, link)

        context.startActivity(intent)
    }
}