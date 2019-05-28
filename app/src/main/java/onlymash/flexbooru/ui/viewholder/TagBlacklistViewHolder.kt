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
import android.widget.TextView
import androidx.appcompat.widget.ActionMenuView
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.database.TagBlacklistManager
import onlymash.flexbooru.entity.TagBlacklist
import onlymash.flexbooru.extension.copyText

class TagBlacklistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tagTextView = itemView.findViewById<TextView>(R.id.tag_blacklist)
    private val actionMenu = itemView.findViewById<ActionMenuView>(R.id.action_menu)
    private lateinit var tag: TagBlacklist
    init {
        MenuInflater(itemView.context).inflate(R.menu.tag_blacklist_item, actionMenu.menu)
        actionMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_tag_blacklist_item_copy -> {
                    itemView.context.copyText(tag.tag)
                }
                R.id.action_tag_blacklist_item_delete -> {
                    TagBlacklistManager.deleteTagBlacklist(tag)
                }
            }
            true
        }
    }
    fun bind(tagBlacklist: TagBlacklist) {
        tag = tagBlacklist
        tagTextView.text = tag.tag
    }
}