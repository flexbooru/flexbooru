package onlymash.flexbooru.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.viewpager.widget.PagerAdapter
import com.github.chrisbanes.photoview.PhotoView
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe

class BrowsePagerAdapter(private val glideRequests: GlideRequests): PagerAdapter() {

    private var type = -1
    private var postsDan: MutableList<PostDan> = mutableListOf()
    private var postsMoe: MutableList<PostMoe> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    fun updateData(posts: Any, type: Int) {
        this.type = type
        if (type == Constants.TYPE_DANBOORU) {
            postsDan = posts as MutableList<PostDan>
            Log.e("BrowsePagerAdapter", "Dan: ${postsDan.size}")
            postsMoe = mutableListOf()
        } else {
            postsMoe = posts as MutableList<PostMoe>
            Log.e("BrowsePagerAdapter", "Moe: ${postsMoe.size}")
            postsDan = mutableListOf()
        }
        notifyDataSetChanged()
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return if (type == Constants.TYPE_DANBOORU) postsDan.size else postsMoe.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context).inflate(R.layout.item_post_pager, null)
        val photoView = view.findViewById<PhotoView>(R.id.photo_view)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val url = when (type) {
            Constants.TYPE_DANBOORU -> postsDan[position].large_file_url
            Constants.TYPE_MOEBOORU -> postsMoe[position].sample_url
            else -> null
        }
        if (!url.isNullOrEmpty()) {
            glideRequests.load(url)
                .fitCenter()
                .into(photoView)
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}