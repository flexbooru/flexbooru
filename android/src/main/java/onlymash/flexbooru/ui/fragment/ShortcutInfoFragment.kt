/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Keys
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.databinding.FragmentShortcutInfoBinding
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.extension.formatDate
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.activity.AccountActivity
import onlymash.flexbooru.ui.base.PathActivity
import onlymash.flexbooru.ui.base.ShortcutFragment
import onlymash.flexbooru.widget.LinkTransformationMethod
import onlymash.flexbooru.worker.DownloadWorker

enum class UrlType {
    SAMPLE,
    MEDIUM,
    ORIGIN,
}

class ShortcutInfoFragment : ShortcutFragment<FragmentShortcutInfoBinding>() {

    companion object {
        fun create(postId: Int): ShortcutInfoFragment {
            return ShortcutInfoFragment().apply {
                arguments = Bundle().apply {
                    putInt(Keys.POST_ID, postId)
                }
            }
        }
    }
    
    private lateinit var booru: Booru
    private var post: Post? = null

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentShortcutInfoBinding {
        return FragmentShortcutInfoBinding.inflate(inflater, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onBaseViewCreated(view: View, savedInstanceState: Bundle?) {
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
        binding.urlSampleSize.text = "Unknown"
        binding.urlLargerSize.text = "Unknown"
        setupCopyUrlListener(binding.urlSampleContainer, UrlType.SAMPLE)
        setupCopyUrlListener(binding.urlLargerContainer, UrlType.MEDIUM)
        setupCopyUrlListener(binding.urlOriginContainer, UrlType.ORIGIN)
        setupOpenUrlListener(binding.urlSampleOpen, UrlType.SAMPLE)
        setupOpenUrlListener(binding.urlLargerOpen, UrlType.MEDIUM)
        setupOpenUrlListener(binding.urlOriginOpen, UrlType.ORIGIN)
        setupDownloadUrlListener(binding.urlSampleDownload, UrlType.SAMPLE)
        setupDownloadUrlListener(binding.urlLargerDownload, UrlType.MEDIUM)
        setupDownloadUrlListener(binding.urlOriginDownload, UrlType.ORIGIN)
    }

    override fun onBooruLoaded(booru: Booru?) {
        super.onBooruLoaded(booru)
        this.booru = booru ?: return
    }

    override fun onPostLoaded(post: Post?) {
        bindData(post)
    }

    private fun bindData(post: Post?) {
        this.post = post ?: return
        binding.userName.text = post.uploader.name
        binding.userId.text = post.uploader.id.toString()
        binding.sourceUrl.text = post.source
        binding.urlOriginSize.text = getSize(post.width, post.height, post.size)
        if (booru.type != Values.BOORU_TYPE_GEL && booru.type != Values.BOORU_TYPE_GEL_LEGACY && booru.type != Values.BOORU_TYPE_SHIMMIE) {
            binding.userContainer.setOnClickListener {
                startActivity(Intent(requireContext(), AccountActivity::class.java).apply {
                    putExtra(AccountActivity.USER_ID_KEY, post.uploader.id)
                    putExtra(AccountActivity.USER_NAME_KEY, post.uploader.name)
                    putExtra(AccountActivity.USER_AVATAR_KEY, post.uploader.avatar)
                })
            }
        }
        if (booru.type == Values.BOORU_TYPE_MOE && post.uploader.id > 0) {
            GlideApp.with(this)
                .load(String.format(getString(R.string.account_user_avatars), booru.scheme, booru.host, post.uploader.id))
                .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_account))
                .into(binding.userAvatar)
        } else if (booru.type == Values.BOORU_TYPE_SANKAKU && !post.uploader.avatar.isNullOrBlank()) {
            GlideApp.with(this)
                .load(post.uploader.avatar)
                .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_account))
                .into(binding.userAvatar)
        }
        binding.rating.text = when (post.rating) {
            "s" -> getString(sRatingNameRes)
            "q" -> getString(R.string.browse_info_rating_questionable)
            "g" -> getString(R.string.browse_info_rating_general)
            else -> getString(R.string.browse_info_rating_explicit)
        }
        binding.score.text = post.score.toString()
        binding.createdDate.text = binding.root.context.formatDate(post.time)
    }

    val sRatingNameRes: Int get() = if (booru.type == Values.BOORU_TYPE_DAN) R.string.browse_info_rating_sensitive else R.string.browse_info_rating_safe

    private fun getSize(width: Int, height: Int, size: Int): String {
        return "$width x $height ${Formatter.formatFileSize(context, size.toLong())}"
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
            copyUrl(type)
            true
        }
    }
    
    private fun copyUrl(type: UrlType) {
        val post = post ?: return
        context?.copyText(getUrl(post, type))
    }

    private fun downloadUrl(type: UrlType) {
        val post = post ?: return
        val activity = activity as? PathActivity ?: return
        val url = getUrl(post, type)
        DownloadWorker.download(
            url = url,
            postId = post.id,
            host = booru.host,
            activity = activity
        )
    }

    private fun openUrl(type: UrlType) {
        val post = post ?: return
        val url = getUrl(post, type)
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(url)
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {}
    }

    private fun getUrl(post: Post, type: UrlType): String {
        return when (type) {
            UrlType.SAMPLE -> post.sample
            UrlType.MEDIUM -> post.medium
            else -> post.origin
        }
    }
}