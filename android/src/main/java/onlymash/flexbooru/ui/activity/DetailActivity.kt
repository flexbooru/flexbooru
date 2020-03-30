package onlymash.flexbooru.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.bottom_shortcut_bar.*
import kotlinx.android.synthetic.main.toolbar_transparent.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Keys.POST_POSITION
import onlymash.flexbooru.common.Keys.POST_QUERY
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.data.action.ActionVote
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.repository.favorite.VoteRepository
import onlymash.flexbooru.data.repository.favorite.VoteRepositoryImpl
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.hideBar
import onlymash.flexbooru.extension.showBar
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.adapter.DetailAdapter
import onlymash.flexbooru.ui.viewmodel.DetailViewModel
import onlymash.flexbooru.ui.viewmodel.getDetailViewModel
import onlymash.flexbooru.widget.DismissFrameLayout
import org.kodein.di.erased.instance
import java.util.concurrent.Executor

private const val ALPHA_MAX = 0xFF
private const val ALPHA_MIN = 0x00
private const val POSITION_INIT = -1
private const val POSITION_INITED = -2

class DetailActivity : BaseActivity(), DismissFrameLayout.OnDismissListener {

    companion object {
        fun start(context: Context, query: String?, position: Int) {
            context.startActivity(Intent(context, DetailActivity::class.java).apply {
                putExtra(POST_QUERY, query)
                putExtra(POST_POSITION, position)
            })
        }
    }

    private val postDao by instance<PostDao>()
    private val ioExecutor by instance<Executor>()
    private val booruApis by instance<BooruApis>()
    private val voteRepository: VoteRepository by lazy { VoteRepositoryImpl(booruApis, postDao) }

    private lateinit var booru: Booru
    private lateinit var actionVote: ActionVote
    private var initPosition = POSITION_INIT
    private lateinit var colorDrawable: ColorDrawable
    private lateinit var detailViewModel: DetailViewModel
    private lateinit var detailAdapter: DetailAdapter

    private val currentPost: Post?
        get() = detailAdapter.getPost(detail_pager.currentItem)

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {

        }
        override fun onPageSelected(position: Int) {
            syncInfo(detailAdapter.getPost(position))
        }
    }

    private fun syncInfo(post: Post?) {
        if (post == null) return
        actionVote.postId = post.id
        setVoteItemIcon(post.isFavored)
        toolbar_transparent.title = "# ${post.id}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val booru = BooruManager.getBooruByUid(activatedBooruUid)
        if (booru == null) {
            finish()
            return
        }
        this.booru = booru
        actionVote = ActionVote(
            booru = booru,
            postId = -1
        )
        setContentView(R.layout.activity_detail)
        initInsets()
        initPager()
        initShortcutBar()
    }

    private fun initInsets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.showBar()
        findViewById<View>(android.R.id.content).setOnApplyWindowInsetsListener { _, insets ->
            toolbar_container.minimumHeight = toolbar_transparent.height + insets.systemWindowInsetTop
            toolbar_container.updatePadding(
                left = insets.systemWindowInsetLeft,
                top = insets.systemWindowInsetTop,
                right = insets.systemWindowInsetRight
            )
            space_nav_bar.minimumHeight = insets.systemWindowInsetBottom
            insets
        }
    }

    private fun initPager() {
        val query = intent?.getStringExtra(POST_QUERY) ?: ""
        if (initPosition == POSITION_INIT) {
            initPosition = intent?.getIntExtra(POST_POSITION, POSITION_INITED) ?: POSITION_INITED
        }
        val glide = GlideApp.with(this)
        detailAdapter = DetailAdapter(
            glide = glide,
            picasso = Picasso.Builder(this).build(),
            dismissListener = this,
            ioExecutor = ioExecutor
        ) {
            if (toolbar_container.isVisible) {
                window.hideBar()
                toolbar_container.isVisible = false
                bottom_bar_container.isVisible = false
                shadow.isVisible = false
            } else {
                window.showBar()
                toolbar_container.isVisible = true
                bottom_bar_container.isVisible = true
                shadow.isVisible = true
            }
        }
        colorDrawable = ColorDrawable(ContextCompat.getColor(this, R.color.black))
        detail_pager.apply {
            background = colorDrawable
            adapter = detailAdapter
            registerOnPageChangeCallback(pageChangeCallback)
        }
        detailViewModel = getDetailViewModel(postDao, booru.uid, query)
        detailViewModel.posts.observe(this, Observer {
            detailAdapter.submitList(it)
            if (initPosition != POSITION_INITED) {
                if (detailAdapter.itemCount > 0) {
                    detail_pager.setCurrentItem(initPosition, false)
                    lifecycleScope.launch {
                        delay(100)
                        syncInfo(currentPost)
                    }
                }
                initPosition = POSITION_INITED
            }
        })
    }

    private fun initShortcutBar() {
        TooltipCompat.setTooltipText(post_tags, post_tags.contentDescription)
        TooltipCompat.setTooltipText(post_info, post_info.contentDescription)
        TooltipCompat.setTooltipText(post_fav, post_fav.contentDescription)
        TooltipCompat.setTooltipText(post_save, post_save.contentDescription)
        post_fav.setOnClickListener {
            if (booru.user == null) {
                startActivity(Intent(this, AccountConfigActivity::class.java))
                finish()
                return@setOnClickListener
            }
            currentPost?.let {
                if (it.isFavored) {
                    lifecycleScope.launch {
                        val result = voteRepository.removeFav(actionVote)
                        if (result is NetResult.Error) {
                            Toast.makeText(this@DetailActivity, result.errorMsg, Toast.LENGTH_SHORT).show()
                        }
                        delay(100)
                        setVoteItemIcon(currentPost?.isFavored == true)
                    }
                } else {
                    lifecycleScope.launch {
                        val result = voteRepository.addFav(actionVote)
                        if (result is NetResult.Error) {
                            Toast.makeText(this@DetailActivity, result.errorMsg, Toast.LENGTH_SHORT).show()
                        }
                        delay(100)
                        setVoteItemIcon(currentPost?.isFavored == true)
                    }
                }
            }
        }
    }

    private fun setVoteItemIcon(checked: Boolean) {
        post_fav.setImageResource(if (checked) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp)
    }

    override fun onDismissStart() {
        colorDrawable.alpha = ALPHA_MIN
    }

    override fun onDismissProgress(progress: Float) {

    }

    override fun onDismissed() {
        finish()
    }

    override fun onDismissCancel() {
        colorDrawable.alpha = ALPHA_MAX
    }
}