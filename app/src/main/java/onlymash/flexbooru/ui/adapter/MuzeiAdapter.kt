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
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.entity.Muzei
import onlymash.flexbooru.ui.viewholder.MuzeiViewHolder

/**
 * Muzei list Adapter
 * */
class MuzeiAdapter(private val list: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var activeMuzeiUid = Settings.instance().activeMuzeiUid

    private var data: MutableList<Muzei> = mutableListOf()

    /**
     * Update adapter data set
     * */
    fun updateData(data: MutableList<Muzei>) {
        this.data = data
        var isExist = false
        data.forEachIndexed { index, muzei ->
            if (muzei.uid == activeMuzeiUid) {
                isExist = true
                return@forEachIndexed
            }
        }
        if (!isExist && data.size > 0) {
            val uid = data[0].uid
            Settings.instance().activeMuzeiUid = uid
            activeMuzeiUid = uid
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
            isSelected = uid == activeMuzeiUid
            setOnClickListener {
                if (!isSelected) {
                    list.findViewWithTag<View>(activeMuzeiUid)?.isSelected = false
                    isSelected = true
                    activeMuzeiUid = uid
                    Settings.instance().activeMuzeiUid = uid
                }
            }
        }
    }
}