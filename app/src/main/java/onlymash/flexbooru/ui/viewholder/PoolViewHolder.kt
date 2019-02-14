package onlymash.flexbooru.ui.viewholder

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import onlymash.flexbooru.R
import onlymash.flexbooru.entity.PoolDan
import onlymash.flexbooru.entity.PoolMoe
import onlymash.flexbooru.glide.GlideRequests
import onlymash.flexbooru.util.ViewAnimation
import onlymash.flexbooru.util.formatDate
import onlymash.flexbooru.util.toggleArrow
import onlymash.flexbooru.widget.AutoCollapseTextView
import onlymash.flexbooru.widget.CircularImageView
import onlymash.flexbooru.widget.LinkTransformationMethod
import java.text.SimpleDateFormat
import java.util.*

class PoolViewHolder(itemView: View, private val glide: GlideRequests): RecyclerView.ViewHolder(itemView) {

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): PoolViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pool, parent, false)
            return PoolViewHolder(view, glide)
        }
    }
    private val container: ConstraintLayout = itemView.findViewById(R.id.container)
    private val userAvatar: CircularImageView = itemView.findViewById(R.id.user_avatar)
    private val poolName: TextView = itemView.findViewById(R.id.pool_name)
    private val poolIdCount: TextView = itemView.findViewById(R.id.pool_id_and_count)
    private val poolDescription: AutoCollapseTextView = itemView.findViewById(R.id.pool_description)
    private val poolDate: TextView = itemView.findViewById(R.id.pool_date)
    private val expandBottom: ImageButton = itemView.findViewById(R.id.bt_expand)
    private val descriptionContainer: LinearLayout = itemView.findViewById(R.id.description_container)
    private var pool: Any? = null
    private var isShow = false

    private var itemListener: ItemListener? = null

    fun setItemListener(listener: ItemListener) {
        itemListener = listener
    }

    interface ItemListener {
        fun onClickItem(keyword: String)
        fun onClickUserAvatar(id: Int, name: String?)
    }

    init {
        container.setOnClickListener {
            when (pool) {
                is PoolDan -> {
                    val keyword = String.format("pool:%d", (pool as PoolDan).id)
                    itemListener?.onClickItem(keyword)
                }
                is PoolMoe -> {
                    val keyword = String.format("pool:%d", (pool as PoolMoe).id)
                    itemListener?.onClickItem(keyword)
                }
            }
        }
        expandBottom.setOnClickListener {
            if (!poolDescription.text.isNullOrBlank()) {
                isShow = toggleLayoutExpand(!isShow, expandBottom, descriptionContainer)
            } else {
                Snackbar.make(container, container.context.getString(R.string.pool_description_is_empty),
                    Snackbar.LENGTH_SHORT).show()
            }
        }
        poolDescription.apply {
            transformationMethod = LinkTransformationMethod()
            movementMethod = LinkMovementMethod.getInstance()
        }
        userAvatar.setOnClickListener {
            when (pool) {
                is PoolDan -> {
                    itemListener?.onClickUserAvatar((pool as PoolDan).creator_id, (pool as PoolDan).creator_name)
                }
                is PoolMoe -> {
                    itemListener?.onClickUserAvatar((pool as PoolMoe).user_id, null)
                }
            }
        }
    }

    fun bind(data: Any?) {
        pool = data
        val res = container.context.resources
        when (data) {
            is PoolDan -> {
                poolName.text = data.name
                poolIdCount.text = String.format(res.getString(R.string.pool_info_id_and_count), data.id, data.post_count)
                poolDescription.text = data.description
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.ENGLISH)
                poolDate.text = formatDate(sdf.parse(data.updated_at).time)
            }
            is PoolMoe -> {
                poolName.text = data.name
                poolIdCount.text = String.format(res.getString(R.string.pool_info_id_and_count), data.id, data.post_count)
                poolDescription.text = data.description
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.ENGLISH)
                poolDate.text = formatDate(sdf.parse(data.updated_at).time)
                glide.load(String.format(res.getString(R.string.account_user_avatars), data.scheme, data.host, data.user_id))
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