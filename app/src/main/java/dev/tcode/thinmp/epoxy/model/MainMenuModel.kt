package dev.tcode.thinmp.epoxy.model

import com.airbnb.epoxy.EpoxyModelClass
import dev.tcode.thinmp.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelWithHolder
import androidx.annotation.LayoutRes
import dev.tcode.thinmp.epoxy.viewHolder.MainMenuViewHolder

@EpoxyModelClass
abstract class MainMenuModel: EpoxyModelWithHolder<MainMenuViewHolder>() {
    @EpoxyAttribute lateinit var primaryText: String

    @LayoutRes
    override fun getDefaultLayout(): Int {
        return R.layout.list_item_linear_main_menu
    }

    override fun bind(holder: MainMenuViewHolder) {
        holder.primaryText.text = primaryText
    }
}