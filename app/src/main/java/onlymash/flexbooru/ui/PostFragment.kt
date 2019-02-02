package onlymash.flexbooru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
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
import kotlinx.android.synthetic.main.refreshable_list.*
import kotlinx.android.synthetic.main.search_bar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.model.Search
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.post.PostRepository
import onlymash.flexbooru.ui.adapter.PostDanAdapter
import onlymash.flexbooru.ui.adapter.PostMoeAdapter
import onlymash.flexbooru.ui.viewmodel.PostViewModel

class PostFragment : Fragment() {

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param booru active booru
         * @param tags keyword of search
         * @return A new instance of fragment PostFragment.
         */
        @JvmStatic
        fun newInstance(booru: Booru, tags: String) =
            PostFragment().apply {
                arguments = when (booru.type) {
                    Constants.TYPE_DANBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_DANBOORU)
                        putString(Constants.TAGS_KEY, tags)
                    }
                    Constants.TYPE_MOEBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_MOEBOORU)
                        putString(Constants.TAGS_KEY, tags)
                    }
                    else -> throw IllegalArgumentException("unknown booru type ${booru.type}")
                }
            }
    }

    private lateinit var postViewModel: PostViewModel
    private lateinit var glide: GlideRequests

    private lateinit var leftDrawable: DrawerArrowDrawable

    private var scheme: String = Constants.NULL_STRING_VALUE
    private var host: String = Constants.NULL_STRING_VALUE
    private var type: Int = Constants.TYPE_UNKNOWN
    private var tags: String = Constants.EMPTY_STRING_VALUE
    private var limit: Int = 20

    private lateinit var search: Search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            scheme = it.getString(Constants.SCHEME_KEY, Constants.NULL_STRING_VALUE)
            host = it.getString(Constants.HOST_KEY, Constants.NULL_STRING_VALUE)
            type = it.getInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
            tags = it.getString(Constants.TAGS_KEY, Constants.EMPTY_STRING_VALUE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        leftDrawable = DrawerArrowDrawable(requireContext())
        menu_button.setImageDrawable(leftDrawable)
        menu_button.setOnClickListener {
            val activity = requireActivity()
            if (activity is MainActivity) activity.drawer.openDrawer()
        }
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
        postViewModel = getPostViewModel(ServiceLocator.instance().getPostRepository())
        glide = GlideApp.with(this)
        val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.STRETCH
        }
        list.apply {
            itemAnimator = null
            layoutManager = flexboxLayoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> glide.resumeRequests()
                        else -> glide.pauseRequests()
                    }
                }
            })
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                initPostDanAdapter()
            }
            Constants.TYPE_MOEBOORU -> {
                initPostMoeAdapter()
            }
            else -> {
                // unknown type
            }
        }
        search = Search(scheme = scheme, host = host, limit = limit, tags = tags)
        postViewModel.show(search)
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