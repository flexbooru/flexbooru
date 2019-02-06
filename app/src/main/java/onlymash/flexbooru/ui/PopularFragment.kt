package onlymash.flexbooru.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
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
import onlymash.flexbooru.repository.popular.PopularRepository
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.viewholder.PostViewHolder
import onlymash.flexbooru.ui.viewmodel.PopularViewModel
import onlymash.flexbooru.widget.AutoStaggeredGridLayoutManager
import onlymash.flexbooru.widget.SearchBar
import onlymash.flexbooru.widget.SearchBarMover
import java.util.*

private const val SCALE_DAY = "day"
private const val SCALE_WEEK = "week"
private const val SCALE_MONTH = "month"
private const val PERIOD_DAY = "1d"
private const val PERIOD_WEEK = "1w"
private const val PERIOD_MONTH = "1m"
private const val PERIOD_YEAR = "1y"

/**
 * Use the [PopularFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PopularFragment : Fragment() {

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param booru
         * @return A new instance of fragment PopularFragment.
         */
        @JvmStatic
        fun newInstance(booru: Booru) =
            PopularFragment().apply {
                arguments = when (booru.type) {
                    Constants.TYPE_DANBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_DANBOORU)
                    }
                    Constants.TYPE_MOEBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_MOEBOORU)
                    }
                    else -> throw IllegalArgumentException("unknown booru type ${booru.type}")
                }
            }

        private const val STATE_NORMAL = 0
        private const val STATE_SEARCH = 1
    }
    private var scheme: String = Constants.NULL_STRING_VALUE
    private var host: String = Constants.NULL_STRING_VALUE
    private var type: Int = Constants.TYPE_UNKNOWN
    private var date: String = Constants.EMPTY_STRING_VALUE

    private lateinit var popular: Popular

    private var state = STATE_NORMAL

    private lateinit var searchBarMover: SearchBarMover

    private val sbMoverHelper = object : SearchBarMover.Helper {
        override val validRecyclerView get() = list

        override fun isValidView(recyclerView: RecyclerView): Boolean {
            return state == STATE_NORMAL && recyclerView == list
        }

        override fun forceShowSearchBar(): Boolean {
            return state == STATE_SEARCH
        }
    }

    private val itemListener = object : PostViewHolder.ItemListener {
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
    }

    private lateinit var popularViewModel: PopularViewModel
    private lateinit var glide: GlideRequests

    private lateinit var leftDrawable: DrawerArrowDrawable

    private lateinit var postAdapter: PostAdapter

    private val helper = object : SearchBar.Helper {

        override fun onLeftButtonClick() {
            val activity = requireActivity()
            if (activity is MainActivity) activity.drawer.openDrawer()
        }

        override fun onMenuItemClick(menuItem: MenuItem) {
            when (type) {
                Constants.TYPE_DANBOORU -> {
                    when (menuItem.itemId) {
                        R.id.action_date -> {
                            val currentTimeMillis = System.currentTimeMillis()
                            val currentCalendar = Calendar.getInstance(Locale.getDefault()).apply {
                                timeInMillis = currentTimeMillis
                            }
                            val minCalendar = Calendar.getInstance(Locale.getDefault()).apply {
                                timeInMillis = currentTimeMillis
                                add(Calendar.YEAR, -20)
                            }
                            DatePickerDialog(
                                requireContext(),
                                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                    val yearString = year.toString()
                                    val realMonth = month + 1
                                    val monthString = if (realMonth < 10) "0$realMonth" else realMonth.toString()
                                    val dayString = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                                    date = "$yearString-$monthString-$dayString"
                                    popular.date = date
                                    popularViewModel.show(popular)
                                    swipe_refresh.isRefreshing = true
                                    popularViewModel.refreshDan()
                                },
                                currentCalendar.get(Calendar.YEAR),
                                currentCalendar.get(Calendar.MONTH),
                                currentCalendar.get(Calendar.DAY_OF_MONTH)
                            ).apply {
                                datePicker.minDate = minCalendar.timeInMillis
                                datePicker.maxDate = currentTimeMillis
                            }
                                .show()
                        }
                        R.id.action_day -> {
                            popular.scale = SCALE_DAY
                            popularViewModel.show(popular)
                            swipe_refresh.isRefreshing = true
                            popularViewModel.refreshDan()
                        }
                        R.id.action_week -> {
                            popular.scale = SCALE_WEEK
                            popularViewModel.show(popular)
                            swipe_refresh.isRefreshing = true
                            popularViewModel.refreshDan()
                        }
                        R.id.action_month -> {
                            popular.scale = SCALE_MONTH
                            popularViewModel.show(popular)
                            swipe_refresh.isRefreshing = true
                            popularViewModel.refreshDan()
                        }
                        else -> throw IllegalArgumentException("unknown menu item. title: ${menuItem.title}")
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    when (menuItem.itemId) {
                        R.id.action_day -> popular.period = PERIOD_DAY
                        R.id.action_week -> popular.period = PERIOD_WEEK
                        R.id.action_month -> popular.period = PERIOD_MONTH
                        R.id.action_year -> popular.period = PERIOD_YEAR
                        else -> throw IllegalArgumentException("unknown menu item. title: ${menuItem.title}")
                    }
                    popularViewModel.show(popular)
                    swipe_refresh.isRefreshing = true
                    popularViewModel.refreshMoe()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            scheme = it.getString(Constants.SCHEME_KEY, Constants.NULL_STRING_VALUE)
            host = it.getString(Constants.HOST_KEY, Constants.NULL_STRING_VALUE)
            type = it.getInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        leftDrawable = DrawerArrowDrawable(requireContext())
        when (type) {
            Constants.TYPE_DANBOORU -> search_bar.setMenu(R.menu.popular_dan, requireActivity().menuInflater)
            Constants.TYPE_MOEBOORU -> search_bar.setMenu(R.menu.popular_moe, requireActivity().menuInflater)
            else -> throw IllegalArgumentException("unknown type $type")
        }
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
        popularViewModel = getPopularViewModel(ServiceLocator.instance().getPopularRepository())
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
                    Constants.TYPE_DANBOORU -> popularViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> popularViewModel.retryMoe()
                }
            })
        list.apply {
            setHasFixedSize(true)
            layoutManager = staggeredGridLayoutManager
            adapter = postAdapter
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                popularViewModel.postsDan.observe(this, Observer<PagedList<PostDan>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<Any>)
                })
                popularViewModel.networkStateDan.observe(this, Observer { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                popularViewModel.postsMoe.observe(this, Observer<PagedList<PostMoe>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<Any>)
                })
                popularViewModel.networkStateMoe.observe(this, Observer { networkState ->
                    postAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
        }
        popular = Popular(
            scheme = scheme,
            host = host,
            date = date,
            scale = SCALE_DAY,
            period = PERIOD_DAY)
        popularViewModel.show(popular)
    }

    private fun initSwipeToRefreshDan() {
        popularViewModel.refreshStateDan.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { popularViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshMoe() {
        popularViewModel.refreshStateMoe.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { popularViewModel.refreshMoe() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPopularViewModel(repo: PopularRepository): PopularViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PopularViewModel(repo) as T
            }
        })[PopularViewModel::class.java]
    }
}
