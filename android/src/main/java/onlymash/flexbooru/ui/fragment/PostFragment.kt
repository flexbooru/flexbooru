package onlymash.flexbooru.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Keys.PAGE_TYPE
import onlymash.flexbooru.common.Keys.POST_QUERY
import onlymash.flexbooru.common.Settings.gridWidthResId
import onlymash.flexbooru.common.Settings.pageLimit
import onlymash.flexbooru.common.Settings.safeMode
import onlymash.flexbooru.common.Values.PAGE_TYPE_POPULAR
import onlymash.flexbooru.common.Values.PAGE_TYPE_POSTS
import onlymash.flexbooru.data.action.ActionPost
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.MyDatabase
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.data.repository.post.PostRepositoryImpl
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.viewmodel.PostViewModel
import onlymash.flexbooru.ui.viewmodel.getPostViewModel
import org.kodein.di.erased.instance
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.math.roundToInt

private const val SCALE_DAY = "day"
private const val SCALE_WEEK = "week"
private const val SCALE_MONTH = "month"
private const val PERIOD_DAY = "1d"
private const val PERIOD_WEEK = "1w"
private const val PERIOD_MONTH = "1m"
private const val PERIOD_YEAR = "1y"

class PostFragment : ListFragment() {

    private lateinit var date: ActionPost.Date

    private var action: ActionPost? = null
    private var currentPageType: Int = PAGE_TYPE_POSTS
    private lateinit var query: String
    private lateinit var postViewModel: PostViewModel

    private val db by instance<MyDatabase>()
    private val booruApis by instance<BooruApis>()
    private val ioExecutor by instance<Executor>()

    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPageType = arguments?.getInt(PAGE_TYPE, PAGE_TYPE_POSTS) ?: PAGE_TYPE_POSTS
        query = if (currentPageType == PAGE_TYPE_POPULAR) {
            "order:popular"
        } else {
            activity?.intent?.getStringExtra(POST_QUERY) ?: ""
        }
        initDate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postViewModel = getPostViewModel(PostRepositoryImpl(
            booruApis = booruApis,
            db = db,
            ioExecutor = ioExecutor
        ))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val glide = GlideApp.with(this)
        postAdapter = PostAdapter(glide) {
            postViewModel.retry()
        }
        list.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount, RecyclerView.VERTICAL)
            adapter = postAdapter
        }
        postViewModel.posts.observe(viewLifecycleOwner, Observer {
            postAdapter.submitList(it)
        })
        postViewModel.networkState.observe(viewLifecycleOwner, Observer {
            postAdapter.setNetworkState(it)
        })
        postViewModel.refreshState.observe(viewLifecycleOwner, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            postViewModel.refresh()
        }
    }

    override fun onBooruLoaded(booru: Booru?) {
        if (booru != null) {
            if (action == null) {
                action = ActionPost(
                    booru = booru,
                    pageType = currentPageType,
                    limit = pageLimit,
                    date = date,
                    isSafeMode = safeMode,
                    query = query
                )
            } else {
                action?.booru = booru
            }
        } else {
            action == null
        }
        postViewModel.show(action)
    }

    private fun initDate() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            timeInMillis = System.currentTimeMillis()
        }
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        date = ActionPost.Date(
            day = currentDay,
            month = currentMonth,
            year = currentYear,
            dayEnd = currentDay,
            monthEnd = currentMonth,
            yearEnd = currentYear
        )
    }

    private val spanCount: Int
        get() = activity?.getSanCount() ?: 3

    private fun Activity.getSanCount(): Int {
        val itemWidth = resources.getDimensionPixelSize(gridWidthResId)
        val count = (getWindowWidth().toFloat() / itemWidth.toFloat()).roundToInt()
        return if (count < 1) 1 else count
    }

    private fun Activity.getWindowWidth(): Int {
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    override fun getSearchBarHint(): CharSequence {
        return getString(R.string.search_bar_hint_search_posts)
    }
}