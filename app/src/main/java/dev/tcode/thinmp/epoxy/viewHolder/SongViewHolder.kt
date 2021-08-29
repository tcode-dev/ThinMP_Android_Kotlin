package dev.tcode.thinmp.epoxy.viewHolder

import android.widget.TextView
import dev.tcode.thinmp.R

class SongViewHolder: KotlinEpoxyHolder() {
    val primaryText by bind<TextView>(R.id.primaryText)
}