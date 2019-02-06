package onlymash.flexbooru.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    private val postLoadedListener: PostLoadedListener = object : PostLoadedListener {
        override fun onDanItemsLoaded(posts: MutableList<PostDan>) {
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
            pager_browse.currentItem = position
        }

        override fun onMoeItemsLoaded(posts: MutableList<PostMoe>) {
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
            pager_browse.currentItem = position
        }
    }

    private lateinit var pagerAdapter: BrowsePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        toolbar.setTitle(R.string.browse_toolbar_title)
        toolbar.setBackgroundColor(resources.getColor(R.color.transparent, theme))
        val host = intent.getStringExtra(Constants.HOST_KEY)
        val type = intent.getIntExtra(Constants.TYPE_KEY, -1)
        val tags = intent.getStringExtra(Constants.TAGS_KEY)
        startId = intent.getIntExtra(Constants.ID_KEY, -1)
        Log.e(TAG, "id: $startId")
        pagerAdapter = BrowsePagerAdapter(GlideApp.with(this))
        pager_browse.adapter = pagerAdapter
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
}
