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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostMoe
import onlymash.flexbooru.entity.TagBrowse
import onlymash.flexbooru.ui.adapter.TagBrowseAdapter
import onlymash.flexbooru.ui.viewholder.TagViewHolder

class TagBottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var behavior: BottomSheetBehavior<View>
    private var tags: MutableList<TagBrowse> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(requireContext(), R.layout.fragment_bottom_sheet_tag, null)
        view.findViewById<RecyclerView>(R.id.tags_list).apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = TagBrowseAdapter(tags)
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

    fun setTags(post: Any?) {
        val booruUid = Settings.instance().activeBooruUid
        when (post) {
            is PostMoe -> {
                post.tags?.split(" ")?.forEach { tag ->
                    tags.add(TagBrowse(booru_uid = booruUid, name = tag))
                }
            }
            is PostDan -> {
                post.tag_string_general.split(" ").forEach { tag ->
                    tags.add(TagBrowse(
                        booru_uid = booruUid,
                        name = tag,
                        type = TagViewHolder.GENERAL))
                }
                post.tag_string_artist.split(" ").forEach { tag ->
                    tags.add(TagBrowse(
                        booru_uid = booruUid,
                        name = tag,
                        type = TagViewHolder.ARTIST))
                }
                post.tag_string_copyright.split(" ").forEach { tag ->
                    tags.add(TagBrowse(
                        booru_uid = booruUid,
                        name = tag,
                        type = TagViewHolder.COPYRIGHT))
                }
                post.tag_string_character.split(" ").forEach { tag ->
                    tags.add(TagBrowse(
                        booru_uid = booruUid,
                        name = tag,
                        type = TagViewHolder.CHARACTER))
                }
                post.tag_string_meta.split(" ").forEach { tag ->
                    tags.add(TagBrowse(
                        booru_uid = booruUid,
                        name = tag,
                        type = TagViewHolder.META))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}