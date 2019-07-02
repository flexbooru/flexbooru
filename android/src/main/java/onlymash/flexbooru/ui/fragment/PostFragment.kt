/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.ui.fragment

import android.content.*
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.empty_list_network_state.*
import kotlinx.android.synthetic.main.refreshable_list.*
import kotlinx.android.synthetic.main.search_layout.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.animation.RippleAnimation
import onlymash.flexbooru.database.*
import onlymash.flexbooru.database.dao.TagBlacklistDao
import onlymash.flexbooru.entity.*
import onlymash.flexbooru.entity.post.*
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.extension.getViewModel
import onlymash.flexbooru.extension.gridWidth
import onlymash.flexbooru.extension.rotate
import onlymash.flexbooru.extension.toVisibility
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.favorite.VoteRepositoryImpl
import onlymash.flexbooru.repository.post.PostRepositoryImpl
import onlymash.flexbooru.repository.post.PostRepository
import onlymash.flexbooru.repository.tagfilter.TagFilterRepositoryImpl
import onlymash.flexbooru.repository.tagfilter.TagFilterRepository
import onlymash.flexbooru.ui.activity.*
import onlymash.flexbooru.ui.adapter.PostAdapter
import onlymash.flexbooru.ui.adapter.TagFilterAdapter
import onlymash.flexbooru.ui.viewholder.PostViewHolder
import onlymash.flexbooru.ui.viewmodel.PostViewModel
import onlymash.flexbooru.ui.viewmodel.TagBlacklistViewModel
import onlymash.flexbooru.ui.viewmodel.TagFilterViewModel
import onlymash.flexbooru.util.*
import onlymash.flexbooru.widget.AutoStaggeredGridLayoutManager
import onlymash.flexbooru.widget.search.SearchBar
import onlymash.flexbooru.worker.DownloadWorker
import org.kodein.di.erased.instance

