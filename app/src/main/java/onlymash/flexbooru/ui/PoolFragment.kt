package onlymash.flexbooru.ui

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_list.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.repository.pool.PoolRepository
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
                limit = Settings.instance().postLimit)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_bar.setTitle(R.string.title_pools)
        poolViewModel = getPoolViewModel(ServiceLocator.instance().getPoolRepository())
        if (search == null) return
        when (type) {
            Constants.TYPE_DANBOORU -> {
                poolViewModel.poolsDan.observe(this, Observer {
                    Log.e(TAG, "Danbooru pools: ${it.loadedCount}")
                })
            }
            Constants.TYPE_MOEBOORU -> {
                poolViewModel.poolsMoe.observe(this, Observer {
                    Log.e(TAG, "Moebooru pools: ${it.loadedCount}")
                })
            }
        }
        poolViewModel.show(search = search!!)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPoolViewModel(repo: PoolRepository): PoolViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PoolViewModel(repo) as T
            }
        })[PoolViewModel::class.java]
    }
}