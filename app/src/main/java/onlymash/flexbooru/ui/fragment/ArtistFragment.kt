package onlymash.flexbooru.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.SearchArtist
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.artist.ArtistRepository
import onlymash.flexbooru.ui.MainActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.ui.adapter.ArtistAdapter
import onlymash.flexbooru.ui.viewholder.ArtistViewHolder
import onlymash.flexbooru.ui.viewmodel.ArtistViewModel
import onlymash.flexbooru.widget.SearchBar

class ArtistFragment : ListFragment() {

    companion object {
        private const val TAG = "ArtistFragment"

        private const val ORDER_DEFAULT = ""
        private const val ORDER_DATE = "date"
        private const val ORDER_UPDATED_AT = "updated_at"
        private const val ORDER_NAME = "name"
        private const val ORDER_COUNT = "post_count"

        @JvmStatic
        fun newInstance(booru: Booru, user: User?) =
            ArtistFragment().apply {
                arguments = when (booru.type) {
                    Constants.TYPE_DANBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_DANBOORU)
                        if (user != null) {
                            putString(Constants.USERNAME_KEY, user.name)
                            putString(Constants.AUTH_KEY, user.api_key)
                        } else {
                            putString(Constants.USERNAME_KEY, "")
                            putString(Constants.AUTH_KEY, "")
                        }
                    }
                    Constants.TYPE_MOEBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_MOEBOORU)
                        if (user != null) {
                            putString(Constants.USERNAME_KEY, user.name)
                            putString(Constants.AUTH_KEY, user.password_hash)
                        } else {
                            putString(Constants.USERNAME_KEY, "")
                            putString(Constants.AUTH_KEY, "")
                        }
                    }
                    else -> throw IllegalArgumentException("unknown booru type ${booru.type}")
                }
            }
    }

    private var type = -1
    private var search: SearchArtist? = null

    override val searchBarHelper: SearchBarHelper
        get() = object : ListFragment.SearchBarHelper {
            override fun onMenuItemClick(menuItem: MenuItem) {
                when (menuItem.itemId) {
                    R.id.action_artist_order_default -> {
                        search!!.order = ORDER_DEFAULT
                        refresh()
                    }
                    R.id.action_artist_order_date -> {
                        when (type) {
                            Constants.TYPE_DANBOORU -> search!!.order = ORDER_UPDATED_AT
                            Constants.TYPE_MOEBOORU -> search!!.order = ORDER_DATE
                        }
                        refresh()
                    }
                    R.id.action_artist_order_name -> {
                        search!!.order = ORDER_NAME
                        refresh()
                    }
                    R.id.action_artist_order_count -> {
                        search!!.order = ORDER_COUNT
                        refresh()
                    }
                }
            }

            override fun onApplySearch(query: String) {
                search!!.name = query
                refresh()
            }
        }

    private fun refresh() {
        when (type) {
            Constants.TYPE_DANBOORU -> {
                swipe_refresh.isRefreshing = true
                artistViewModel.show(search!!)
                artistViewModel.refreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                swipe_refresh.isRefreshing = true
                artistViewModel.show(search!!)
                artistViewModel.refreshMoe()
            }
        }
    }

    private lateinit var artistViewModel: ArtistViewModel
    private lateinit var artistAdapter: ArtistAdapter

    private val itemListener = object : ArtistViewHolder.ItemListener {
        override fun onClickItem(keyword: String) {
            SearchActivity.startActivity(requireContext(), keyword)
        }
    }

    private val userListener = object : UserManager.Listener {
        override fun onAdd(user: User) {
            updateUserInfoAndRefresh(user)
        }
        override fun onDelete(user: User) {
            if (user.booru_uid != Settings.instance().activeBooruUid) return
            search!!.username = ""
            search!!.auth_key = ""
            when (type) {
                Constants.TYPE_DANBOORU -> {
                    artistViewModel.apply {
                        show(search!!)
                        refreshDan()
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    artistViewModel.apply {
                        show(search!!)
                        refreshMoe()
                    }
                }
            }
        }
        override fun onUpdate(user: User) {
            updateUserInfoAndRefresh(user)
        }
    }

    private fun updateUserInfoAndRefresh(user: User) {
        when (type) {
            Constants.TYPE_DANBOORU -> {
                search!!.username = user.name
                search!!.auth_key = user.api_key ?: ""
                artistViewModel.apply {
                    show(search!!)
                    refreshDan()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                search!!.username = user.name
                search!!.auth_key = user.password_hash ?: ""
                artistViewModel.apply {
                    show(search!!)
                    refreshMoe()
                }
            }
        }
    }

    private val navigationListener = object : MainActivity.NavigationListener {
        override fun onClickPosition(position: Int) {
            if (position == 4) {
                list.smoothScrollToPosition(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
            search = SearchArtist(
                scheme = it.getString(Constants.SCHEME_KEY, ""),
                host = it.getString(Constants.HOST_KEY, ""),
                name = "",
                order = ORDER_DEFAULT,
                username = it.getString(Constants.USERNAME_KEY, ""),
                auth_key = it.getString(Constants.AUTH_KEY, ""),
                limit = Settings.instance().pageSize
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_bar.setTitle(R.string.title_artists)
        search_bar.setEditTextHint(getString(R.string.search_bar_hint_search_artists))
        artistViewModel = getArtistViewModel(ServiceLocator.instance().getArtistRepository())
        if (search == null) return
        artistAdapter = ArtistAdapter(
            listener = itemListener,
            retryCallback = {
                when (type) {
                    Constants.TYPE_DANBOORU -> artistViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> artistViewModel.retryMoe()
                }
            }
        )
        list.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = artistAdapter
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                search_bar.setMenu(R.menu.artist_dan, requireActivity().menuInflater)
                artistViewModel.artistsDan.observe(this, Observer { artists ->
                    @Suppress("UNCHECKED_CAST")
                    artistAdapter.submitList(artists as PagedList<Any>)
                })
                artistViewModel.networkStateDan.observe(this, Observer { networkState ->
                    artistAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                search_bar.setMenu(R.menu.artist_moe, requireActivity().menuInflater)
                artistViewModel.artistsMoe.observe(this, Observer { artists ->
                    @Suppress("UNCHECKED_CAST")
                    artistAdapter.submitList(artists as PagedList<Any>)
                })
                artistViewModel.networkStateMoe.observe(this, Observer { networkState ->
                    artistAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
        }
        artistViewModel.show(search = search!!)
        UserManager.listeners.add(userListener)
        (requireActivity() as MainActivity).addNavigationListener(navigationListener)
    }

    private fun initSwipeToRefreshDan() {
        artistViewModel.refreshStateDan.observe(this, Observer<NetworkState> {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { artistViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshMoe() {
        artistViewModel.refreshStateMoe.observe(this, Observer<NetworkState> {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { artistViewModel.refreshMoe() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getArtistViewModel(repo: ArtistRepository): ArtistViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return ArtistViewModel(repo) as T
            }
        })[ArtistViewModel::class.java]
    }

    override fun onDestroy() {
        UserManager.listeners.remove(userListener)
        (requireActivity() as MainActivity).removeNavigationListener(navigationListener)
        super.onDestroy()
    }
}