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
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.database.TagFilterManager
import onlymash.flexbooru.entity.TagFilter
import onlymash.flexbooru.extension.copyText

class TagBrowseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun create(parent: ViewGroup): TagBrowseViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tag_browse, parent, false)
            return TagBrowseViewHolder(view)
        }
    }
    private val dot: AppCompatImageView = itemView.findViewById(R.id.dot)
    private val tagName: AppCompatTextView = itemView.findViewById(R.id.tag_name)
    private val tagExclude: AppCompatImageView = itemView.findViewById(R.id.tag_exclude)
    private val tagInclude: AppCompatImageView = itemView.findViewById(R.id.tag_include)

    private var type = -1

    private var itemListener: ItemListener? = null

    fun setItemListener(listener: ItemListener) {
        itemListener = listener
    }

    interface ItemListener {
        fun onClickItem(keyword: String)
    }

    init {
        TooltipCompat.setTooltipText(tagExclude, tagExclude.contentDescription)
        TooltipCompat.setTooltipText(tagInclude, tagInclude.contentDescription)
        itemView.setOnClickListener {
            itemListener?.onClickItem(tagName.text.toString())
        }
        itemView.setOnLongClickListener {
            itemView.context.copyText(tagName.text)
            true
        }
        tagExclude.setOnClickListener {
            val name = tagName.text?:return@setOnClickListener
            val booruUid = Settings.activeBooruUid
            TagFilterManager.createTagFilter(TagFilter(booru_uid = booruUid, name = "-$name", type = type))
        }
        tagInclude.setOnClickListener {
            val name = tagName.text?:return@setOnClickListener
            val booruUid = Settings.activeBooruUid
            TagFilterManager.createTagFilter(TagFilter(booru_uid = booruUid, name = name.toString(), type = type))
        }
    }

    fun bind(tag: TagFilter, postType: Int) {
        tagName.text = tag.name
        type = tag.type
        when (postType) {
            Constants.TYPE_DANBOORU -> {
                when (type) {
                    TagViewHolder.GENERAL -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_general))
                    TagViewHolder.ARTIST -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_artist))
                    TagViewHolder.COPYRIGHT -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_copyright))
                    TagViewHolder.CHARACTER -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_character))
                    TagViewHolder.META -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_meta))
                    else -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_unknown))
                }
            }
            Constants.TYPE_SANKAKU -> {
                when (type) {
                    TagViewHolder.GENERAL -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_general))
                    TagViewHolder.ARTIST -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_artist))
                    TagViewHolder.COPYRIGHT -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_copyright))
                    TagViewHolder.CHARACTER -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_character))
                    TagViewHolder.META_SANKAKU -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_meta))
                    TagViewHolder.GENRE -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_genre))
                    TagViewHolder.MEDIUM -> dot.setColorFilter(ContextCompat.getColor(itemView.context, R.color.tag_type_medium))
                    TagViewHolder.STUDIO -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_studio))
                    else -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_unknown))
                }
            }
            else -> {
                dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_unknown))
            }
        }
    }
}