package onlymash.flexbooru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.fragment_post.*
import onlymash.flexbooru.App.Companion.app
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.model.Search
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.post.PostRepository
import onlymash.flexbooru.ui.adapter.PostDanAdapter
import onlymash.flexbooru.ui.adapter.PostMoeAdapter
import onlymash.flexbooru.ui.viewmodel.PostViewModel

class PostFragment : Fragment() {

    private lateinit var postViewModel: PostViewModel
    private lateinit var glide: GlideRequests

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        postViewModel.show(Search(scheme = "https", host = "danbooru.donmai.us", limit = 20, tags = ""))
//        postViewModel.show(Search(scheme = "https", host = "yande.re", limit = 20, tags = ""))
//        postViewModel.show(Search(scheme = "https", host = "konachan.com", limit = 20, tags = ""))
    }

    private fun init() {
        val start = resources.getDimensionPixelSize(R.dimen.swipe_refresh_layout_offset_start)
        val end = resources.getDimensionPixelSize(R.dimen.swipe_refresh_layout_offset_end)
        swipe_refresh.apply {
            setProgressViewOffset(false, start, end)
            setColorSchemeResources(
                R.color.blue,
                R.color.purple,
                R.color.green,
                R.color.orange,
                R.color.red
            )
        }
        postViewModel = getPostViewModel(app.serviceLocator.getPostRepository())
        glide = GlideApp.with(this)
        val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.STRETCH
        }
        list.layoutManager = flexboxLayoutManager
        list.itemAnimator = null
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> glide.resumeRequests()
                    else -> glide.pauseRequests()
                }
            }
        })
        initPostDanAdapter()
//        initPostMoeAdapter()
    }


    private fun initPostDanAdapter() {
        val postDanAdapter = PostDanAdapter(glide, requireActivity()) {
            postViewModel.retryDan()
        }
        list.adapter = postDanAdapter
        postViewModel.postsDan.observe(this, Observer<PagedList<PostDan>> { posts ->
            postDanAdapter.submitList(posts)
        })
        postViewModel.networkStateDan.observe(this, Observer { networkState ->
            postDanAdapter.setNetworkState(networkState)
        })
        initSwipeToRefreshDan()
    }

    private fun initPostMoeAdapter() {
        val postMoeAdapter = PostMoeAdapter(glide, requireActivity()) {
            postViewModel.retryMoe()
        }
        list.adapter = postMoeAdapter
        postViewModel.postsMoe.observe(this, Observer<PagedList<PostMoe>> { posts ->
            postMoeAdapter.submitList(posts)
        })
        postViewModel.networkStateMoe.observe(this, Observer { networkState ->
            postMoeAdapter.setNetworkState(networkState)
        })
        initSwipeToRefreshMoe()
    }

    private fun initSwipeToRefreshDan() {
        postViewModel.refreshStateDan.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshMoe() {
        postViewModel.refreshStateMoe.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshMoe() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPostViewModel(repo: PostRepository): PostViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PostViewModel(repo) as T
            }
        })[PostViewModel::class.java]
    }
}