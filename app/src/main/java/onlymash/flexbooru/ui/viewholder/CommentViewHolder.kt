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
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.entity.comment.*
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.ui.AccountActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.widget.CircularImageView
import onlymash.flexbooru.widget.CommentView

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

    private var comment: BaseComment? = null

    init {
        avatar.setOnClickListener {
            comment?.let {
                if (it is CommentGel) {
                    val context = itemView.context
                    context.startActivity(Intent(context, AccountActivity::class.java).apply {
                        putExtra(AccountActivity.USER_ID_KEY, it.getCreatorId())
                        putExtra(AccountActivity.USER_NAME_KEY, it.getCreatorName())
                    })
                }
            }
        }
        itemView.setOnClickListener {
            comment?.let {
                if (it !is CommentGel) {
                    SearchActivity.startActivity(itemView.context, "id:${it.getPostId()}")
                }
            }
        }
    }

    interface Listener {
        fun onReply(postId: Int)
        fun onQuote(postId: Int, quote: String)
        fun onDelete(commentId: Int)
    }

    private fun setMenuClickListener() {
        menuView.setOnMenuItemClickListener { menuItem ->
            comment?.let {
                when (menuItem?.itemId) {
                    R.id.action_comment_reply -> listener.onReply(it.getPostId())
                    R.id.action_comment_quote -> listener.onQuote(it.getPostId(),
                        "[quote]${it.getCreatorName()} said:\r\n${commentView.getLastCommentText()}[/quote]")
                    R.id.action_comment_delete -> listener.onDelete(it.getCommentId())
                }
            }
            true
        }
    }
    fun bind(data: BaseComment?) {
        comment = data ?: return
        userName.text = data.getCreatorName()
        postIdView.text = String.format("Post %d", data.getPostId())
        commentDate.text = data.getCommentDate()
        commentView.setComment(data.getCommentBody())
        if (user != null && data !is CommentGel) {
            if (user.id == data.getCreatorId()) {
                menuView.menu.clear()
                MenuInflater(itemView.context).inflate(R.menu.comment_item_me, menuView.menu)
            } else {
                menuView.menu.clear()
                MenuInflater(itemView.context).inflate(R.menu.comment_item, menuView.menu)
            }
            setMenuClickListener()
        }
        if (data is CommentMoe) {
            glide.load(String.format(itemView.resources.getString(R.string.account_user_avatars), data.scheme, data.host, data.creator_id))
                .placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.avatar_account))
                .into(avatar)
        }
    }
}