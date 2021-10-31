/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Keys
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.database.TagFilterManager
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.TagFilter
import onlymash.flexbooru.databinding.FragmentShortcutTagsBinding
import onlymash.flexbooru.databinding.ItemTagBrowseBinding
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.ui.base.ShortcutFragment
import onlymash.flexbooru.ui.viewbinding.viewBinding

class ShortcutTagFragment : ShortcutFragment<FragmentShortcutTagsBinding>() {

    companion object {
        fun create(postId: Int): ShortcutTagFragment {
            return ShortcutTagFragment().apply {
                arguments = Bundle().apply {
                    putInt(Keys.POST_ID, postId)
                }
            }
        }
    }

    private lateinit var booru: Booru
    private var post: Post? = null

    private lateinit var tagListAdapter: TagListAdapter

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentShortcutTagsBinding {
        return FragmentShortcutTagsBinding.inflate(inflater, container, false)
    }

    override fun onBaseViewCreated(view: View, savedInstanceState: Bundle?) {
        tagListAdapter = TagListAdapter()
        binding.tagsList.adapter = tagListAdapter
    }

    override fun onBooruLoaded(booru: Booru?) {
        super.onBooruLoaded(booru)
        this.booru = booru ?: return
    }

    override fun onPostLoaded(post: Post?) {
        this.post = post
        tagListAdapter.notifyDataSetChanged()
    }

    inner class TagListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = post?.tags?.size ?: 0

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val tag = post?.tags?.get(position)
            (holder as TagListViewHolder).bind(tag)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int): RecyclerView.ViewHolder {
            return TagListViewHolder(parent)
        }
    }

    inner class TagListViewHolder(binding: ItemTagBrowseBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup): this(parent.viewBinding(ItemTagBrowseBinding::inflate))

        private val dot = binding.dot
        private val tagName = binding.tagName
        private val tagExclude = binding.tagExclude
        private val tagInclude = binding.tagInclude

        private var tag: TagBase? = null

        init {
            TooltipCompat.setTooltipText(tagExclude, tagExclude.contentDescription)
            TooltipCompat.setTooltipText(tagInclude, tagInclude.contentDescription)
            itemView.setOnClickListener {
                tag?.name?.let { query ->
                    SearchActivity.startSearch(itemView.context, query)
                }
            }
            itemView.setOnLongClickListener {
                itemView.context.copyText(tagName.text)
                true
            }
            tagExclude.setOnClickListener {
                tag?.let {
                    TagFilterManager.createTagFilter(
                        TagFilter(
                            booruUid = booru.uid,
                            name = "-${it.name}",
                            type = it.category
                        )
                    )
                }
            }
            tagInclude.setOnClickListener {
                tag?.let {
                    TagFilterManager.createTagFilter(
                        TagFilter(
                            booruUid = booru.uid,
                            name = it.name,
                            type = it.category
                        )
                    )
                }
            }
        }

        fun bind(tag: TagBase?) {
            this.tag = tag ?: return
            tagName.text = tag.name
            when (booru.type) {
                Values.BOORU_TYPE_DAN -> {
                    when (tag.category) {
                        Values.Tags.TYPE_GENERAL -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_general))
                        Values.Tags.TYPE_ARTIST -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_artist))
                        Values.Tags.TYPE_COPYRIGHT -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_copyright))
                        Values.Tags.TYPE_CHARACTER -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_character))
                        Values.Tags.TYPE_META -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_meta))
                        else -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_unknown))
                    }
                }
                Values.BOORU_TYPE_SANKAKU -> {
                    when (tag.category) {
                        Values.Tags.TYPE_GENERAL -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_general))
                        Values.Tags.TYPE_ARTIST -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_artist))
                        Values.Tags.TYPE_COPYRIGHT -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_copyright))
                        Values.Tags.TYPE_CHARACTER -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_character))
                        Values.Tags.TYPE_META_SANKAKU -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_meta))
                        Values.Tags.TYPE_GENRE -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_genre))
                        Values.Tags.TYPE_MEDIUM -> dot.setColorFilter(ContextCompat.getColor(itemView.context, R.color.tag_type_medium))
                        Values.Tags.TYPE_STUDIO -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_studio))
                        else -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_unknown))
                    }
                }
                Values.BOORU_TYPE_MOE -> {
                    when (tag.category) {
                        Values.Tags.TYPE_GENERAL -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_general))
                        Values.Tags.TYPE_ARTIST -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_artist))
                        Values.Tags.TYPE_COPYRIGHT -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_copyright))
                        Values.Tags.TYPE_CHARACTER -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_character))
                        Values.Tags.TYPE_CIRCLE -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_circle))
                        Values.Tags.TYPE_FAULTS -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_faults))
                        else -> dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_unknown))
                    }
                }
                else -> {
                    dot.setColorFilter(ContextCompat.getColor(itemView.context,  R.color.tag_type_unknown))
                }
            }
        }
    }
}