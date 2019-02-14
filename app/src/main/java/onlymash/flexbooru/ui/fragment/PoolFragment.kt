package onlymash.flexbooru.ui.fragment

import android.content.Intent
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
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.pool.PoolRepository
import onlymash.flexbooru.ui.AccountActivity
import onlymash.flexbooru.ui.MainActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.ui.adapter.PoolAdapter
import onlymash.flexbooru.ui.viewholder.PoolViewHolder
import onlymash.flexbooru.ui.viewmodel.PoolViewModel
import onlymash.flexbooru.widget.SearchBar

class PoolFragment : ListFragment() {

    companion object {
        private const val TAG = "PoolFragment"

        @JvmStatic
        fun newInstance(keyword: String, booru: Booru, user: User?) =
            PoolFragment().apply {
                arguments = when (booru.type) {
                    Constants.TYPE_DANBOORU -> Bundle().apply {
                        putString(Constants.SCHEME_KEY, booru.scheme)
                        putString(Constants.HOST_KEY, booru.host)
                        putInt(Constants.TYPE_KEY, Constants.TYPE_DANBOORU)
                        putString(Constants.KEYWORD_KEY, keyword)
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
                        putString(Constants.KEYWORD_KEY, keyword)
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
    private var search: Search? = null

    override val helper: SearchBar.Helper
        get() = object : SearchBar.Helper {
            override fun onMenuItemClick(menuItem: MenuItem) {

            }
        }

    private lateinit var poolViewModel: PoolViewModel
    private lateinit var poolAdapter: PoolAdapter

    private val itemListener = object : PoolViewHolder.ItemListener {
        override fun onClickUserAvatar(id: Int, name: String?) {
            startActivity(Intent(requireContext(), AccountActivity::class.java).apply {
                putExtra(AccountActivity.USER_ID_KEY, id)
                putExtra(AccountActivity.USER_NAME_KEY, name)
            })
        }

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
                    poolViewModel.apply {
                        show(search!!)
                        refreshDan()
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    poolViewModel.apply {
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
                poolViewModel.apply {
                    show(search!!)
                    refreshDan()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                search!!.username = user.name
                search!!.auth_key = user.password_hash ?: ""
                poolViewModel.apply {
                    show(search!!)
                    refreshMoe()
                }
            }
        }
    }

    private val navigationListener = object : MainActivity.NavigationListener {
        override fun onClickPosition(position: Int) {
            if (position == 2) {
                list.smoothScrollToPosition(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
            search = Search(
                scheme = it.getString(Constants.SCHEME_KEY, ""),
                host = it.getString(Constants.HOST_KEY, ""),
                keyword = it.getString(Constants.KEYWORD_KEY, ""),
                username = it.getString(Constants.USERNAME_KEY, ""),
                auth_key = it.getString(Constants.AUTH_KEY, ""),
                limit = Settings.instance().pageSize)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_bar.setTitle(R.string.title_pools)
        poolViewModel = getPoolViewModel(ServiceLocator.instance().getPoolRepository())
        if (search == null) return
        val glide = GlideApp.with(this)
        poolAdapter = PoolAdapter(
            glide = glide,
            listener = itemListener,
            retryCallback = {
                when (type) {
                    Constants.TYPE_DANBOORU -> poolViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> poolViewModel.retryMoe()
                }
            }
        )
        list.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = poolAdapter
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                poolViewModel.poolsDan.observe(this, Observer { pools ->
                    @Suppress("UNCHECKED_CAST")
                    poolAdapter.submitList(pools as PagedList<Any>)
                })
                poolViewModel.networkStateDan.observe(this, Observer { networkState ->
                    poolAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                poolViewModel.poolsMoe.observe(this, Observer { pools ->
                    @Suppress("UNCHECKED_CAST")
                    poolAdapter.submitList(pools as PagedList<Any>)
                })
                poolViewModel.networkStateMoe.observe(this, Observer { networkState ->
                    poolAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
        }
        poolViewModel.show(search = search!!)
        UserManager.listeners.add(userListener)
        (requireActivity() as MainActivity).addNavigationListener(navigationListener)
    }

    private fun initSwipeToRefreshDan() {
        poolViewModel.refreshStateDan.observe(this, Observer<NetworkState> {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { poolViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshMoe() {
        poolViewModel.refreshStateMoe.observe(this, Observer<NetworkState> {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { poolViewModel.refreshMoe() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPoolViewModel(repo: PoolRepository): PoolViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PoolViewModel(repo) as T
            }
        })[PoolViewModel::class.java]
    }

    override fun onDestroy() {
        UserManager.listeners.remove(userListener)
        (requireActivity() as MainActivity).removeNavigationListener(navigationListener)
        super.onDestroy()
    }
}