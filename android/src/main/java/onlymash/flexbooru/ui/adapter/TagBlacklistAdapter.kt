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

import android.view.MenuInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.databinding.ItemTagBlacklistBinding
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.ui.viewbinding.viewBinding


class TagBlacklistAdapter(
    private val removeTagCallback: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        fun listStringDiffCallback(oldItems: List<String>, newItems: List<String>) = object : DiffUtil.Callback() {
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition] == newItems[newItemPosition]

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition] == newItems[newItemPosition]

            override fun getOldListSize(): Int = oldItems.size

            override fun getNewListSize(): Int = newItems.size
        }
    }

    private var data: MutableList<String> = mutableListOf()

    fun updateData(tags: List<String>) {
        val result = DiffUtil.calculateDiff(listStringDiffCallback(data, tags))
        data.clear()
        data.addAll(tags)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): RecyclerView.ViewHolder = TagBlacklistViewHolder(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TagBlacklistViewHolder).bind(data[position])
    }

    inner class TagBlacklistViewHolder(binding: ItemTagBlacklistBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup): this(parent.viewBinding(ItemTagBlacklistBinding::inflate))

        private val tagTextView = binding.tagBlacklist
        private val actionMenu = binding.actionMenu
        private lateinit var tag: String

        init {
            MenuInflater(itemView.context).inflate(R.menu.tag_blacklist_item, actionMenu.menu)
            actionMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_tag_blacklist_item_copy -> {
                        itemView.context.copyText(tag)
                    }
                    R.id.action_tag_blacklist_item_delete -> {
                        removeTagCallback(tag)
                    }
                }
                true
            }
        }

        fun bind(tag: String) {
            this.tag = tag
            tagTextView.text = tag
        }
    }
}