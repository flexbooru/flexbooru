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

package onlymash.flexbooru.ui.viewholder

import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ActionMenuView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.CommentDan
import onlymash.flexbooru.entity.CommentMoe
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.ui.AccountActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.util.formatDate
import onlymash.flexbooru.widget.CircularImageView
import onlymash.flexbooru.widget.CommentView
import java.text.SimpleDateFormat
import java.util.*

class CommentViewHolder(itemView: View,
                        private val glide: GlideRequests,
                        private val user: User?,
                        private val listener: Listener) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests, user: User?, listener: Listener): CommentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(view, glide, user, listener)
        }
    }

    private val avatar: CircularImageView = itemView.findViewById(R.id.user_avatar)
    private val userName: TextView = itemView.findViewById(R.id.user_name)
    private val postIdView: TextView = itemView.findViewById(R.id.post_id)
    private val commentDate: TextView = itemView.findViewById(R.id.comment_date)
    private val commentView: CommentView = itemView.findViewById(R.id.comment_view)
    private val menuView: ActionMenuView = itemView.findViewById(R.id.menu_view)

    private var creatorId = -1
    private var creatorName = ""
    private var postId = -1
    private var commentId = -1

    init {
        avatar.setOnClickListener {
            if (creatorId < 0) return@setOnClickListener
            val context = itemView.context
            context.startActivity(Intent(context, AccountActivity::class.java).apply {
                putExtra(AccountActivity.USER_ID_KEY, creatorId)
                putExtra(AccountActivity.USER_NAME_KEY, creatorName)
            })
        }
        itemView.setOnClickListener {
            if (postId < 0) return@setOnClickListener
            SearchActivity.startActivity(itemView.context, "id:$postId")
        }
    }

    interface Listener {
        fun onReply(postId: Int)
        fun onQuote(postId: Int, quote: String)
        fun onDelete(commentId: Int)
    }

    private fun setMenuClickListener() {
        menuView.setOnMenuItemClickListener {
            if (postId < 0) return@setOnMenuItemClickListener true
            when (it?.itemId) {
                R.id.action_comment_reply -> listener.onReply(postId)
                R.id.action_comment_quote -> listener.onQuote(postId, "[quote]$creatorName said:\r\n${commentView.getLastCommentText()}[/quote]")
                R.id.action_comment_delete -> listener.onDelete(commentId)
            }
            true
        }
    }
    fun bind(data: Any?) {
        when (data) {
            is CommentDan -> {
                creatorId = data.creator_id
                creatorName = data.creator_name
                postId = data.post_id
                commentId = data.id

                userName.text = data.creator_name
                postIdView.text = String.format("Post %d", data.post_id)
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.ENGLISH)
                commentDate.text = formatDate(sdf.parse(data.updated_at).time)
                commentView.setComment(data.body)
                if (user != null) {
                    if (user.id == data.creator_id) {
                        if (menuView.menu.findItem(R.id.action_comment_reply) == null) {
                            MenuInflater(itemView.context).inflate(R.menu.comment_item_me, menuView.menu)
                        } else if (menuView.menu.findItem(R.id.action_comment_delete) == null) {
                            menuView.menu.apply {
                                removeItem(R.id.action_comment_reply)
                                removeItem(R.id.action_comment_quote)
                            }
                            MenuInflater(itemView.context).inflate(R.menu.comment_item_me, menuView.menu)
                        }
                    } else if (menuView.menu.findItem(R.id.action_comment_reply) == null) {
                        MenuInflater(itemView.context).inflate(R.menu.comment_item, menuView.menu)
                    }
                    setMenuClickListener()
                }
            }
            is CommentMoe -> {
                creatorId = data.creator_id
                creatorName = data.creator
                postId = data.post_id
                commentId = data.id

                userName.text = data.creator
                postIdView.text = String.format("Post %d", data.post_id)
                val date = data.created_at
                val sdf =  when {
                    date.contains("T") -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH)
                    date.contains(" ") -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                    else -> {
                        Crashlytics.log("Unknown date format: $date. Host: ${data.host}")
                        throw IllegalStateException("Unknown date format: $date")
                    }
                }
                commentDate.text = formatDate(sdf.parse(date).time)
                commentView.setComment(data.body)
                if (user != null) {
                    if (user.id == data.creator_id) {
                        if (menuView.menu.findItem(R.id.action_comment_reply) == null) {
                            MenuInflater(itemView.context).inflate(R.menu.comment_item_me, menuView.menu)
                        } else if (menuView.menu.findItem(R.id.action_comment_delete) == null) {
                            menuView.menu.apply {
                                removeItem(R.id.action_comment_reply)
                                removeItem(R.id.action_comment_quote)
                            }
                            MenuInflater(itemView.context).inflate(R.menu.comment_item_me, menuView.menu)
                        }
                    } else if (menuView.menu.findItem(R.id.action_comment_reply) == null) {
                        MenuInflater(itemView.context).inflate(R.menu.comment_item, menuView.menu)
                    }
                    setMenuClickListener()
                }
                glide.load(String.format(itemView.resources.getString(R.string.account_user_avatars), data.scheme, data.host, data.creator_id))
                    .placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.avatar_account))
                    .into(avatar)
            }
        }
    }
}