package onlymash.flexbooru.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.ui.viewmodel.BooruViewModel
import onlymash.flexbooru.ui.viewmodel.getBooruViewModel
import org.kodein.di.erased.instance

abstract class ListFragment : BaseFragment() {

    private lateinit var booruViewModel: BooruViewModel
    private val booruDao by instance<BooruDao>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        booruViewModel = getBooruViewModel(booruDao)
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                swipe_refresh.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val appBarHeight = app_bar.height
                swipe_refresh.translationY = -appBarHeight.toFloat()
                swipe_refresh.layoutParams.height = swipe_refresh.height + appBarHeight
            }
        })
        booruViewModel.booru.observe(viewLifecycleOwner, Observer {
            onBooruLoaded(it)
        })
        booruViewModel.loadBooru(activatedBooruUid)
    }

    abstract fun onBooruLoaded(booru: Booru?)

}