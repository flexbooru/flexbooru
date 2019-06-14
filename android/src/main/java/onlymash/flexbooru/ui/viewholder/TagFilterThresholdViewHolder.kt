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

package onlymash.flexbooru.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.widget.TagFilterView

class TagFilterThresholdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun create(parent: ViewGroup): TagFilterThresholdViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tag_filter, parent, false)
            return TagFilterThresholdViewHolder(view)
        }
    }
    init {
        (itemView as TagFilterView).apply {
            color = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
            selectedTextColor = ContextCompat.getColor(itemView.context, R.color.white)
        }
    }
}