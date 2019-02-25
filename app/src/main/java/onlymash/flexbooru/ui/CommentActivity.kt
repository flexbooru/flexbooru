/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.ui

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.refreshable_list.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.CommentAction
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.repository.NetworkState
import onlymash.flexbooru.repository.comment.CommentRepository
import onlymash.flexbooru.ui.adapter.CommentAdapter
import onlymash.flexbooru.ui.viewmodel.CommentViewModel

class CommentActivity : AppCompatActivity() {

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentViewModel: CommentViewModel
    private lateinit var commentAction: CommentAction
    private var type = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        toolbar.setTitle(R.string.title_comments)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        val uid = Settings.instance().activeBooruUid
        val booru = BooruManager.getBooruByUid(uid)
        if (booru == null) {
            startActivity(Intent(this, BooruActivity::class.java))
            finish()
            return
        }
        type = booru.type
        commentAction = CommentAction(
            scheme = booru.scheme,
            host = booru.host,
            query = "",
            limit = Settings.instance().pageSize
        )
        UserManager.getUserByBooruUid(uid)?.let {
            with(commentAction) {
                username = it.name
                auth_key = when (type) {
                    Constants.TYPE_DANBOORU -> it.api_key ?: ""
                    else -> it.password_hash ?: ""
                }
            }
        }
        commentViewModel = getCommentViewModel(ServiceLocator.instance().getCommentRepository())
        commentAdapter = CommentAdapter(GlideApp.with(this)) {
            when (type) {
                Constants.TYPE_DANBOORU -> commentViewModel.retryDan()
                Constants.TYPE_MOEBOORU -> commentViewModel.retryMoe()
            }
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
                    commentAdapter.submitList(it as PagedList<Any>)
                })
                commentViewModel.networkStateDan.observe(this, Observer {
                    commentAdapter.setNetworkState(it)
                })
                initSwipeToRefreshDan()
            }
            Constants.TYPE_MOEBOORU -> {
                commentViewModel.commentsMoe.observe(this, Observer {
                    @Suppress("UNCHECKED_CAST")
                    commentAdapter.submitList(it as PagedList<Any>)
                })
                commentViewModel.networkStateMoe.observe(this, Observer {
                    commentAdapter.setNetworkState(it)
                })
                initSwipeToRefreshMoe()
            }
        }
        commentViewModel.show(commentAction)
    }

    private fun initSwipeToRefreshDan() {
        commentViewModel.refreshStateDan.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { commentViewModel.refreshDan() }
    }

    private fun initSwipeToRefreshMoe() {
        commentViewModel.refreshStateMoe.observe(this, Observer {
            if (it != NetworkState.LOADING) {
                swipe_refresh.isRefreshing = false
            }
        })
        swipe_refresh.setOnRefreshListener { commentViewModel.refreshMoe() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCommentViewModel(repo: CommentRepository): CommentViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return CommentViewModel(repo) as T
            }
        })[CommentViewModel::class.java]
    }
}