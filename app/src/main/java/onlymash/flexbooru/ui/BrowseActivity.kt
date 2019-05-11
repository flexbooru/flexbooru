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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
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
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.api.*
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.entity.Vote
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.repository.browse.PostLoadedListener
import onlymash.flexbooru.repository.browse.PostLoaderRepositoryIml
import onlymash.flexbooru.repository.browse.PostLoaderRepository
import onlymash.flexbooru.repository.favorite.VoteCallback
import onlymash.flexbooru.repository.favorite.VoteRepositoryIml
import onlymash.flexbooru.ui.adapter.BrowsePagerAdapter
import onlymash.flexbooru.ui.fragment.InfoBottomSheetDialog
import onlymash.flexbooru.ui.fragment.TagBottomSheetDialog
import onlymash.flexbooru.ui.viewmodel.FavPostViewModel
import onlymash.flexbooru.util.*
import onlymash.flexbooru.widget.DismissFrameLayout
import org.kodein.di.generic.instance
import java.io.*
import java.util.concurrent.Executor

class BrowseActivity : BaseActivity() {

    companion object {
        private const val TAG = "BrowseActivity"
        const val ACTION = "current_browse_id"
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
    private val db: FlexbooruDatabase by instance()
    private val ioExecutor: Executor by instance()

    private var postsDan: MutableList<PostDan>? = null
    private var postsDanFav: MutableList<PostDan>? = null
    private var postsMoe: MutableList<PostMoe>? = null
    private var postsMoeFav: MutableList<PostMoe>? = null
    private var postsGel: MutableList<PostGel>? = null
    private var postsGelFav: MutableList<PostGel>? = null
    private var postsDanOne: MutableList<PostDanOne>? = null
    private var postsDanOneFav: MutableList<PostDanOne>? = null
    private var postsSankaku: MutableList<PostSankaku>? = null
    private var postsSankakuFav: MutableList<PostSankaku>? = null

    private lateinit var booru: Booru
    private var pageType = Constants.PAGE_TYPE_POST
    private var startId = -1
    private var keyword = ""
    private var user: User? = null
    private var currentPosition = -1
    private var canTransition = true
    private val postLoader by lazy {
        PostLoaderRepositoryIml(
            db = db,
            ioExecutor = ioExecutor
        )
    }

    @Suppress("UNCHECKED_CAST")
    private val postLoadedListener: PostLoadedListener = object : PostLoadedListener {
        override fun onDanItemsLoaded(posts: MutableList<PostDan>) {
            postsDan = posts
            initItemsLoaded(posts as MutableList<PostBase>)
        }

        override fun onMoeItemsLoaded(posts: MutableList<PostMoe>) {
            postsMoe = posts
            initItemsLoaded(posts as MutableList<PostBase>)
        }

        override fun onDanOneItemsLoaded(posts: MutableList<PostDanOne>) {
            postsDanOne = posts
            initItemsLoaded(posts as MutableList<PostBase>)
        }

        override fun onGelItemsLoaded(posts: MutableList<PostGel>) {
            postsGel = posts
            initItemsLoaded(posts as MutableList<PostBase>)
        }

        override fun onSankakuItemsLoaded(posts: MutableList<PostSankaku>) {
            postsSankaku = posts
            initItemsLoaded(posts as MutableList<PostBase>)
        }
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
        pager_browse.currentItem = if (currentPosition >= 0) currentPosition else position
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
        user?.let {
            val type = booru.type
            favPostViewModel.loadFav(
                host = booru.host,
                keyword = when (type) {
                    Constants.TYPE_MOEBOORU -> "vote:3:${it.name} order:vote"
                    else -> "fav:${it.name}"
                },
                type = type)
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
            when (booru.type) {
                Constants.TYPE_DANBOORU -> {
                    postsDan?.get(position)?.let {
                        url = it.getSampleUrl()
                        id = it.id
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    postsMoe?.get(position)?.let {
                        url = it.getSampleUrl()
                        id = it.id
                    }
                }
                Constants.TYPE_DANBOORU_ONE -> {
                    postsDanOne?.get(position)?.let {
                        url = it.getSampleUrl()
                        id = it.id
                    }
                }
                Constants.TYPE_GELBOORU -> {
                    postsGel?.get(position)?.let {
                        url = it.getSampleUrl()
                        id = it.id
                    }
                }
                Constants.TYPE_SANKAKU -> {
                    postsSankaku?.get(position)?.let {
                        url = it.getSampleUrl()
                        id = it.id
                    }
                }
            }
            if (id > 0) toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), id)
            val intent = Intent(ACTION).apply {
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
        VoteRepositoryIml(
            danbooruApi = danApi,
            danbooruOneApi = danOneApi,
            moebooruApi = moeApi,
            sankakuApi = sankakuApi,
            db = db,
            ioExecutor = ioExecutor
        )
    }
    private lateinit var favPostViewModel: FavPostViewModel

    private val voteCallback = object : VoteCallback {
        override fun onSuccess() {

        }
        override fun onFailed(msg: String) {
            Toast.makeText(this@BrowseActivity, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun getCurrentPostFav(): Any? {
        val position = pager_browse.currentItem
        var post: Any? = null
        when (booru.type) {
            Constants.TYPE_DANBOORU -> {
                val id = postsDan?.get(position)?.id ?: return null
                postsDanFav?.forEach {
                    if (it.id == id) {
                        post = it
                        return@forEach
                    }
                }
            }
            Constants.TYPE_MOEBOORU -> {
                val id = postsMoe?.get(position)?.id ?: return null
                postsMoeFav?.forEach {
                    if (it.id == id) {
                        post = it
                        return@forEach
                    }
                }
            }
            Constants.TYPE_DANBOORU_ONE -> {
                val id = postsDanOne?.get(position)?.id ?: return null
                postsDanOneFav?.forEach {
                    if (it.id == id) {
                        post = it
                        return@forEach
                    }
                }
            }
            Constants.TYPE_GELBOORU -> {
                val id = postsGel?.get(position)?.id ?: return null
                postsGelFav?.forEach {
                    if (it.id == id) {
                        post = it
                        return@forEach
                    }
                }
            }
            Constants.TYPE_SANKAKU -> {
                val id = postsSankaku?.get(position)?.id ?: return null
                postsSankakuFav?.forEach {
                    if (it.id == id) {
                        post = it
                        return@forEach
                    }
                }
            }
        }
        return post
    }

    private fun getCurrentPost(): Any? {
        return when (booru.type) {
            Constants.TYPE_DANBOORU -> postsDan?.get(pager_browse.currentItem)
            Constants.TYPE_MOEBOORU -> postsMoe?.get(pager_browse.currentItem)
            Constants.TYPE_DANBOORU_ONE -> postsDanOne?.get(pager_browse.currentItem)
            Constants.TYPE_GELBOORU -> postsGel?.get(pager_browse.currentItem)
            Constants.TYPE_SANKAKU -> postsSankaku?.get(pager_browse.currentItem)
            else -> null
        }
    }

    private fun getCurrentPostId(): Int {
        return when (val post = getCurrentPost()) {
            is PostBase -> post.getPostId()
            else -> -1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
        setContentView(R.layout.activity_browse)
        pageType = intent?.getIntExtra(Constants.PAGE_TYPE_KEY, Constants.PAGE_TYPE_POST) ?: Constants.PAGE_TYPE_POST
        colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.black))
        pager_browse.background = colorDrawable
        postponeEnterTransition()
        setEnterSharedElementCallback(sharedElementCallback)
        toolbar.setTitle(R.string.browse_toolbar_title)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar.inflateMenu(R.menu.browse)
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
        booru = BooruManager.getBooruByUid(Settings.instance().activeBooruUid) ?: return
        user = UserManager.getUserByBooruUid(booruUid = booru.uid)
        user?.let {
            voteRepository.voteCallback = voteCallback
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
        postLoader.postLoadedListener = postLoadedListener
        postLoader.loadPosts(host = booru.host, keyword = keyword, type = booru.type)
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
            val type = booru.type
            if (type == Constants.TYPE_GELBOORU) {
                Toast.makeText(this@BrowseActivity,
                    getString(R.string.msg_not_supported), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            user?.let { user ->
                when (type) {
                    Constants.TYPE_DANBOORU -> {
                        val post = postsDan?.get(pager_browse.currentItem) ?: return@let
                        val vote = Vote(
                            scheme = booru.scheme,
                            host = booru.host,
                            post_id = post.id,
                            username = user.name,
                            auth_key = user.api_key ?: return@let)
                        val postFav = getCurrentPostFav()
                        if (postFav is PostDan) {
                            voteRepository.removeDanFav(vote, postFav)
                        } else {
                            voteRepository.addDanFav(vote, post)
                        }
                    }
                    Constants.TYPE_MOEBOORU -> {
                        val post = postsMoe?.get(pager_browse.currentItem) ?: return@let
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
                        voteRepository.voteMoePost(vote)
                    }
                    Constants.TYPE_DANBOORU_ONE -> {
                        val post = postsDanOne?.get(pager_browse.currentItem) ?: return@let
                        val postFav = getCurrentPostFav()
                        val vote = Vote(
                            scheme = booru.scheme,
                            host = booru.host,
                            post_id = post.id,
                            username = user.name,
                            auth_key = user.password_hash ?: return@let)
                        if (postFav is PostDanOne) {
                            voteRepository.removeDanOneFav(vote, postFav)
                        } else {
                            voteRepository.addDanOneFav(vote, post)
                        }
                    }
                    Constants.TYPE_GELBOORU -> {

                    }
                    Constants.TYPE_SANKAKU -> {
                        val post = postsSankaku?.get(pager_browse.currentItem) ?: return@let
                        val postFav = getCurrentPostFav()
                        val vote = Vote(
                            scheme = booru.scheme,
                            host = booru.host,
                            post_id = post.id,
                            username = user.name,
                            auth_key = user.password_hash ?: return@let)
                        if (postFav is PostSankaku) {
                            voteRepository.removeSankakuFav(vote, postFav)
                        } else {
                            voteRepository.addSankakuFav(vote, post)
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
        if (!Settings.instance().isOrderSuccess) {
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
        val url = when (val post = getCurrentPost()) {
            is PostDan -> String.format("%s://%s/posts/%d", booru.scheme, booru.host, post.id)
            is PostDanOne -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host, post.id)
            is PostMoe -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host, post.id)
            is PostGel -> String.format("%s://%s/index.php?page=post&s=view&id=%d", booru.scheme, booru.host, post.id)
            is PostSankaku -> String.format("%s://%s/post/show/%d", booru.scheme, booru.host.replace("capi-v2.", "beta."), post.id)
            else -> ""
        }
        if (url.isNotEmpty()) {
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

    private fun initFavViewModel() {
        favPostViewModel = getFavPostViewModel(postLoader)
        when (booru.type) {
            Constants.TYPE_DANBOORU -> {
                favPostViewModel.postsDan.observe(this, Observer { posts ->
                    postsDanFav = posts
                    setCurrentVoteItemIcon()
                })
            }
            Constants.TYPE_MOEBOORU -> {
                favPostViewModel.postsMoe.observe(this, Observer { posts ->
                    postsMoeFav = posts
                    setCurrentVoteItemIcon()
                })
            }
            Constants.TYPE_DANBOORU_ONE -> {
                favPostViewModel.postsDanOne.observe(this, Observer { posts ->
                    postsDanOneFav = posts
                    setCurrentVoteItemIcon()
                })
            }
            Constants.TYPE_GELBOORU -> {
                favPostViewModel.postsGel.observe(this, Observer { posts ->
                    postsGelFav = posts
                    setCurrentVoteItemIcon()
                })
            }
            Constants.TYPE_SANKAKU -> {
                favPostViewModel.postsSankaku.observe(this, Observer { posts ->
                    postsSankakuFav = posts
                    setCurrentVoteItemIcon()
                })
            }
        }
    }

    private fun setCurrentVoteItemIcon() {
        when (val post = getCurrentPost()) {
            is PostDan -> {
                var exist = false
                postsDanFav?.forEach {
                    if (post.id == it.id) {
                        setVoteItemIcon(true)
                        exist = true
                        return@forEach
                    }
                }
                if (!exist) {
                    setVoteItemIcon(false)
                }
            }
            is PostMoe -> {
                var exist = false
                postsMoeFav?.forEach {
                    if (post.id == it.id) {
                        setVoteItemIcon(true)
                        exist = true
                        return@forEach
                    }
                }
                if (!exist) {
                    setVoteItemIcon(false)
                }
            }
            is PostDanOne -> {
                var exist = false
                postsDanOneFav?.forEach {
                    if (post.id == it.id) {
                        setVoteItemIcon(true)
                        exist = true
                        return@forEach
                    }
                }
                if (!exist) {
                    setVoteItemIcon(false)
                }
            }
            is PostSankaku -> {
                var exist = false
                postsSankakuFav?.forEach {
                    if (post.id == it.id) {
                        setVoteItemIcon(true)
                        exist = true
                        return@forEach
                    }
                }
                if (!exist) {
                    setVoteItemIcon(false)
                }
            }
        }
    }

    private fun setVoteItemIcon(checked: Boolean) {
        post_fav.setImageResource(if (checked) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(PAGER_CURRENT_POSITION_KEY, pager_browse.currentItem)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        currentPosition = savedInstanceState?.getInt(PAGER_CURRENT_POSITION_KEY) ?: -1
        super.onRestoreInstanceState(savedInstanceState)
        if (currentPosition >= 0) pager_browse.currentItem = currentPosition
    }

    private fun saveAndAction(action: Int) {
        val position = pager_browse.currentItem
        val url = when (booru.type) {
            Constants.TYPE_DANBOORU -> {
                when (Settings.instance().browseSize) {
                    Settings.POST_SIZE_SAMPLE -> postsDan?.get(position)?.getSampleUrl()
                    Settings.POST_SIZE_LARGER -> postsDan?.get(position)?.getLargerUrl()
                    else -> postsDan?.get(position)?.getOriginUrl()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                when (Settings.instance().browseSize) {
                    Settings.POST_SIZE_SAMPLE -> postsMoe?.get(position)?.getSampleUrl()
                    Settings.POST_SIZE_LARGER -> postsMoe?.get(position)?.getLargerUrl()
                    else -> postsMoe?.get(position)?.getOriginUrl()
                }
            }
            Constants.TYPE_DANBOORU_ONE -> {
                when (Settings.instance().browseSize) {
                    Settings.POST_SIZE_SAMPLE -> postsDanOne?.get(position)?.getSampleUrl()
                    Settings.POST_SIZE_LARGER -> postsDanOne?.get(position)?.getLargerUrl()
                    else -> postsDanOne?.get(position)?.getOriginUrl()
                }
            }
            Constants.TYPE_GELBOORU -> {
                when (Settings.instance().browseSize) {
                    Settings.POST_SIZE_SAMPLE -> postsGel?.get(position)?.getSampleUrl()
                    Settings.POST_SIZE_LARGER -> postsGel?.get(position)?.getLargerUrl()
                    else -> postsGel?.get(position)?.getOriginUrl()
                }
            }
            else -> {
                when (Settings.instance().browseSize) {
                    Settings.POST_SIZE_SAMPLE -> postsSankaku?.get(position)?.getSampleUrl()
                    Settings.POST_SIZE_LARGER -> postsSankaku?.get(position)?.getLargerUrl()
                    else -> postsSankaku?.get(position)?.getOriginUrl()
                }
            }
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
                    val handler = Handler()
                    val uri = getSaveUri(fileName) ?: return
                    Thread {
                        var `is`: InputStream? = null
                        var os: OutputStream? = null
                        try {
                            `is` = FileInputStream(resource)
                            os = this@BrowseActivity.contentResolver.openOutputStream(uri)
                            IOUtils.copy(`is`, os)
                        } catch (_: IOException) {
                            return@Thread
                        } finally {
                            IOUtils.closeQuietly(`is`)
                            IOUtils.closeQuietly(os)
                        }
                        handler.post {
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
                                            putExtra(Intent.EXTRA_MIME_TYPES, "image/*")
                                            data = uri
                                        },
                                        getString(R.string.share_via)
                                    ))
                                }
                                ACTION_SAVE_SEND -> {
                                    this@BrowseActivity.startActivity(Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            type = "image/*"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                        },
                                        getString(R.string.share_via)
                                    ))
                                }
                            }
                        }
                    }.start()
                }
            })
    }

    private fun download() {
        val position = pager_browse.currentItem
        var post: PostBase? = null
        when (booru.type) {
            Constants.TYPE_DANBOORU -> post = postsDan?.get(position)
            Constants.TYPE_DANBOORU_ONE -> post = postsDanOne?.get(position)
            Constants.TYPE_MOEBOORU -> post = postsMoe?.get(position)
            Constants.TYPE_GELBOORU -> post = postsGel?.get(position)
            Constants.TYPE_SANKAKU -> post = postsSankaku?.get(position)
        }
        DownloadUtil.downloadPost(post, this)
    }

    private fun checkAndAction(action: Int) {
        when (action) {
            ACTION_DOWNLOAD -> download()
            else -> saveAndAction(action)
        }
    }

    private fun setBg() {
        when (toolbar.visibility) {
            View.VISIBLE -> {
                hideBar()
                toolbar.visibility = View.GONE
                bottom_bar_container.visibility = View.GONE
                shadow.visibility = View.GONE
            }
            else -> {
                showBar()
                toolbar.visibility = View.VISIBLE
                bottom_bar_container.visibility = View.VISIBLE
                shadow.visibility = View.VISIBLE
            }
        }
    }

    private fun resetBg() {
        if (toolbar.visibility == View.GONE) {
            showBar()
            toolbar.visibility = View.VISIBLE
            bottom_bar_container.visibility = View.VISIBLE
            shadow.visibility = View.VISIBLE
        }
    }

    private fun initWindow() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        showBar()
    }

    private fun showBar() {
        val uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = uiFlags
    }

    private fun hideBar() {
        val uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE
        window.decorView.systemUiVisibility = uiFlags
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
            Settings.instance().downloadDirPath = Uri.decode(uri.toString())
            Settings.instance().downloadDirPathTreeId = DocumentsContract.getTreeDocumentId(uri)
            Settings.instance().downloadDirPathAuthority = uri.authority
        }
    }
}
