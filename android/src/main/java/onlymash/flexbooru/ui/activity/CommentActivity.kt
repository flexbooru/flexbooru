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

package onlymash.flexbooru.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.refreshable_list.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.api.*
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.comment.CommentBase
import onlymash.flexbooru.entity.comment.CommentAction
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.comment.CommentRepositoryImpl
import onlymash.flexbooru.repository.comment.CommentRepository
import onlymash.flexbooru.repository.comment.CommentState
import onlymash.flexbooru.ui.adapter.CommentAdapter
import onlymash.flexbooru.ui.viewholder.CommentViewHolder
import onlymash.flexbooru.ui.viewmodel.CommentViewModel
import org.kodein.di.KodeinAware
import org.kodein.di.erased.instance
import java.util.concurrent.Executor

class CommentActivity : BaseActivity(), KodeinAware {

    companion object {
        private const val POST_ID_KEY = "post_id"
        private const val USER_NAME_KEY = "user_name"
        fun startActivity(context: Context, postId: Int = -1, username: String = "") {
            context.startActivity(Intent(context, CommentActivity::class.java).apply {
                if (postId > 0) {
                    putExtra(POST_ID_KEY, postId)
                } else if (username.isNotEmpty()) {
                    putExtra(USER_NAME_KEY, username)
                }
            })
        }
    }

    private val danApi: DanbooruApi by instance()
    private val danOneApi: DanbooruOneApi by instance()
    private val moeApi: MoebooruApi by instance()
    private val gelApi: GelbooruApi by instance()
    private val sankakuApi: SankakuApi by instance()
    private val ioExecutor: Executor by instance()

    private lateinit var commentAdapter: CommentAdapter
    private val commentViewModel by lazy { getCommentViewModel(
        CommentRepositoryImpl(
            danbooruApi = danApi,
            danbooruOneApi = danOneApi,
            moebooruApi = moeApi,
            gelbooruApi = gelApi,
            sankakuApi = sankakuApi,
            networkExecutor = ioExecutor
        )
    ) }
    private lateinit var commentAction: CommentAction
    private var type = -1

    private val commentListener = object : CommentViewHolder.Listener {
        override fun onReply(postId: Int) {
            val action = commentAction.copy()
            action.post_id = postId
            action.body = ""
            replay(action)
        }

        override fun onQuote(postId: Int, quote: String) {
            val action = commentAction.copy()
            action.post_id = postId
            action.body = quote
            replay(action)
        }

        override fun onDelete(commentId: Int) {
            val action = commentAction.copy()
            action.comment_id = commentId
            delete(action)
        }

    }

