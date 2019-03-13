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

package onlymash.flexbooru.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.entity.post.PostDan
import onlymash.flexbooru.entity.post.PostDanOne
import onlymash.flexbooru.entity.post.PostMoe
import onlymash.flexbooru.entity.TagFilter
import onlymash.flexbooru.entity.post.PostGel
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.ui.adapter.TagBrowseAdapter
import onlymash.flexbooru.ui.viewholder.TagBrowseViewHolder
import onlymash.flexbooru.ui.viewholder.TagViewHolder

class TagBottomSheetDialog : TransparentBottomSheetDialogFragment() {
    companion object {
        private const val POST_TYPE = "post_type"
        private const val TAG_ALL_KEY = "all"
        private const val TAG_GENERAL_KEY = "general"
        private const val TAG_ARTIST_KEY = "artist"
        private const val TAG_COPYRIGHT_KEY = "copyright"
        private const val TAG_CHARACTER_KEY = "character"
        private const val TAG_CIRCLE_KEY = "circle"
        private const val TAG_FAULTS_KEY = "faults"
        private const val TAG_META_KEY = "meta"
        fun create(post: Any?): TagBottomSheetDialog {
            return TagBottomSheetDialog().apply {
                arguments = when (post) {
                    is PostDan -> {
                        Bundle().apply {
                            putInt(POST_TYPE, Constants.TYPE_DANBOORU)
                            putString(TAG_GENERAL_KEY, post.tag_string_general)
                            putString(TAG_ARTIST_KEY, post.tag_string_artist)
                            putString(TAG_COPYRIGHT_KEY, post.tag_string_copyright)
                            putString(TAG_CHARACTER_KEY, post.tag_string_character)
                            putString(TAG_META_KEY, post.tag_string_meta)
                        }
                    }
                    is PostMoe -> {
                        Bundle().apply {
                            putInt(POST_TYPE, Constants.TYPE_MOEBOORU)
                            putString(TAG_ALL_KEY, post.tags)
                        }
                    }
                    is PostDanOne -> {
                        Bundle().apply {
                            putInt(POST_TYPE, Constants.TYPE_DANBOORU_ONE)
                            putString(TAG_ALL_KEY, post.tags)
                        }
                    }
                    is PostGel -> {
                        Bundle().apply {
                            putInt(POST_TYPE, Constants.TYPE_GELBOORU)
                            putString(TAG_ALL_KEY, post.tags)
                        }
                    }
                    else -> throw IllegalStateException("unknown post type or post is null")
                }
            }
        }
    }
    private lateinit var behavior: BottomSheetBehavior<View>
    private var tags: MutableList<TagFilter> = mutableListOf()
    private val itemListener = object : TagBrowseViewHolder.ItemListener {
        override fun onClickItem(keyword: String) {
            SearchActivity.startActivity(requireContext(), keyword)
            dismissAllowingStateLoss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val booruUid = Settings.instance().activeBooruUid
        val arg = arguments ?: throw RuntimeException("arg is null")
        when (arg.getInt(POST_TYPE)) {
            Constants.TYPE_DANBOORU -> {
                arg.apply {
                    getString(TAG_GENERAL_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(TagFilter(
                                booru_uid = booruUid,
                                name = tag,
                                type = TagViewHolder.GENERAL))
                        }
                    }
                    getString(TAG_ARTIST_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(TagFilter(
                                booru_uid = booruUid,
                                name = tag,
                                type = TagViewHolder.ARTIST))
                        }
                    }
                    getString(TAG_COPYRIGHT_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(TagFilter(
                                booru_uid = booruUid,
                                name = tag,
                                type = TagViewHolder.COPYRIGHT))
                        }
                    }
                    getString(TAG_CHARACTER_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(TagFilter(
                                booru_uid = booruUid,
                                name = tag,
                                type = TagViewHolder.CHARACTER))
                        }
                    }
                    getString(TAG_META_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(TagFilter(
                                booru_uid = booruUid,
                                name = tag,
                                type = TagViewHolder.META))
                        }
                    }
                }
            }
            Constants.TYPE_MOEBOORU,
            Constants.TYPE_DANBOORU_ONE,
            Constants.TYPE_GELBOORU -> {
                arg.getString(TAG_ALL_KEY)?.trim()?.split(" ")?.forEach {  tag ->
                    if (tag.isNotEmpty()) tags.add(TagFilter(booru_uid = booruUid, name = tag))
                }
            }
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(requireContext(), R.layout.fragment_bottom_sheet_tag, null)
        view.findViewById<RecyclerView>(R.id.tags_list).apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = TagBrowseAdapter(tags, itemListener)
        }
        view.findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.title_tags)
            setOnClickListener {
                dismiss()
            }
        }
        dialog.setContentView(view)
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

        })
        return dialog
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}