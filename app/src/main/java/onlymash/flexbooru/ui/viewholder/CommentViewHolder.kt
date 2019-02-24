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
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.CommentDan
import onlymash.flexbooru.entity.CommentMoe
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.ui.AccountActivity
import onlymash.flexbooru.ui.SearchActivity
import onlymash.flexbooru.util.formatDate
import onlymash.flexbooru.widget.CircularImageView
import onlymash.flexbooru.widget.LinkTransformationMethod
import java.text.SimpleDateFormat
import java.util.*

class CommentViewHolder(itemView: View, private val glide: GlideRequests) : RecyclerView.ViewHolder(itemView) {
    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): CommentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(view, glide)
        }
    }

    private val avatar: CircularImageView = itemView.findViewById(R.id.user_avatar)
    private val userName: TextView = itemView.findViewById(R.id.user_name)
    private val postId: TextView = itemView.findViewById(R.id.post_id)
    private val commentDate: TextView = itemView.findViewById(R.id.comment_date)
    private val commentBody: TextView = itemView.findViewById(R.id.comment_body)

    private var comment: Any? = null

    init {
        commentBody.transformationMethod = LinkTransformationMethod()
        avatar.setOnClickListener {
            when (comment) {
                is CommentDan -> {

                }
                is CommentMoe -> {
                    val context = itemView.context
                    val id = (comment as CommentMoe).creator_id
                    val name = (comment as CommentMoe).creator
                    context.startActivity(Intent(context, AccountActivity::class.java).apply {
                        putExtra(AccountActivity.USER_ID_KEY, id)
                        putExtra(AccountActivity.USER_NAME_KEY, name)
                    })
                }
            }
        }
        itemView.setOnClickListener {
            when (comment) {
                is CommentDan -> {

                }
                is CommentMoe -> {
                    SearchActivity.startActivity(itemView.context, "id:${(comment as CommentMoe).post_id}")
                }
            }
        }
    }

    fun bind(data: Any?) {
        comment = data
        if (data is CommentMoe) {
            userName.text = data.creator
            postId.text = String.format("Post %d", data.post_id)
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
            commentBody.text = data.body
            glide.load(String.format(itemView.resources.getString(R.string.account_user_avatars), data.scheme, data.host, data.creator_id))
                .placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.avatar_account))
                .into(avatar)
        }
    }
}