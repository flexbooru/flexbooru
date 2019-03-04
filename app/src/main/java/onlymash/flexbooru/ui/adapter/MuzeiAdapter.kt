/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.entity.Muzei
import onlymash.flexbooru.ui.viewholder.MuzeiViewHolder

/**
 * Muzei list Adapter
 * */
class MuzeiAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: MutableList<Muzei> = mutableListOf()

    private var activeUid = Settings.instance().activeMuzeiUid

    private fun refresh(uid: Long) {
        val index = data.indexOfFirst { it.uid == uid }
        notifyItemChanged(index)
    }

    /**
     * Update adapter data set
     * */
    fun updateData(data: MutableList<Muzei>) {
        this.data = data
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
            Settings.instance().activeMuzeiUid = uid
        }
        notifyDataSetChanged()
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
                    refresh(Settings.instance().activeMuzeiUid)
                    Settings.instance().activeMuzeiUid = uid
                    isSelected = true
                }
            }
        }
    }
}