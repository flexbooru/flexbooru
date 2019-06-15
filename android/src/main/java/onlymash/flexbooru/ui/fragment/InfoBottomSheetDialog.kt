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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.activity.AccountActivity
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.worker.DownloadWorker
import de.hdodenhof.circleimageview.CircleImageView
import onlymash.flexbooru.widget.LinkTransformationMethod

class InfoBottomSheetDialog : TransparentBottomSheetDialogFragment() {

    companion object {
        private const val POST_TYPE_KEY = "post_type"
        private const val USER_NAME_KEY = "name"
        private const val USER_ID_KEY = "id"
        private const val USER_AVATAR_KEY = "avatar"
        private const val DATE_KEY = "date"
        private const val SOURCE_KEY = "source"
        private const val RATING_KEY = "rating"
        private const val SCORE_KEY = "score"
        private const val PARENT_KEY = "parent"

        private const val URL_SAMPLE_KEY = "sample_url"
        private const val URL_LARGER_KEY = "larger_url"
        private const val URL_ORIGIN_KEY = "origin_url"

        private const val SIZE_SAMPLE_KEY = "sample_size"
        private const val SIZE_LARGER_KEY = "larger_size"
        private const val SIZE_ORIGIN_KEY = "origin_size"

        private const val POST_ID_KEY = "post_id"
        private const val HOST_KEY = "host"
        private const val SCHEME_KEY = "scheme"

        fun create(post: Any?): InfoBottomSheetDialog {
            return InfoBottomSheetDialog().apply {
                val bundle = Bundle()
                if (post is PostBase) {
                    bundle.apply {
                        putString(SCHEME_KEY, post.scheme)
                        putString(HOST_KEY, post.host)
                        putInt(POST_ID_KEY, post.getPostId())
                        putString(URL_SAMPLE_KEY, post.getSampleUrl())
                        putString(URL_LARGER_KEY, post.getLargerUrl())
                        putString(URL_ORIGIN_KEY, post.getOriginUrl())
                        putString(SIZE_SAMPLE_KEY, post.getSampleSize())
                        putString(SIZE_LARGER_KEY, post.getLargerSize())
                        putString(SIZE_ORIGIN_KEY, post.getOriginSize())
                    }
                }
                arguments = when (post) {
                    is PostDan -> bundle.apply {
                        putInt(POST_TYPE_KEY, Constants.TYPE_DANBOORU)
                        putString(USER_NAME_KEY, post.uploader_name)
                        putInt(USER_ID_KEY, post.uploader_id)
                        putString(DATE_KEY, post.getCreatedDate())
                        putString(SOURCE_KEY, post.source)
                        putString(RATING_KEY, post.rating)
                        putInt(SCORE_KEY, post.getPostScore())
                        putInt(PARENT_KEY, post.parent_id ?: -1)
                    }
                    is PostMoe -> bundle.apply {
                        putInt(POST_TYPE_KEY, Constants.TYPE_MOEBOORU)
                        putString(USER_NAME_KEY, post.author)
                        putInt(USER_ID_KEY, post.creator_id)
                        putString(DATE_KEY, post.getCreatedDate())
                        putString(SOURCE_KEY, post.source)
                        putString(RATING_KEY, post.rating)
                        putInt(SCORE_KEY, post.getPostScore())
                        putInt(PARENT_KEY, post.parent_id ?: -1)
                    }
                    is PostDanOne -> bundle.apply {
                        putInt(POST_TYPE_KEY, Constants.TYPE_DANBOORU_ONE)
                        putString(USER_NAME_KEY, post.author)
                        putInt(USER_ID_KEY, post.creator_id)
                        putString(DATE_KEY, post.getCreatedDate())
                        putString(SOURCE_KEY, post.source)
                        putString(RATING_KEY, post.rating)
                        putInt(SCORE_KEY, post.getPostScore())
                        putInt(PARENT_KEY, post.parent_id ?: -1)
                    }
                    is PostGel -> bundle.apply {
                        putInt(POST_TYPE_KEY, Constants.TYPE_GELBOORU)
                        putString(USER_NAME_KEY, "")
                        putInt(USER_ID_KEY, post.creator_id)
                        putString(DATE_KEY, post.getCreatedDate())
                        putString(SOURCE_KEY, post.source)
                        putString(RATING_KEY, post.rating)
                        putInt(SCORE_KEY, post.getPostScore())
                    }
                    is PostSankaku -> bundle.apply {
                        putInt(POST_TYPE_KEY, Constants.TYPE_SANKAKU)
                        putString(USER_NAME_KEY, post.author.name)
                        putInt(USER_ID_KEY, post.author.id)
                        putString(USER_AVATAR_KEY, post.author.avatar)
                        putString(DATE_KEY, post.getCreatedDate())
                        putString(SOURCE_KEY, post.source)
                        putString(RATING_KEY, post.rating)
                        putInt(SCORE_KEY, post.getPostScore())
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
    private var avatar = ""
    private var sizeSampleString = ""
    private var sizeLargerString = ""
    private var sizeOriginString = ""
    private var urlSampleString = ""
    private var urlLargerString = ""
    private var urlOriginString = ""
    private var postId = -1
    private var host = "save"
    private var scheme = "http"

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
                    parent = -1
                }
                Constants.TYPE_SANKAKU -> {
                    name = getString(USER_NAME_KEY) ?: ""
                    userId = getInt(USER_ID_KEY, -1)
                    avatar = getString(USER_AVATAR_KEY) ?: ""
                    date = getString(DATE_KEY) ?: ""
                    source = getString(SOURCE_KEY) ?: ""
                    rating = getString(RATING_KEY) ?: ""
                    score = getInt(SCORE_KEY, -1)
                    parent = -1
                }
            }
            sizeSampleString = getString(SIZE_SAMPLE_KEY, "")
            sizeLargerString = getString(SIZE_LARGER_KEY, "")
            sizeOriginString = getString(SIZE_ORIGIN_KEY, "")
            urlSampleString = getString(URL_SAMPLE_KEY, "")
            urlLargerString = getString(URL_LARGER_KEY, "")
            urlOriginString = getString(URL_ORIGIN_KEY, "")
            host = getString(HOST_KEY, host)
            postId = getInt(POST_ID_KEY, -1)
            scheme = getString(SCHEME_KEY, "http")
        }
    }
    private lateinit var behavior: BottomSheetBehavior<View>
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(requireContext(), R.layout.fragment_bottom_sheet_info, null)
        view.findViewById<AppCompatTextView>(R.id.user_name).text = name
        view.findViewById<AppCompatTextView>(R.id.user_id).text = userId.toString()
        view.findViewById<AppCompatTextView>(R.id.created_date).text = date
        view.findViewById<AppCompatTextView>(R.id.source).apply {
            text = source
            transformationMethod = LinkTransformationMethod()
        }
        view.findViewById<View>(R.id.source_container).apply {
            setOnClickListener {
                if (source.isNotEmpty()) {
                    context?.launchUrl(source)
                }
            }
            setOnLongClickListener {
                if (source.isNotEmpty()) {
                    context?.copyText(source)
                }
                true
            }
        }
        view.findViewById<AppCompatTextView>(R.id.rating).text =
            when (rating) {
                "s" -> getString(R.string.browse_info_rating_safe)
                "q" -> getString(R.string.browse_info_rating_questionable)
                else -> getString(R.string.browse_info_rating_explicit)
            }
        view.findViewById<AppCompatTextView>(R.id.score).text = score.toString()
        if (parent > 0) {
            view.findViewById<AppCompatTextView>(R.id.parent).text = parent.toString()
            view.findViewById<LinearLayout>(R.id.parent_container).setOnClickListener {
                SearchActivity.startActivity(requireContext(), "parent:$parent")
            }
        } else {
            view.findViewById<LinearLayout>(R.id.parent_container).visibility = View.GONE
        }
        if (type == Constants.TYPE_MOEBOORU  && userId > 0) {
            GlideApp.with(this)
                .load(String.format(getString(R.string.account_user_avatars), scheme, host, userId))
                .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_account))
                .into(view.findViewById<CircleImageView>(R.id.user_avatar))
        } else if (type == Constants.TYPE_SANKAKU && avatar.isNotEmpty()) {
            GlideApp.with(this)
                .load(avatar)
                .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_account))
                .into(view.findViewById<CircleImageView>(R.id.user_avatar))
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
                    if (avatar.isNotEmpty()) {
                        putExtra(AccountActivity.USER_AVATAR_KEY, avatar)
                    }
                })
                dismiss()
            }
        }
        view.findViewById<LinearLayout>(R.id.url_sample_container).setOnLongClickListener {
            context?.copyText(urlSampleString)
            true
        }
        view.findViewById<LinearLayout>(R.id.url_larger_container).setOnLongClickListener {
            context?.copyText(urlLargerString)
            true
        }
        view.findViewById<LinearLayout>(R.id.url_origin_container).setOnLongClickListener {
            context?.copyText(urlOriginString)
            true
        }
        view.findViewById<AppCompatTextView>(R.id.url_sample_size).text = sizeSampleString
        view.findViewById<AppCompatTextView>(R.id.url_larger_size).text = sizeLargerString
        view.findViewById<AppCompatTextView>(R.id.url_origin_size).text = sizeOriginString
        view.findViewById<ImageView>(R.id.url_sample_download).setOnClickListener {
            DownloadWorker.download(
                url = urlSampleString,
                postId = postId,
                host = host,
                activity = requireActivity()
            )
        }
        view.findViewById<ImageView>(R.id.url_larger_download).setOnClickListener {
            DownloadWorker.download(
                url = urlLargerString,
                postId = postId,
                host = host,
                activity = requireActivity()
            )
        }
        view.findViewById<ImageView>(R.id.url_origin_download).setOnClickListener {
            DownloadWorker.download(
                url = urlOriginString,
                postId = postId,
                host = host,
                activity = requireActivity()
            )
        }
        view.findViewById<ImageView>(R.id.url_sample_open).setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse(urlSampleString)
            }
            try {
                startActivity(intent)
            } catch (_: ActivityNotFoundException) {}
        }
        view.findViewById<ImageView>(R.id.url_larger_open).setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse(urlLargerString)
            }
            try {
                startActivity(intent)
            } catch (_: ActivityNotFoundException) {}
        }
        view.findViewById<ImageView>(R.id.url_origin_open).setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse(urlOriginString)
            }
            try {
                startActivity(intent)
            } catch (_: ActivityNotFoundException) {}
        }
        dialog.setContentView(view)
        behavior = BottomSheetBehavior.from(view.parent as View)
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
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}