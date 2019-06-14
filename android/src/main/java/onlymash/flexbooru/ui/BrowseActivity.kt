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

package onlymash.flexbooru.ui

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.*
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_browse.*
import kotlinx.android.synthetic.main.bottom_shortcut_bar.*
import kotlinx.android.synthetic.main.toolbar_transparent.*
import kotlinx.coroutines.*
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.api.*
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.entity.Vote
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.extension.*
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.repository.browse.PostLoaderRepository
import onlymash.flexbooru.repository.browse.PostLoaderRepositoryImpl
import onlymash.flexbooru.repository.favorite.VoteRepositoryImpl
import onlymash.flexbooru.ui.adapter.BrowsePagerAdapter
import onlymash.flexbooru.ui.fragment.InfoBottomSheetDialog
import onlymash.flexbooru.ui.fragment.TagBottomSheetDialog
import onlymash.flexbooru.ui.viewmodel.FavPostViewModel
import onlymash.flexbooru.widget.DismissFrameLayout
import onlymash.flexbooru.worker.DownloadWorker
import org.kodein.di.erased.instance
import java.io.*
import java.util.concurrent.Executor

class BrowseActivity : BaseActivity() {

    companion object {
        private const val TAG = "BrowseActivity"
        const val ACTION_NORMAL = "current_browse_normal_id"
        const val ACTION_POPULAR = "current_browse_popular_id"
        const val EXT_POST_ID_KEY = "post_id"
        const val EXT_POST_POSITION_KEY = "post_position"
        const val EXT_POST_KEYWORD_KEY = "post_keyword"
        private const val PAGER_CURRENT_POSITION_KEY = "current_position"
        private const val ACTION_DOWNLOAD = 0
        private const val ACTION_SAVE = 1
        private const val ACTION_SAVE_SET_AS = 2
        private const val ACTION_SAVE_SEND = 3
        private const val ALPHA_MAX = 0xFF
        private const val ALPHA_MIN = 0x00
        fun startActivity(activity: Activity,
                          view: View,
                          postId: Int,
                          keyword: String,
                          pageType: Int) {
            val intent = Intent(activity, BrowseActivity::class.java)
                .apply {
                    putExtra(Constants.ID_KEY, postId)
                    putExtra(Constants.KEYWORD_KEY, keyword)
                    putExtra(Constants.PAGE_TYPE_KEY, pageType)
                }
            val tranName = when (pageType) {
                Constants.PAGE_TYPE_POST -> activity.getString(R.string.post_transition_name, postId)
                Constants.PAGE_TYPE_POPULAR -> activity.getString(R.string.post_popular_transition_name, postId)
                else -> throw IllegalStateException("unknown post type $pageType")
            }
            val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, view, tranName)
            activity.startActivity(intent, options.toBundle())
        }
    }

    private val danApi: DanbooruApi by instance()
    private val danOneApi: DanbooruOneApi by instance()
    private val moeApi: MoebooruApi by instance()
    private val sankakuApi: SankakuApi by instance()
    private val gelApi: GelbooruApi by instance()
    private val db: FlexbooruDatabase by instance()
    private val ioExecutor: Executor by instance()

    private var posts: MutableList<PostBase>? = null
    private var postsFav: MutableList<PostBase>? = null

    private lateinit var booru: Booru
    private var pageType = Constants.PAGE_TYPE_POST
    private var startId = -1
    private var keyword = ""
    private var user: User? = null
    private var currentPosition = -1
    private var canTransition = true
    private val postLoader by lazy { PostLoaderRepositoryImpl(db) }

    @Suppress("UNCHECKED_CAST")
    private fun handleResult(data: MutableList<PostBase>) {
        posts = data
        initItemsLoaded(data)
    }

    private fun initItemsLoaded(posts: MutableList<PostBase>) {
        var url = ""
        var position = 0
        if (startId >= 0) {
            posts.forEachIndexed { index, postMoe ->
                if (postMoe.getPostId() == startId) {
                    position = index
                    url = postMoe.getSampleUrl()
                    return@forEachIndexed
                }
            }
        }
        toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), posts[position].getPostId())
        pagerAdapter.updateData(posts)
        pager_browse.adapter = pagerAdapter
        pager_browse.setCurrentItem(if (currentPosition >= 0) currentPosition else position, false)
        if (canTransition) startPostponedEnterTransition()
        if (url.isNotEmpty() && !url.isImage()) {
            Handler().postDelayed({
                val playerView: Any? = pager_browse.findViewWithTag(String.format("player_%d", position))
                if (playerView is PlayerView) {
                    currentPlayerView = playerView
                    val uri = Uri.parse(url)
                    currentVideoUri = uri
                    playerHolder.start(uri = uri, playerView = playerView)
                }
            }, 300)
        }
    }

    private val playerHolder: PlayerHolder by lazy { PlayerHolder(this) }
    private var currentPlayerView: PlayerView? = null
    private var currentVideoUri: Uri? = null

    private lateinit var pagerAdapter: BrowsePagerAdapter

    private val pagerChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            currentPlayerView?.onPause()
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

        }

        override fun onPageSelected(position: Int) {
            var url = ""
            var id = -1
            posts?.get(position)?.let {
                url = it.getSampleUrl()
                id = it.getPostId()
            }
            if (id > 0) toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), id)
            val action = if (pageType == Constants.PAGE_TYPE_POST)
                ACTION_NORMAL
            else
                ACTION_POPULAR
            val intent = Intent(action).apply {
                putExtra(EXT_POST_ID_KEY, id)
                putExtra(EXT_POST_POSITION_KEY, position)
                putExtra(EXT_POST_KEYWORD_KEY, keyword)
            }
            this@BrowseActivity.sendBroadcast(intent)
            if (url.isNotEmpty() && !url.isImage()) {
                val playerView: Any? = pager_browse.findViewWithTag(String.format("player_%d", position))
                if (playerView is PlayerView) {
                    currentPlayerView = playerView
                    val uri = Uri.parse(url)
                    currentVideoUri = uri
                    playerHolder.start(uri = uri, playerView = playerView)
                }
            } else {
                currentPlayerView = null
            }
            setCurrentVoteItemIcon()
        }
    }

    private val photoViewListener = object : BrowsePagerAdapter.PhotoViewListener {
        override fun onClickPhotoView() {
            setBg()
        }
    }

    private val sharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
            val pos = pager_browse.currentItem
            val sharedElement = pager_browse.findViewWithTag<ViewGroup>(pos)?.getChildAt(0)
            if (sharedElement == null) {
                canTransition = false
                return
            } else {
                canTransition = true
                val name = sharedElement.transitionName
                names.clear()
                names.add(name)
                sharedElements.clear()
                sharedElements[name] = sharedElement
            }
        }
    }

    private lateinit var colorDrawable: ColorDrawable

    private val onDismissListener = object : DismissFrameLayout.OnDismissListener {
        override fun onStart() {
            colorDrawable.alpha = ALPHA_MIN
        }

        override fun onProgress(progress: Float) {

        }

        override fun onDismiss() {
            finishAfterTransition()
        }

        override fun onCancel() {
            colorDrawable.alpha = ALPHA_MAX
        }
    }

    private val voteRepository by lazy {
        VoteRepositoryImpl(
            danbooruApi = danApi,
            danbooruOneApi = danOneApi,
            moebooruApi = moeApi,
            sankakuApi = sankakuApi,
            gelbooruApi = gelApi,
            db = db
        )
    }

    private lateinit var favPostViewModel: FavPostViewModel

    private fun getCurrentPostFav(): PostBase? {
        val position = pager_browse.currentItem
        var post: PostBase? = null
        val id = posts?.get(position)?.getPostId() ?: return null
        postsFav?.forEach {
            if (it.getPostId() == id) {
                post = it
                return@forEach
            }
        }
        return post
    }

    private fun getCurrentPost(): PostBase? {
        return posts?.get(pager_browse.currentItem)
    }

    private fun getCurrentPostId(): Int = getCurrentPost()?.getPostId() ?: -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.showBar()
        setContentView(R.layout.activity_browse)
        pageType = intent?.getIntExtra(Constants.PAGE_TYPE_KEY, Constants.PAGE_TYPE_POST) ?: Constants.PAGE_TYPE_POST
        colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.black))
        pager_browse.background = colorDrawable
        postponeEnterTransition()
        setEnterSharedElementCallback(sharedElementCallback)
        booru = BooruManager.getBooruByUid(Settings.activeBooruUid) ?: return
        toolbar.setTitle(R.string.browse_toolbar_title)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar.inflateMenu(if (booru.type == Constants.TYPE_SANKAKU) R.menu.browse_sankaku else R.menu.browse)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_browse_comment -> {
                    val id = getCurrentPostId()
                    if (id > 0) CommentActivity.startActivity(context = this, postId = id)
                }
                R.id.action_browse_download -> {
                    checkAndAction(ACTION_DOWNLOAD)
                }
                R.id.action_browse_set_as -> {
                    checkAndAction(ACTION_SAVE_SET_AS)
                }
                R.id.action_browse_send -> {
                    checkAndAction(ACTION_SAVE_SEND)
                }
                R.id.action_browse_share -> {
                    shareLink()
                }
                R.id.action_browse_recommended -> {
                    val id = getCurrentPostId()
                    if (id > 0) {
                        SearchActivity.startActivity(this, "recommended_for_post:$id")
                    }
                }
                R.id.action_browse_open_browser -> {
                    val url = getLink()
                    if (!url.isNullOrEmpty()) {
                        launchUrl(url)
                    }
                }
            }
            return@setOnMenuItemClickListener true
        }
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { _, insets ->
            toolbar_container.minimumHeight = toolbar.height + insets.systemWindowInsetTop
            toolbar_container.setPadding(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                0
            )
            space_nav_bar.minimumHeight = insets.systemWindowInsetBottom
            insets
        }
        keyword = intent.getStringExtra(Constants.KEYWORD_KEY) ?: ""
        startId = intent.getIntExtra(Constants.ID_KEY, -1)
        user = UserManager.getUserByBooruUid(booruUid = booru.uid)
        user?.let {
            initFavViewModel()
        }
        pagerAdapter = BrowsePagerAdapter(
            glideRequests = GlideApp.with(this),
            picasso = Picasso.Builder(this).build(),
            onDismissListener = onDismissListener,
            pageType = pageType,
            ioExecutor = ioExecutor)
        pagerAdapter.setPhotoViewListener(photoViewListener)
        pager_browse.addOnPageChangeListener(pagerChangeListener)
        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                postLoader.loadPosts(
                    host = booru.host,
                    keyword = keyword,
                    type = booru.type
                )
            }
            handleResult(data)
        }
        initBottomBar()
    }

    private fun startAccountConfigAndFinish() {
        startActivity(Intent(this, AccountConfigActivity::class.java))
        finish()
    }

    private fun initBottomBar() {
        post_tags.setOnClickListener {
            TagBottomSheetDialog.create(getCurrentPost()).show(supportFragmentManager, "tags")
        }
        post_info.setOnClickListener {
            InfoBottomSheetDialog.create(getCurrentPost()).show(supportFragmentManager, "info")
        }
        post_fav.setOnClickListener {
            val post = getCurrentPost() ?: return@setOnClickListener
            user?.let { user ->
                when (post) {
                    is PostDan -> {
                        val vote = Vote(
                            scheme = booru.scheme,
                            host = booru.host,
                            post_id = post.id,
                            username = user.name,
                            auth_key = user.api_key ?: return@let)
                        val postFav = getCurrentPostFav()
                        if (postFav is PostDan) {
                            lifecycleScope.launch {
                                val result = voteRepository.removeDanFav(vote, postFav)
                                if (result is NetResult.Error) {
                                    Toast.makeText(this@BrowseActivity, result.errorMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            lifecycleScope.launch {
                                val result = voteRepository.addDanFav(vote, post)
                                if (result is NetResult.Error) {
                                    Toast.makeText(this@BrowseActivity, result.errorMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    is PostMoe -> {
                        val vote = when (getCurrentPostFav()) {
                            is PostMoe -> {
                                Vote(
                                    scheme = booru.scheme,
                                    host = booru.host,
                                    score = 0,
                                    post_id = post.id,
                                    username = user.name,
                                    auth_key = user.password_hash ?: return@let)
                            }
                            else -> {
                                Vote(
                                    scheme = booru.scheme,
                                    host = booru.host,
                                    score = 3,
                                    post_id = post.id,
                                    username = user.name,
                                    auth_key = user.password_hash ?: return@let)
                            }
                        }
                        lifecycleScope.launch {
                            val result = voteRepository.voteMoePost(vote)
                            if (result is NetResult.Error) {
                                Toast.makeText(this@BrowseActivity, result.errorMsg, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    is PostDanOne -> {
                        val postFav = getCurrentPostFav()
                        val vote = Vote(
                            scheme = booru.scheme,
                            host = booru.host,
                            post_id = post.id,
                            username = user.name,
                            auth_key = user.password_hash ?: return@let)
                        if (postFav is PostDanOne) {
                            lifecycleScope.launch {
                                val result = voteRepository.removeDanOneFav(vote, postFav)
                                if (result is NetResult.Error) {
                                    Toast.makeText(this@BrowseActivity, result.errorMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            lifecycleScope.launch {
                                val result = voteRepository.addDanOneFav(vote, post)
                                if (result is NetResult.Error) {
                                    Toast.makeText(this@BrowseActivity, result.errorMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    is PostSankaku -> {
                        val postFav = getCurrentPostFav()
                        val vote = Vote(
                            scheme = booru.scheme,
                            host = booru.host,
                            post_id = post.id,
                            username = user.name,
                            auth_key = user.password_hash ?: return@let)
                        if (postFav is PostSankaku) {
                            lifecycleScope.launch {
                                val result = voteRepository.removeSankakuFav(vote, postFav)
                                if (result is NetResult.Error) {
                                    Toast.makeText(this@BrowseActivity, result.errorMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            lifecycleScope.launch {
                                val result = voteRepository.addSankakuFav(vote, post)
                                if (result is NetResult.Error) {
                                    Toast.makeText(this@BrowseActivity, result.errorMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    is PostGel -> {
                        val vote = Vote(
                            scheme = booru.scheme,
                            host = booru.host,
                            post_id = post.getPostId(),
                            username = user.name,
                            auth_key = user.password_hash ?: ""
                        )
                        lifecycleScope.launch {
                            when (val result = voteRepository.addGelFav(vote, post)) {
                                is NetResult.Success -> {
                                    Toast.makeText(this@BrowseActivity, "Success", Toast.LENGTH_SHORT).show()
                                }
                                is NetResult.Error -> {
                                    Toast.makeText(this@BrowseActivity, "Error: ${result.errorMsg}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            } ?: startAccountConfigAndFinish()
        }
        post_save.setOnClickListener {
            checkAndAction(ACTION_SAVE)
        }
        TooltipCompat.setTooltipText(post_tags, post_tags.contentDescription)
        TooltipCompat.setTooltipText(post_info, post_info.contentDescription)
        TooltipCompat.setTooltipText(post_fav, post_fav.contentDescription)
        TooltipCompat.setTooltipText(post_save, post_save.contentDescription)
        if (!Settings.isOrderSuccess) {
            val adBuilder = AdRequest.Builder().addTestDevice("10776CDFD3CAEC0AA6A8349F4298F209")
            val adView = AdView(this)
            bottom_bar_container.addView(adView, 1, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            adView.apply {
                visibility = View.VISIBLE
                adSize = AdSize.SMART_BANNER
                adUnitId = "ca-app-pub-1547571472841615/1729907816"
                loadAd(adBuilder.build())
            }
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

    private fun getLink(): String? = when (val post = getCurrentPost()) {
        is PostDan -> String.format("%s://%s/posts/%d", booru.scheme, booru.host, post.id)
        is PostDanOne -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host, post.id)
        is PostMoe -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host, post.id)
        is PostGel -> String.format("%s://%s/index.php?page=post&s=view&id=%d", booru.scheme, booru.host, post.id)
        is PostSankaku -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host.replace("capi-v2.", "beta."), post.id)
        else -> null
    }

    private fun initFavViewModel() {
        val u = user ?: return
        val type = booru.type
        favPostViewModel = getFavPostViewModel(postLoader)
        favPostViewModel.load(
            host = booru.host,
            keyword = when (type) {
                Constants.TYPE_MOEBOORU -> "vote:3:${u.name} order:vote"
                else -> "fav:${u.name}"
            },
            type = type
        ).observe(this, Observer {
            postsFav = it
            setCurrentVoteItemIcon()
        })
    }

    private fun setCurrentVoteItemIcon() {
        val post = getCurrentPost() ?: return
        val index = postsFav?.indexOfFirst { post.getPostId() == it.getPostId() } ?: return
        setVoteItemIcon(index >= 0)
    }

    private fun setVoteItemIcon(checked: Boolean) {
        post_fav.setImageResource(if (checked) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(PAGER_CURRENT_POSITION_KEY, pager_browse.currentItem)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        currentPosition = savedInstanceState.getInt(PAGER_CURRENT_POSITION_KEY, -1)
        super.onRestoreInstanceState(savedInstanceState)
        if (currentPosition >= 0) pager_browse.setCurrentItem(currentPosition, false)
    }

    private fun saveAndAction(action: Int) {
        val position = pager_browse.currentItem
        val url = when (Settings.browseSize) {
            Settings.POST_SIZE_SAMPLE -> posts?.get(position)?.getSampleUrl()
            Settings.POST_SIZE_LARGER -> posts?.get(position)?.getLargerUrl()
            else -> posts?.get(position)?.getOriginUrl()
        }
        if (url.isNullOrEmpty()) return
        GlideApp.with(this)
            .downloadOnly()
            .load(url)
            .into(object : CustomTarget<File>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    val fileName = url.fileName()
                    val uri = getSaveUri(fileName) ?: return
                    lifecycleScope.launch {
                        val success = withContext(Dispatchers.IO) {
                            var `is`: InputStream? = null
                            var os: OutputStream? = null
                            try {
                                `is` = FileInputStream(resource)
                                os = contentResolver.openOutputStream(uri)
                                `is`.copyToOS(os)
                                return@withContext true
                            } catch (_: IOException) {
                                return@withContext false
                            } finally {
                                `is`?.safeCloseQuietly()
                                os?.safeCloseQuietly()
                            }
                        }
                        if (success) {
                            when (action) {
                                ACTION_SAVE -> {
                                    Toast.makeText(this@BrowseActivity,
                                        getString(R.string.msg_file_save_success, DocumentsContract.getDocumentId(uri)),
                                        Toast.LENGTH_LONG).show()
                                }
                                ACTION_SAVE_SET_AS -> {
                                    this@BrowseActivity.startActivity(Intent.createChooser(
                                        Intent(Intent.ACTION_ATTACH_DATA).apply {
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            putExtra(Intent.EXTRA_MIME_TYPES, fileName.getMimeType())
                                            data = uri
                                        },
                                        getString(R.string.share_via)
                                    ))
                                }
                                ACTION_SAVE_SEND -> {
                                    this@BrowseActivity.startActivity(Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            type = fileName.getMimeType()
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                        },
                                        getString(R.string.share_via)
                                    ))
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun checkAndAction(action: Int) {
        when (action) {
            ACTION_DOWNLOAD -> DownloadWorker.downloadPost(getCurrentPost(), this)
            else -> saveAndAction(action)
        }
    }

    private fun setBg() {
        when (toolbar.visibility) {
            View.VISIBLE -> {
                window.hideBar()
                toolbar.visibility = View.GONE
                bottom_bar_container.visibility = View.GONE
                shadow.visibility = View.GONE
            }
            else -> {
                window.showBar()
                toolbar.visibility = View.VISIBLE
                bottom_bar_container.visibility = View.VISIBLE
                shadow.visibility = View.VISIBLE
            }
        }
    }

    private fun resetBg() {
        if (toolbar.visibility == View.GONE) {
            window.showBar()
            toolbar.visibility = View.VISIBLE
            bottom_bar_container.visibility = View.VISIBLE
            shadow.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }

    override fun onResume() {
        super.onResume()
        currentPlayerView?.let { playerView ->
            currentVideoUri?.let { uri ->
                playerHolder.start(uri, playerView)
            }
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

    @Suppress("UNCHECKED_CAST")
    private fun getFavPostViewModel(loader: PostLoaderRepository): FavPostViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return FavPostViewModel(loader) as T
            }
        })[FavPostViewModel::class.java]
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(
                uri,
                takeFlags
            )
            Settings.downloadDirPath = Uri.decode(uri.toString())
            Settings.downloadDirPathTreeId = DocumentsContract.getTreeDocumentId(uri)
            Settings.downloadDirPathAuthority = uri.authority
        }
    }
}
