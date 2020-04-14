/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.data.model.common.History
import onlymash.flexbooru.ui.activity.SearchActivity
import onlymash.flexbooru.ui.helper.ItemTouchCallback

class HistoryAdapter(
    private val deleteHistoryCallback: (History) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>(), ItemTouchCallback {

    companion object {
        fun historyDiffCallback(oldItems: List<History>, newItems: List<History>) = object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition].uid == newItems[newItemPosition].uid

            override fun getOldListSize(): Int = oldItems.size

            override fun getNewListSize(): Int = newItems.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition] == newItems[newItemPosition]
        }
    }

    private var data: MutableList<History> = mutableListOf()

    fun updateData(data: List<History>) {
        val result = DiffUtil.calculateDiff(historyDiffCallback(this.data, data))
        this.data.clear()
        this.data.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    override fun onSwipeItem(position: Int) {
        deleteHistoryCallback(data[position])
    }

    override fun onDragItem(position: Int, targetPosition: Int) {

    }

    override val isDragEnabled: Boolean
        get() = false

    override val isSwipeEnabled: Boolean
        get() = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder =
        HistoryViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false))

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val queryText: AppCompatTextView = itemView.findViewById(R.id.history_query_text)
        private var history: History? = null

        init {
            itemView.setOnClickListener {
                history?.let {
                    SearchActivity.startSearch(itemView.context, it.query)
                }
            }
        }

        fun bind(history: History) {
            this.history = history
            queryText.text = history.query
        }
    }
}