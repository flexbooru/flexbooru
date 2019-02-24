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
import kotlinx.android.synthetic.main.activity_list_toolbar.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.CommentAction
import onlymash.flexbooru.glide.GlideApp
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
        setContentView(R.layout.activity_list_toolbar)
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
            query = ""
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
        commentAdapter = CommentAdapter(GlideApp.with(this)) {

        }
        list.apply {
            addItemDecoration(DividerItemDecoration(this@CommentActivity, RecyclerView.VERTICAL))
            layoutManager = LinearLayoutManager(this@CommentActivity, RecyclerView.VERTICAL, false)
            adapter = commentAdapter
        }
        commentViewModel = getCommentViewModel(ServiceLocator.instance().getCommentRepository())
        commentViewModel.commentsMoe.observe(this, Observer {
            @Suppress("UNCHECKED_CAST")
            commentAdapter.submitList(it as PagedList<Any>)
        })
        commentViewModel.show(commentAction)
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