class PostFragment : ListFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "PostFragment"

        private const val SHOW_SEARCH_LAYOUT_KEY = "show_search_layout"
        private const val POST_TYPE_KEY = "post_type"
        const val POST_ALL = 0
        const val POST_SEARCH = 1
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param booru active booru
         * @param keyword keyword of search
         * @param user account info
         * @return A new instance of fragment PostFragment.
         */
        @JvmStatic
        fun newInstance(keyword: String = "", booru: Booru, user: User?, postType: Int = POST_SEARCH) =
            PostFragment().apply {
                arguments = Bundle().apply {
                    putInt(POST_TYPE_KEY, postType)
                    putString(Constants.SCHEME_KEY, booru.scheme)
                    putString(Constants.HOST_KEY, booru.host)
                    putInt(Constants.TYPE_KEY, booru.type)
                    putString(Constants.KEYWORD_KEY, keyword)
                    if (user != null) {
                        putInt(Constants.USER_ID_KEY, user.id)
                        putString(Constants.USERNAME_KEY, user.name)
                        putString(
                            Constants.AUTH_KEY,
                            when (booru.type) {
                                Constants.TYPE_DANBOORU -> user.api_key
                                else -> user.password_hash
                            }
                            )
                    } else {
                        putString(Constants.USERNAME_KEY, "")
                        putString(Constants.AUTH_KEY, "")
                    }
                }
            }
    }

    private val db: FlexbooruDatabase by instance()
    private val tagFilterRepositoryImpl: TagFilterRepositoryImpl by instance()
    private val tagBlacklistDao: TagBlacklistDao by instance()
    private val sp: SharedPreferences by instance()

    private lateinit var tagBlacklistViewModel: TagBlacklistViewModel
    private lateinit var postViewModel: PostViewModel
    private lateinit var glide: GlideRequests

    private lateinit var postAdapter: PostAdapter

    private lateinit var viewTransition: ViewTransition

    private var postType = -1
    private var booruType = -1
    private lateinit var search: Search
    private lateinit var searchTag: SearchTag
    private lateinit var rightButton: View
    private var tagBlacklists: MutableList<TagBlacklist> = mutableListOf()

    override val stateChangeListener: SearchBar.StateChangeListener
        get() = object : SearchBar.StateChangeListener {
            override fun onStateChange(newState: Int, oldState: Int, animation: Boolean) {
                rightButton.rotate(135f)
                if (requireActivity() is MainActivity) {
                    toggleArrowLeftDrawable()
                }
                if (newState == SearchBar.STATE_NORMAL && search_layout.visibility == View.VISIBLE) {
                    RippleAnimation.create(searchBar.getLeftButton()).setDuration(300).start()
                    viewTransition.showView(0, false)
                }
            }
        }

    private lateinit var tagFilterAdapter: TagFilterAdapter
    private lateinit var tagFilterViewModel: TagFilterViewModel

    override val searchBarHelper: SearchBarHelper
        get() = object : SearchBarHelper {
            override fun onMenuItemClick(menuItem: MenuItem) {
                if (menuItem.itemId == R.id.action_expand_or_clear) {
                    when {
                        !searchBar.isSearchState() && search_layout.visibility != View.VISIBLE -> {
                            searchBar.toExpandState(false)
                            RippleAnimation.create(rightButton).setDuration(300).start()
                            viewTransition.showView(1, false)
                        }
                        else -> searchBar.setText("")
                    }
                }
            }
            override fun onApplySearch(query: String) {
                if (query.isNotEmpty() && query != search.keyword) SearchActivity.startActivity(requireContext(), query)
            }
        }

    private val voteRepo by lazy {
        VoteRepositoryImpl(
            danbooruApi = danApi,
            danbooruOneApi = danOneApi,
            moebooruApi = moeApi,
            sankakuApi = sankakuApi,
            gelbooruApi = gelApi,
            db = db
        )
    }

    private fun setSharedElement() {
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.sharedElement = list.findViewWithTag<View>(currentPostId)?.findViewById(R.id.preview)
        } else if (activity is SearchActivity) {
            activity.sharedElement = list.findViewWithTag<View>(currentPostId)?.findViewById(R.id.preview)
        }
    }

    private val itemListener: PostViewHolder.ItemListener = object : PostViewHolder.ItemListener {
        override fun onClickItem(post: PostBase?, view: View) {
            if (post == null) return
            currentPostId = post.getPostId()
            setSharedElement()
            BrowseActivity.startActivity(requireActivity(), view, post.getPostId(), post.keyword, Constants.PAGE_TYPE_POST)
        }

        override fun onLongClickItem(post: PostBase?) {
            if (post == null) return
            val context = context ?: return
            val id = post.getPostId()
            AlertDialog.Builder(context)
                .setTitle("Post $id")
                .setItems(context.resources.getTextArray(R.array.post_item_action)) { _, which ->
                    when (which) {
                        0 -> {
                            DownloadWorker.downloadPost(post, requireActivity())
                        }
                        1 -> {
                            if (post is PostGel) {
                                if (search.username.isEmpty()) {
                                    requireActivity().startActivity(Intent(requireActivity(), AccountConfigActivity::class.java))
                                } else {
                                    val vote = Vote(
                                        scheme = search.scheme,
                                        host = search.host,
                                        post_id = id,
                                        username = search.username,
                                        auth_key = search.auth_key
                                    )
                                    GlobalScope.launch {
                                        voteRepo.addGelFav(vote, post)
                                    }
                                }
                                return@setItems
                            }
                            if (search.auth_key.isEmpty()) {
                                requireActivity().startActivity(Intent(requireActivity(), AccountConfigActivity::class.java))
                            } else {
                                val vote = Vote(
                                    scheme = search.scheme,
                                    host = search.host,
                                    post_id = id,
                                    score = 3,
                                    username = search.username,
                                    auth_key = search.auth_key
                                )
                                GlobalScope.launch {
                                    when (post) {
                                        is PostDan -> voteRepo.addDanFav(vote, post)
                                        is PostMoe -> voteRepo.voteMoePost(vote)
                                        is PostDanOne -> voteRepo.addDanOneFav(vote, post)
                                        is PostSankaku -> voteRepo.addSankakuFav(vote, post)
                                    }
                                }
                            }
                        }
                        2 -> {
                            SauceNaoActivity.startSearch(context, post.getSampleUrl())
                        }
                    }
                }
                .create()
                .show()
        }
    }

    private val navigationListener by lazy {
        object : MainActivity.NavigationListener {
            override fun onClickPosition(position: Int) {
                showSearchBar()
                if (position == 0) list.scrollToPosition(0)
            }
        }
    }

    private var currentPostId = 0

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val bundle= intent.extras ?: return
            val pos = bundle.getInt(BrowseActivity.EXT_POST_POSITION_KEY, -1)
            val key = bundle.getString(BrowseActivity.EXT_POST_KEYWORD_KEY)
            if (pos >= 0 && search.keyword == key) {
                currentPostId = bundle.getInt(BrowseActivity.EXT_POST_ID_KEY, currentPostId)
                if (postAdapter.itemCount > pos + 1) {
                    list.scrollToPosition(pos + 1)
                }
                setSharedElement()
            }
        }
    }

    private val userListener = object : UserManager.Listener {
        override fun onAdd(user: User) {
            updateUserInfoAndRefresh(user)
        }
        override fun onDelete(user: User) {
            if (user.booru_uid != Settings.activeBooruUid) return
            search.username = ""
            search.auth_key = ""
            when (booruType) {
                Constants.TYPE_DANBOORU -> {
                    postViewModel.apply {
                        show(search, tagBlacklists)
                        refreshDan()
                    }
                }
                Constants.TYPE_MOEBOORU -> {
                    postViewModel.apply {
                        show(search, tagBlacklists)
                        refreshMoe()
                    }
                }
                Constants.TYPE_DANBOORU_ONE -> {
                    postViewModel.apply {
                        show(search, tagBlacklists)
                        refreshDanOne()
                    }
                }
                Constants.TYPE_GELBOORU -> {
                    postViewModel.apply {
                        show(search, tagBlacklists)
                        refreshGel()
                    }
                }
                Constants.TYPE_SANKAKU -> {
                    postViewModel.apply {
                        show(search, tagBlacklists)
                        refreshSankaku()
                    }
                }
            }
        }
        override fun onUpdate(user: User) {
            updateUserInfoAndRefresh(user)
        }
    }

    private fun updateUserInfoAndRefresh(user: User) {
        when (booruType) {
            Constants.TYPE_DANBOORU -> {
                search.username = user.name
                search.auth_key = user.api_key ?: ""
                postViewModel.apply {
                    show(search, tagBlacklists)
                    refreshDan()
                }
            }
            Constants.TYPE_MOEBOORU -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                postViewModel.apply {
                    show(search, tagBlacklists)
                    refreshMoe()
                }
            }
            Constants.TYPE_DANBOORU_ONE -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                postViewModel.apply {
                    show(search, tagBlacklists)
                    refreshDanOne()
                }
            }
            Constants.TYPE_GELBOORU -> {
                search.username = user.name
                search.auth_key = user.api_key ?: ""
                postViewModel.apply {
                    show(search, tagBlacklists)
                    refreshGel()
                }
            }
            Constants.TYPE_SANKAKU -> {
                search.username = user.name
                search.auth_key = user.password_hash ?: ""
                postViewModel.apply {
                    show(search, tagBlacklists)
                    refreshSankaku()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postType = it.getInt(POST_TYPE_KEY, -1)
            booruType = it.getInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
            search = Search(
                scheme = it.getString(Constants.SCHEME_KEY, ""),
                host = it.getString(Constants.HOST_KEY, ""),
                keyword = it.getString(Constants.KEYWORD_KEY, ""),
                user_id = it.getInt(Constants.USER_ID_KEY, -1),
                username = it.getString(Constants.USERNAME_KEY, ""),
                auth_key = it.getString(Constants.AUTH_KEY, ""),
                limit = Settings.pageLimit)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val activity = requireActivity()
        when (postType) {
            POST_ALL -> (activity as MainActivity).addNavigationListener(navigationListener)
            POST_SEARCH ->  searchBar.setTitleOnLongClickCallback {
                MuzeiManager.createMuzei(Muzei(booru_uid = Settings.activeBooruUid, keyword = search.keyword))
                Snackbar.make(searchBar, getString(R.string.post_add_search_to_muzei, search.keyword), Snackbar.LENGTH_LONG).show()
            }
            else -> {
                activity.finish()
                return
            }
        }
        searchTag = SearchTag(
            scheme = search.scheme,
            host = search.host,
            name = "",
            order = "name",
            limit = 6,
            type = ""
        )
        if (Settings.safeMode) {
            search.keyword = "rating:safe ${search.keyword}"
        }
        viewTransition = ViewTransition(swipe_refresh, search_layout)
        if (requireActivity() !is MainActivity) {
            leftDrawable.progress = 1f
            val keyword = search.keyword
            searchBar.setTitle(keyword)
            searchBar.setText(keyword)
        }
        searchBar.setEditTextHint(getString(R.string.search_bar_hint_search_posts))
        searchBar.setMenu(menuId = R.menu.post, menuInflater = requireActivity().menuInflater)
        rightButton = searchBar.findViewById<View>(R.id.action_expand_or_clear)
        progress_bar_empty.toVisibility(false)
        postViewModel = getPostViewModel(
            PostRepositoryImpl(
                db = db,
                danbooruApi = danApi,
                danbooruOneApi = danOneApi,
                moebooruApi = moeApi,
                gelbooruApi = gelApi,
                sankakuApi = sankakuApi,
                ioExecutor = ioExecutor
            )
        )
        glide = GlideApp.with(this)
        val staggeredGridLayoutManager = AutoStaggeredGridLayoutManager(
            columnSize = resources.gridWidth(),
            orientation = StaggeredGridLayoutManager.VERTICAL).apply {
                setStrategy(AutoStaggeredGridLayoutManager.STRATEGY_SUITABLE_SIZE)
            }
        postAdapter = PostAdapter(
            glide = glide,
            listener = itemListener,
            showInfoBar = Settings.showInfoBar,
            pageType = Constants.PAGE_TYPE_POST,
            retryCallback = { retry() }
        )
        list.apply {
            setHasFixedSize(true)
            layoutManager = staggeredGridLayoutManager
            adapter = postAdapter
        }
        val orders =
            if (booruType == Constants.TYPE_SANKAKU)
                resources.getStringArray(R.array.filter_order_sankaku)
            else
                resources.getStringArray(R.array.filter_order)
        val ratings = resources.getStringArray(R.array.filter_rating)
        val thresholds =
            if (booruType == Constants.TYPE_SANKAKU)
                resources.getStringArray(R.array.filter_threshold)
            else arrayOf()
        tagFilterAdapter = TagFilterAdapter(
            orders = orders,
            ratings = ratings,
            thresholds = thresholds,
            booruType = booruType
        ) {
            val text = searchBar.getEditTextText()
            if(text.isNotEmpty()) {
                TagFilterManager.createTagFilter(TagFilter(booru_uid = Settings.activeBooruUid, name = text))
            }
        }
        tags_filter_list.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexWrap = FlexWrap.WRAP
                flexDirection = FlexDirection.ROW
                alignItems = AlignItems.STRETCH
            }
            adapter = tagFilterAdapter
        }
        tagFilterViewModel = getTagFilterViewModel(tagFilterRepositoryImpl)
        tagFilterViewModel.loadTags().observe(this, Observer {
            tagFilterAdapter.updateData(it, Settings.activeBooruUid, Settings.isShowAllTags)
        })
        action_search.setOnClickListener {
            val tagString = tagFilterAdapter.getSelectedTagsString()
            if (!tagString.isBlank()) {
//                searchBar.disableSearchState()
                SuggestionManager.createSuggestion(Suggestion(
                    booru_uid = Settings.activeBooruUid,
                    keyword = tagString))
                SearchActivity.startActivity(requireContext(), tagString)
            }
        }
        when (booruType) {
            Constants.TYPE_DANBOORU -> {
                postViewModel.postsDan.observe(this, Observer<PagedList<PostDan>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                postViewModel.networkStateDan.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                    handleNetworkState(networkState, postAdapter.itemCount)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                postViewModel.postsMoe.observe(this, Observer<PagedList<PostMoe>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                postViewModel.networkStateMoe.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                    handleNetworkState(networkState, postAdapter.itemCount)
                })
                initSwipeToRefreshMoe()
            }
            Constants.TYPE_DANBOORU_ONE -> {
                postViewModel.postsDanOne.observe(this, Observer<PagedList<PostDanOne>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                postViewModel.networkStateDanOne.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                    handleNetworkState(networkState, postAdapter.itemCount)
                })
                initSwipeToRefreshDanOne()
            }
            Constants.TYPE_GELBOORU -> {
                postViewModel.postsGel.observe(this, Observer<PagedList<PostGel>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                postViewModel.networkStateGel.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                    handleNetworkState(networkState, postAdapter.itemCount)
                })
                initSwipeToRefreshGel()
            }
            Constants.TYPE_SANKAKU -> {
                postViewModel.postsSankaku.observe(this, Observer<PagedList<PostSankaku>> { posts ->
                    @Suppress("UNCHECKED_CAST")
                    postAdapter.submitList(posts as PagedList<PostBase>)
                })
                postViewModel.networkStateSankaku.observe(this, Observer<NetworkState> { networkState ->
                    postAdapter.setNetworkState(networkState)
                    handleNetworkState(networkState, postAdapter.itemCount)
                })
                initSwipeToRefreshSankaku()
            }
        }
        tagBlacklistViewModel = getViewModel(object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return TagBlacklistViewModel(tagBlacklistDao) as T
            }
        })
        tagBlacklistViewModel.loadTags(Settings.activeBooruUid).observe(this, Observer {
            postViewModel.show(search, it)
        })
        UserManager.listeners.add(userListener)
        searchBar.setType(booruType)
        searchBar.setSearchTag(searchTag)
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun retry() {
        when (booruType) {
            Constants.TYPE_DANBOORU -> postViewModel.retryDan()
            Constants.TYPE_MOEBOORU -> postViewModel.retryMoe()
            Constants.TYPE_DANBOORU_ONE -> postViewModel.retryDanOne()
            Constants.TYPE_GELBOORU -> postViewModel.retryGel()
            Constants.TYPE_SANKAKU -> postViewModel.retrySankaku()
        }
    }

    private fun initSwipeToRefreshDanOne() {
        postViewModel.refreshStateDanOne.observe(this, Observer<NetworkState> {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshDanOne() }
    }

    private fun initSwipeToRefreshDan() {
        postViewModel.refreshStateDan.observe(this, Observer<NetworkState> {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshMoe() {
        postViewModel.refreshStateMoe.observe(this, Observer<NetworkState> {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshMoe() }
    }

    private fun initSwipeToRefreshGel() {
        postViewModel.refreshStateGel.observe(this, Observer<NetworkState> {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshGel() }
    }

    private fun initSwipeToRefreshSankaku() {
        postViewModel.refreshStateSankaku.observe(this, Observer<NetworkState> {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener { postViewModel.refreshSankaku() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPostViewModel(repo: PostRepository): PostViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PostViewModel(repo) as T
            }
        })[PostViewModel::class.java]
    }

    @Suppress("UNCHECKED_CAST")
    private fun getTagFilterViewModel(repo: TagFilterRepository): TagFilterViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return TagFilterViewModel(repo) as T
            }
        })[TagFilterViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.removeNavigationListener(navigationListener)
            activity.sharedElement = null
        }
        sp.unregisterOnSharedPreferenceChangeListener(this)
        UserManager.listeners.remove(userListener)
    }

    override fun onStart() {
        super.onStart()
        requireActivity().registerReceiver(
            broadcastReceiver,
            IntentFilter(BrowseActivity.ACTION_NORMAL)
        )
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(broadcastReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SHOW_SEARCH_LAYOUT_KEY, search_layout.visibility == View.VISIBLE)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getBoolean(SHOW_SEARCH_LAYOUT_KEY, false)?.let {
            if (it) viewTransition.showView(1, true)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Settings.SHOW_ALL_TAGS -> {
                tagFilterAdapter.updateData(Settings.activeBooruUid, Settings.isShowAllTags)
            }
        }
    }

    override val isUnsupported: Boolean
        get() = booruType == -1

}