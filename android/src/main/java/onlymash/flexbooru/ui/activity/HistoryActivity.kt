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

package onlymash.flexbooru.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.data.database.dao.HistoryDao
import onlymash.flexbooru.data.database.dao.PostDao
import onlymash.flexbooru.databinding.ActivityListCommonBinding
import onlymash.flexbooru.ui.adapter.HistoryAdapter
import onlymash.flexbooru.ui.base.BaseActivity
import onlymash.flexbooru.ui.helper.ItemTouchHelperCallback
import onlymash.flexbooru.ui.viewmodel.HistoryViewModel
import onlymash.flexbooru.ui.viewmodel.getHistoryViewModel
import onlymash.flexbooru.ui.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class HistoryActivity : BaseActivity() {

    private val historyDao by inject<HistoryDao>()
    private val postDao by inject<PostDao>()

    private val binding by viewBinding(ActivityListCommonBinding::inflate)

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.list)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_history)
        }
        historyViewModel = getHistoryViewModel(historyDao, postDao)
        historyAdapter = HistoryAdapter { history ->
            historyViewModel.deleteByUid(history)
        }
        binding.list.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@HistoryActivity, RecyclerView.VERTICAL))
            adapter = historyAdapter
            ItemTouchHelper(ItemTouchHelperCallback(historyAdapter)).attachToRecyclerView(this)
        }
        historyViewModel.loadHistory(activatedBooruUid).observe(this) {
            historyAdapter.updateData(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            item.itemId == R.id.action_history_clear_all && historyAdapter.itemCount > 0 -> {
                clearAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearAll() {
        if (isFinishing) {
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.history_clear_all)
            .setMessage(R.string.history_clear_all_content)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                historyViewModel.deleteAll(activatedBooruUid)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
            .show()
    }
}