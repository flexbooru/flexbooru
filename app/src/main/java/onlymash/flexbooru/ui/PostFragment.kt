package onlymash.flexbooru.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_post.*
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.*
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.post.PostRepository
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.viewholder.PostViewHolder
import onlymash.flexbooru.ui.viewmodel.PostViewModel
import onlymash.flexbooru.widget.AutoStaggeredGridLayoutManager
import onlymash.flexbooru.widget.SearchBar
import onlymash.flexbooru.widget.SearchBarMover

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
        private const val STATE_NORMAL = 0
        private const val STATE_SEARCH = 1
    }

    private lateinit var postViewModel: PostViewModel
    private lateinit var glide: GlideRequests

    private lateinit var leftDrawable: DrawerArrowDrawable

    private lateinit var postAdapter: PostAdapter

    private var state = STATE_NORMAL

    private var scheme: String = Constants.NULL_STRING_VALUE
    private var host: String = Constants.NULL_STRING_VALUE
    private var type: Int = Constants.TYPE_UNKNOWN
    private var tags: String = Constants.EMPTY_STRING_VALUE
    private var limit: Int = 10

    private lateinit var search: Search

    private lateinit var searchBarMover: SearchBarMover

    private val helper = object : SearchBar.Helper {

        override fun onLeftButtonClick() {
            val activity = requireActivity()
            if (activity is MainActivity) activity.drawer.openDrawer()
        }

        override fun onMenuItemClick(menuItem: MenuItem) {

        }
    }

    private val sbMoverHelper = object : SearchBarMover.Helper {
        override val validRecyclerView get() = list

        override fun isValidView(recyclerView: RecyclerView): Boolean {
            return state == STATE_NORMAL && recyclerView == list
        }

        override fun forceShowSearchBar(): Boolean {
            return state == STATE_SEARCH
        }
    }

    private val itemListener: PostViewHolder.ItemListener = object : PostViewHolder.ItemListener {
        override fun onClickDanItem(post: PostDan) {
            val intent = Intent(requireContext(), BrowseActivity::class.java)
                .apply {
                    putExtra(Constants.ID_KEY, post.id)
                    putExtra(Constants.HOST_KEY, post.host)
                    putExtra(Constants.TYPE_KEY, Constants.TYPE_DANBOORU)
                    putExtra(Constants.TAGS_KEY, post.keyword)
                }
            startActivity(intent)
        }
        override fun onClickMoeItem(post: PostMoe) {
            val intent = Intent(requireContext(), BrowseActivity::class.java)
                .apply {
                    putExtra(Constants.ID_KEY, post.id)
                    putExtra(Constants.HOST_KEY, post.host)
                    putExtra(Constants.TYPE_KEY, Constants.TYPE_MOEBOORU)
                    putExtra(Constants.TAGS_KEY, post.keyword)
                }
            startActivity(intent)
        }

    }

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
        search_bar.setLeftDrawable(leftDrawable)
        search_bar.setHelper(helper)
        searchBarMover = SearchBarMover(sbMoverHelper, search_bar, list)
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
        val staggeredGridLayoutManager = AutoStaggeredGridLayoutManager(
            columnSize = resources.getDimensionPixelSize(R.dimen.post_item_width),
            orientation = StaggeredGridLayoutManager.VERTICAL).apply {
                setStrategy(AutoStaggeredGridLayoutManager.STRATEGY_SUITABLE_SIZE)
            }
        postAdapter = PostAdapter(
            glide = glide,
            placeholder = Placeholder.create(
                resources = resources,
                theme = requireActivity().theme),
            listener = itemListener,
            retryCallback = {
                when (type) {
                    Constants.TYPE_DANBOORU -> postViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> postViewModel.retryMoe()
                }
            })
        list.apply {
            setHasFixedSize(true)
            layoutManager = staggeredGridLayoutManager
            adapter = postAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    Log.e("PostFragment", "newState: $newState")
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> glide.resumeRequests()
                        else -> glide.pauseRequests()
                    }
                }
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    Log.e("PostFragment", "onScrolled")
                }
            })
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                postViewModel.postsDan.observe(this, Observer<PagedList<PostDan>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<Any>)
                })
                postViewModel.networkStateDan.observe(this, Observer { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                postViewModel.postsMoe.observe(this, Observer<PagedList<PostMoe>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<Any>)
                })
                postViewModel.networkStateMoe.observe(this, Observer { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
        }
        search = Search(scheme = scheme, host = host, limit = limit, tags = tags)
        postViewModel.show(search)
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