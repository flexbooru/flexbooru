/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.entity.common.Muzei
import onlymash.flexbooru.ui.viewholder.MuzeiViewHolder

/**
 * Muzei list Adapter
 * */
class MuzeiAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: MutableList<Muzei> = mutableListOf()

    private var activeUid = Settings.activeMuzeiUid

    private fun refresh(uid: Long) {
        val index = data.indexOfFirst { it.uid == uid }
        notifyItemChanged(index)
    }

    /**
     * Update adapter data set
     * */
    fun updateData(data: MutableList<Muzei>) {
        val tmpData: MutableList<Muzei> = mutableListOf()
        tmpData.addAll(this.data)
        this.data.clear()
        this.data.addAll(data)
        var isExist = false
        data.forEachIndexed { _, muzei ->
            if (muzei.uid == activeUid) {
                isExist = true
                return@forEachIndexed
            }
        }
        if (!isExist && data.size > 0) {
            val uid = data[0].uid
            activeUid = uid
            Settings.activeMuzeiUid = uid
        }
        val diffResult = DiffUtil.calculateDiff(MuzeiAdapterDiffCallback(tmpData, this.data))
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MuzeiViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_muzei, parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = data[position]
        val uid = data.uid
        (holder as MuzeiViewHolder).bind(data)
        holder.itemView.apply {
            tag = uid
            isSelected = uid == activeUid
            setOnClickListener {
                if (!isSelected) {
                    activeUid = uid
                    refresh(Settings.activeMuzeiUid)
                    Settings.activeMuzeiUid = uid
                    isSelected = true
                }
            }
        }
    }

    inner class MuzeiAdapterDiffCallback(private val oldMuzeis: MutableList<Muzei>,
                                         private val newMuzeis: MutableList<Muzei>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldMuzeis[oldItemPosition].uid == newMuzeis[newItemPosition].uid

        override fun getOldListSize(): Int = oldMuzeis.size

        override fun getNewListSize(): Int = newMuzeis.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldMuzeis[oldItemPosition] == newMuzeis[newItemPosition]

    }
}