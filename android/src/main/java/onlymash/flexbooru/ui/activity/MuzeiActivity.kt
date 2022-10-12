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

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.database.dao.MuzeiDao
import onlymash.flexbooru.data.model.common.Muzei
import onlymash.flexbooru.data.repository.suggestion.SuggestionRepositoryImpl
import onlymash.flexbooru.databinding.ActivityMuzeiBinding
import onlymash.flexbooru.extension.openAppInMarket
import onlymash.flexbooru.ui.adapter.MuzeiAdapter
import onlymash.flexbooru.ui.helper.ItemTouchCallback
import onlymash.flexbooru.ui.helper.ItemTouchHelperCallback
import onlymash.flexbooru.ui.viewmodel.MuzeiViewModel
import onlymash.flexbooru.ui.viewmodel.SuggestionViewModel
import onlymash.flexbooru.ui.viewmodel.getMuzeiViewModel
import onlymash.flexbooru.ui.viewmodel.getSuggestionViewModel
import onlymash.flexbooru.ui.base.KodeinActivity
import onlymash.flexbooru.ui.viewbinding.viewBinding
import onlymash.flexbooru.worker.MuzeiArtWorker
import org.kodein.di.instance

class MuzeiActivity : KodeinActivity() {

    private val muzeiDao by instance<MuzeiDao>()
    private val booruApis by instance<BooruApis>()

    private val binding by viewBinding(ActivityMuzeiBinding::inflate)

    private lateinit var muzeiViewModel: MuzeiViewModel
    private lateinit var muzeiAdapter: MuzeiAdapter
    private lateinit var actionTag: ActionTag

    private val suggestions: MutableList<String> = mutableListOf()
    private var suggestionsAdapter: CursorAdapter? = null
    private lateinit var suggestionViewModel: SuggestionViewModel

    private val itemTouchCallback = object : ItemTouchCallback {
        override val isDragEnabled: Boolean
            get() = false
        override val isSwipeEnabled: Boolean
            get() = true
        override fun onDragItem(position: Int, targetPosition: Int) {

        }
        override fun onSwipeItem(position: Int) {
            val uid = muzeiAdapter.getUidByPosition(position) ?: -1L
            if (uid >= 0) {
                muzeiViewModel.deleteByUid(uid)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val booru = BooruManager.getBooruByUid(activatedBooruUid)
        if (booru == null) {
            finish()
            return
        }
        actionTag = ActionTag(
            booru = booru,
            order = "count",
            limit = 6
        )
        initView()
        muzeiViewModel = getMuzeiViewModel(muzeiDao)
        muzeiViewModel.loadMuzei(booru.uid).observe(this, {
            muzeiAdapter.updateData(it)
        })
        suggestionViewModel = getSuggestionViewModel(SuggestionRepositoryImpl(booruApis))
        suggestionViewModel.suggestions.observe(this, {
            suggestions.clear()
            suggestions.addAll(it)
            handleSuggestions(it)
        })
    }

    private fun initView() {
        val list = binding.muzeiList
        val fabMuzei = binding.muzeiButton
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_muzei)
        }
        fabMuzei.setOnClickListener {
            val muzeiPackageName = "net.nurik.roman.muzei"
            try {
                val intent = packageManager.getLaunchIntentForPackage(muzeiPackageName)
                if (intent == null) {
                    openAppInMarket(muzeiPackageName)
                } else {
                    startActivity(intent)
                }
            } catch (_: PackageManager.NameNotFoundException) {
                openAppInMarket(muzeiPackageName)
            } catch (_: ActivityNotFoundException) {
                openAppInMarket(muzeiPackageName)
            }
        }
        muzeiAdapter = MuzeiAdapter {
            muzeiViewModel.deleteByUid(it)
        }
        list.apply {
            layoutManager = LinearLayoutManager(this@MuzeiActivity, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@MuzeiActivity, RecyclerView.VERTICAL))
            adapter = muzeiAdapter
            ItemTouchHelper(ItemTouchHelperCallback(itemTouchCallback)).attachToRecyclerView(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.muzei, menu)
        val searchView = menu.findItem(R.id.action_muzei_search)?.actionView as? SearchView
        if (searchView != null) {
            initSearchView(searchView)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_muzei_fetch -> {
                MuzeiArtWorker.enqueueLoad()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initSearchView(searchView: SearchView) {
        suggestionsAdapter = SimpleCursorAdapter(
            this,
            R.layout.item_suggestion_muzei,
            null,
            arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
            intArrayOf(android.R.id.text1),
            0
        )
        searchView.suggestionsAdapter = suggestionsAdapter
        searchView.queryHint = getString(R.string.muzei_search_hint)
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val tag = suggestions[position]
                muzeiViewModel.create(Muzei(
                    booruUid = actionTag.booru.uid,
                    query = tag
                ))
                return true
            }
            override fun onSuggestionSelect(position: Int): Boolean {
                return true
            }
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (actionTag.booru.type == BOORU_TYPE_SHIMMIE || newText.isNullOrBlank()) {
                    return false
                }
                val query = newText.trim()
                actionTag.query = when (actionTag.booru.type) {
                    Values.BOORU_TYPE_MOE,
                    Values.BOORU_TYPE_DAN,
                    Values.BOORU_TYPE_DAN1 -> "$query*"
                    else -> query
                }
                suggestionViewModel.fetchSuggestions(actionTag)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrBlank()) return false
                muzeiViewModel.create(
                    Muzei(
                        booruUid = actionTag.booru.uid,
                        query = query.trim()
                    )
                )
                return true
            }
        })
    }

    private fun handleSuggestions(suggestions: List<String>) {
        val columns = arrayOf(
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
        )
        val cursor = MatrixCursor(columns)
        suggestions.forEachIndexed { index, suggestion ->
            val tmp = arrayOf(
                index.toString(),
                suggestion,
                suggestion
            )
            cursor.addRow(tmp)
        }
        suggestionsAdapter?.swapCursor(cursor)
    }
}