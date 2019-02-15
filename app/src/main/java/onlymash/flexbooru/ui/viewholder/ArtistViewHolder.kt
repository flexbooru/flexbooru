package onlymash.flexbooru.ui.viewholder

import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import onlymash.flexbooru.App
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.ArtistDan
import onlymash.flexbooru.entity.ArtistMoe
import onlymash.flexbooru.util.ViewAnimation
import onlymash.flexbooru.util.toggleArrow
import onlymash.flexbooru.widget.LinkTransformationMethod

class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun create(parent: ViewGroup): ArtistViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_artist, parent, false)
            return ArtistViewHolder(view)
        }
    }

    private val artistName: TextView = itemView.findViewById(R.id.artist_name)
    private val artistId: TextView = itemView.findViewById(R.id.artist_id)
    private val btExpand: ImageButton = itemView.findViewById(R.id.bt_expand)
    private val urlsContainer: LinearLayout = itemView.findViewById(R.id.urls_container)
    private val artistUrls: TextView = itemView.findViewById(R.id.artist_urls)
    private var isShow = false
    private var artist: Any? = null

    init {
        itemView.setOnClickListener {
            when (artist) {
                is ArtistDan -> itemListener?.onClickItem((artist as ArtistDan).name)
                is ArtistMoe -> itemListener?.onClickItem((artist as ArtistMoe).name)
            }
        }
        itemView.setOnLongClickListener {
            val text = artistName.text
            if (!text.isNullOrBlank()) {
                App.app.clipboard.primaryClip = ClipData.newPlainText("Artist", text)
            }
            true
        }
        btExpand.setOnClickListener {
            if (!artistUrls.text.isNullOrBlank()) {
                isShow = toggleLayoutExpand(!isShow, btExpand, urlsContainer)
            } else {
                Snackbar.make(itemView, itemView.context.getString(R.string.artist_urls_is_empty),
                    Snackbar.LENGTH_SHORT).show()
            }
        }
        artistUrls.transformationMethod = LinkTransformationMethod()
    }

    private var itemListener: ItemListener? = null

    fun setItemListener(listener: ItemListener) {
        itemListener = listener
    }

    interface ItemListener {
        fun onClickItem(keyword: String)
    }

    fun bind(data: Any?) {
        artist = data
        when (data) {
            is ArtistDan -> {
                artistName.text = data.name
                artistId.text = String.format("#%d", data.id)
                var urlsText = ""
                data.urls?.let { urls ->
                    urls.forEach { url ->
                        urlsText = String.format("%s\r\n%s", url.url, urlsText)
                    }
                }
                if (urlsText.isNotBlank()) {
                    artistUrls.text = urlsText
                }
            }
            is ArtistMoe -> {
                artistName.text = data.name
                artistId.text = String.format("#%d", data.id)
                var urlsText = ""
                data.urls?.let { urls ->
                    urls.forEach { url ->
                        urlsText = String.format("%s\r\n%s", url, urlsText)
                    }
                }
                if (urlsText.isNotBlank()) {
                    artistUrls.text = urlsText
                }
            }
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