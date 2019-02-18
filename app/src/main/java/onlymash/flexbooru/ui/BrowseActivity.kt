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
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.activity_browse.*
import kotlinx.android.synthetic.main.bottom_shortcut_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostMoe
import onlymash.flexbooru.repository.browse.PostLoadedListener
import onlymash.flexbooru.ui.adapter.BrowsePagerAdapter
import onlymash.flexbooru.ui.fragment.TagBottomSheetDialog
import onlymash.flexbooru.util.UserAgent
import java.io.File
import java.net.URLDecoder

class BrowseActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BrowseActivity"
        const val ACTION = "current_browse_id"
        const val EXT_POST_ID_KEY = "post_id"
        const val EXT_POST_POSITION_KEY = "post_position"
        const val EXT_POST_KEYWORD_KEY = "post_keyword"
        private const val REQUEST_CODE_STORAGE = 10
        private const val PAGER_CURRENT_POSITION_KEY = "current_position"
    }
    private var startId = -1
    private var postsDan: MutableList<PostDan>? = null
    private var postsMoe: MutableList<PostMoe>? = null
    private var keyword = ""
    private var type = -1
    private var currentPosition = -1
    private val postLoadedListener: PostLoadedListener = object : PostLoadedListener {
        override fun onDanItemsLoaded(posts: MutableList<PostDan>) {
            type = Constants.TYPE_DANBOORU
            postsDan = posts
            var url: String? = null
            var position = 0
            var ext = ""
            if (startId >= 0) {
                posts.forEachIndexed { index, postDan ->
                    if (postDan.id == startId) {
                        position = index
                        url = postDan.large_file_url
                        ext = postDan.file_ext ?: ""
                        return@forEachIndexed
                    }
                }
            }
            toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), posts[position].id)
            pagerAdapter.updateData(posts, Constants.TYPE_DANBOORU)
            pager_browse.adapter = pagerAdapter
            pager_browse.currentItem = if (currentPosition >= 0) currentPosition else position
            startPostponedEnterTransition()
            if (!url.isNullOrBlank() && ext.isNotBlank() && ext != "jpg" && ext != "png" && ext != "gif") {
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
            var ext = ""
            if (startId >= 0) {
                posts.forEachIndexed { index, postMoe ->
                    if (postMoe.id == startId) {
                        position = index
                        url = postMoe.sample_url
                        ext = postMoe.file_ext ?: ""
                        return@forEachIndexed
                    }
                }
            }
            toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), posts[position].id)
            pagerAdapter.updateData(posts, Constants.TYPE_MOEBOORU)
            pager_browse.adapter = pagerAdapter
            pager_browse.currentItem = if (currentPosition >= 0) currentPosition else position
            startPostponedEnterTransition()
            if (!url.isNullOrBlank() && ext.isNotBlank() && ext != "jpg" && ext != "png" && ext != "gif") {
                Handler().postDelayed({
                    val playerView: Any? = pager_browse.findViewWithTag(String.format("player_%d", position))
                    if (playerView is PlayerView) {
                        playerHolder.start(uri = Uri.parse(url), playerView = playerView)
                    }
                }, 500)
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
            var ext = ""
            val id = when {
                postsDan != null -> {
                    url = postsDan!![position].large_file_url
                    ext = postsDan!![position].file_ext ?: ""
                    postsDan!![position].id
                }
                postsMoe != null -> {
                    url = postsMoe!![position].sample_url
                    ext = postsMoe!![position].file_ext ?: ""
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
            if (!url.isNullOrBlank() && ext.isNotBlank() && ext != "jpg" && ext != "png" && ext != "gif") {
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
                    checkStoragePermissionAndDownload()
                }
            }
            return@setOnMenuItemClickListener true
        }
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { _, insets ->
            toolbar_container.minimumHeight = toolbar.height + insets.systemWindowInsetTop
            toolbar_container.setPadding(0, insets.systemWindowInsetTop, 0, 0)
            val bottomPadding = (insets.systemWindowInsetBottom * 1.5f).toInt()
            bottom_bar_container.minimumHeight = resources.getDimensionPixelSize(R.dimen.browse_bottom_bar_height) + bottomPadding
            bottom_bar_container.setPadding(0, 0, 0, bottomPadding)
            insets
        }
        val host = intent.getStringExtra(Constants.HOST_KEY)
        val type = intent.getIntExtra(Constants.TYPE_KEY, -1)
        keyword = intent.getStringExtra(Constants.KEYWORD_KEY)
        startId = intent.getIntExtra(Constants.ID_KEY, -1)
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
        post_tags.setOnClickListener {
            when (type) {
                Constants.TYPE_DANBOORU -> TagBottomSheetDialog.create(postsDan!![pager_browse.currentItem])
                else -> TagBottomSheetDialog.create(postsMoe!![pager_browse.currentItem])
            }.apply {
                show(supportFragmentManager, "tags")
            }
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
                        Settings.DOWNLOAD_SIZE_ORIGIN -> {
                            it[position].file_url ?: it[position].large_file_url ?: ""
                        }
                        else -> it[position].large_file_url ?: ""
                    }
                }
            }
            Constants.TYPE_MOEBOORU -> {
                postsMoe?.let {
                    host = it[position].host
                    id = it[position].id
                    url = when (Settings.instance().downloadSize) {
                        Settings.DOWNLOAD_SIZE_SAMPLE -> getMoeSampleUrl(it[position])
                        Settings.DOWNLOAD_SIZE_LARGER -> getMoeLargerUrl(it[position])
                        else -> getMoeOriginUrl(it[position])
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
            val downloadId = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        }
    }

    private fun getMoeSampleUrl(post: PostMoe): String = post.sample_url

    private fun getMoeLargerUrl(post: PostMoe): String {
        val url = post.jpeg_url
        return if (url.isNullOrBlank()) getMoeSampleUrl(post) else url
    }

    private fun getMoeOriginUrl(post: PostMoe): String {
        val url = post.file_url
        return if (url.isNullOrBlank()) getMoeLargerUrl(post) else url
    }

    private fun checkStoragePermissionAndDownload() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,  arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE)
            }
        } else {
            download()
        }
    }

    private fun setBg() {
        when (toolbar.visibility) {
            View.VISIBLE -> {
                hideBar()
                toolbar.visibility = View.GONE
                bottom_bar_container.visibility = View.GONE
            }
            else -> {
                showBar()
                toolbar.visibility = View.VISIBLE
                bottom_bar_container.visibility = View.VISIBLE
            }
        }
    }

    private fun resetBg() {
        if (toolbar.visibility == View.GONE) {
            showBar()
            toolbar.visibility = View.VISIBLE
            bottom_bar_container.visibility = View.VISIBLE
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
            REQUEST_CODE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    download()
                }
            }
        }
    }
}
