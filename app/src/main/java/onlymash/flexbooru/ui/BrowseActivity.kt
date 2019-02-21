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
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.activity_browse.*
import kotlinx.android.synthetic.main.bottom_shortcut_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostMoe
import onlymash.flexbooru.repository.browse.PostLoadedListener
import onlymash.flexbooru.ui.adapter.BrowsePagerAdapter
import onlymash.flexbooru.ui.fragment.InfoBottomSheetDialog
import onlymash.flexbooru.ui.fragment.TagBottomSheetDialog
import onlymash.flexbooru.util.FileUtil
import onlymash.flexbooru.util.UserAgent
import onlymash.flexbooru.util.isImage
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
        fun startActivity(activity: Activity, view: View, postId: Int, keyword: String) {
            val intent = Intent(activity, BrowseActivity::class.java)
                .apply {
                    putExtra(Constants.ID_KEY, postId)
                    putExtra(Constants.KEYWORD_KEY, keyword)
                }
            val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, view, String.format(activity.getString(R.string.post_transition_name), postId))
            activity.startActivity(intent, options.toBundle())
        }
    }
    private var startId = -1
    private var postsDan: MutableList<PostDan>? = null
    private var postsMoe: MutableList<PostMoe>? = null
    private var keyword = ""
    private var type = -1
    private var host = ""
    private var scheme = ""
    private var currentPosition = -1
    private val postLoadedListener: PostLoadedListener = object : PostLoadedListener {
        override fun onDanItemsLoaded(posts: MutableList<PostDan>) {
            type = Constants.TYPE_DANBOORU
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
            startPostponedEnterTransition()
            if (!url.isNullOrBlank() && !url!!.isImage()) {
                Handler().postDelayed({
                    val playerView: Any? = pager_browse.findViewWithTag(String.format("player_%d", position))
                    if (playerView is PlayerView) {
                        playerHolder.start(uri = Uri.parse(url), playerView = playerView)
                    }
                }, 500)
            }
        }

        override fun onMoeItemsLoaded(posts: MutableList<PostMoe>) {
            type = Constants.TYPE_MOEBOORU
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
            startPostponedEnterTransition()
            if (!url.isNullOrEmpty() && !url!!.isImage()) {
                Handler().postDelayed({
                    val playerView: Any? = pager_browse.findViewWithTag(String.format("player_%d", position))
                    if (playerView is PlayerView) {
                        playerHolder.start(uri = Uri.parse(url), playerView = playerView)
                    }
                }, 300)
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
            val sharedElement = pager_browse.findViewWithTag<View>(pos).findViewById<View>(R.id.photo_view)
            val name = sharedElement.transitionName
            names.clear()
            names.add(name)
            sharedElements.clear()
            sharedElements[name] = sharedElement
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        postponeEnterTransition()
        setEnterSharedElementCallback(sharedElementCallback)
        toolbar.setTitle(R.string.browse_toolbar_title)
        toolbar.setBackgroundColor(resources.getColor(R.color.transparent, theme))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar.inflateMenu(R.menu.browse)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_browse_vote -> {

                }
                R.id.action_browse_download -> {
                    checkStoragePermissionAndAction(ACTION_DOWNLOAD)
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
        keyword = intent.getStringExtra(Constants.KEYWORD_KEY)
        startId = intent.getIntExtra(Constants.ID_KEY, -1)
        val booru = BooruManager.getBooruByUid(Settings.instance().activeBooruUid) ?: return
        type = booru.type
        scheme = booru.scheme
        host = booru.host
        pagerAdapter = BrowsePagerAdapter(GlideApp.with(this))
        pagerAdapter.setPhotoViewListener(photoViewListener)
        pager_browse.addOnPageChangeListener(pagerChangeListener)
        val loader = ServiceLocator.instance().getPostLoader().apply {
            setPostLoadedListener(postLoadedListener)
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                loader.loadDanPosts(host = host, keyword = keyword)
            }
            Constants.TYPE_MOEBOORU -> {
                loader.loadMoePosts(host = host, keyword = keyword)
            }
        }
        post_share.setOnClickListener {
            when (type) {
                Constants.TYPE_DANBOORU -> {
                    val url = String.format("%s://%s/posts/%d", scheme, host, postsDan!![pager_browse.currentItem].id)
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
                    val url = String.format("%s://%s/post/show/%d", scheme, host, postsMoe!![pager_browse.currentItem].id)
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
        post_tags.setOnClickListener {
            when (type) {
                Constants.TYPE_DANBOORU -> TagBottomSheetDialog.create(postsDan!![pager_browse.currentItem])
                else -> TagBottomSheetDialog.create(postsMoe!![pager_browse.currentItem])
            }.apply {
                show(supportFragmentManager, "tags")
            }
        }
        post_info.setOnClickListener {
            when (type) {
                Constants.TYPE_DANBOORU -> InfoBottomSheetDialog.create(postsDan!![pager_browse.currentItem])
                else -> InfoBottomSheetDialog.create(postsMoe!![pager_browse.currentItem])
            }.apply {
                show(supportFragmentManager, "info")
            }
        }
        post_save.setOnClickListener {
            checkStoragePermissionAndAction(ACTION_SAVE)
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

    private fun save() {
        val position = pager_browse.currentItem
        val url = when (type) {
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
                    FileUtil.copy(resource, file)
                    Toast.makeText(this@BrowseActivity,
                        getString(R.string.msg_file_save_success, file.absolutePath),
                        Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun download() {
        val position = pager_browse.currentItem
        var url = ""
        var host = "Flexbooru"
        var id = -1
        when (type) {
            Constants.TYPE_DANBOORU -> {
                postsDan?.let {
                    host = it[position].host
                    id = it[position].id
                    url = when (Settings.instance().downloadSize) {
                        Settings.POST_SIZE_ORIGIN -> it[position].getOriginUrl()
                        else -> it[position].getLargerUrl()
                    }
                }
            }
            Constants.TYPE_MOEBOORU -> {
                postsMoe?.let {
                    host = it[position].host
                    id = it[position].id
                    url = when (Settings.instance().downloadSize) {
                        Settings.POST_SIZE_SAMPLE -> it[position].getSampleUrl()
                        Settings.POST_SIZE_LARGER -> it[position].getLargerUrl()
                        else -> it[position].getOriginUrl()
                    }
                }
            }
        }
        if (url.isNotBlank()) {
            val fileName = URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1), "UTF-8")
            val path = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),
                String.format("%s/%s/%s", getString(R.string.app_name), host, fileName))
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setTitle(String.format("%s - %d", host, id))
                setDescription(fileName)
                setDestinationUri(Uri.fromFile(path))
                addRequestHeader(Constants.USER_AGENT_KEY, UserAgent.get())
            }
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
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
                ACTION_SAVE -> save()
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
            ACTION_SAVE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    save()
                }
            }
        }
    }
}
