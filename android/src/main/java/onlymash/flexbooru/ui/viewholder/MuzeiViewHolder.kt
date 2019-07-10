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

import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.database.MuzeiManager
import onlymash.flexbooru.entity.common.Muzei
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.ui.activity.SearchActivity

class MuzeiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val keyword = itemView.findViewById<AppCompatTextView>(R.id.muzei_keyword)
    private val actionMenu = itemView.findViewById<ActionMenuView>(R.id.action_menu)
    private lateinit var muzei: Muzei

    init {
        MenuInflater(itemView.context).inflate(R.menu.muzei_item, actionMenu.menu)
        actionMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_muzei_item_search -> {
                    SearchActivity.startActivity(itemView.context, muzei.keyword)
                }
                R.id.action_muzei_item_copy -> {
                    itemView.context.copyText(muzei.keyword)
                }
                R.id.action_muzei_item_delete -> {
                    MuzeiManager.deleteMuzei(muzei)
                }
            }
            true
        }
    }

    fun bind(muzei: Muzei) {
        this.muzei = muzei
        keyword.text = muzei.keyword
    }
}