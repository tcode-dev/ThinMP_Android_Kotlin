package dev.tcode.thinmp.epoxy.model

import android.view.View
import com.airbnb.epoxy.EpoxyModelClass
import dev.tcode.thinmp.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelWithHolder
import androidx.annotation.LayoutRes
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import dev.tcode.thinmp.epoxy.viewHolder.SongViewHolder

@EpoxyModelClass
abstract class SongModel: EpoxyModelWithHolder<SongViewHolder>() {
    @EpoxyAttribute
    lateinit var primaryText: String

    @EpoxyAttribute(DoNotHash)
    lateinit var clickListener: View.OnClickListener

    @LayoutRes
    override fun getDefaultLayout(): Int {
        return R.layout.list_item_linear_song
    }

    override fun bind(holder: SongViewHolder) {
        holder.view.setOnClickListener(clickListener)
        holder.primaryText.text = primaryText
    }
}