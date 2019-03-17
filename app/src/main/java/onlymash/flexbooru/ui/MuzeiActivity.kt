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

package onlymash.flexbooru.ui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_muzei.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.content.muzei.FlexArtWorker
import onlymash.flexbooru.database.MuzeiManager
import onlymash.flexbooru.entity.Muzei
import onlymash.flexbooru.ui.adapter.MuzeiAdapter
import onlymash.flexbooru.ui.viewmodel.MuzeiViewModel

class MuzeiActivity : BaseActivity() {

    companion object {
        private const val TAG = "MuzeiActivity"
    }

    private lateinit var muzeiViewModel: MuzeiViewModel
    private lateinit var muzeiAdapter: MuzeiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muzei)
        val uid = Settings.instance().activeBooruUid
        toolbar.apply {
            setTitle(R.string.title_muzei)
            inflateMenu(R.menu.muzei)
            setNavigationOnClickListener {
                onBackPressed()
            }
            setOnMenuItemClickListener {
                if (it.itemId == R.id.action_muzei_add) {
                    val padding = resources.getDimensionPixelSize(R.dimen.spacing_middle)
                    val layout = FrameLayout(this@MuzeiActivity).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        setPadding(padding, padding, padding, 0)
                    }
                    val editText = EditText(this@MuzeiActivity)
                    layout.addView(editText)
                    AlertDialog.Builder(this@MuzeiActivity)
                        .setTitle(R.string.muzei_add)
                        .setView(layout)
                        .setPositiveButton(R.string.dialog_yes) { _, _ ->
                            val text = (editText.text ?: "").toString().trim()
                            if (!text.isBlank()) {
                                MuzeiManager.createMuzei(Muzei(booru_uid = uid, keyword = text))
                            } else {
                                Snackbar.make(toolbar, getString(R.string.muzei_input_cant_be_empty), Snackbar.LENGTH_LONG).show()
                            }
                        }
                        .setNegativeButton(R.string.dialog_no, null)
                        .create()
                        .show()
                } else if (it.itemId == R.id.action_muzei_fetch) {
                    FlexArtWorker.enqueueLoad()
                }
                true
            }
        }
        muzeiAdapter = MuzeiAdapter()
        muzei_list.apply {
            layoutManager = LinearLayoutManager(this@MuzeiActivity, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@MuzeiActivity, RecyclerView.VERTICAL))
            adapter = muzeiAdapter
        }
        muzeiViewModel = getMuzeiViewModel()
        muzeiViewModel.muzeiOutcome.observe(this, Observer {
            muzeiAdapter.updateData(it)
        })
        muzeiViewModel.loadMuzei(uid)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getMuzeiViewModel(): MuzeiViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return MuzeiViewModel() as T
            }
        })[MuzeiViewModel::class.java]
    }
}
