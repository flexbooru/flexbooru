package onlymash.flexbooru.ui.viewholder

import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.widget.AutoCollapseTextView

class BooruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    lateinit var booru: Booru

    private val booruName: TextView = itemView.findViewById(R.id.booru_name)
    private val booruShare: AppCompatImageView = itemView.findViewById(R.id.booru_share)
    private val booruEdit: AppCompatImageView = itemView.findViewById(R.id.booru_edit)
    private val booruUrl: AutoCollapseTextView = itemView.findViewById(R.id.booru_url)
    private val booruHashSalt: AutoCollapseTextView = itemView.findViewById(R.id.booru_hash_salt)

    fun bind(booru: Booru) {
        this.booru = booru
        booruName.text = booru.name
        "${booru.scheme}//${booru.host}"
        booruUrl.text = String.format("%s//%s", booru.scheme, booru.host)
        if (booru.type == Constants.TYPE_MOEBOORU && !booru.hash_salt.isNullOrEmpty()) {
            booruHashSalt.text = booru.hash_salt
        }
    }

    override fun onClick(v: View?) {

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return true
    }
}