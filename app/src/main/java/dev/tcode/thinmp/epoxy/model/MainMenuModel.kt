package dev.tcode.thinmp.epoxy.model

import android.view.View
import com.airbnb.epoxy.EpoxyModelClass
import dev.tcode.thinmp.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelWithHolder
import androidx.annotation.LayoutRes
import dev.tcode.thinmp.epoxy.viewHolder.MainMenuViewHolder
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash

@EpoxyModelClass
abstract class MainMenuModel: EpoxyModelWithHolder<MainMenuViewHolder>() {
    @EpoxyAttribute
    lateinit var primaryText: String

    @EpoxyAttribute(DoNotHash)
    lateinit var clickListener: View.OnClickListener

    @LayoutRes
    override fun getDefaultLayout(): Int {
        return R.layout.list_item_linear_main_menu
    }

    override fun bind(holder: MainMenuViewHolder) {
        holder.view.setOnClickListener(clickListener)
        holder.primaryText.text = primaryText
    }
}