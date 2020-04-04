package onlymash.flexbooru.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ui.PlayerView

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.bottom_shortcut_bar.*
import kotlinx.android.synthetic.main.toolbar_transparent.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Keys.POST_POSITION
import onlymash.flexbooru.common.Keys.POST_QUERY
import onlymash.flexbooru.common.Settings.POST_SIZE_LARGER
import onlymash.flexbooru.common.Settings.POST_SIZE_SAMPLE
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.common.Settings.detailSize
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.common.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.data.action.ActionVote
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.repository.favorite.VoteRepository
import onlymash.flexbooru.data.repository.favorite.VoteRepositoryImpl
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.extension.*
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.adapter.DetailAdapter
import onlymash.flexbooru.ui.fragment.ShortcutInfoFragment
import onlymash.flexbooru.ui.fragment.ShortcutTagFragment
import onlymash.flexbooru.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ui.viewmodel.getDetailViewModel
import onlymash.flexbooru.widget.DismissFrameLayout
import onlymash.flexbooru.worker.DownloadWorker
import org.kodein.di.erased.instance
import java.io.*
import java.util.concurrent.Executor

private const val ALPHA_MAX = 0xFF
private const val ALPHA_MIN = 0x00
private const val POSITION_INIT = -1
private const val POSITION_INITED = -2

private const val ACTION_SAVE = 101
private const val ACTION_SET_AS = 102
private const val ACTION_SEND = 103

class DetailActivity : BaseActivity(), DismissFrameLayout.OnDismissListener, Toolbar.OnMenuItemClickListener {

    companion object {
        const val ACTION_DETAIL_POST_POSITION = "detail_post_position"

        fun start(activity: Activity, query: String?, position: Int, view: View, tranName: String) {
            val intent = Intent(activity, DetailActivity::class.java).apply {
                putExtra(POST_QUERY, query)
                putExtra(POST_POSITION, position)
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                view,
                tranName
            ).toBundle()
            activity.startActivity(intent, options)
        }
    }

    private val postDao by instance<PostDao>()
    private val ioExecutor by instance<Executor>()
    private val booruApis by instance<BooruApis>()
    private val voteRepository: VoteRepository by lazy { VoteRepositoryImpl(booruApis, postDao) }

    private val playerHolder by lazy { PlayerHolder(this) }

    private lateinit var booru: Booru
    private lateinit var actionVote: ActionVote
    private var initPosition = POSITION_INIT
    private lateinit var colorDrawable: ColorDrawable
    private lateinit var detailViewModel: DetailViewModel
    private lateinit var detailAdapter: DetailAdapter

    private val currentPost: Post?
        get() = detailAdapter.getPost(detail_pager.currentItem)

