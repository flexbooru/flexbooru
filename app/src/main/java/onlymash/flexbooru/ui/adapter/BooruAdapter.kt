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

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.ui.viewholder.BooruViewHolder

class BooruAdapter(private val activity: Activity) : RecyclerView.Adapter<BooruViewHolder>(), BooruManager.Listener {

    private var boorus: MutableList<Booru> = BooruManager.getAllBoorus()?.toMutableList() ?: mutableListOf()

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
    fun updateData(boorus: MutableList<Booru>) {
        this.boorus = boorus
        notifyDataSetChanged()
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
                b.hash_salt = booru.hash_salt
                b.type = booru.type
                notifyItemChanged(i)
                return@forEachIndexed
            }
        }
    }
}