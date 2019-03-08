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

package onlymash.flexbooru.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.app.ActivityCompat
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
import kotlinx.android.synthetic.main.activity_browse.*
import kotlinx.android.synthetic.main.bottom_shortcut_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.*
import onlymash.flexbooru.content.FlexProvider
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.*
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.repository.browse.PostLoadedListener
import onlymash.flexbooru.repository.browse.PostLoader
import onlymash.flexbooru.repository.favorite.VoteRepository
import onlymash.flexbooru.ui.adapter.BrowsePagerAdapter
import onlymash.flexbooru.ui.fragment.InfoBottomSheetDialog
import onlymash.flexbooru.ui.fragment.TagBottomSheetDialog
import onlymash.flexbooru.ui.viewmodel.FavPostViewModel
import onlymash.flexbooru.util.FileUtil
import onlymash.flexbooru.util.downloadPost
import onlymash.flexbooru.util.isImage
import onlymash.flexbooru.widget.DismissFrameLayout
import java.io.File
import java.net.URLDecoder

class BrowseActivity : AppCompatActivity() {

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
    private var pageType = Constants.PAGE_TYPE_POST

    private var startId = -1
    private var postsDan: MutableList<PostDan>? = null
    private var postsMoe: MutableList<PostMoe>? = null
    private var postsDanFav: MutableList<PostDan>? = null
    private var postsMoeFav: MutableList<PostMoe>? = null
    private var keyword = ""
    private lateinit var booru: Booru
    private var user: User? = null
    private var currentPosition = -1
    private var canTransition = true
    private val postLoader by lazy { ServiceLocator.instance().getPostLoader() }
    private val postLoadedListener: PostLoadedListener = object : PostLoadedListener {
        override fun onDanItemsLoaded(posts: MutableList<PostDan>) {
            postsDan = posts
            var url: String? = null
            var position = 0
            if (startId >= 0) {
                posts.forEachIndexed { index, postDan ->
                    if (postDan.id == startId) {
                        position = index
                        url = postDan.large_file_url
                        return@forEachIndexed
                    }
                }
            }
            toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), posts[position].id)
            pagerAdapter.updateData(posts, Constants.TYPE_DANBOORU)
            pager_browse.adapter = pagerAdapter
            pager_browse.currentItem = if (currentPosition >= 0) currentPosition else position
            if (canTransition) startPostponedEnterTransition()
            if (!url.isNullOrBlank() && !url!!.isImage()) {
                Handler().postDelayed({
                    val playerView: Any? = pager_browse.findViewWithTag(String.format("player_%d", position))
                    if (playerView is PlayerView) {
                        playerHolder.start(uri = Uri.parse(url), playerView = playerView)
                    }
                }, 500)
            }
            user?.let {
                favPostViewModel.loadDanFav(booru.host, it.name)
            }
        }

        override fun onMoeItemsLoaded(posts: MutableList<PostMoe>) {
            postsMoe = posts
            var url: String? = null
            var position = 0
            if (startId >= 0) {
                posts.forEachIndexed { index, postMoe ->
                    if (postMoe.id == startId) {
                        position = index
                        url = postMoe.sample_url
                        return@forEachIndexed
                    }
                }
            }
            toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), posts[position].id)
            pagerAdapter.updateData(posts, Constants.TYPE_MOEBOORU)
            pager_browse.adapter = pagerAdapter
            pager_browse.currentItem = if (currentPosition >= 0) currentPosition else position
            if (canTransition) startPostponedEnterTransition()
            if (!url.isNullOrEmpty() && !url!!.isImage()) {
                Handler().postDelayed({
                    val playerView: Any? = pager_browse.findViewWithTag(String.format("player_%d", position))
                    if (playerView is PlayerView) {
                        playerHolder.start(uri = Uri.parse(url), playerView = playerView)
                    }
                }, 300)
            }
            user?.let {
                favPostViewModel.loadMoeFav(booru.host, it.name)
            }
        }
    }

    private val playerHolder: PlayerHolder by lazy { PlayerHolder(this) }

    private lateinit var pagerAdapter: BrowsePagerAdapter

    private val pagerChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
