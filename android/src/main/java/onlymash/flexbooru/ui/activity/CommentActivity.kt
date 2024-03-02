/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.app.Settings.pageLimit
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.action.ActionComment
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.repository.comment.CommentRepositoryImpl
import onlymash.flexbooru.databinding.ActivityCommentBinding
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.ui.adapter.CommentAdapter
import onlymash.flexbooru.ui.adapter.StateAdapter
import onlymash.flexbooru.ui.base.BaseActivity
import onlymash.flexbooru.ui.viewmodel.CommentViewModel
import onlymash.flexbooru.ui.viewmodel.getCommentViewModel
import onlymash.flexbooru.ui.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class CommentActivity : BaseActivity() {

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

    private val booruApis by inject<BooruApis>()
    private val binding by viewBinding(ActivityCommentBinding::inflate)
    private val list get() = binding.refreshableList.list
    private val refresh get() = binding.refreshableList.swipeRefresh
    private val progressBarHorizontal get() = binding.progressHorizontal.progressBarHorizontal

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
        setContentView(binding.root)
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
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_comments)
            when {
                action.postId > 0 -> {
                    subtitle = "Post ${action.postId}"
                }
                action.query.isNotBlank() -> {
                    subtitle = action.query
                }
            }
        }
        commentAdapter = CommentAdapter(
            booru = action.booru,
            replyCallback = { postId ->
                reply(postId)
            },
            quoteCallback = { postId, qoute ->
                reply(postId, qoute)
            },
            deleteCallback = { id ->
                delete(id)
            }
        )
        list.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity, RecyclerView.VERTICAL, false)
            adapter = commentAdapter.withLoadStateFooter(StateAdapter(commentAdapter))
            updatePadding(top = 0)
        }
        refresh.setColorSchemeResources(
            R.color.blue,
            R.color.purple,
            R.color.green,
            R.color.orange,
            R.color.red
        )
        refresh.setOnRefreshListener {
            commentAdapter.refresh()
        }
        binding.networkState.retryButton.setOnClickListener {
            commentAdapter.refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (action.postId > 0 &&
            action.booru.type != BOORU_TYPE_GEL &&
            action.booru.type != BOORU_TYPE_GEL_LEGACY &&
            action.booru.user != null) {

            menuInflater.inflate(R.menu.comment, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_comment_reply -> {
                if (action.booru.user == null) {
                    startActivity(Intent(this, AccountConfigActivity::class.java))
                    finish()
                } else {
                    reply(postId = action.postId)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViewModel() {
        commentViewModel = getCommentViewModel(CommentRepositoryImpl(booruApis))
        lifecycleScope.launch {
            commentViewModel.comments.collectLatest {
                commentAdapter.submitData(it)
            }
        }
        commentAdapter.addLoadStateListener { loadStates ->
            updateStates(loadStates)
        }
        if (commentViewModel.show(action)) {
            commentAdapter.refresh()
        }
        commentViewModel.commentState.observe(this) {
            when (it) {
                is NetResult.Success -> {
                    commentAdapter.refresh()
                }

                is NetResult.Error -> {
                    Toast.makeText(this, it.errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateStates(loadStates: CombinedLoadStates) {
        progressBarHorizontal.isVisible = loadStates.source.append is LoadState.Loading
        val refreshState = loadStates.source.refresh
        refresh.isRefreshing = refreshState is LoadState.Loading
        if (refreshState is LoadState.Error && commentAdapter.itemCount == 0) {
            binding.networkState.networkStateContainer.isVisible = true
            binding.networkState.errorMsg.text = refreshState.error.message
        } else {
            binding.networkState.networkStateContainer.isVisible = false
        }
    }

    private fun reply(postId: Int, qoute: String = "") {
        if (isFinishing) {
            return
        }
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