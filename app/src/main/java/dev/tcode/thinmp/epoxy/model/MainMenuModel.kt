package dev.tcode.thinmp.epoxy.model

import com.airbnb.epoxy.EpoxyModelClass
import dev.tcode.thinmp.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelWithHolder
import android.widget.TextView
import androidx.annotation.LayoutRes

@EpoxyModelClass
abstract class MainMenuModel: EpoxyModelWithHolder<MainMenuHolder>() {
    @EpoxyAttribute lateinit var primaryText: String

    @LayoutRes
    override fun getDefaultLayout(): Int {
        return R.layout.list_item_linear_main_menu
    }

    override fun bind(holder: MainMenuHolder) {
        holder.primaryText.text = primaryText
    }
}

class MainMenuHolder: KotlinEpoxyHolder() {
    val primaryText by bind<TextView>(R.id.primaryText)
}