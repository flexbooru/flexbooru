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
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.App.Companion.app

import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.model.Popular
import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.popular.PopularRepository
import onlymash.flexbooru.ui.adapter.PostDanAdapter
import onlymash.flexbooru.ui.adapter.PostMoeAdapter
import onlymash.flexbooru.ui.viewmodel.PopularViewModel

private const val ARG_SCHEME = "scheme"
private const val ARG_HOST = "host"
private const val ARG_TYPE = "type"
private const val TYPE_DANBOORU = "type_danbooru"
private const val TYPE_MOEBOORU = "type_moebooru"

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
                    0 -> Bundle().apply {
                        putString(ARG_SCHEME, booru.scheme)
                        putString(ARG_HOST, booru.host)
                        putString(ARG_TYPE, TYPE_DANBOORU)
                    }
                    else -> Bundle().apply {
                        putString(ARG_SCHEME, booru.scheme)
                        putString(ARG_HOST, booru.host)
                        putString(ARG_TYPE, TYPE_MOEBOORU)
                    }
                }
            }
    }
    private var scheme: String? = null
    private var host: String? = null
    private var type: String? = null

    private lateinit var popularViewModel: PopularViewModel
    private lateinit var glide: GlideRequests

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            scheme = it.getString(ARG_SCHEME)
            host = it.getString(ARG_HOST)
            type = it.getString(ARG_TYPE)
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
        popularViewModel = getPopularViewModel(app.serviceLocator.getPopularRepository())
        glide = GlideApp.with(this)
        val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.STRETCH
        }
        list.layoutManager = flexboxLayoutManager
        when (type) {
            TYPE_DANBOORU -> {
                initPostDanAdapter()
                popularViewModel.show(
                    Popular(
                        scheme = scheme!!,
                        host = host!!,
                        date = null,
                        scale = null,
                        period = null))
            }
            TYPE_MOEBOORU -> {
                initPostMoeAdapter()
                popularViewModel.show(
                    Popular(
                        scheme = scheme!!,
                        host = host!!,
                        date = null,
                        scale = null,
                        period = "1d"))
            }
        }
    }

    private fun initPostDanAdapter() {
        val postDanAdapter = PostDanAdapter(glide, requireActivity()) {
            popularViewModel.retryDan()
        }
        list.adapter = postDanAdapter
        popularViewModel.postsDan.observe(this, Observer<PagedList<PostDan>> { posts ->
            postDanAdapter.submitList(posts)
        })
        popularViewModel.networkStateDan.observe(this, Observer { networkState ->
            postDanAdapter.setNetworkState(networkState)
        })
        initSwipeToRefreshDan()
    }

    private fun initPostMoeAdapter() {
        val postMoeAdapter = PostMoeAdapter(glide, requireActivity()) {
            popularViewModel.retryMoe()
        }
        list.adapter = postMoeAdapter
        popularViewModel.postsMoe.observe(this, Observer<PagedList<PostMoe>> { posts ->
            postMoeAdapter.submitList(posts)
        })
        popularViewModel.networkStateMoe.observe(this, Observer { networkState ->
            postMoeAdapter.setNetworkState(networkState)
        })
        initSwipeToRefreshMoe()
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
