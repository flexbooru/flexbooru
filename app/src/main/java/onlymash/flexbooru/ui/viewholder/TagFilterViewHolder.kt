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

package onlymash.flexbooru.ui.viewholder

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.TagFilter
import onlymash.flexbooru.widget.TagFilterView

class TagFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        private const val TAG = "TagFilterViewHolder"
        fun create(parent: ViewGroup): TagFilterViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tag_filter, parent, false)
            return TagFilterViewHolder(view)
        }
    }
    private val tagFilterView: TagFilterView = itemView.findViewById(R.id.tag_filter_label)
    private var tag: TagFilter? = null
    init {
        tagFilterView.setOnClickListener {
            tagFilterView.animateCheckedAndInvoke(!tagFilterView.isChecked) {
                tag?.checked = tagFilterView.isChecked
            }
        }
    }
    fun bind(tag: TagFilter) {
        this.tag = tag
        tagFilterView.apply {
            text = tag.name
            color = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
            selectedTextColor = ContextCompat.getColor(itemView.context, R.color.white)
            isChecked = tag.checked
        }
    }
}