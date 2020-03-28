package onlymash.flexbooru.widget.searchbar

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.math.MathUtils
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.animation.SimpleAnimatorListener

class SearchBarMover(private val helper: Helper,
                     private val searchBar: View,
                     vararg recyclerViews: RecyclerView) : RecyclerView.OnScrollListener() {

    companion object {
        private const val ANIMATE_TIME = 400L
    }

    private var isShow: Boolean = false
    private var searchBarMoveAnimator: ValueAnimator? = null

    init {
        for (recyclerView in recyclerViews) {
            recyclerView.addOnScrollListener(this)
        }
    }

    fun cancelAnimation() {
        searchBarMoveAnimator?.cancel()
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE && helper.isValidView(recyclerView)) {
            returnSearchBarPosition(true)
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (helper.isValidView(recyclerView)) {
            val y2 = getY2(searchBar).toInt()
            val ty = searchBar.translationY.toInt()
            val offsetYStep = MathUtils.clamp(-dy, -y2, -ty)
            if (offsetYStep != 0) {
                searchBar.translationY = searchBar.translationY + offsetYStep
            }
        }
    }

    fun returnSearchBarPosition(animation: Boolean) {
        if (searchBar.height == 0) {
            // Layout not called
            return
        }

        val show = if (helper.forceShowSearchBar()) {
            true
        } else {
            val recyclerView = helper.validRecyclerView
            if (!recyclerView.isShown) {
                true
            } else if (recyclerView.computeVerticalScrollOffset() < searchBar.bottom) {
                true
            } else {
                getY2(searchBar).toInt() > searchBar.height/2
            }
        }

        val offset = if (show) {
            -searchBar.translationY.toInt()
        } else {
            -getY2(searchBar).toInt()
        }

        if (offset == 0) {
            // No need to scroll
            return
        }

        if (animation) {
            if (searchBarMoveAnimator != null) {
                if (isShow == show) {
                    // The same target, no need to do animation
                    return
                } else {
                    // Cancel it
                    searchBarMoveAnimator?.cancel()
                    searchBarMoveAnimator = null
                }
            }

            isShow = show
            val va = ValueAnimator.ofInt(0, offset)
            va.duration = ANIMATE_TIME
            va.addListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator?) {
                    searchBarMoveAnimator = null
                }
            })
            va.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                var lastValue: Int = 0
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    val value = animation.animatedValue as Int
                    val offsetStep = value - lastValue
                    lastValue = value
                    searchBar.translationY = searchBar.translationY + offsetStep
                }
            })
            searchBarMoveAnimator = va
            va.start()
        } else {
            searchBarMoveAnimator?.cancel()
            searchBar.translationY = searchBar.translationY + offset
        }
    }

    @JvmOverloads
    fun showSearchBar(animation: Boolean = true) {
        if (searchBar.height == 0) {
            // Layout not called
            return
        }

        val offset = -searchBar.translationY.toInt()

        if (offset == 0) {
            // No need to scroll
            return
        }

        if (animation) {
            if (searchBarMoveAnimator != null) {
                if (isShow) {
                    // The same target, no need to do animation
                    return
                } else {
                    // Cancel it
                    searchBarMoveAnimator!!.cancel()
                    searchBarMoveAnimator = null
                }
            }

            isShow = true
            val va = ValueAnimator.ofInt(0, offset)
            va.duration = ANIMATE_TIME
            va.addListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    searchBarMoveAnimator = null
                }
            })
            va.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                var lastValue: Int = 0
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    val value = animation.animatedValue as Int
                    val offsetStep = value - lastValue
                    lastValue = value
                    searchBar.translationY = searchBar.translationY + offsetStep
                }
            })
            searchBarMoveAnimator = va
            va.start()
        } else {
            if (searchBarMoveAnimator != null) {
                searchBarMoveAnimator!!.cancel()
            }
            searchBar.translationY = searchBar.translationY + offset
        }
    }

    private fun getY2(view: View): Float {
        return view.y + view.height
    }

    interface Helper {

        val validRecyclerView: RecyclerView

        fun isValidView(recyclerView: RecyclerView): Boolean

        fun forceShowSearchBar(): Boolean
    }

}