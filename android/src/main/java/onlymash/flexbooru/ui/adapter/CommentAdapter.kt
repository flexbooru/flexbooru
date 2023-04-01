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

package onlymash.flexbooru.ui.adapter

import android.content.Intent
import android.view.MenuInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.databinding.ItemCommentBinding
import onlymash.flexbooru.extension.formatDate
import onlymash.flexbooru.ui.activity.AccountActivity
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.ui.viewbinding.viewBinding

class CommentAdapter(
    private val booru: Booru,
    private val replyCallback: (Int) -> Unit,
    private val quoteCallback: (Int, String) -> Unit,
    private val deleteCallback: (Int) -> Unit
) : PagingDataAdapter<Comment, CommentAdapter.CommentViewHolder>(COMMENT_COMPARATOR) {

    companion object {
        val COMMENT_COMPARATOR = object : DiffUtil.ItemCallback<Comment>() {
            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem.body == newItem.body
            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(parent)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup): this(parent.viewBinding(ItemCommentBinding::inflate))

        private val avatar = binding.userAvatar
        private val userName = binding.userName
        private val postIdView = binding.postId
        private val commentDate = binding.commentDate
        private val commentView = binding.commentView
        private val menuView = binding.menuView

        private var comment: Comment? = null

        init {
            avatar.setOnClickListener {
                comment?.let {
                    if (it.booruType != BOORU_TYPE_GEL && it.booruType != BOORU_TYPE_GEL_LEGACY) {
                        val context = itemView.context
                        context.startActivity(Intent(context, AccountActivity::class.java).apply {
                            putExtra(AccountActivity.USER_ID_KEY, it.creatorId)
                            putExtra(AccountActivity.USER_NAME_KEY, it.creatorName)
                            putExtra(AccountActivity.USER_AVATAR_KEY, it.creatorAvatar)
                        })
                    }
                }
            }
            itemView.setOnClickListener {
                comment?.let {
                    if (it.booruType != BOORU_TYPE_GEL && it.booruType != BOORU_TYPE_GEL_LEGACY) {
                        SearchActivity.startSearch(itemView.context, "id:${it.postId}")
                    }
                }
            }
            menuView.setOnMenuItemClickListener { menuItem ->
                comment?.let {
                    when (menuItem?.itemId) {
                        R.id.action_comment_reply -> {
                            replyCallback(it.postId)
                        }
                        R.id.action_comment_quote -> {
                            quoteCallback(it.postId, "[quote]${it.creatorName} said:\r\n${commentView.getLastCommentText()}[/quote]")
                        }
                        R.id.action_comment_delete -> {
                            deleteCallback(it.id)
                        }
                        else -> {

                        }
                    }
                }
                true
            }
        }

        fun bind(data: Comment?) {
            comment = data ?: return
            userName.text = data.creatorName
            postIdView.text = String.format("Post %d", data.postId)
            commentDate.text = itemView.context.formatDate(data.time)
            commentView.setComment(data.body)
            val user = booru.user
            if (user != null && data.booruType != BOORU_TYPE_GEL && data.booruType != BOORU_TYPE_GEL_LEGACY) {
                if (user.id == data.creatorId) {
                    menuView.menu.clear()
                    MenuInflater(itemView.context).inflate(R.menu.comment_item_me, menuView.menu)
                } else {
                    menuView.menu.clear()
                    MenuInflater(itemView.context).inflate(R.menu.comment_item, menuView.menu)
                }
            }
            if (data.booruType == BOORU_TYPE_MOE) {
                avatar.load(String.format(itemView.resources.getString(R.string.account_user_avatars), booru.scheme, booru.host, data.creatorId)) {
                    placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.avatar_account))
                    error(ContextCompat.getDrawable(itemView.context, R.drawable.avatar_account))
                }
            } else if (data.booruType == BOORU_TYPE_SANKAKU) {
                avatar.load(data.creatorAvatar) {
                    placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.avatar_account))
                    error(ContextCompat.getDrawable(itemView.context, R.drawable.avatar_account))
                }
            }
        }
    }
}