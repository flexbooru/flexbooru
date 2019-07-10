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

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.entity.common.Booru
import onlymash.flexbooru.ui.viewholder.BooruViewHolder

class BooruAdapter(private val activity: Activity) : RecyclerView.Adapter<BooruViewHolder>(), BooruManager.Listener {
    private var boorus: MutableList<Booru> =
        BooruManager.getAllBoorus()?.toMutableList() ?: mutableListOf()
    init {
        setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooruViewHolder =
        BooruViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booru, parent, false), activity)
    override fun getItemCount(): Int = boorus.size
    override fun onBindViewHolder(holder: BooruViewHolder, position: Int) {
        holder.bind(booru = boorus[position])
    }
    override fun getItemId(position: Int): Long = boorus[position].uid
    override fun onAdd(booru: Booru) {
        val pos = itemCount
        boorus.add(booru)
        notifyItemInserted(pos)
    }
    override fun onDelete(booruUid: Long) {
        val index = boorus.indexOfFirst { it.uid == booruUid }
        if (index < 0) return
        boorus.removeAt(index)
        notifyItemRemoved(index)
    }
    override fun onUpdate(booru: Booru) {
        boorus.forEachIndexed { i, b ->
            if (b.uid == booru.uid) {
                b.name = booru.name
                b.scheme = booru.scheme
                b.host = booru.host
                b.hashSalt = booru.hashSalt
                b.type = booru.type
                notifyItemChanged(i)
                return@forEachIndexed
            }
        }
    }

    override fun onChanged(boorus: MutableList<Booru>) {
        val tmpBoorus: MutableList<Booru> = mutableListOf()
        tmpBoorus.addAll(this.boorus)
        this.boorus.clear()
        this.boorus.addAll(boorus)
        val diffResult = DiffUtil.calculateDiff(BooruAdapterDiffCallback(tmpBoorus, this.boorus))
        diffResult.dispatchUpdatesTo(this)
    }

    inner class BooruAdapterDiffCallback(private val oldBoorus: MutableList<Booru>,
                                         private val newBoorus: MutableList<Booru>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldBoorus[oldItemPosition].uid == newBoorus[newItemPosition].uid

        override fun getOldListSize(): Int = oldBoorus.size

        override fun getNewListSize(): Int = newBoorus.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldBoorus[oldItemPosition] == newBoorus[newItemPosition]

    }
}