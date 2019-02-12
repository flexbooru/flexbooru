package onlymash.flexbooru.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.activity_browse.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.exoplayer.PlayerHolder
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.repository.browse.PostLoadedListener
import onlymash.flexbooru.ui.adapter.BrowsePagerAdapter
import onlymash.flexbooru.util.UrlUtil

class BrowseActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BrowseActivity"
        const val ACTION = "current_browse_id"
        const val EXT_POST_ID_KEY = "post_id"
        const val EXT_POST_POSITION_KEY = "post_position"
        const val EXT_POST_KEYWORD_KEY = "post_keyword"
    }
    private var startId = -1
    private var postsDan: MutableList<PostDan>? = null
    private var postsMoe: MutableList<PostMoe>? = null
    private var keyword = ""
    private val postLoadedListener: PostLoadedListener = object : PostLoadedListener {
        override fun onDanItemsLoaded(posts: MutableList<PostDan>) {
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
            pager_browse.currentItem = position
            startPostponedEnterTransition()
            if (!url.isNullOrBlank() && ext.isNotBlank() && ext != "jpg" && ext != "png") {
                Handler().postDelayed({
                    val playerView: Any? = pager_browse.findViewWithTag(String.format("player_%d", position))
                    if (playerView is PlayerView) {
                        playerHolder.start(uri = Uri.parse(url), playerView = playerView)
                    }
                }, 500)
            }
        }

        override fun onMoeItemsLoaded(posts: MutableList<PostMoe>) {
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
            pager_browse.currentItem = position
            startPostponedEnterTransition()
            if (!url.isNullOrBlank() && ext.isNotBlank() && ext != "jpg" && ext != "png") {
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
            if (!url.isNullOrBlank() && ext.isNotBlank() && ext != "jpg" && ext != "png") {
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
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { _, insets ->
            toolbar_container.minimumHeight = toolbar.height + insets.systemWindowInsetTop
            toolbar_container.setPadding(0, insets.systemWindowInsetTop, 0, 0)
            bottom_bar_container.minimumHeight = resources.getDimensionPixelSize(R.dimen.browse_bottom_bar_height) + insets.systemWindowInsetBottom
            bottom_bar_container.setPadding(0, 0, 0, insets.systemWindowInsetBottom)
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
}
