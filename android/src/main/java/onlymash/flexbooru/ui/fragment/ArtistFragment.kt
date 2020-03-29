package onlymash.flexbooru.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.pageLimit
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.data.action.ActionArtist
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.data.repository.artist.ArtistRepositoryImpl
import onlymash.flexbooru.ui.adapter.ArtistAdapter
import onlymash.flexbooru.ui.viewmodel.ArtistViewModel
import onlymash.flexbooru.ui.viewmodel.getArtistViewModel

private const val ORDER_DEFAULT = ""
private const val ORDER_DATE = "date"
private const val ORDER_UPDATED_AT = "updated_at"
private const val ORDER_NAME = "name"
private const val ORDER_COUNT = "post_count"

class ArtistFragment : SearchBarFragment() {

    private var action: ActionArtist? = null

    private lateinit var artistViewModel: ArtistViewModel
    private lateinit var artistAdapter: ArtistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        artistViewModel = getArtistViewModel(ArtistRepositoryImpl(booruApis))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getSearchBarHint(): CharSequence =
        getString(R.string.search_bar_hint_search_artists)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSearchBarTitle(getString(R.string.title_artists))
        artistAdapter = ArtistAdapter {
            artistViewModel.retry()
        }
        list.apply {
            layoutManager = LinearLayoutManager(this@ArtistFragment.context, RecyclerView.VERTICAL, false)
            adapter = artistAdapter
        }
        artistViewModel.artists.observe(viewLifecycleOwner, Observer {
            artistAdapter.submitList(it)
        })
        artistViewModel.networkState.observe(viewLifecycleOwner, Observer {
            artistAdapter.setNetworkState(it)
        })
        artistViewModel.refreshState.observe(viewLifecycleOwner, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener {
            artistViewModel.refresh()
        }
    }

    override fun onBooruLoaded(booru: Booru?) {
        if (booru == null) {
            action == null
            artistViewModel.show(null)
            return
        }
        if (action == null) {
            action = ActionArtist(
                booru = booru,
                limit = artistLimit(booru.type),
                order = ORDER_COUNT,
                query = ""
            )
            setSearchBarMenu(when (booru.type) {
                BOORU_TYPE_GEL -> R.menu.artist_moe
                else -> R.menu.artist_moe
            })
        } else {
            action?.let {
                it.booru = booru
                it.limit = artistLimit(booru.type)
            }
        }
        artistViewModel.show(action)
    }

    private fun artistLimit(booruType: Int): Int {
        return when (booruType) {
            BOORU_TYPE_MOE, BOORU_TYPE_DAN1 -> 20
            else -> pageLimit
        }
    }

    override fun onApplySearch(query: String) {
        super.onApplySearch(query)
        action?.let {
            it.query = query
            artistViewModel.show(action)
            artistViewModel.refresh()
        }
    }

    private fun updateActionAndRefresh(action: ActionArtist) {
        artistViewModel.show(action)
        artistViewModel.refresh()
    }

    override fun onMenuItemClick(menuItem: MenuItem) {
        super.onMenuItemClick(menuItem)
        when (menuItem.itemId) {
            R.id.action_artist_order_default -> {
                action?.let{
                    it.order = ORDER_DEFAULT
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_artist_order_date -> {
                action?.let{
                    it.order = if (it.booru.type == BOORU_TYPE_DAN) {
                        ORDER_UPDATED_AT
                    } else {
                        ORDER_DATE
                    }
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_artist_order_name -> {
                action?.let{
                    it.order = ORDER_NAME
                    updateActionAndRefresh(it)
                }
            }
            R.id.action_artist_order_count -> {
                action?.let{
                    it.order = ORDER_COUNT
                    updateActionAndRefresh(it)
                }
            }
        }
    }
}