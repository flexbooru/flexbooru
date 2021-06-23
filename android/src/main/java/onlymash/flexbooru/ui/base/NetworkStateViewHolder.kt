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

package onlymash.flexbooru.ui.base

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.databinding.ItemNetworkStateBinding
import onlymash.flexbooru.ui.viewbinding.viewBinding

class NetworkStateViewHolder(binding: ItemNetworkStateBinding, retryCallback: () -> Unit) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup, retryCallback: () -> Unit): this(
        parent.viewBinding(ItemNetworkStateBinding::inflate), retryCallback)

    private val retry = binding.retryButton.also {
        it.setOnClickListener { retryCallback() }
    }
    private val errorMsg = binding.errorMsg

    fun bindTo(loadState: LoadState) {
        retry.isVisible = loadState is LoadState.Error
        errorMsg.isVisible = !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
        errorMsg.text = (loadState as? LoadState.Error)?.error?.message
    }
}