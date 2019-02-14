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
import onlymash.flexbooru.entity.SearchTag
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.tag.TagRepository
import onlymash.flexbooru.ui.MainActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.ui.adapter.TagAdapter
import onlymash.flexbooru.ui.viewholder.TagViewHolder
import onlymash.flexbooru.ui.viewmodel.TagViewModel
import onlymash.flexbooru.widget.SearchBar

class TagFragment : ListFragment() {

    companion object {
        private const val TAG = "TagFragment"

        private const val TYPE_ALL = ""
        private const val TYPE_GENERAL = "0"
        private const val TYPE_ARTIST = "1"
        private const val TYPE_COPYRIGHT = "3"
        private const val TYPE_CHARACTER = "4"
        private const val TYPE_CIRCLE = "5"
        private const val TYPE_FAULTS = "6"
        private const val TYPE_META = "5"

        private const val ORDER_DATE = "date"
        private const val ORDER_NAME = "name"
        private const val ORDER_COUNT = "count"

        @JvmStatic
        fun newInstance(booru: Booru, user: User?) =
            TagFragment().apply {
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
    private var search: SearchTag? = null

    override val helper: SearchBar.Helper
        get() = object : SearchBar.Helper {
            override fun onMenuItemClick(menuItem: MenuItem) {
                when (menuItem.itemId) {
                    R.id.action_tag_order_date -> {
                        search!!.order = ORDER_DATE
                        refresh()
                    }
                    R.id.action_tag_order_name -> {
                        search!!.order = ORDER_NAME
                        refresh()
                    }
                    R.id.action_tag_order_count -> {
                        search!!.order = ORDER_COUNT
                        refresh()
                    }
                    R.id.action_tag_type_all -> {
                        search!!.type = TYPE_ALL
                        refresh()
                    }
                    R.id.action_tag_type_general -> {
                        search!!.type = TYPE_GENERAL
                        refresh()
                    }
                    R.id.action_tag_type_artist -> {
                        search!!.type = TYPE_ARTIST
                        refresh()
                    }
                    R.id.action_tag_type_copyright -> {
                        search!!.type = TYPE_COPYRIGHT
                        refresh()
                    }
                    R.id.action_tag_type_character -> {
                        search!!.type = TYPE_CHARACTER
                        refresh()
                    }
                    R.id.action_tag_type_circle -> {
                        search!!.type = TYPE_CIRCLE
                        refresh()
                    }
                    R.id.action_tag_type_faults -> {
                        search!!.type = TYPE_FAULTS
                        refresh()
                    }
                    R.id.action_tag_type_meta -> {
                        search!!.type = TYPE_META
                        refresh()
                    }
                }
            }
        }

    private fun refresh() {
        when (type) {
            Constants.TYPE_DANBOORU -> {
                swipe_refresh.isRefreshing = true
                tagViewModel.show(search!!)
                tagViewModel.refreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                swipe_refresh.isRefreshing = true
                tagViewModel.show(search!!)
                tagViewModel.refreshMoe()
            }
        }
    }

    private lateinit var tagViewModel: TagViewModel
    private lateinit var tagAdapter: TagAdapter

    private val itemListener = object : TagViewHolder.ItemListener {
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
                    tagViewModel.apply {
                        show(search!!)
                        refreshDan()
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    tagViewModel.apply {
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
                tagViewModel.apply {
                    show(search!!)
                    refreshDan()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                search!!.username = user.name
                search!!.auth_key = user.password_hash ?: ""
                tagViewModel.apply {
                    show(search!!)
                    refreshMoe()
                }
            }
        }
    }

    private val navigationListener = object : MainActivity.NavigationListener {
        override fun onClickPosition(position: Int) {
            if (position == 3) {
                list.smoothScrollToPosition(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
            search = SearchTag(
                scheme = it.getString(Constants.SCHEME_KEY, ""),
                host = it.getString(Constants.HOST_KEY, ""),
                name = "",
                order = ORDER_DATE,
                type = TYPE_ALL,
                username = it.getString(Constants.USERNAME_KEY, ""),
                auth_key = it.getString(Constants.AUTH_KEY, ""),
                limit = Settings.instance().pageSize
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_bar.setTitle(R.string.title_tags)
        tagViewModel = getTagViewModel(ServiceLocator.instance().getTagRepository())
        if (search == null) return
        tagAdapter = TagAdapter(
            listener = itemListener,
            retryCallback = {
                when (type) {
                    Constants.TYPE_DANBOORU -> tagViewModel.retryDan()
                    Constants.TYPE_MOEBOORU -> tagViewModel.retryMoe()
                }
            }
        )
        list.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = tagAdapter
        }
        when (type) {
            Constants.TYPE_DANBOORU -> {
                search_bar.setMenu(R.menu.tag_dan, requireActivity().menuInflater)
                tagViewModel.tagsDan.observe(this, Observer { tags ->
                    @Suppress("UNCHECKED_CAST")
                    tagAdapter.submitList(tags as PagedList<Any>)
                })
                tagViewModel.networkStateDan.observe(this, Observer { networkState ->
                    tagAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                search_bar.setMenu(R.menu.tag_moe, requireActivity().menuInflater)
                tagViewModel.tagsMoe.observe(this, Observer { tags ->
                    @Suppress("UNCHECKED_CAST")
                    tagAdapter.submitList(tags as PagedList<Any>)
                })
                tagViewModel.networkStateMoe.observe(this, Observer { networkState ->
                    tagAdapter.setNetworkState(networkState)
                })
                initSwipeToRefreshMoe()
            }
        }
        tagViewModel.show(search = search!!)
        UserManager.listeners.add(userListener)
        (requireActivity() as MainActivity).addNavigationListener(navigationListener)
    }

    private fun initSwipeToRefreshDan() {
        tagViewModel.refreshStateDan.observe(this, Observer<NetworkState> {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { tagViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshMoe() {
        tagViewModel.refreshStateMoe.observe(this, Observer<NetworkState> {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { tagViewModel.refreshMoe() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getTagViewModel(repo: TagRepository): TagViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return TagViewModel(repo) as T
            }
        })[TagViewModel::class.java]
    }

    override fun onDestroy() {
        UserManager.listeners.remove(userListener)
        (requireActivity() as MainActivity).removeNavigationListener(navigationListener)
        super.onDestroy()
    }
}