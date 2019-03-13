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
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.entity.post.PostDan
import onlymash.flexbooru.entity.post.PostDanOne
import onlymash.flexbooru.entity.post.PostGel
import onlymash.flexbooru.entity.post.PostMoe
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.AccountActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.widget.CircularImageView
import onlymash.flexbooru.widget.LinkTransformationMethod

class InfoBottomSheetDialog : TransparentBottomSheetDialogFragment() {

    companion object {
        private const val POST_TYPE_KEY = "post_type"
        private const val USER_NAME_KEY = "name"
        private const val USER_ID_KEY = "id"
        private const val DATE_KEY = "date"
        private const val SOURCE_KEY = "source"
        private const val RATING_KEY = "rating"
        private const val SCORE_KEY = "score"
        private const val PARENT_KEY = "parent"
        fun create(post: Any?): InfoBottomSheetDialog {
            return InfoBottomSheetDialog().apply {
                arguments = when (post) {
                    is PostDan -> Bundle().apply {
                        putInt(POST_TYPE_KEY, Constants.TYPE_DANBOORU)
                        putString(USER_NAME_KEY, post.uploader_name)
                        putInt(USER_ID_KEY, post.uploader_id)
                        putString(DATE_KEY, post.getCreatedDate())
                        putString(SOURCE_KEY, post.source)
                        putString(RATING_KEY, post.rating)
                        putInt(SCORE_KEY, post.score)
                        putInt(PARENT_KEY, post.parent_id ?: -1)
                    }
                    is PostMoe -> Bundle().apply {
                        putInt(POST_TYPE_KEY, Constants.TYPE_MOEBOORU)
                        putString(USER_NAME_KEY, post.author)
                        putInt(USER_ID_KEY, post.creator_id)
                        putString(DATE_KEY, post.getCreatedDate())
                        putString(SOURCE_KEY, post.source)
                        putString(RATING_KEY, post.rating)
                        putInt(SCORE_KEY, post.score)
                        putInt(PARENT_KEY, post.parent_id ?: -1)
                    }
                    is PostDanOne -> Bundle().apply {
                        putInt(POST_TYPE_KEY, Constants.TYPE_DANBOORU_ONE)
                        putString(USER_NAME_KEY, post.author)
                        putInt(USER_ID_KEY, post.creator_id)
                        putString(DATE_KEY, post.getCreatedDate())
                        putString(SOURCE_KEY, post.source)
                        putString(RATING_KEY, post.rating)
                        putInt(SCORE_KEY, post.score)
                        putInt(PARENT_KEY, post.parent_id ?: -1)
                    }
                    is PostGel -> Bundle().apply {
                        putInt(POST_TYPE_KEY, Constants.TYPE_GELBOORU)
                        putString(USER_NAME_KEY, "")
                        putInt(USER_ID_KEY, post.creator_id)
                        putString(DATE_KEY, post.getCreatedDate())
                        putString(SOURCE_KEY, post.source)
                        putString(RATING_KEY, post.rating)
                        putInt(SCORE_KEY, post.score)
                        putString(PARENT_KEY, post.parent_id)
                    }
                    else -> throw IllegalStateException("unknown post type")
                }
            }
        }
    }

    private var type = -1
    private var name = ""
    private var userId = -1
    private var date = ""
    private var source = ""
    private var rating = ""
    private var score = -1
    private var parent = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arg = arguments ?: throw RuntimeException("arg is null")
        arg.apply {
            type = getInt(POST_TYPE_KEY)
            when (type) {
                Constants.TYPE_DANBOORU -> {
                    name = getString(USER_NAME_KEY) ?: ""
                    userId = getInt(USER_ID_KEY, -1)
                    date = getString(DATE_KEY) ?: ""
                    source = getString(SOURCE_KEY) ?: ""
                    rating = getString(RATING_KEY) ?: ""
                    score = getInt(SCORE_KEY, -1)
                    parent = getInt(PARENT_KEY, -1)
                }
                Constants.TYPE_MOEBOORU,
                Constants.TYPE_DANBOORU_ONE -> {
                    name = getString(USER_NAME_KEY) ?: ""
                    userId = getInt(USER_ID_KEY, -1)
                    date = getString(DATE_KEY) ?: ""
                    source = getString(SOURCE_KEY) ?: ""
                    rating = getString(RATING_KEY) ?: ""
                    score = getInt(SCORE_KEY, -1)
                    parent = getInt(PARENT_KEY, -1)
                }
                Constants.TYPE_GELBOORU -> {
                    name = getString(USER_NAME_KEY) ?: ""
                    userId = getInt(USER_ID_KEY, -1)
                    date = getString(DATE_KEY) ?: ""
                    source = getString(SOURCE_KEY) ?: ""
                    rating = getString(RATING_KEY) ?: ""
                    score = getInt(SCORE_KEY, -1)
                    val p = getString(PARENT_KEY)
                    parent = if (p.isNullOrEmpty()) -1 else p.toInt()
                }
            }
        }
    }
    private lateinit var behavior: BottomSheetBehavior<View>
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(requireContext(), R.layout.fragment_bottom_sheet_info, null)
        view.findViewById<TextView>(R.id.user_name).text = name
        view.findViewById<TextView>(R.id.user_id).text = userId.toString()
        view.findViewById<TextView>(R.id.created_date).text = date
        view.findViewById<TextView>(R.id.source).apply {
            text = source
            transformationMethod = LinkTransformationMethod()
        }
        view.findViewById<TextView>(R.id.rating).text =
            when (rating) {
                "s" -> getString(R.string.browse_info_rating_safe)
                "q" -> getString(R.string.browse_info_rating_questionable)
                else -> getString(R.string.browse_info_rating_explicit)
            }
        view.findViewById<TextView>(R.id.score).text = score.toString()
        if (parent > 0) {
            view.findViewById<TextView>(R.id.parent).text = parent.toString()
            view.findViewById<LinearLayout>(R.id.parent_container).setOnClickListener {
                SearchActivity.startActivity(requireContext(), "parent:$parent")
            }
        } else {
            view.findViewById<LinearLayout>(R.id.parent_container).visibility = View.GONE
        }
        if (type == Constants.TYPE_MOEBOORU  && userId > 0) {
            BooruManager.getBooruByUid(Settings.instance().activeBooruUid)?.let { booru ->
                GlideApp.with(this)
                    .load(String.format(getString(R.string.account_user_avatars), booru.scheme, booru.host, userId))
                    .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_account))
                    .into(view.findViewById<CircularImageView>(R.id.user_avatar))
            }
        }
        view.findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.browse_info_title)
            setNavigationOnClickListener {
                dismiss()
            }
        }
        if (type != Constants.TYPE_GELBOORU) {
            view.findViewById<ConstraintLayout>(R.id.user_container).setOnClickListener {
                startActivity(Intent(requireContext(), AccountActivity::class.java).apply {
                    putExtra(AccountActivity.USER_ID_KEY, userId)
                    putExtra(AccountActivity.USER_NAME_KEY, name)
                })
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