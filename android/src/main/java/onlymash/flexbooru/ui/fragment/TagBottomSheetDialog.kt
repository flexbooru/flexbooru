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

package onlymash.flexbooru.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.entity.common.TagFilter
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.ui.activity.SearchActivity
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
        private const val TAG_GENRE_KEY = "genre"
        private const val TAG_MEDIUM_KEY = "medium"
        private const val TAG_STUDIO_KEY = "studio"

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
                    is PostSankaku -> {
                        Bundle().apply {
                            putInt(POST_TYPE, Constants.TYPE_SANKAKU)
                            putString(TAG_GENERAL_KEY, post.getTagString(0))
                            putString(TAG_ARTIST_KEY, post.getTagString(1))
                            putString(TAG_STUDIO_KEY, post.getTagString(2))
                            putString(TAG_COPYRIGHT_KEY, post.getTagString(3))
                            putString(TAG_CHARACTER_KEY, post.getTagString(4))
                            putString(TAG_GENRE_KEY, post.getTagString(5))
                            putString(TAG_MEDIUM_KEY, post.getTagString(8))
                            putString(TAG_META_KEY, post.getTagString(9))
                        }
                    }
                    else -> null
                }
            }
        }
        private fun PostSankaku.getTagString(type: Int): String {
            var tag = ""
            tags.forEach {
                if (it.type == type) {
                    tag = "$tag ${it.name}"
                }
            }
            return tag.trim()
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

    private var postType = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val booruUid = Settings.activeBooruUid
        val arg = arguments
        if (arg == null) {
            dismiss()
            return
        }
        postType = arg.getInt(POST_TYPE)
        when (postType) {
            Constants.TYPE_DANBOORU -> {
                arg.apply {
                    getString(TAG_ARTIST_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.ARTIST
                                )
                            )
                        }
                    }
                    getString(TAG_COPYRIGHT_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.COPYRIGHT
                                )
                            )
                        }
                    }
                    getString(TAG_CHARACTER_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.CHARACTER
                                )
                            )
                        }
                    }
                    getString(TAG_META_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.META
                                )
                            )
                        }
                    }
                    getString(TAG_GENERAL_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.GENERAL
                                )
                            )
                        }
                    }
                }
            }
            Constants.TYPE_MOEBOORU,
            Constants.TYPE_DANBOORU_ONE,
            Constants.TYPE_GELBOORU -> {
                arg.getString(TAG_ALL_KEY)?.trim()?.split(" ")?.forEach {  tag ->
                    if (tag.isNotEmpty()) tags.add(
                        TagFilter(
                            booruUid = booruUid,
                            name = tag
                        )
                    )
                }
            }
            Constants.TYPE_SANKAKU -> {
                arg.apply {
                    getString(TAG_ARTIST_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.ARTIST
                                )
                            )
                        }
                    }
                    getString(TAG_COPYRIGHT_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.COPYRIGHT
                                )
                            )
                        }
                    }
                    getString(TAG_CHARACTER_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.CHARACTER
                                )
                            )
                        }
                    }
                    getString(TAG_META_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.META_SANKAKU
                                )
                            )
                        }
                    }
                    getString(TAG_GENRE_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.GENRE
                                )
                            )
                        }
                    }
                    getString(TAG_MEDIUM_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.MEDIUM
                                )
                            )
                        }
                    }
                    getString(TAG_STUDIO_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.STUDIO
                                )
                            )
                        }
                    }
                    getString(TAG_GENERAL_KEY)?.trim()?.split(" ")?.forEach { tag ->
                        if (tag.isNotEmpty()) {
                            tags.add(
                                TagFilter(
                                    booruUid = booruUid,
                                    name = tag,
                                    type = TagViewHolder.GENERAL
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(requireContext(), R.layout.fragment_bottom_sheet_tag, null)
        view.findViewById<RecyclerView>(R.id.tags_list).apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = TagBrowseAdapter(tags, postType, itemListener)
        }
        view.findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.title_tags)
            setOnClickListener {
                dismiss()
            }
        }
        dialog.setContentView(view)
        behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}