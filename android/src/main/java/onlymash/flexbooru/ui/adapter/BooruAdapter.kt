/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.ui.adapter

import android.content.Intent
import android.view.MenuInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.databinding.ItemBooruBinding
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.ui.activity.BooruConfigActivity
import onlymash.flexbooru.ui.viewbinding.viewBinding

class BooruAdapter(private val qrCallabck: (String) -> Unit) : RecyclerView.Adapter<BooruAdapter.BooruViewHolder>() {

    companion object {
        fun booruDiffCallback(oldBoorus: List<Booru>, newBoorus: List<Booru>) = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldBoorus[oldItemPosition].uid == newBoorus[newItemPosition].uid

            override fun getOldListSize(): Int = oldBoorus.size

            override fun getNewListSize(): Int = newBoorus.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldBoorus[oldItemPosition] == newBoorus[newItemPosition]

        }
    }

    private var activeUid = activatedBooruUid

    private val _boorus: MutableList<Booru> = mutableListOf()

    init {
        setHasStableIds(true)
    }

    private fun refresh(uid: Long) {
        val index = _boorus.indexOfFirst { it.uid == uid }
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): BooruViewHolder = BooruViewHolder(parent)

    override fun getItemCount(): Int = _boorus.size

    override fun onBindViewHolder(holder: BooruViewHolder, position: Int) {
        holder.bind(_boorus[position])
    }

    override fun getItemId(position: Int): Long = _boorus[position].uid

    fun getUidByPosition(position: Int) = _boorus.getOrNull(position)?.uid

    fun updateBoorus(boorus: List<Booru>) {
        val result = DiffUtil.calculateDiff(booruDiffCallback(_boorus, boorus))
        _boorus.clear()
        _boorus.addAll(boorus)
        result.dispatchUpdatesTo(this)
    }

    fun getBoorus() = _boorus

    inner class BooruViewHolder(binding: ItemBooruBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup): this(parent.viewBinding(ItemBooruBinding::inflate))

        private lateinit var booru: Booru

        private val booruName = binding.booruName
        private val booruActionMenuView = binding.booruActionMenu
        private val booruUrl = binding.booruUrl
        private val booruType = binding.booruType

        init {
            itemView.setOnClickListener {
                if (!itemView.isSelected) {
                    val oldUid = activeUid
                    val newUid = booru.uid
                    activeUid = newUid
                    activatedBooruUid = newUid
                    itemView.isSelected = true
                    refresh(oldUid)
                }
            }
            MenuInflater(itemView.context).inflate(R.menu.booru_action_menu, booruActionMenuView.menu)
            booruActionMenuView.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_booru_share_qr_code -> qrCallabck.invoke(booru.toString())
                    R.id.action_booru_share_clipboard -> itemView.context.copyText(booru.toString())
                    R.id.action_booru_edit -> startConfig(booru.uid)
                    R.id.action_booru_delete -> BooruManager.deleteBooru(booru.uid)
                }
                true
            }
        }

        fun bind(booru: Booru) {
            this.booru = booru
            itemView.isSelected = booru.uid == activeUid
            booruName.text = booru.name
            booruUrl.text = String.format("%s://%s", booru.scheme, booru.host)
            booruType.setText(
                when (booru.type) {
                    BOORU_TYPE_DAN -> R.string.booru_type_danbooru
                    BOORU_TYPE_MOE -> R.string.booru_type_moebooru
                    BOORU_TYPE_DAN1 -> R.string.booru_type_danbooru_one
                    BOORU_TYPE_GEL -> R.string.booru_type_gelbooru
                    BOORU_TYPE_SANKAKU -> R.string.booru_type_sankaku
                    BOORU_TYPE_SHIMMIE -> R.string.booru_type_shimmie
                    BOORU_TYPE_GEL_LEGACY -> R.string.booru_type_gelbooru_legacy
                    else -> R.string.booru_type_unknown
                }
            )
        }

        private fun startConfig(uid: Long) {
            itemView.context.apply {
                val intent = Intent(this, BooruConfigActivity::class.java)
                    .putExtra(BooruConfigActivity.EXTRA_BOORU_UID, uid)
                startActivity(intent)
            }
        }
    }
}