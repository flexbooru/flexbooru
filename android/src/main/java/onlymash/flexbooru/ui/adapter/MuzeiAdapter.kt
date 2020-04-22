/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

import android.view.MenuInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activeMuzeiUid
import onlymash.flexbooru.data.model.common.Muzei
import onlymash.flexbooru.databinding.ItemMuzeiBinding
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.ui.viewbinding.viewBinding

class MuzeiAdapter(
    private val deleteMuzeiCallback: (Long) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        fun muzeiDiffCallback(oldItems: List<Muzei>, newItems: List<Muzei>) = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition].uid == newItems[newItemPosition].uid

            override fun getOldListSize(): Int = oldItems.size

            override fun getNewListSize(): Int = newItems.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition] == newItems[newItemPosition]
        }
    }

    private var data: MutableList<Muzei> = mutableListOf()

    private var activeUid = activeMuzeiUid

    fun getUidByPosition(position: Int) = data.getOrNull(position)?.uid

    private fun refresh(uid: Long) {
        val index = data.indexOfFirst { it.uid == uid }
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    fun updateData(data: List<Muzei>) {
        val result = DiffUtil.calculateDiff(muzeiDiffCallback(this.data, data))
        this.data.clear()
        this.data.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): RecyclerView.ViewHolder = MuzeiViewHolder(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MuzeiViewHolder).bind(data[position])
    }

    inner class MuzeiViewHolder(binding: ItemMuzeiBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup): this(parent.viewBinding(ItemMuzeiBinding::inflate))

        private val keyword = binding.muzeiKeyword
        private val actionMenu = binding.actionMenu
        private lateinit var muzei: Muzei

        init {
            itemView.setOnClickListener {
                if (!itemView.isSelected) {
                    val oldUid = activeUid
                    val newUid = muzei.uid
                    activeUid = newUid
                    activeMuzeiUid = newUid
                    itemView.isSelected = true
                    refresh(oldUid)
                }
            }
            MenuInflater(itemView.context).inflate(R.menu.muzei_item, actionMenu.menu)
            actionMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_muzei_item_search -> {
                        SearchActivity.startSearch(itemView.context, muzei.query)
                    }
                    R.id.action_muzei_item_copy -> {
                        itemView.context.copyText(muzei.query)
                    }
                    R.id.action_muzei_item_delete -> {
                        deleteMuzeiCallback(muzei.uid)
                    }
                }
                true
            }
        }

        fun bind(muzei: Muzei) {
            this.muzei = muzei
            itemView.tag = muzei.uid
            itemView.isSelected = muzei.uid == activeUid
            keyword.text = muzei.query
        }
    }
}