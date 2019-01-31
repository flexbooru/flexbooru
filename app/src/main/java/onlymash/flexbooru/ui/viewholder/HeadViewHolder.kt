package onlymash.flexbooru.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R

class HeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun create(parent: ViewGroup): HeadViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.head_item, parent, false)
            return HeadViewHolder(view)
        }
    }
}