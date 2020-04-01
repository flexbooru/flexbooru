package onlymash.flexbooru.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.Comment
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.ui.activity.AccountActivity
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.widget.CommentView

class CommentAdapter(
    private val glide: GlideRequests,
    private val booru: Booru,
    private val replyCallback: (Int) -> Unit,
    private val quoteCallback: (Int, String) -> Unit,
    private val deleteCallback: (Int) -> Unit,
    retryCallback: () -> Unit
) : BasePagedListAdapter<Comment, RecyclerView.ViewHolder>(COMMENT_COMPARATOR, retryCallback) {

    companion object {
        val COMMENT_COMPARATOR = object : DiffUtil.ItemCallback<Comment>() {
            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem.body == newItem.body
            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem.id == newItem.id
        }
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        CommentViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false))

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CommentViewHolder).bind(getItem(position))
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val avatar: CircleImageView = itemView.findViewById(R.id.user_avatar)
        private val userName: AppCompatTextView = itemView.findViewById(R.id.user_name)
        private val postIdView: AppCompatTextView = itemView.findViewById(R.id.post_id)
        private val commentDate: AppCompatTextView = itemView.findViewById(R.id.comment_date)
        private val commentView: CommentView = itemView.findViewById(R.id.comment_view)
        private val menuView: ActionMenuView = itemView.findViewById(R.id.menu_view)

        private var comment: Comment? = null

        init {
            avatar.setOnClickListener {
                comment?.let {
                    if (it.booruType != BOORU_TYPE_GEL) {
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
                    if (it.booruType != BOORU_TYPE_GEL) {
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
            commentDate.text = data.date
            commentView.setComment(data.body)
            val user = booru.user
            if (user != null && data.booruType != BOORU_TYPE_GEL) {
                if (user.id == data.creatorId) {
                    menuView.menu.clear()
                    MenuInflater(itemView.context).inflate(R.menu.comment_item_me, menuView.menu)
                } else {
                    menuView.menu.clear()
                    MenuInflater(itemView.context).inflate(R.menu.comment_item, menuView.menu)
                }
            }
            if (data.booruType == BOORU_TYPE_MOE) {
                glide.load(String.format(itemView.resources.getString(R.string.account_user_avatars),
                    booru.scheme, booru.host, data.creatorId))
                    .placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.avatar_account))
                    .into(avatar)
            } else if (data.booruType == BOORU_TYPE_SANKAKU) {
                glide.load(data.creatorAvatar)
                    .placeholder(ContextCompat.getDrawable(itemView.context, R.drawable.avatar_account))
                    .into(avatar)
            }
        }
    }
}