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

package onlymash.flexbooru.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Keys.POST_POSITION
import onlymash.flexbooru.app.Keys.POST_QUERY
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.app.Settings.POST_SIZE_LARGER
import onlymash.flexbooru.app.Settings.POST_SIZE_SAMPLE
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.app.Settings.detailSize
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
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
import onlymash.flexbooru.ui.adapter.DetailAdapter
import onlymash.flexbooru.ui.base.PathActivity
import onlymash.flexbooru.ui.fragment.InfoDialog
import onlymash.flexbooru.ui.helper.CreateFileLifecycleObserver
import onlymash.flexbooru.ui.viewbinding.viewBinding
import onlymash.flexbooru.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ui.viewmodel.getDetailViewModel
import onlymash.flexbooru.widget.DismissFrameLayout
import onlymash.flexbooru.worker.DownloadWorker
import org.kodein.di.instance
import java.io.*

private const val ALPHA_MAX = 0xFF
private const val ALPHA_MIN = 0x00

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
    private lateinit var query: String
    private lateinit var actionVote: ActionVote
    private lateinit var colorDrawable: ColorDrawable
    private lateinit var detailViewModel: DetailViewModel
    private lateinit var detailAdapter: DetailAdapter
    private lateinit var createFileObserver: CreateFileLifecycleObserver

    private var tmpFile: File? = null

    private val currentPost: Post?
        get() = detailAdapter.getPost(detailPager.currentItem)

    private var oldPlayerView: StyledPlayerView? = null

    private val playerView: StyledPlayerView?
        get() = detailPager.findViewWithTag(String.format("player_%d", detailPager.currentItem))

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                playerHolder.pause()
            }
        }
        override fun onPageSelected(position: Int) {
            detailViewModel.currentPosition = position
            var post = detailAdapter.getPost(position)
            if (post == null) {
                post = postDao.getPost(booruUid = booru.uid, query = query, index = position)
                if (post != null) {
                    syncInfo(post, position)
                }
            } else syncInfo(post, position)
        }
    }

    private fun syncInfo(post: Post, position: Int) {
        setVoteItemIcon(post.isFavored)
        toolbar.title = "Post ${post.id}"
        val intent = Intent(ACTION_DETAIL_POST_POSITION).apply {
            putExtra(POST_QUERY, post.query)
            putExtra(POST_POSITION, position)
        }
        sendBroadcast(intent)
        if (post.origin.isVideo()) {
            playVideo(post.origin)
        }
    }

    private fun playVideo(url: String) {
        val playerView = playerView
        if (playerView == null) {
            delayExecute {
                playVideo(url)
            }
            return
        }
        oldPlayerView?.player = null
        oldPlayerView = playerView
        playerHolder.start(applicationContext, url.toUri(), playerView)
    }

    private fun delayExecute(callback: () -> Unit) {
        lifecycleScope.launch {
            delay(200L)
            callback()
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
        createFileObserver = CreateFileLifecycleObserver(activityResultRegistry){ uri ->
            saveFile(uri)
        }
        lifecycle.addObserver(createFileObserver)
        initInsets()
        initPager()
        initToolbar()
        initShortcutBar()
    }

    private fun saveFile(uri: Uri) {
        val file = tmpFile ?: return
        lifecycleScope.launch {
            if (copyFile(file, uri)) {
                showToast(getString(R.string.msg_file_save_success, DocumentsContract.getDocumentId(uri)))
            }
            tmpFile = null
        }
    }

    private fun initInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.isShowBar = true
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            toolbarContainer.minimumHeight = toolbar.height + systemBarsInsets.top
            toolbarContainer.updatePadding(
                left = systemBarsInsets.left,
                top = systemBarsInsets.top,
                right = systemBarsInsets.right
            )
            bottomSpace.minimumHeight = systemBarsInsets.bottom
            insets
        }
    }

    private fun initPager() {
        query = intent?.getStringExtra(POST_QUERY) ?: ""
        detailViewModel = getDetailViewModel(postDao, booru.uid, query)
        if (detailViewModel.currentPosition < 0) {
            detailViewModel.currentPosition = intent?.getIntExtra(POST_POSITION, -1) ?: -1
        }
        val glide = Glide.with(this)
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
        detailAdapter.addLoadStateListener {
            if (it.refresh is LoadState.NotLoading) {
                val currentPosition = detailViewModel.currentPosition
                if (detailPager.currentItem != currentPosition && currentPosition in 0 until detailAdapter.itemCount) {
                    detailPager.setCurrentItem(currentPosition, false)
                }
                startPostponedEnterTransition()
            }
        }
        lifecycleScope.launch {
            detailViewModel.posts.collectLatest {
                detailAdapter.submitData(it)
            }
        }
    }

    private fun setupBarVisable() {
        val isVisible = !toolbarContainer.isVisible
        window.isShowBar = isVisible
        toolbarContainer.isVisible = isVisible
        shortcut.isVisible = isVisible
        shadow.isVisible = isVisible
    }

    private fun initToolbar() {
        toolbar.inflateMenu(
            when (booru.type) {
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
        if (!Settings.isOrderSuccess) {
            val adView = AdView(this)
            binding.bottomShortcut.bottomBarContainer.addView(adView, 0, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            })
            var adWidth = getScreenWidthDp()
            if (adWidth > 500) {
                adWidth = 500
            }
            adView.apply {
                visibility = View.VISIBLE
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this@DetailActivity, adWidth))
                adUnitId = "ca-app-pub-1547571472841615/1729907816"
                loadAd(AdRequest.Builder().build())
            }
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
            in arrayOf(BOORU_TYPE_GEL, BOORU_TYPE_GEL_LEGACY) -> String.format("%s://%s/index.php?page=post&s=view&id=%d", booru.scheme, booru.host, post.id)
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
        createFileObserver.createDocument(fileName)
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
                Glide.with(this@DetailActivity)
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

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private var Window.isShowBar: Boolean
        get() = isStatusBarShown
        set(value) {
            if (value) {
                showSystemBars()
            } else {
                hideSystemBars()
            }
        }
}