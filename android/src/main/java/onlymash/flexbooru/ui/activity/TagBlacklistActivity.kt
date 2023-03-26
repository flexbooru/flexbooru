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
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.databinding.ActivityTagBlacklistBinding
import onlymash.flexbooru.ui.adapter.TagBlacklistAdapter
import onlymash.flexbooru.ui.viewmodel.BooruViewModel
import onlymash.flexbooru.ui.viewmodel.getBooruViewModel
import onlymash.flexbooru.ui.base.KodeinActivity
import onlymash.flexbooru.ui.viewbinding.viewBinding
import org.kodein.di.instance

class TagBlacklistActivity : KodeinActivity() {

    private val booruDao by instance<BooruDao>()
    private val binding by viewBinding(ActivityTagBlacklistBinding::inflate)
    private lateinit var booruViewModel: BooruViewModel
    private lateinit var tagBlacklistAdapter: TagBlacklistAdapter
    private var booru: Booru? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_tag_blacklist)
        }
        tagBlacklistAdapter = TagBlacklistAdapter { tag ->
            booru?.apply {
                if (blacklists.contains(tag)) {
                    blacklists = blacklists.filter { it != tag }
                    booruViewModel.updateBooru(this)
                }
            }
        }
        binding.list.apply {
            layoutManager = LinearLayoutManager(this@TagBlacklistActivity, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@TagBlacklistActivity, RecyclerView.VERTICAL))
            adapter = tagBlacklistAdapter
        }
        booruViewModel = getBooruViewModel(booruDao)
        booruViewModel.booru.observe(this) {
            booru = it
            tagBlacklistAdapter.updateData(it.blacklists)
        }
        booruViewModel.loadBooru(activatedBooruUid)
        binding.fab.setOnClickListener {
            createInputDialog()
        }
    }

    private fun createInputDialog() {
        val booru = booru
        if (booru == null || isFinishing) {
            return
        }
        val padding = resources.getDimensionPixelSize(R.dimen.spacing_mlarge)
        val layout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(padding, padding / 2, padding, 0)
        }
        val editText = EditText(this)
        layout.addView(editText)
        AlertDialog.Builder(this)
            .setTitle(R.string.tag_blacklist_add)
            .setView(layout)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                val text = (editText.text ?: "").toString().trim()
                if (text.isNotBlank()) {
                    val blacks = booru.blacklists.toMutableList()
                    if (blacks.add(text)) {
                        booru.blacklists = blacks
                        booruViewModel.updateBooru(booru)
                    }
                } else {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.muzei_input_cant_be_empty),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton(R.string.dialog_no, null)
            .create()
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}