    private fun replay(action: CommentAction) {
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
            if (action.body.isNotEmpty()) {
                setText(action.body)
            }
        }
        layout.addView(editText)
        AlertDialog.Builder(this)
            .setTitle("Post ${action.post_id}")
            .setPositiveButton(R.string.comment_send) { _, _ ->
                val str = (editText.text ?: "").toString().trim()
                if (str.isNotEmpty()) {
                    action.body = str
                    when (type) {
                        Constants.TYPE_DANBOORU -> commentViewModel.createDanComment(action)
                        Constants.TYPE_MOEBOORU -> commentViewModel.createMoeComment(action)
                        Constants.TYPE_DANBOORU_ONE -> commentViewModel.createDanOneComment(action)
                        Constants.TYPE_SANKAKU -> commentViewModel.createSankakuComment(action)
                    }
                }
            }
            .setNegativeButton(R.string.comment_cancel, null)
            .setView(layout)
            .create()
            .show()
    }

    private fun delete(action: CommentAction) {
        when (type) {
            Constants.TYPE_DANBOORU -> commentViewModel.deleteDanComment(action)
            Constants.TYPE_MOEBOORU -> commentViewModel.deleteMoeComment(action)
            Constants.TYPE_DANBOORU_ONE -> commentViewModel.deleteDanOneComment(action)
            Constants.TYPE_SANKAKU -> commentViewModel.deleteSankakuComment(action)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        toolbar.setTitle(R.string.title_comments)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        val uid = Settings.activeBooruUid
        val booru = BooruManager.getBooruByUid(uid)
        if (booru == null) {
            startActivity(Intent(this, BooruActivity::class.java))
            finish()
            return
        }
        type = booru.type
        var postId = -1
        var name = ""
        intent?.extras?.let {
            postId = it.getInt(POST_ID_KEY, -1)
            name = it.getString(USER_NAME_KEY, "")
        }
        commentAction = CommentAction(
            scheme = booru.scheme,
            host = booru.host,
            post_id = postId,
            limit = Settings.pageLimit
        )
        val user = UserManager.getUserByBooruUid(uid)
        user?.let {
            with(commentAction) {
                username = it.name
                auth_key = when (type) {
                    Constants.TYPE_DANBOORU -> it.apiKey ?: ""
                    else -> it.passwordHash ?: ""
                }
            }
        }
        if (postId > 0) {
            toolbar.apply {
                subtitle = "Post $postId"
                inflateMenu(R.menu.comment)
                setOnMenuItemClickListener {
                    if (booru.type == Constants.TYPE_GELBOORU) {
                        Snackbar.make(toolbar, getString(R.string.msg_not_supported), Snackbar.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }
                    if (it.itemId == R.id.action_comment_reply) {
                        if (commentAction.username.isEmpty() || commentAction.auth_key.isEmpty()) {
                            startActivity(Intent(this@CommentActivity, AccountConfigActivity::class.java))
                            finish()
                            return@setOnMenuItemClickListener true
                        }
                        commentAction.body = ""
                        replay(commentAction)
                    }
                    true
                }
            }
        } else if (name.isNotEmpty()) {
            when (type) {
                Constants.TYPE_DANBOORU -> commentAction.query = name
                Constants.TYPE_MOEBOORU,
                Constants.TYPE_DANBOORU_ONE,
                Constants.TYPE_SANKAKU -> commentAction.query = "user:$name"
            }
            toolbar.subtitle = commentAction.query
        }
        commentAdapter = CommentAdapter(GlideApp.with(this), user, commentListener) {
            retry()
        }
        list.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity, RecyclerView.VERTICAL, false)
            adapter = commentAdapter
        }
        swipe_refresh.setColorSchemeResources(
            R.color.blue,
            R.color.purple,
            R.color.green,
            R.color.orange,
            R.color.red
        )
        when (type) {
            Constants.TYPE_DANBOORU -> {
                commentViewModel.commentsDan.observe(this, Observer {
                    @Suppress("UNCHECKED_CAST")
                    commentAdapter.submitList(it as PagedList<CommentBase>)
                })
                commentViewModel.networkStateDan.observe(this, Observer {
                    commentAdapter.setNetworkState(it)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                commentViewModel.commentsMoe.observe(this, Observer {
                    @Suppress("UNCHECKED_CAST")
                    commentAdapter.submitList(it as PagedList<CommentBase>)
                })
                commentViewModel.networkStateMoe.observe(this, Observer {
                    commentAdapter.setNetworkState(it)
                })
                initSwipeToRefreshMoe()
            }
            Constants.TYPE_DANBOORU_ONE -> {
                commentViewModel.commentsDanOne.observe(this, Observer {
                    @Suppress("UNCHECKED_CAST")
                    commentAdapter.submitList(it as PagedList<CommentBase>)
                })
                commentViewModel.networkStateDanOne.observe(this, Observer {
                    commentAdapter.setNetworkState(it)
                })
                initSwipeToRefreshDanOne()
            }
            Constants.TYPE_GELBOORU -> {
                commentViewModel.commentsGel.observe(this, Observer {
                    @Suppress("UNCHECKED_CAST")
                    commentAdapter.submitList(it as PagedList<CommentBase>)
                })
                commentViewModel.networkStateGel.observe(this, Observer {
                    commentAdapter.setNetworkState(it)
                })
                initSwipeToRefreshGel()
            }
            Constants.TYPE_SANKAKU -> {
                commentViewModel.commentsSankaku.observe(this, Observer {
                    @Suppress("UNCHECKED_CAST")
                    commentAdapter.submitList(it as PagedList<CommentBase>)
                })
                commentViewModel.networkStateSankaku.observe(this, Observer {
                    commentAdapter.setNetworkState(it)
                })
                initSwipeToRefreshSankaku()
            }
        }
        commentViewModel.commentState.observe(this, Observer {
            if (it == CommentState.SUCCESS) {
                when (type) {
                    Constants.TYPE_DANBOORU -> commentViewModel.refreshDan()
                    Constants.TYPE_MOEBOORU -> commentViewModel.refreshMoe()
                    Constants.TYPE_DANBOORU_ONE -> commentViewModel.refreshDanOne()
                    Constants.TYPE_GELBOORU -> commentViewModel.refreshGel()
                }
            } else {
                Snackbar.make(toolbar, it.msg.toString(), Snackbar.LENGTH_LONG).show()
            }
        })
        commentViewModel.show(commentAction)
    }

    private fun retry() {
        when (type) {
            Constants.TYPE_DANBOORU -> commentViewModel.retryDan()
            Constants.TYPE_MOEBOORU -> commentViewModel.retryMoe()
            Constants.TYPE_DANBOORU_ONE -> commentViewModel.retryDanOne()
            Constants.TYPE_GELBOORU -> commentViewModel.retryGel()
            Constants.TYPE_SANKAKU -> commentViewModel.retrySankaku()
        }
    }

    private fun initSwipeToRefreshDan() {
        commentViewModel.refreshStateDan.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { commentViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshDanOne() {
        commentViewModel.refreshStateDanOne.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { commentViewModel.refreshDanOne() }
    }

    private fun initSwipeToRefreshMoe() {
        commentViewModel.refreshStateMoe.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { commentViewModel.refreshMoe() }
    }

    private fun initSwipeToRefreshGel() {
        commentViewModel.refreshStateGel.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { commentViewModel.refreshGel() }
    }

    private fun initSwipeToRefreshSankaku() {
        commentViewModel.refreshStateSankaku.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { commentViewModel.refreshSankaku() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCommentViewModel(repo: CommentRepository): CommentViewModel {
        return ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return CommentViewModel(repo) as T
            }
        })[CommentViewModel::class.java]
    }
}