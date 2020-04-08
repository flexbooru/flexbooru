package onlymash.flexbooru.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_tag_blacklist.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.ui.adapter.TagBlacklistAdapter
import onlymash.flexbooru.ui.viewmodel.BooruViewModel
import onlymash.flexbooru.ui.viewmodel.getBooruViewModel
import onlymash.flexbooru.widget.hideNavBar
import org.kodein.di.erased.instance

class TagBlacklistActivity : KodeinActivity() {

    private val booruDao: BooruDao by instance()
    private lateinit var booruViewModel: BooruViewModel
    private lateinit var tagBlacklistAdapter: TagBlacklistAdapter
    private var booru: Booru? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_blacklist)
        hideNavBar {
            tag_blacklist_list.updatePadding(bottom = it.systemWindowInsetBottom)
            add_button.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = it.systemWindowInsetBottom +
                        resources.getDimensionPixelSize(R.dimen.margin_normal)
            }
        }
        toolbar.apply {
            setTitle(R.string.title_tag_blacklist)
            setNavigationOnClickListener {
                onBackPressed()
            }
        }
        tagBlacklistAdapter = TagBlacklistAdapter {
            val booru = booru ?: return@TagBlacklistAdapter
            if (booru.blacklists.remove(it)) {
                booruViewModel.updateBooru(booru)
            }
        }
        tag_blacklist_list.apply {
            layoutManager = LinearLayoutManager(this@TagBlacklistActivity, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this@TagBlacklistActivity, RecyclerView.VERTICAL))
            adapter = tagBlacklistAdapter
        }
        booruViewModel = getBooruViewModel(booruDao)
        booruViewModel.booru.observe(this, Observer {
            booru = it
            tagBlacklistAdapter.updateData(it.blacklists)
        })
        booruViewModel.loadBooru(activatedBooruUid)
        add_button.setOnClickListener {
            val booru = booru ?: return@setOnClickListener
            val padding = resources.getDimensionPixelSize(R.dimen.spacing_mlarge)
            val layout = FrameLayout(this@TagBlacklistActivity).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(padding, padding / 2, padding, 0)
            }
            val editText = EditText(this@TagBlacklistActivity)
            layout.addView(editText)
            AlertDialog.Builder(this@TagBlacklistActivity)
                .setTitle(R.string.tag_blacklist_add)
                .setView(layout)
                .setPositiveButton(R.string.dialog_yes) { _, _ ->
                    val text = (editText.text ?: "").toString().trim()
                    if (!text.isBlank()) {
                        if (booru.blacklists.add(text)) {
                            booruViewModel.updateBooru(booru)
                        }
                    } else {
                        Snackbar.make(
                            toolbar,
                            getString(R.string.muzei_input_cant_be_empty),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton(R.string.dialog_no, null)
                .create()
                .show()
        }
    }
}