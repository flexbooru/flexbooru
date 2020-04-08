package onlymash.flexbooru.ui.activity

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_muzei.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.common.Values
import onlymash.flexbooru.common.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.data.action.ActionTag
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.database.dao.MuzeiDao
import onlymash.flexbooru.data.model.common.Muzei
import onlymash.flexbooru.data.repository.suggestion.SuggestionRepositoryImpl
import onlymash.flexbooru.extension.openAppInMarket
import onlymash.flexbooru.ui.adapter.MuzeiAdapter
import onlymash.flexbooru.ui.viewmodel.MuzeiViewModel
import onlymash.flexbooru.ui.viewmodel.SuggestionViewModel
import onlymash.flexbooru.ui.viewmodel.getMuzeiViewModel
import onlymash.flexbooru.ui.viewmodel.getSuggestionViewModel
import onlymash.flexbooru.widget.hideNavBar
import onlymash.flexbooru.worker.MuzeiArtWorker
import org.kodein.di.erased.instance

class MuzeiActivity : KodeinActivity() {

    private val muzeiDao by instance<MuzeiDao>()
    private val booruApis by instance<BooruApis>()

    private lateinit var muzeiViewModel: MuzeiViewModel
    private lateinit var muzeiAdapter: MuzeiAdapter
    private lateinit var actionTag: ActionTag

    private val suggestions: MutableList<String> = mutableListOf()
    private var suggestionsAdapter: CursorAdapter? = null
    private lateinit var suggestionViewModel: SuggestionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muzei)
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
        muzeiViewModel.loadMuzei(booru.uid).observe(this, Observer {
            muzeiAdapter.updateData(it)
        })
        suggestionViewModel = getSuggestionViewModel(SuggestionRepositoryImpl(booruApis))
        suggestionViewModel.suggestions.observe(this, Observer {
            suggestions.clear()
            suggestions.addAll(it)
            handleSuggestions(it)
        })
    }

    private fun initView() {
        hideNavBar { insets ->
            muzei_list.updatePadding(bottom = insets.systemWindowInsetBottom)
            muzei_button.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_normal) + insets.systemWindowInsetBottom
            }
        }
        toolbar.apply {
            setTitle(R.string.title_muzei)
            inflateMenu(R.menu.muzei)
            setNavigationOnClickListener {
                onBackPressed()
            }
            setOnMenuItemClickListener { menuItem ->
                if (menuItem?.itemId == R.id.action_muzei_fetch) {
                    MuzeiArtWorker.enqueueLoad()
                }
                true
            }
        }
        val searchView = toolbar.menu?.findItem(R.id.action_muzei_search)?.actionView as? SearchView
        if (searchView != null) {
            initSearchView(searchView)
        }
        muzei_button.setOnClickListener {
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
        muzeiAdapter = MuzeiAdapter()
        muzei_list.apply {
            layoutManager = LinearLayoutManager(this@MuzeiActivity, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@MuzeiActivity, RecyclerView.VERTICAL))
            adapter = muzeiAdapter
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