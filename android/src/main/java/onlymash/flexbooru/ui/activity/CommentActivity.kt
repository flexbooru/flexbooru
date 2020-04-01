package onlymash.flexbooru.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.refreshable_list.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.common.Settings.pageLimit
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.action.ActionComment
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.repository.NetworkState
import onlymash.flexbooru.data.repository.comment.CommentRepositoryImpl
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.adapter.CommentAdapter
import onlymash.flexbooru.ui.viewmodel.CommentViewModel
import onlymash.flexbooru.ui.viewmodel.getCommentViewModel
import org.kodein.di.erased.instance

class CommentActivity : KodeinActivity() {

    companion object {
        private const val POST_ID_KEY = "post_id"
        private const val QUERY_KEY = "query"
        fun startActivity(context: Context, postId: Int = -1, query: String = "") {
            context.startActivity(Intent(context, CommentActivity::class.java).apply {
                putExtra(POST_ID_KEY, postId)
                putExtra(QUERY_KEY, query)
            })
        }
    }

    private val booruApis by instance<BooruApis>()
    private lateinit var commentViewModel: CommentViewModel
    private lateinit var action: ActionComment
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val booru = BooruManager.getBooruByUid(activatedBooruUid)
        if (booru == null) {
            finish()
            return
        }
        action = ActionComment(
            booru = booru,
            limit = getPageLimit(booru.type)
        )
        intent?.let {
            action.postId = it.getIntExtra(POST_ID_KEY, -1)
            action.query = it.getStringExtra(QUERY_KEY) ?: ""
        }
        setContentView(R.layout.activity_comment)
        initView()
        initViewModel()
    }

    private fun getPageLimit(type: Int): Int {
        return when (type) {
            BOORU_TYPE_MOE -> 30
            BOORU_TYPE_DAN1, BOORU_TYPE_SANKAKU -> 25
            else -> pageLimit
        }
    }

    private fun initView() {
        toolbar.setTitle(R.string.title_comments)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        when {
            action.postId > 0 -> {
                toolbar.subtitle = "Post ${action.postId}"
                if (action.booru.type != BOORU_TYPE_GEL && action.booru.user != null) {
                    toolbar.inflateMenu(R.menu.comment)
                    toolbar.setOnMenuItemClickListener { menuItem ->
                        if (menuItem?.itemId == R.id.action_comment_reply) {
                            if (action.booru.user == null) {
                                startActivity(Intent(this, AccountConfigActivity::class.java))
                                finish()
                            } else {
                                reply(postId = action.postId)
                            }
                        }
                        true
                    }
                }
            }
            action.query.isNotBlank() -> {
                toolbar.subtitle = action.query
            }
        }
        commentAdapter = CommentAdapter(
            glide = GlideApp.with(this),
            booru = action.booru,
            replyCallback = { postId ->
                reply(postId)
            },
            quoteCallback = { postId, qoute ->
                reply(postId, qoute)
            },
            deleteCallback = { id ->
                delete(id)
            },
            retryCallback = {
                commentViewModel.retry()
            }
        )
        list.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity, RecyclerView.VERTICAL, false)
            adapter = commentAdapter
            updatePadding(top = 0)
        }
        swipe_refresh.setColorSchemeResources(
            R.color.blue,
            R.color.purple,
            R.color.green,
            R.color.orange,
            R.color.red
        )
        swipe_refresh.setOnRefreshListener {
            commentViewModel.refresh()
        }
    }

    private fun initViewModel() {
        commentViewModel = getCommentViewModel(CommentRepositoryImpl(booruApis))
        commentViewModel.comments.observe(this, Observer {
            commentAdapter.submitList(it)
        })
        commentViewModel.networkState.observe(this, Observer {
            commentAdapter.setNetworkState(it)
        })
        commentViewModel.refreshState.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        commentViewModel.show(action)
        commentViewModel.commentState.observe(this, Observer {
            when (it) {
                is NetResult.Success -> {
                    commentViewModel.refresh()
                }
                is NetResult.Error -> {
                    Toast.makeText(this, it.errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun reply(postId: Int, qoute: String = "") {
        val padding = resources.getDimensionPixelSize(R.dimen.spacing_mlarge)
        val layout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(padding, padding / 2, padding, 0)
        }
        val editText = EditText(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.TOP
            }
            minLines = 6
            maxLines = 10
            hint = getString(R.string.comment_hint)
            if (qoute.isNotBlank()) {
                setText(qoute)
            }
        }
        layout.addView(editText)
        AlertDialog.Builder(this)
            .setTitle("Post $postId")
            .setPositiveButton(R.string.comment_send) { _, _ ->
                val str = (editText.text ?: "").toString().trim()
                if (str.isNotEmpty()) {
                    val action = action.copy()
                    action.postId = postId
                    action.body = str
                    commentViewModel.createCommment(action)
                }
            }
            .setNegativeButton(R.string.comment_cancel, null)
            .setView(layout)
            .create()
            .show()
    }

    private fun delete(id: Int) {
        val action = action.copy()
        action.commentId = id
        commentViewModel.deleteComment(action)
    }
}