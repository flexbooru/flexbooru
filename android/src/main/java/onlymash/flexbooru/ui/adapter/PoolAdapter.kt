package onlymash.flexbooru.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import de.hdodenhof.circleimageview.CircleImageView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.model.common.Pool
import onlymash.flexbooru.extension.toggleArrow
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.ui.activity.AccountActivity
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.util.ViewAnimation
import onlymash.flexbooru.widget.LinkTransformationMethod

class PoolAdapter(
    private val glide: GlideRequests,
    private val downloadPoolCallback: (Pool) -> Unit,
    retryCallback: () -> Unit
) : BasePagedListAdapter<Pool, RecyclerView.ViewHolder>(POOL_COMPARATOR, retryCallback) {
    companion object {
        val POOL_COMPARATOR = object : DiffUtil.ItemCallback<Pool>() {
            override fun areContentsTheSame(oldItem: Pool, newItem: Pool): Boolean =
                oldItem == newItem
            override fun areItemsTheSame(oldItem: Pool, newItem: Pool): Boolean =
                oldItem.id == newItem.id
        }
    }

    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        PoolViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pool, parent, false))

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PoolViewHolder).apply {
            val pool = getItem(position)
            bind(pool)
            itemView.setOnClickListener {
                pool?.let {
                    SearchActivity.startSearch(itemView.context, "pool:${it.id}")
                }
            }
            itemView.setOnLongClickListener {
                if (pool?.booruType == BOORU_TYPE_MOE) {
                    downloadPoolCallback(pool)
                }
                true
            }
            userAvatar.setOnClickListener {
                if (pool?.booruType == BOORU_TYPE_MOE || pool?.booruType == BOORU_TYPE_SANKAKU) {
                    itemView.context.startActivity(Intent(itemView.context, AccountActivity::class.java).apply {
                        putExtra(AccountActivity.USER_ID_KEY, pool.creatorId)
                        putExtra(AccountActivity.USER_NAME_KEY, pool.creatorName)
                        putExtra(AccountActivity.USER_AVATAR_KEY, pool.creatorAvatar)
                    })
                }
            }
        }
    }

    inner class PoolViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val container: ConstraintLayout = itemView.findViewById(R.id.container)
        val userAvatar: CircleImageView = itemView.findViewById(R.id.user_avatar)
        private val poolName: AppCompatTextView = itemView.findViewById(R.id.pool_name)
        private val poolIdCount: AppCompatTextView = itemView.findViewById(R.id.pool_id_and_count)
        private val poolDescription: AppCompatTextView = itemView.findViewById(R.id.pool_description)
        private val poolDate: AppCompatTextView = itemView.findViewById(R.id.pool_date)
        private val expandBottom: ImageButton = itemView.findViewById(R.id.bt_expand)
        private val descriptionContainer: LinearLayout = itemView.findViewById(R.id.description_container)
        private var isShowing = false


        init {
            expandBottom.setOnClickListener {
                if (!poolDescription.text.isNullOrBlank()) {
                    isShowing = toggleLayoutExpand(!isShowing, expandBottom, descriptionContainer)
                } else {
                    Snackbar.make(container, container.context.getString(R.string.pool_description_is_empty),
                        Snackbar.LENGTH_SHORT).show()
                }
            }
            poolDescription.transformationMethod = LinkTransformationMethod()

        }

        fun bind(pool: Pool?) {
            if (descriptionContainer.visibility == View.VISIBLE) {
                isShowing = false
                expandBottom.toggleArrow(show = false, delay = false)
                descriptionContainer.visibility = View.GONE
            }
            if (pool == null) return
            val res = container.context.resources
            poolName.text = pool.name
            poolIdCount.text = String.format(res.getString(R.string.pool_info_id_and_count), pool.id, pool.count)
            poolDescription.text = pool.description
            poolDate.text = pool.date
            when (pool.booruType) {
                BOORU_TYPE_MOE -> {
                    glide.load(String.format(res.getString(R.string.account_user_avatars), pool.scheme, pool.host, pool.creatorId))
                        .placeholder(res.getDrawable(R.drawable.avatar_account, container.context.theme))
                        .into(userAvatar)
                }
                BOORU_TYPE_SANKAKU -> {
                    glide.load(pool.creatorAvatar)
                        .placeholder(res.getDrawable(R.drawable.avatar_account, container.context.theme))
                        .into(userAvatar)
                }
            }
        }

        private fun toggleLayoutExpand(show: Boolean, view: View, container: View): Boolean {
            view.toggleArrow(show)
            if (show)
                ViewAnimation.expand(container)
            else
                ViewAnimation.collapse(container)
            return show
        }
    }
}