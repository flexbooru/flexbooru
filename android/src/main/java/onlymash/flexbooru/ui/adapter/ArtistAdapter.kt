package onlymash.flexbooru.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import onlymash.flexbooru.R
import onlymash.flexbooru.data.model.common.Artist
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.extension.toggleArrow
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.util.ViewAnimation
import onlymash.flexbooru.widget.LinkTransformationMethod

class ArtistAdapter(retryCallback: () -> Unit) :
    BasePagedListAdapter<Artist, RecyclerView.ViewHolder>(ARTIST_COMPARATOR, retryCallback) {
    companion object {
        val ARTIST_COMPARATOR = object : DiffUtil.ItemCallback<Artist>() {
            override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean =
                oldItem == newItem
            override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean =
                oldItem.id == newItem.id
        }
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ArtistViewHolder.create(parent)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val artist = getItem(position)
        (holder as ArtistViewHolder).apply {
            bind(artist)
            itemView.setOnClickListener {
                artist?.let { a ->
                    SearchActivity.startSearch(itemView.context, a.name)
                }
            }
            itemView.setOnLongClickListener {
                itemView.context.copyText(artist?.name)
                true
            }
        }
    }

    class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun create(parent: ViewGroup): ArtistViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_artist, parent, false)
                return ArtistViewHolder(view)
            }
        }

        private val artistName: AppCompatTextView = itemView.findViewById(R.id.artist_name)
        private val artistId: AppCompatTextView = itemView.findViewById(R.id.artist_id)
        private val btExpand: ImageButton = itemView.findViewById(R.id.bt_expand)
        private val urlsContainer: View = itemView.findViewById(R.id.urls_container)
        private val artistUrls: AppCompatTextView = itemView.findViewById(R.id.artist_urls)
        private var isShowing = false
        private var artist: Artist? = null

        init {
            btExpand.setOnClickListener {
                if (!artistUrls.text.isNullOrBlank()) {
                    isShowing = toggleLayoutExpand(!isShowing, btExpand, urlsContainer)
                } else {
                    Snackbar.make(itemView, itemView.context.getString(R.string.artist_urls_is_empty),
                        Snackbar.LENGTH_SHORT).show()
                }
            }
            artistUrls.transformationMethod = LinkTransformationMethod()
        }

        fun bind(data: Artist?) {
            artist = data
            if (urlsContainer.isVisible) {
                isShowing = false
                btExpand.toggleArrow(show = false, delay = false)
                urlsContainer.isVisible = false
            }
            if (data == null) return
            artistName.text = data.name
            artistId.text = String.format("#%d", data.id)
            var urlsText = ""
            data.urls?.forEach { url ->
                urlsText = String.format("%s\r\n%s", url, urlsText)
            }
            if (urlsText.isNotBlank()) {
                artistUrls.text = urlsText
            }
        }

        private fun toggleLayoutExpand(show: Boolean, view: View, container: View): Boolean {
            view.toggleArrow(show)
            if (show)
                ViewAnimation.expand(container)
            else
                ViewAnimation.collapse(container)
            return show
        }
    }
}