//            resetBg()
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

        }

        override fun onPageSelected(position: Int) {
            var url: String? = null
            val id = when {
                postsDan != null -> {
                    url = postsDan!![position].large_file_url
                    postsDan!![position].id
                }
                postsMoe != null -> {
                    url = postsMoe!![position].sample_url
                    postsMoe!![position].id
                }
                else -> -1
            }
            if (id > 0) toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), id)
            val intent = Intent(ACTION).apply {
                putExtra(EXT_POST_ID_KEY, id)
                putExtra(EXT_POST_POSITION_KEY, position)
                putExtra(EXT_POST_KEYWORD_KEY, keyword)
            }
            this@BrowseActivity.sendBroadcast(intent)
            playerHolder.stop()
            if (!url.isNullOrBlank() && !url.isImage()) {
                val playerView: Any? = pager_browse.findViewWithTag(String.format("player_%d", position))
                if (playerView is PlayerView) {
                    playerHolder.start(uri = Uri.parse(url), playerView = playerView)
                }
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

    private lateinit var voteRepository: VoteRepository
    private lateinit var favPostViewModel: FavPostViewModel
    private lateinit var voteItemView: ActionMenuItemView

    private fun getCurrentPostFav(): Any? {
        val position = pager_browse.currentItem
        var post: Any? = null
        when (booru.type) {
            Constants.TYPE_DANBOORU -> {
                val id = postsDan!![position].id
                postsDanFav?.forEach {
                    if (it.id == id) {
                        post = it
                        return@forEach
                    }
                }
            }
            Constants.TYPE_MOEBOORU -> {
                val id = postsMoe!![position].id
                postsMoeFav?.forEach {
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
            Constants.TYPE_DANBOORU -> {
                postsDan!![pager_browse.currentItem]
            }
            Constants.TYPE_MOEBOORU -> {
                postsMoe!![pager_browse.currentItem]
            }
            else -> null
        }
    }

    private fun getCurrentPostId(): Int {
        val post = getCurrentPost()
        return when (post) {
            is PostDan -> post.id
            is PostMoe -> post.id
            else -> -1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        pageType = intent?.getIntExtra(Constants.PAGE_TYPE_KEY, Constants.PAGE_TYPE_POST) ?: Constants.PAGE_TYPE_POST
        colorDrawable = ColorDrawable(resources.getColor(R.color.black, theme))
        pager_browse.background = colorDrawable
        postponeEnterTransition()
        setEnterSharedElementCallback(sharedElementCallback)
        toolbar.setTitle(R.string.browse_toolbar_title)
        toolbar.setBackgroundColor(resources.getColor(R.color.transparent, theme))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar.inflateMenu(R.menu.browse)
        voteItemView = toolbar.findViewById(R.id.action_browse_vote)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_browse_vote -> {
                    if (user == null) {
                        startActivity(Intent(this, AccountConfigActivity::class.java))
                        finish()
                    } else {
                        when (booru.type) {
                            Constants.TYPE_DANBOORU -> {
                                val vote = Vote(
                                    scheme = booru.scheme,
                                    host = booru.host,
                                    post_id = postsDan!![pager_browse.currentItem].id,
                                    username = user!!.name,
                                    auth_key = user!!.api_key!!)
                                val post = getCurrentPostFav()
                                if (post is PostDan) {
                                    voteRepository.removeDanFav(vote, post)
                                } else {
                                    voteRepository.addDanFav(vote, postsDan!![pager_browse.currentItem])
                                }
                            }
                            Constants.TYPE_MOEBOORU -> {
                                val vote = when (getCurrentPostFav()) {
                                    is PostMoe -> {
                                        Vote(
                                            scheme = booru.scheme,
                                            host = booru.host,
                                            score = 0,
                                            post_id = postsMoe!![pager_browse.currentItem].id,
                                            username = user!!.name,
                                            auth_key = user!!.password_hash!!)
                                    }
                                    else -> {
                                        Vote(
                                            scheme = booru.scheme,
                                            host = booru.host,
                                            score = 3,
                                            post_id = postsMoe!![pager_browse.currentItem].id,
                                            username = user!!.name,
                                            auth_key = user!!.password_hash!!)
                                    }
                                }
                                voteRepository.voteMoePost(vote)
                            }
                        }
                    }
                }
                R.id.action_browse_download -> {
                    checkStoragePermissionAndAction(ACTION_DOWNLOAD)
                }
                R.id.action_browse_set_as -> {
                    checkStoragePermissionAndAction(ACTION_SAVE_SET_AS)
                }
                R.id.action_browse_send -> {
                    checkStoragePermissionAndAction(ACTION_SAVE_SEND)
                }
                R.id.action_browse_share -> {
                    shareLink()
                }
            }
            return@setOnMenuItemClickListener true
        }
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { _, insets ->
            toolbar_container.minimumHeight = toolbar.height + insets.systemWindowInsetTop
            toolbar_container.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, 0)
            val bottomPadding = (insets.systemWindowInsetBottom * 1.5f).toInt()
            bottom_bar_container.minimumHeight = resources.getDimensionPixelSize(R.dimen.browse_bottom_bar_height) + bottomPadding
            bottom_bar_container.setPadding(insets.systemWindowInsetLeft, 0, insets.systemWindowInsetRight, bottomPadding)
            shadow.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
        keyword = intent.getStringExtra(Constants.KEYWORD_KEY) ?: ""
        startId = intent.getIntExtra(Constants.ID_KEY, -1)
        booru = BooruManager.getBooruByUid(Settings.instance().activeBooruUid) ?: return
        user = UserManager.getUserByBooruUid(booruUid = booru.uid)
        user?.let {
            initFavViewModel()
        }
        pagerAdapter = BrowsePagerAdapter(GlideApp.with(this), onDismissListener, pageType)
        pagerAdapter.setPhotoViewListener(photoViewListener)
        pager_browse.addOnPageChangeListener(pagerChangeListener)
        postLoader.setPostLoadedListener(postLoadedListener)
        voteRepository = ServiceLocator.instance().getVoteRepository()
        when (booru.type) {
            Constants.TYPE_DANBOORU -> {
                postLoader.loadDanPosts(host = booru.host, keyword = keyword)
            }
            Constants.TYPE_MOEBOORU -> {
                postLoader.loadMoePosts(host = booru.host, keyword = keyword)
            }
        }
        initBottomBar()
    }

    private fun initBottomBar() {
        post_tags.setOnClickListener {
            when (booru.type) {
                Constants.TYPE_DANBOORU -> TagBottomSheetDialog.create(postsDan!![pager_browse.currentItem])
                else -> TagBottomSheetDialog.create(postsMoe!![pager_browse.currentItem])
            }.apply {
                show(supportFragmentManager, "tags")
            }
        }
        post_info.setOnClickListener {
            when (booru.type) {
                Constants.TYPE_DANBOORU -> InfoBottomSheetDialog.create(postsDan!![pager_browse.currentItem])
                else -> InfoBottomSheetDialog.create(postsMoe!![pager_browse.currentItem])
            }.apply {
                show(supportFragmentManager, "info")
            }
        }
        post_comment.setOnClickListener {
            val id = getCurrentPostId()
            if (id > 0) CommentActivity.startActivity(context = this, postId = id)
        }
        post_save.setOnClickListener {
            checkStoragePermissionAndAction(ACTION_SAVE)
        }
    }

    private fun shareLink() {
        when (booru.type) {
            Constants.TYPE_DANBOORU -> {
                val url = String.format("%s://%s/posts/%d", booru.scheme, booru.host, postsDan!![pager_browse.currentItem].id)
                startActivity(Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, url)
                    },
                    getString(R.string.share_via)
                ))
            }
            else -> {
                val url = String.format("%s://%s/post/show/%d", booru.scheme, booru.host, postsMoe!![pager_browse.currentItem].id)
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
        }
    }

    private fun setCurrentVoteItemIcon() {
        when (booru.type) {
            Constants.TYPE_DANBOORU -> {
                if (postsDan != null && postsDanFav != null) {
                    var exist = false
                    val id = postsDan!![pager_browse.currentItem].id
                    postsDanFav!!.forEach { post ->
                        if (post.id == id) {
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
            Constants.TYPE_MOEBOORU -> {
                if (postsMoe != null && postsMoeFav != null) {
                    val id = postsMoe!![pager_browse.currentItem].id
                    var exist = false
                    postsMoeFav!!.forEach { post ->
                        if (post.id == id) {
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
    }

    @SuppressLint("RestrictedApi")
    private fun setVoteItemIcon(checked: Boolean) {
        if (checked) {
            voteItemView.setIcon(getDrawable(R.drawable.ic_star_24dp))
        } else {
            voteItemView.setIcon(getDrawable(R.drawable.ic_star_border_24dp))
        }
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
                    Settings.POST_SIZE_SAMPLE -> postsDan!![position].getSampleUrl()
                    Settings.POST_SIZE_LARGER -> postsDan!![position].getLargerUrl()
                    else -> postsDan!![position].getOriginUrl()
                }
            }
            else -> {
                when (Settings.instance().browseSize) {
                    Settings.POST_SIZE_SAMPLE -> postsMoe!![position].getSampleUrl()
                    Settings.POST_SIZE_LARGER -> postsMoe!![position].getLargerUrl()
                    else -> postsMoe!![position].getOriginUrl()
                }
            }
        }
        GlideApp.with(this)
            .downloadOnly()
            .load(url)
            .into(object : CustomTarget<File>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        String.format("%s/%s",
                            getString(R.string.app_name),
                            "save"))
                    if (!path.exists()) {
                        path.mkdirs()
                    } else if (path.isFile) {
                        path.delete()
                        path.mkdirs()
                    }
                    val fileName = URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1), "UTF-8")
                    val file = File(path, fileName)
                    if (file.exists()) file.delete()
                    val handler = Handler()
                    Thread {
                        if (!FileUtil.copy(resource, file)) return@Thread
                        App.app.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                        handler.post {
                            when (action) {
                                ACTION_SAVE -> Toast.makeText(this@BrowseActivity,
                                    getString(R.string.msg_file_save_success, file.absolutePath),
                                    Toast.LENGTH_LONG).show()
                                ACTION_SAVE_SET_AS -> {
                                    this@BrowseActivity.startActivity(Intent.createChooser(
                                        Intent(Intent.ACTION_ATTACH_DATA).apply {
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            putExtra(Intent.EXTRA_MIME_TYPES, "image/*")
                                            data = FlexProvider.getUriFromFile(this@BrowseActivity, file)
                                        },
                                        getString(R.string.share_via)
                                    ))
                                }
                                ACTION_SAVE_SEND -> {
                                    this@BrowseActivity.startActivity(Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            type = "image/*"
                                            putExtra(Intent.EXTRA_STREAM, FlexProvider.getUriFromFile(this@BrowseActivity, file))
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
        when (booru.type) {
            Constants.TYPE_DANBOORU -> {
                postsDan?.let {
                    downloadPost(it[position])
                }
            }
            Constants.TYPE_MOEBOORU -> {
                postsMoe?.let {
                    downloadPost(it[position])
                }
            }
        }
    }

    private fun checkStoragePermissionAndAction(action: Int) {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,  arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), action)
            }
        } else {
            when (action) {
                ACTION_DOWNLOAD -> download()
                else -> saveAndAction(action)
            }
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

    private fun showBar() {
        val uiFlags = View.SYSTEM_UI_FLAG_VISIBLE
        window.decorView.systemUiVisibility = uiFlags
    }

    private fun hideBar() {
        val uiFlags = View.SYSTEM_UI_FLAG_IMMERSIVE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        window.decorView.systemUiVisibility = uiFlags
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }

    override fun onStop() {
        super.onStop()
        playerHolder.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHolder.release()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            ACTION_DOWNLOAD -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    download()
                }
            }
            ACTION_SAVE, ACTION_SAVE_SET_AS, ACTION_SAVE_SEND -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveAndAction(requestCode)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getFavPostViewModel(loader: PostLoader): FavPostViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return FavPostViewModel(loader) as T
            }
        })[FavPostViewModel::class.java]
    }
}
