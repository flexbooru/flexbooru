package onlymash.flexbooru.ui.adapter

import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activeMuzeiUid
import onlymash.flexbooru.data.database.MuzeiManager
import onlymash.flexbooru.data.model.common.Muzei
import onlymash.flexbooru.extension.copyText
import onlymash.flexbooru.ui.activity.SearchActivity

class MuzeiAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        fun muzeiDiffCallback(oldItems: List<Muzei>, newItems: List<Muzei>) = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition].uid == newItems[newItemPosition].uid

            override fun getOldListSize(): Int = oldItems.size

            override fun getNewListSize(): Int = newItems.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition] == newItems[newItemPosition]
        }
    }

    private var data: MutableList<Muzei> = mutableListOf()

    private var activeUid = activeMuzeiUid

    private fun refresh(uid: Long) {
        val index = data.indexOfFirst { it.uid == uid }
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }


    fun updateData(data: List<Muzei>) {
        val result = DiffUtil.calculateDiff(muzeiDiffCallback(this.data, data))
        this.data.clear()
        this.data.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MuzeiViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_muzei, parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = data[position]
        val uid = data.uid
        (holder as MuzeiViewHolder).bind(data)
        holder.itemView.apply {
            tag = uid
            isSelected = uid == activeUid
            setOnClickListener {
                if (!isSelected) {
                    refresh(activeUid)
                    activeUid = uid
                    activeMuzeiUid = uid
                    isSelected = true
                }
            }
        }
    }

    class MuzeiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val keyword = itemView.findViewById<AppCompatTextView>(R.id.muzei_keyword)
        private val actionMenu = itemView.findViewById<ActionMenuView>(R.id.action_menu)
        private lateinit var muzei: Muzei

        init {
            MenuInflater(itemView.context).inflate(R.menu.muzei_item, actionMenu.menu)
            actionMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_muzei_item_search -> {
                        SearchActivity.startSearch(itemView.context, muzei.query)
                    }
                    R.id.action_muzei_item_copy -> {
                        itemView.context.copyText(muzei.query)
                    }
                    R.id.action_muzei_item_delete -> {
                        MuzeiManager.deleteMuzei(muzei)
                    }
                }
                true
            }
        }

        fun bind(muzei: Muzei) {
            this.muzei = muzei
            keyword.text = muzei.query
        }
    }
}