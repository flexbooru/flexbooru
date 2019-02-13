package onlymash.flexbooru.ui.adapter

import androidx.paging.AsyncPagedListDiffer
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.*

abstract class BaseHeaderPagedListAdapter<T, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>) : PagedListAdapter<T, VH>(diffCallback) {

    private val adapterCallback = AdapterListUpdateCallback(this)

    private val listUpdateCallback = object : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapterCallback.onChanged(position + 1, count, payload)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapterCallback.onMoved(fromPosition + 1, toPosition + 1)
        }

        override fun onInserted(position: Int, count: Int) {
            adapterCallback.onInserted(position + 1, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapterCallback.onRemoved(position + 1, count)
        }
    }

    private val differ = AsyncPagedListDiffer<T>(listUpdateCallback, AsyncDifferConfig.Builder<T>(diffCallback).build())

    override fun getItem(position: Int): T? = differ.getItem(position - 1)

    override fun submitList(pagedList: PagedList<T>?) {
        differ.submitList(pagedList)
    }

    override fun getCurrentList(): PagedList<T>? = differ.currentList

    override fun getItemCount(): Int = differ.itemCount + 1
}