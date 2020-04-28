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

package onlymash.flexbooru.ui.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ui.PlayerView

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Keys.POST_POSITION
import onlymash.flexbooru.app.Keys.POST_QUERY
import onlymash.flexbooru.app.Settings.POST_SIZE_LARGER
import onlymash.flexbooru.app.Settings.POST_SIZE_SAMPLE
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.app.Settings.detailSize
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.app.Values.REQUEST_CODE_SAVE_FILE
import onlymash.flexbooru.data.action.ActionVote
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.repository.favorite.VoteRepository
import onlymash.flexbooru.data.repository.favorite.VoteRepositoryImpl
import onlymash.flexbooru.databinding.ActivityDetailBinding
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.extension.*
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.adapter.DetailAdapter
import onlymash.flexbooru.ui.base.PathActivity
import onlymash.flexbooru.ui.fragment.InfoDialog
import onlymash.flexbooru.ui.viewbinding.viewBinding
import onlymash.flexbooru.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ui.viewmodel.getDetailViewModel
import onlymash.flexbooru.widget.DismissFrameLayout
import onlymash.flexbooru.worker.DownloadWorker
import org.kodein.di.erased.instance
import java.io.*

private const val ALPHA_MAX = 0xFF
private const val ALPHA_MIN = 0x00
private const val POSITION_INIT = -1
private const val POSITION_INITED = -2

private const val ACTION_SAVE = 11
private const val ACTION_SAVE_AS = 12
private const val ACTION_SET_AS = 13
private const val ACTION_SEND = 14

