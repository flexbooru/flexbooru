package onlymash.flexbooru.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_browse.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.repository.browse.PostLoadedListener
import onlymash.flexbooru.ui.adapter.BrowsePagerAdapter

class BrowseActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BrowseActivity"
    }
    private var startId = -1
    private var postsDan: MutableList<PostDan>? = null
    private var postsMoe: MutableList<PostMoe>? = null
    private val postLoadedListener: PostLoadedListener = object : PostLoadedListener {
        override fun onDanItemsLoaded(posts: MutableList<PostDan>) {
            postsDan = posts
            var position = 0
            if (startId >= 0) {
                posts.forEachIndexed { index, postDan ->
                    if (postDan.id == startId) {
                        position = index
                        return@forEachIndexed
                    }
                }
            }
            pagerAdapter.updateData(posts, Constants.TYPE_DANBOORU)
            toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), posts[position].id)
            pager_browse.currentItem = position
        }

        override fun onMoeItemsLoaded(posts: MutableList<PostMoe>) {
            postsMoe = posts
            var position = 0
            if (startId >= 0) {
                posts.forEachIndexed { index, postMoe ->
                    if (postMoe.id == startId) {
                        position = index
                        return@forEachIndexed
                    }
                }
            }
            pagerAdapter.updateData(posts, Constants.TYPE_MOEBOORU)
            toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), posts[position].id)
            pager_browse.currentItem = position
        }
    }

    private lateinit var pagerAdapter: BrowsePagerAdapter

    private val pagerChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
//            resetBg()
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val id = when {
                postsDan != null -> postsDan!![position].id
                postsMoe != null -> postsMoe!![position].id
                else -> -1
            }
            if (id > 0) toolbar.title = String.format(getString(R.string.browse_toolbar_title_and_id), id)
        }

        override fun onPageSelected(position: Int) {

        }

    }

    private val photoViewListener = object : BrowsePagerAdapter.PhotoViewListener {
        override fun onClickPhotoView() {
            setBg()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        toolbar.setTitle(R.string.browse_toolbar_title)
        toolbar.setBackgroundColor(resources.getColor(R.color.transparent, theme))
        toolbar.setNavigationOnClickListener {
            finish()
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
        val tags = intent.getStringExtra(Constants.TAGS_KEY)
        startId = intent.getIntExtra(Constants.ID_KEY, -1)
        pagerAdapter = BrowsePagerAdapter(GlideApp.with(this))
        pagerAdapter.setPhotoViewListener(photoViewListener)
        pager_browse.adapter = pagerAdapter
        pager_browse.addOnPageChangeListener(pagerChangeListener)
        val loader = ServiceLocator.instance().getPostLoader()
        loader.setPostLoadedListener(postLoadedListener)
        when (type) {
            Constants.TYPE_DANBOORU -> {
                loader.loadDanPosts(host = host, keyword = tags)
            }
            Constants.TYPE_MOEBOORU -> {
                loader.loadMoePosts(host = host, keyword = tags)
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
}
