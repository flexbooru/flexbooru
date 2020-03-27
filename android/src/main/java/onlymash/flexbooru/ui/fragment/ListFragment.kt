package onlymash.flexbooru.ui.fragment

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.refreshable_list.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.ui.activity.MainActivity
import onlymash.flexbooru.ui.viewmodel.BooruViewModel
import onlymash.flexbooru.ui.viewmodel.getBooruViewModel
import onlymash.flexbooru.widget.SearchBar
import org.kodein.di.erased.instance

abstract class ListFragment : BaseFragment(), SearchBar.Helper, SearchBar.StateListener {

    private lateinit var booruViewModel: BooruViewModel
    private val booruDao by instance<BooruDao>()

    private lateinit var searchBar: SearchBar
    private lateinit var leftDrawable: DrawerArrowDrawable

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
        searchBar = view.findViewById(R.id.search_bar)
        initSearchBar()
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

    private fun initSearchBar() {
        leftDrawable = DrawerArrowDrawable(context)
        searchBar.setLeftDrawable(leftDrawable)
        searchBar.setHelper(this)
        searchBar.setStateListener(this)
        searchBar.setEditTextHint(getSearchBarHint())
    }

    abstract fun getSearchBarHint(): CharSequence

    private fun toggleArrowLeftDrawable() {
        toggleArrow(leftDrawable)
    }

    private fun toggleArrow(drawerArrow: DrawerArrowDrawable) {
        if (drawerArrow.progress == 0f) {
            ValueAnimator.ofFloat(0f, 1f)
        } else {
            ValueAnimator.ofFloat(1f, 0f)
        }.apply {
            addUpdateListener { animation ->
                drawerArrow.progress = animation.animatedValue as Float
            }
            interpolator = DecelerateInterpolator()
            duration = 300
            start()
        }
    }

    abstract fun onBooruLoaded(booru: Booru?)


    override fun onApplySearch(query: String) {

    }

    override fun onClickTitle() {

    }

    override fun onEditTextBackPressed() {

    }

    override fun onEditTextClick() {

    }

    override fun onFetchSuggestion(query: String) {

    }

    override fun onLeftButtonClick() {
        val activity = activity
        if (activity is MainActivity) {
            activity.openDrawer()
        } else {
            activity?.finish()
        }
    }

    override fun onMenuItemClick(menuItem: MenuItem) {

    }

    override fun onStateChange(newState: Int, oldState: Int, animation: Boolean) {
        if (activity is MainActivity) {
            toggleArrowLeftDrawable()
        }
    }
}