class DetailActivity : PathActivity(),
    Toolbar.OnMenuItemClickListener, DismissFrameLayout.OnDismissListener {

    companion object {
        const val ACTION_DETAIL_POST_POSITION = "detail_post_position"

        fun start(activity: Activity, query: String?, position: Int, view: View, tranName: String) {
            val intent = Intent(activity, DetailActivity::class.java).apply {
                putExtra(POST_QUERY, query)
                putExtra(POST_POSITION, position)
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    view,
                    tranName
                ).toBundle()
                activity.startActivity(intent, options)
            } else {
                activity.startActivity(intent)
            }
        }
    }

    private val postDao by instance<PostDao>()
    private val booruApis by instance<BooruApis>()
    private val voteRepository: VoteRepository by lazy { VoteRepositoryImpl(booruApis, postDao) }

    private val binding by viewBinding(ActivityDetailBinding::inflate)
    private val playerHolder by lazy { PlayerHolder() }
    private val detailPager get() = binding.detailPager
    private val toolbar get() = binding.toolbar.toolbarTransparent
    private val toolbarContainer get() = binding.toolbarContainer
    private val shadow get() = binding.shadow
    private val bottomSpace get() = binding.bottomShortcut.spaceNavBar
    private val shortcut get() = binding.bottomShortcut.bottomBarContainer
    private val favButton get() = binding.bottomShortcut.postFav
    private val infoButton get() = binding.bottomShortcut.postInfo
    private val downloadButton get() = binding.bottomShortcut.postDownload
    private val saveButton get() = binding.bottomShortcut.postSave

    private lateinit var booru: Booru
    private lateinit var actionVote: ActionVote
    private var initPosition = POSITION_INIT
    private lateinit var colorDrawable: ColorDrawable
    private lateinit var detailViewModel: DetailViewModel
    private lateinit var detailAdapter: DetailAdapter

    private var tmpFile: File? = null

    private val currentPost: Post?
        get() = detailAdapter.getItemSafe(detailPager.currentItem)

    private var oldPlayerView: PlayerView? = null

    private val playerView: PlayerView?
        get() = detailPager.findViewWithTag(String.format("player_%d", detailPager.currentItem))

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                playerHolder.pause()
            }
        }
        override fun onPageSelected(position: Int) {
            val post = detailAdapter.getItemSafe(position)
            syncInfo(post)
            if (post == null) return
            val intent = Intent(ACTION_DETAIL_POST_POSITION).apply {
                putExtra(POST_QUERY, post.query)
                putExtra(POST_POSITION, position)
            }
            sendBroadcast(intent)
        }
    }

    private fun syncInfo(post: Post?) {
        if (post == null) {
            return
        }
        play(post)
        setVoteItemIcon(post.isFavored)
        toolbar.title = "Post ${post.id}"
    }

    private fun play(post: Post) {
        val playerView = playerView ?: return
        oldPlayerView?.player = null
        oldPlayerView = playerView
        val url = post.origin
        if (url.isVideo()) {
            playerHolder.start(applicationContext, url.toUri(), playerView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val booru = BooruManager.getBooruByUid(activatedBooruUid)
        if (booru == null) {
            finish()
            return
        }
        this.booru = booru
        actionVote = ActionVote(
            booru = booru,
            postId = -1
        )
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition()
        }
        setContentView(binding.root)
        colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.black))
        binding.root.background = colorDrawable
        initInsets()
        initPager()
        initToolbar()
        initShortcutBar()
    }

    private fun initInsets() {
        window.isShowBar = true
        findViewById<View>(android.R.id.content).setOnApplyWindowInsetsListener { _, insets ->
            toolbarContainer.minimumHeight = toolbar.height + insets.systemWindowInsetTop
            toolbarContainer.updatePadding(
                left = insets.systemWindowInsetLeft,
                top = insets.systemWindowInsetTop,
                right = insets.systemWindowInsetRight
            )
            bottomSpace.minimumHeight = insets.systemWindowInsetBottom
            insets
        }
    }

    private fun initPager() {
        val query = intent?.getStringExtra(POST_QUERY) ?: ""
        if (initPosition == POSITION_INIT) {
            initPosition = intent?.getIntExtra(POST_POSITION, POSITION_INITED) ?: POSITION_INITED
        }
        val glide = GlideApp.with(this)
        detailAdapter = DetailAdapter(
            glide = glide,
            dismissListener = this,
            ioExecutor = Dispatchers.IO.asExecutor(),
            clickCallback = { setupBarVisable() },
            longClickCallback = { createLongClickDialog() }
        )
        detailPager.apply {
            adapter = detailAdapter
            registerOnPageChangeCallback(pageChangeCallback)
        }
        detailViewModel = getDetailViewModel(postDao, booru.uid, query)
        detailViewModel.posts.observe(this, Observer { postList ->
            updatePosts(postList)
        })
    }

    private fun setupBarVisable() {
        val isVisible = !toolbarContainer.isVisible
        window.isShowBar = isVisible
        toolbarContainer.isVisible = isVisible
        shortcut.isVisible = isVisible
        shadow.isVisible = isVisible
    }

    private fun updatePosts(postList: PagedList<Post>?) {
        if (postList == null) {
            return
        }
        detailAdapter.submitList(postList)
        if (initPosition != POSITION_INITED) {
            if (initPosition >= 0 && initPosition < postList.size) {
                detailPager.setCurrentItem(initPosition, false)
                delayExecute {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        startPostponedEnterTransition()
                    }
                    syncInfo(currentPost)
                }
            }
            initPosition = POSITION_INITED
        }
    }

    private fun delayExecute(callback: () -> Unit) {
        lifecycleScope.launch {
            delay(100L)
            callback()
        }
    }

    private fun initToolbar() {
        toolbar.inflateMenu(
            when (booru.type) {
                BOORU_TYPE_SANKAKU -> R.menu.detail_sankaku
                BOORU_TYPE_SHIMMIE -> R.menu.detail_shimmie
                else -> R.menu.detail
            }
        )
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setNavigationOnClickListener {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition()
            } else {
                finish()
            }
        }
    }

    private fun initShortcutBar() {
        TooltipCompat.setTooltipText(downloadButton, downloadButton.contentDescription)
        TooltipCompat.setTooltipText(infoButton, infoButton.contentDescription)
        TooltipCompat.setTooltipText(favButton, favButton.contentDescription)
        TooltipCompat.setTooltipText(saveButton, saveButton.contentDescription)
        infoButton.setOnClickListener { createInfoDialog() }
        downloadButton.setOnClickListener {
            currentPost?.let {
                download(it)
            }
        }
        saveButton.setOnClickListener {
            currentPost?.let {
                saveAndAction(it, ACTION_SAVE)
            }
        }
        favButton.setOnClickListener {
            vote()
        }
    }

    private fun createInfoDialog() {
        if (isFinishing) {
            return
        }
        currentPost?.let {
            InfoDialog.create(it.id).show(supportFragmentManager, "info")
        }
    }

    private fun vote() {
        if (booru.type == BOORU_TYPE_SHIMMIE) {
            showToast(getString(R.string.msg_not_supported))
            return
        }
        if (booru.user == null) {
            startActivity(Intent(this, AccountConfigActivity::class.java))
            finish()
            return
        }
        val post = currentPost ?: return
        actionVote.postId = post.id
        lifecycleScope.launch {
            val result = if (post.isFavored) {
                voteRepository.removeFav(actionVote)
            } else {
                voteRepository.addFav(actionVote)
            }
            when {
                result is NetResult.Error -> showToast(result.errorMsg)
                currentPost?.id == post.id -> setVoteItemIcon(!post.isFavored)
            }
        }
    }

    private fun setVoteItemIcon(checked: Boolean) {
        favButton.setImageResource(if (checked) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp)
    }

    override fun onDismissStart() {
        colorDrawable.alpha = ALPHA_MIN
        playerHolder.pause()
    }

    override fun onDismissProgress(progress: Float) {

    }

    override fun onDismissed() {
        playerHolder.pause()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition()
        } else {
            finish()
        }
    }

    override fun onDismissCancel() {
        colorDrawable.alpha = ALPHA_MAX
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val post = currentPost ?: return true
        when (item?.itemId) {
            R.id.action_browse_comment -> {
                currentPost?.let {
                    CommentActivity.startActivity(this, postId = it.id)
                }
            }
            R.id.action_browse_set_as -> saveAndAction(post, ACTION_SET_AS)
            R.id.action_browse_send -> saveAndAction(post, ACTION_SEND)
            R.id.action_browse_share -> shareLink(post)
            R.id.action_browse_recommended -> {
                val query = "recommended_for_post:${post.id}"
                SearchActivity.startSearch(this, query)
            }
            R.id.action_browse_open_browser -> openBrowser(post)
        }
        return true
    }

    private fun download(post: Post) {
        DownloadWorker.downloadPost(post, booru.host, this)
    }

    private fun openBrowser(post: Post) {
        val url = getLink(post)
        if (!url.isNullOrEmpty()) {
            launchUrl(url)
        }
    }

    private fun getLink(post: Post): String? {
        return when (booru.type) {
            BOORU_TYPE_DAN -> String.format("%s://%s/posts/%d", booru.scheme, booru.host, post.id)
            BOORU_TYPE_DAN1 -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host, post.id)
            BOORU_TYPE_MOE -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host, post.id)
            BOORU_TYPE_GEL -> String.format("%s://%s/index.php?page=post&s=view&id=%d", booru.scheme, booru.host, post.id)
            BOORU_TYPE_SANKAKU -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host.replace("capi-v2.", "beta."), post.id)
            BOORU_TYPE_SHIMMIE -> {
                if (booru.path.isNullOrBlank()) {
                    String.format("%s://%s/post/view/%d", booru.scheme, booru.host, post.id)
                } else {
                    String.format("%s://%s/%s/post/view/%d", booru.scheme, booru.host, booru.path, post.id)
                }
            }
            else -> null
        }
    }

    private fun shareLink(post: Post) {
        val url = getLink(post)
        if (!url.isNullOrEmpty()) {
            startActivity(Intent.createChooser(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, url)
                },
                getString(R.string.share_via)
            ))
        }
    }

    private fun createLongClickDialog() {
        val post = currentPost
        if (post == null || isFinishing) {
            return
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("Post ${post.id}")
            .setItems(resources.getTextArray(R.array.detail_item_action)) { _, which ->
                when (which) {
                    0 -> saveAndAction(post, ACTION_SAVE_AS)
                    1 -> download(post)
                    2 -> openBrowser(post)
                }
            }
            .create()
        dialog.show()
    }

    private val mutex = Mutex()

    private fun saveAndAction(post: Post, @IntRange(from = ACTION_SAVE.toLong(), to = ACTION_SEND.toLong()) action: Int) {
        val url = when (detailSize) {
            POST_SIZE_SAMPLE -> post.sample
            POST_SIZE_LARGER -> post.medium
            else -> post.origin
        }
        if (url.isBlank()) return
        lifecycleScope.launch {
            mutex.withLock {
                when (action) {
                    ACTION_SAVE -> saveFile(loadFile(url), url.fileName())
                    ACTION_SAVE_AS -> saveFileAs(loadFile(url), url.fileName())
                    ACTION_SET_AS -> setFileAs(loadFile(url), url.fileName())
                    ACTION_SEND -> sendFile(loadFile(url), url.fileName())
                }
            }
        }
    }

    private suspend fun saveFile(source: File?, fileName: String) {
        if (source == null) {
            return
        }
        val uri = getSaveUri(fileName) ?: return
        if (copyFile(file = source, desUri = uri)) {
            showToast(getString(R.string.msg_file_save_success, DocumentsContract.getDocumentId(uri)))
        }
    }

    private fun saveFileAs(source: File?, fileName: String) {
        if (source == null) {
            return
        }
        tmpFile = source
        val intent = Intent().apply {
            action = Intent.ACTION_CREATE_DOCUMENT
            addCategory(Intent.CATEGORY_OPENABLE)
            type = fileName.getMimeType()
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_SAVE_FILE)
        } catch (_: ActivityNotFoundException) {}
    }

    private suspend fun setFileAs(source: File?, fileName: String) {
        if (source == null) {
            return
        }
        val cacheFile = File(externalCacheDir, fileName)
        val desUri = cacheFile.toUri()
        if (copyFile(source, desUri)) {
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_ATTACH_DATA).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_MIME_TYPES, fileName.getMimeType())
                    data = getUriForFile(cacheFile)
                },
                getString(R.string.share_via)
            ))
        }
    }

    private fun sendFile(source: File?, fileName: String) {
        if (source == null) {
            return
        }
        startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = fileName.getMimeType()
                putExtra(Intent.EXTRA_STREAM, getUriForFile(source))
            },
            getString(R.string.share_via)
        ))
    }

    private suspend fun loadFile(url: String): File? =
        withContext(Dispatchers.IO) {
            try {
                GlideApp.with(this@DetailActivity)
                    .downloadOnly()
                    .load(url)
                    .submit()
                    .get()
            } catch (_: Exception) {
                null
            }
        }

    private suspend fun copyFile(file: File, desUri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            var inputStream: InputStream? = null
            var outputSteam: OutputStream? = null
            try {
                inputStream = FileInputStream(file)
                outputSteam = contentResolver.openOutputStream(desUri)
                inputStream.copyTo(outputSteam)
                true
            } catch (_: IOException) {
                false
            } finally {
                inputStream?.safeCloseQuietly()
                outputSteam?.safeCloseQuietly()
            }
        }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M && playerHolder.playerIsNull()) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            releasePlayer()
        }
    }

    private fun initPlayer() {
        playerHolder.create(applicationContext)
        currentPost?.origin?.let { url ->
            if (url.isVideo()) {
                playerView?.apply {
                    playerHolder.start(applicationContext, url.toUri(), this)
                }
            }
        }
    }

    private fun releasePlayer() {
        playerView?.apply {
            onPause()
            player = null
        }
        playerHolder.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            tmpFile = null
            return
        }
        if (requestCode == REQUEST_CODE_SAVE_FILE) {
            val uri = data.data ?: return
            val file = tmpFile ?: return
            lifecycleScope.launch {
                if (copyFile(file, uri)) {
                    showToast(getString(R.string.msg_file_save_success, DocumentsContract.getDocumentId(uri)))
                }
                tmpFile = null
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}