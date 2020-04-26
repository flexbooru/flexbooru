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

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Keys.POST_ID
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.databinding.FragmentBottomSheetInfoBinding
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.extension.formatDate
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.activity.AccountActivity
import onlymash.flexbooru.ui.base.BaseBottomSheetDialogFragment
import onlymash.flexbooru.ui.viewmodel.ShortcutViewModel
import onlymash.flexbooru.ui.viewmodel.getShortcutViewModel
import onlymash.flexbooru.widget.LinkTransformationMethod
import onlymash.flexbooru.worker.DownloadWorker
import org.kodein.di.erased.instance

class ShortcutInfoFragment : BaseBottomSheetDialogFragment() {

    companion object {
        fun create(postId: Int): ShortcutInfoFragment {
            return ShortcutInfoFragment().apply {
                arguments = Bundle().apply {
                    putInt(POST_ID, postId)
                }
            }
        }
    }

    enum class UrlType {
        SAMPLE,
        MEDIUM,
        ORIGIN,
    }

    private lateinit var behavior: BottomSheetBehavior<View>

    private val postDao by instance<PostDao>()

    private var postId = -1
    private var post: Post? = null

    private lateinit var booru: Booru
    private lateinit var binding: FragmentBottomSheetInfoBinding
    private lateinit var shortcutViewModel: ShortcutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            postId = getInt(POST_ID, -1)
        }
        val booru = BooruManager.getBooruByUid(activatedBooruUid)
        if (booru == null) {
            dismiss()
            return
        }
        this.booru = booru
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = FragmentBottomSheetInfoBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        behavior = BottomSheetBehavior.from(binding.root.parent as View)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

        })
        binding.toolbarLayout.toolbar.apply {
            setTitle(R.string.browse_info_title)
            setNavigationOnClickListener {
                dismiss()
            }
        }
        binding.sourceUrl.transformationMethod = LinkTransformationMethod()
        binding.sourceContainer.apply {
            setOnClickListener {
                binding.sourceUrl.text.let { text ->
                    context?.launchUrl(text.toString())
                }
            }
            setOnLongClickListener {
                binding.sourceUrl.text?.let { text ->
                    context?.copyText(text)
                }
                true
            }
        }
        binding.urlSampleSize.text = "not data"
        binding.urlLargerSize.text = "not data"
        setupCopyUrlListener(binding.urlSampleContainer, UrlType.SAMPLE)
        setupCopyUrlListener(binding.urlLargerContainer, UrlType.MEDIUM)
        setupCopyUrlListener(binding.urlOriginContainer, UrlType.ORIGIN)
        setupOpenUrlListener(binding.urlSampleOpen, UrlType.SAMPLE)
        setupOpenUrlListener(binding.urlLargerOpen, UrlType.MEDIUM)
        setupOpenUrlListener(binding.urlOriginOpen, UrlType.ORIGIN)
        setupDownloadUrlListener(binding.urlSampleDownload, UrlType.SAMPLE)
        setupDownloadUrlListener(binding.urlLargerDownload, UrlType.MEDIUM)
        setupDownloadUrlListener(binding.urlOriginDownload, UrlType.ORIGIN)
        shortcutViewModel = getShortcutViewModel(postDao, booru.uid, postId)
        shortcutViewModel.post.observe(this, Observer { post ->
            bindData(post)
        })
        return dialog
    }

    private fun bindData(post: Post?) {
        this.post = post ?: return
        binding.userName.text = post.uploader.name
        binding.userId.text = post.uploader.id.toString()
        binding.sourceUrl.text = post.source
        binding.urlOriginSize.text = getSize(post.width, post.height, post.size)
        if (booru.type != BOORU_TYPE_GEL && booru.type != BOORU_TYPE_SHIMMIE) {
            binding.userContainer.setOnClickListener {
                startActivity(Intent(requireContext(), AccountActivity::class.java).apply {
                    putExtra(AccountActivity.USER_ID_KEY, post.uploader.id)
                    putExtra(AccountActivity.USER_NAME_KEY, post.uploader.name)
                    putExtra(AccountActivity.USER_AVATAR_KEY, post.uploader.avatar)
                })
                dismiss()
            }
        }
        if (booru.type == BOORU_TYPE_MOE && post.uploader.id > 0) {
            GlideApp.with(this)
                .load(String.format(getString(R.string.account_user_avatars), booru.scheme, booru.host, post.uploader.id))
                .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_account))
                .into(binding.userAvatar)
        } else if (booru.type == BOORU_TYPE_SANKAKU && !post.uploader.avatar.isNullOrBlank()) {
            GlideApp.with(this)
                .load(post.uploader.avatar)
                .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_account))
                .into(binding.userAvatar)
        }
        binding.rating.text = when (post.rating) {
            "s" -> getString(R.string.browse_info_rating_safe)
            "q" -> getString(R.string.browse_info_rating_questionable)
            else -> getString(R.string.browse_info_rating_explicit)
        }
        binding.score.text = post.score.toString()
        binding.createdDate.text = binding.root.context.formatDate(post.time)
    }

    private fun getSize(width: Int, height: Int, size: Int): String {
        return "$width x $height ${Formatter.formatFileSize(context, size.toLong())}"
    }

    override fun onStart() {
        super.onStart()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupDownloadUrlListener(view: View, type: UrlType) {
        view.setOnClickListener {
            downloadUrl(type)
        }
    }

    private fun setupOpenUrlListener(view: View, type: UrlType) {
        view.setOnClickListener {
            openUrl(type)
        }
    }

    private fun setupCopyUrlListener(view: View, type: UrlType) {
        view.setOnLongClickListener{
            context?.copyText(getUrl(type))
            true
        }
    }

    private fun downloadUrl(type: UrlType) {
        val activity = activity ?: return
        val url = getUrl(type) ?: return
        DownloadWorker.download(
            url = url,
            postId = postId,
            host = booru.host,
            activity = activity
        )
    }

    private fun openUrl(type: UrlType) {
        val url = getUrl(type) ?: return
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(url)
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {}
    }

    private fun getUrl(type: UrlType): String? {
        val post = post ?: return null
        return when (type) {
            UrlType.SAMPLE -> post.sample
            UrlType.MEDIUM -> post.medium
            else -> post.origin
        }
    }
}