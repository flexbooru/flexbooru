package onlymash.flexbooru.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.entity.ArtistDan
import onlymash.flexbooru.entity.ArtistMoe
import onlymash.flexbooru.ui.viewholder.ArtistViewHolder

class ArtistAdapter(private val listener: ArtistViewHolder.ItemListener,
                    retryCallback: () -> Unit) : BaseStatePagedListAdapter<Any, RecyclerView.ViewHolder>(ARTIST_COMPARATOR, retryCallback) {

    companion object {
        val ARTIST_COMPARATOR = object : DiffUtil.ItemCallback<Any>() {
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is ArtistDan && newItem is ArtistDan -> oldItem.id == newItem.id
                    oldItem is ArtistMoe && newItem is ArtistMoe -> oldItem.id == newItem.id
                    else -> false
                }
            }
        }
    }

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ArtistViewHolder.create(parent)

    override fun onBindDataViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ArtistViewHolder) {
            holder.apply {
                setItemListener(listener)
                bind(getItem(position))
            }
        }
    }
}