    private var currentPlayerView: PlayerView? = null

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            currentPlayerView?.onPause()
        }
        override fun onPageSelected(position: Int) {
            val post = detailAdapter.getPost(position)
            syncInfo(post)
            if (post == null) return
            play(post)
            val intent = Intent(ACTION_DETAIL_POST_POSITION).apply {
                putExtra(POST_QUERY, post.query)
                putExtra(POST_POSITION, position)
            }
            sendBroadcast(intent)
        }
    }

    private fun syncInfo(post: Post?) {
        if (post == null) {
            currentPlayerView = null
            return
        }
        actionVote.postId = post.id
        setVoteItemIcon(post.isFavored)
        toolbar_transparent.title = "Post ${post.id}"
        play(post)
    }

    private fun play(post: Post) {
        val url = post.origin
        if (url.isNotEmpty() && !url.isImage()) {
            playerHolder.stop()
            currentPlayerView = getPlayerView(post)
            currentPlayerView?.let { playerView ->
                playerHolder.start(url.toUri(), playerView)
            }
        } else {
            currentPlayerView?.onPause()
            currentPlayerView = null
        }
    }

    private fun getPlayerView(post: Post): PlayerView? {
        return detail_pager.findViewWithTag(String.format("player_%d", post.id))
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
        postponeEnterTransition()
        setContentView(R.layout.activity_detail)
        initInsets()
        initPager()
        initToolbar()
        initShortcutBar()
    }

    private fun initInsets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.showBar()
        findViewById<View>(android.R.id.content).setOnApplyWindowInsetsListener { _, insets ->
            toolbar_container.minimumHeight = toolbar_transparent.height + insets.systemWindowInsetTop
            toolbar_container.updatePadding(
                left = insets.systemWindowInsetLeft,
                top = insets.systemWindowInsetTop,
                right = insets.systemWindowInsetRight
            )
            space_nav_bar.minimumHeight = insets.systemWindowInsetBottom
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
            picasso = Picasso.Builder(this).build(),
            dismissListener = this,
            ioExecutor = ioExecutor
        ) {
            if (toolbar_container.isVisible) {
                window.hideBar()
                toolbar_container.isVisible = false
                bottom_bar_container.isVisible = false
                shadow.isVisible = false
            } else {
                window.showBar()
                toolbar_container.isVisible = true
                bottom_bar_container.isVisible = true
                shadow.isVisible = true
            }
        }
        colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.black))
        detail_pager.apply {
            background = colorDrawable
            adapter = detailAdapter
            registerOnPageChangeCallback(pageChangeCallback)
        }
        detailViewModel = getDetailViewModel(postDao, booru.uid, query)
        detailViewModel.posts.observe(this, Observer {
            detailAdapter.submitList(it)
            if (initPosition != POSITION_INITED) {
                if (detailAdapter.itemCount > 0) {
                    detail_pager.setCurrentItem(initPosition, false)
                    lifecycleScope.launch {
                        delay(100)
                        startPostponedEnterTransition()
                        syncInfo(currentPost)
                    }
                }
                initPosition = POSITION_INITED
            }
        })
    }

    private fun initToolbar() {
        toolbar_transparent.inflateMenu(
            when (booru.type) {
                BOORU_TYPE_SANKAKU -> R.menu.detail_sankaku
                BOORU_TYPE_SHIMMIE -> R.menu.detail_shimmie
                else -> R.menu.detail
            }
        )
        toolbar_transparent.setOnMenuItemClickListener(this)
        toolbar_transparent.setNavigationOnClickListener {
            finishAfterTransition()
        }
    }

    private fun initShortcutBar() {
        TooltipCompat.setTooltipText(post_tags, post_tags.contentDescription)
        TooltipCompat.setTooltipText(post_info, post_info.contentDescription)
        TooltipCompat.setTooltipText(post_fav, post_fav.contentDescription)
        TooltipCompat.setTooltipText(post_save, post_save.contentDescription)
        post_tags.setOnClickListener {
            currentPost?.let {
                ShortcutTagFragment.create(it.id)
                    .show(supportFragmentManager, "tag")
            }
        }
        post_info.setOnClickListener {
            currentPost?.let {
                ShortcutInfoFragment.create(it.id)
                    .show(supportFragmentManager, "info")
            }
        }
        post_save.setOnClickListener {
            saveAndAction(ACTION_SAVE)
        }
        post_fav.setOnClickListener {
            if (booru.type == BOORU_TYPE_SHIMMIE) {
                Toast.makeText(this, getString(R.string.msg_not_supported), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (booru.user == null) {
                startActivity(Intent(this, AccountConfigActivity::class.java))
                finish()
                return@setOnClickListener
            }
            currentPost?.let {
                if (it.isFavored) {
                    lifecycleScope.launch {
                        val result = voteRepository.removeFav(actionVote)
                        if (result is NetResult.Error) {
                            Toast.makeText(this@DetailActivity, result.errorMsg, Toast.LENGTH_SHORT).show()
                        }
                        delay(100)
                        setVoteItemIcon(currentPost?.isFavored == true)
                    }
                } else {
                    lifecycleScope.launch {
                        val result = voteRepository.addFav(actionVote)
                        if (result is NetResult.Error) {
                            Toast.makeText(this@DetailActivity, result.errorMsg, Toast.LENGTH_SHORT).show()
                        }
                        delay(100)
                        setVoteItemIcon(currentPost?.isFavored == true)
                    }
                }
            }
        }
    }

    private fun setVoteItemIcon(checked: Boolean) {
        post_fav.setImageResource(if (checked) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp)
    }

    override fun onDismissStart() {
        colorDrawable.alpha = ALPHA_MIN
    }

    override fun onDismissProgress(progress: Float) {

    }

    override fun onDismissed() {
        finishAfterTransition()
    }

    override fun onDismissCancel() {
        colorDrawable.alpha = ALPHA_MAX
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_browse_comment -> {
                currentPost?.let {
                    CommentActivity.startActivity(this, postId = it.id)
                }
            }
            R.id.action_browse_download -> {
                DownloadWorker.downloadPost(currentPost, booru.host, this)
            }
            R.id.action_browse_set_as -> saveAndAction(ACTION_SET_AS)
            R.id.action_browse_send -> saveAndAction(ACTION_SEND)
            R.id.action_browse_share -> shareLink()
            R.id.action_browse_recommended -> {
                val id = currentPost?.id ?: return true
                if (id > 0) {
                    SearchActivity.startSearch(
                        this,
                        "recommended_for_post:$id"
                    )
                }
            }
            R.id.action_browse_open_browser -> {
                val url = getLink()
                if (!url.isNullOrEmpty()) {
                    launchUrl(url)
                }
            }
        }
        return true
    }

    private fun getLink(): String? {
        val id = currentPost?.id ?: return null
        return when (booru.type) {
            BOORU_TYPE_DAN -> String.format("%s://%s/posts/%d", booru.scheme, booru.host, id)
            BOORU_TYPE_DAN1 -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host, id)
            BOORU_TYPE_MOE -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host, id)
            BOORU_TYPE_GEL -> String.format("%s://%s/index.php?page=post&s=view&id=%d", booru.scheme, booru.host, id)
            BOORU_TYPE_SANKAKU -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host.replace("capi-v2.", "beta."), id)
            BOORU_TYPE_SHIMMIE -> {
                if (booru.path.isNullOrBlank()) {
                    String.format("%s://%s/post/view/%d", booru.scheme, booru.host, id)
                } else {
                    String.format("%s://%s/%s/post/view/%d", booru.scheme, booru.host, booru.path, id)
                }
            }
            else -> null
        }
    }

    private fun shareLink() {
        val url = getLink()
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

    private val mutex = Mutex()

    private fun saveAndAction(@IntRange(from = ACTION_SAVE.toLong(), to = ACTION_SEND.toLong()) action: Int) {
        lifecycleScope.launch {
            mutex.withLock {
                val post = currentPost ?: return@launch
                val url = when (detailSize) {
                    POST_SIZE_SAMPLE -> post.sample
                    POST_SIZE_LARGER -> post.medium
                    else -> post.origin
                }
                if (url.isBlank()) return@launch
                val file = loadFile(url) ?: return@launch
                val fileName = url.fileName()
                when (action) {
                    ACTION_SAVE -> {
                        val uri = getSaveUri(fileName) ?: return@launch
                        if (copyFile(file, uri)) {
                            Toast.makeText(this@DetailActivity,
                                getString(R.string.msg_file_save_success, DocumentsContract.getDocumentId(uri)),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    ACTION_SET_AS -> {
                        val cacheFile = File(externalCacheDir, fileName)
                        val desUri = cacheFile.toUri()
                        if (copyFile(file, desUri)) {
                            this@DetailActivity.startActivity(Intent.createChooser(
                                Intent(Intent.ACTION_ATTACH_DATA).apply {
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    putExtra(Intent.EXTRA_MIME_TYPES, fileName.getMimeType())
                                    data = getUriForFile(cacheFile)
                                },
                                getString(R.string.share_via)
                            ))
                        }
                    }
                    ACTION_SEND -> {
                        this@DetailActivity.startActivity(Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                type = fileName.getMimeType()
                                putExtra(Intent.EXTRA_STREAM, getUriForFile(file))
                            },
                            getString(R.string.share_via)
                        ))
                    }
                }
            }
        }
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

    private suspend fun copyFile(file: File, desUri: Uri): Boolean = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var outputSteam: OutputStream? = null
        try {
            inputStream = FileInputStream(file)
            outputSteam = contentResolver.openOutputStream(desUri)
            inputStream.copyTo(outputSteam)
            return@withContext true
        } catch (_: IOException) {
            return@withContext false
        } finally {
            inputStream?.safeCloseQuietly()
            outputSteam?.safeCloseQuietly()
        }
    }

    override fun onStop() {
        super.onStop()
        playerHolder.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHolder.release()
    }
}