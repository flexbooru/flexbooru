/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
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

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_booru.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.ui.adapter.BooruAdapter
import onlymash.flexbooru.ui.fragment.BooruConfigFragment
import onlymash.flexbooru.util.launchUrl

class BooruActivity : BaseActivity() {

    private val booruAdapter by lazy { BooruAdapter(this) }

    val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booru)
        initToolbar()
        BooruManager.isNotEmpty()
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val animator = DefaultItemAnimator().apply {
            supportsChangeAnimations = false
        }
        list.apply {
            setLayoutManager(layoutManager)
            addItemDecoration(DividerItemDecoration(this@BooruActivity, layoutManager.orientation))
            itemAnimator = animator
            adapter = booruAdapter
        }
        BooruManager.listeners.add(booruAdapter)
        if (intent != null) {
            handleShareIntent(intent)
        }
        if (!Settings.instance().isOrderSuccess) {
            val adBuilder = AdRequest.Builder().addTestDevice("10776CDFD3CAEC0AA6A8349F4298F209")
            val adView = AdView(this)
            booru_container.addView(adView, 1, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            adView.apply {
                visibility = View.VISIBLE
                adSize = AdSize.SMART_BANNER
                adUnitId = "ca-app-pub-1547571472841615/5647147698"
                loadAd(adBuilder.build())
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }
    private fun handleShareIntent(intent: Intent) {
        val sharedStr = intent.data?.toString()
        if (sharedStr.isNullOrEmpty()) return
        val b = Booru.url2Booru(sharedStr)
        if (b != null) {
            AlertDialog.Builder(this)
                .setTitle(R.string.booru_add_title_dialog)
                .setPositiveButton(R.string.dialog_yes) { _, _ ->
                    BooruManager.createBooru(b)
                }
                .setNegativeButton(R.string.dialog_no, null)
                .setMessage(b.toString())
                .create()
                .show()
        }
    }
    private fun initToolbar(){
        toolbar.setTitle(R.string.title_manage_boorus)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.booru)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_booru_add_qr -> {
                    addConfigFromQRCode()
                }
                R.id.action_booru_add_clipboard -> {
                    addConfigFromClipboard()
                }
                R.id.action_booru_add_manual -> {
                    addConfig()
                }
                else -> {
                    // unknown id
                }
            }
            true
        }
    }

    private fun addConfig() {
        BooruConfigFragment.reset()
        startActivity(Intent(this, BooruConfigActivity::class.java))
    }

    private fun addConfigFromQRCode() {
        startActivity(Intent(this, ScannerActivity::class.java))
    }

    private fun addConfigFromClipboard() {
        val text = clipboard.primaryClip?.getItemAt(0)?.text
        if (text != null || Uri.parse(text.toString()).scheme != "booru") {
            val booru = Booru.url2Booru(text.toString())
            if (booru != null) {
                BooruManager.createBooru(booru)
            } else {
                Snackbar.make(toolbar, R.string.booru_add_error, Snackbar.LENGTH_LONG).show()
            }
        } else {
            Snackbar.make(toolbar, R.string.booru_add_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.REQUEST_EDIT_CODE -> {
                when (data?.getStringExtra(Constants.EXTRA_RESULT_KEY)) {
                    Constants.RESULT_DELETE -> {

                    }
                    Constants.RESULT_UPDATE -> {

                    }
                }
            }
            Constants.REQUEST_ADD_CODE -> {
                val result = data?.getStringExtra(Constants.EXTRA_RESULT_KEY)
                if (result == Constants.RESULT_ADD) {

                }
            }
        }
    }

    override fun onDestroy() {
        BooruManager.listeners.remove(booruAdapter)
        super.onDestroy()
    }
}
