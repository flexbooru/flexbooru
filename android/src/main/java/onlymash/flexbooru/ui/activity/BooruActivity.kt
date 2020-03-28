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

package onlymash.flexbooru.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_booru.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.parseList
import kotlinx.serialization.stringify
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.common.Settings.isOrderSuccess
import onlymash.flexbooru.common.Values
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.extension.safeCloseQuietly
import onlymash.flexbooru.ui.adapter.BooruAdapter
import onlymash.flexbooru.ui.viewmodel.BooruViewModel
import onlymash.flexbooru.ui.viewmodel.getBooruViewModel
import org.kodein.di.erased.instance
import java.io.IOException
import java.io.InputStream

class BooruActivity : BaseActivity() {

    private val booruDao: BooruDao by instance()

    private val booruAdapter by lazy { BooruAdapter(this) }

    private lateinit var booruViewModel: BooruViewModel

    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booru)
        initToolbar()
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
        booruViewModel = getBooruViewModel(booruDao)
        booruViewModel.loadBoorus().observe(this, Observer { boorus ->
            booruAdapter.updateBoorus(boorus)
            if (boorus.isNullOrEmpty()) {
                 activatedBooruUid = createDefaultBooru()
            } else {
                val uid = activatedBooruUid
                if (boorus.indexOfFirst { it.uid == uid } < 0) {
                    activatedBooruUid = boorus[0].uid
                }
            }
        })
        if (intent != null) {
            handleShareIntent(intent)
        }
    }

    private fun createDefaultBooru(): Long {
        return booruViewModel.createBooru(
            Booru(
                name = "Sample",
                scheme = "https",
                host = "moe.fiepi.com",
                hashSalt = "onlymash--your-password--",
                type = Values.BOORU_TYPE_MOE
            )
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent) {
        val sharedStr = intent.data?.toString()
        if (sharedStr.isNullOrEmpty()) return
        Booru.url2Booru(sharedStr)?.let {
            AlertDialog.Builder(this)
                .setTitle(R.string.booru_add_title_dialog)
                .setPositiveButton(R.string.dialog_yes) { _, _ ->
                    booruViewModel.createBooru(it)
                }
                .setNegativeButton(R.string.dialog_no, null)
                .setMessage(sharedStr)
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
                R.id.action_booru_backup_to_file -> {
                    backupToFile()
                }
                R.id.action_booru_restore_from_file -> {
                    restoreFromFile()
                }
            }
            true
        }
    }

    private fun backupToFile() {
        if (!isOrderSuccess) {
            startActivity(Intent(this, PurchaseActivity::class.java))
            return
        }
        val intent = Intent().apply {
            action = Intent.ACTION_CREATE_DOCUMENT
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "boorus.json")
        }
        try {
            startActivityForResult(intent,
                REQUEST_CODE_BACKUP_TO_FILE
            )
        } catch (_: ActivityNotFoundException) {}
    }

    private fun restoreFromFile() {
        if (!isOrderSuccess) {
            startActivity(Intent(this, PurchaseActivity::class.java))
            return
        }
        val intent = Intent().apply {
            action = Intent.ACTION_OPEN_DOCUMENT
            addCategory(Intent.CATEGORY_OPENABLE)
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "application/json" else "application/octet-stream"
        }
        try {
            startActivityForResult(intent,
                REQUEST_CODE_RESTORE_FROM_FILE
            )
        } catch (_: ActivityNotFoundException) {}
    }

    private fun addConfig() {
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
                booruViewModel.createBooru(booru)
            } else {
                Snackbar.make(toolbar, R.string.booru_add_error, Snackbar.LENGTH_LONG).show()
            }
        } else {
            Snackbar.make(toolbar, R.string.booru_add_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return
        when (requestCode) {
            REQUEST_CODE_BACKUP_TO_FILE -> {
                val uri = data.data ?: return
                GlobalScope.launch(Dispatchers.Main) {
                    val success = withContext(Dispatchers.IO) {
                        val boorus = booruAdapter.getBoorus()
                        if (boorus.isNullOrEmpty()) return@withContext false
                        val outputStream = contentResolver.openOutputStream(uri) ?: return@withContext false
                        var inputStream: InputStream? = null
                        try {
                            inputStream = Json(JsonConfiguration(
                                ignoreUnknownKeys = true,
                                prettyPrint = true
                            )).stringify(boorus).byteInputStream()
                            inputStream.copyTo(outputStream)
                        } catch (_:IOException) {
                            return@withContext false
                        } finally {
                            inputStream?.safeCloseQuietly()
                            outputStream.safeCloseQuietly()
                        }
                        return@withContext true
                    }
                    if (success) {
                        Toast.makeText(this@BooruActivity, "Success: ${uri.path}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@BooruActivity, "Save failed!", Toast.LENGTH_LONG).show()
                    }
                }
            }
            REQUEST_CODE_RESTORE_FROM_FILE -> {
                val uri = data.data ?: return
                val inputStream = contentResolver.openInputStream(uri) ?: return
                var boorus: List<Booru>? = null
                try {
                    val jsonString = inputStream.readBytes().toString(Charsets.UTF_8)
                    boorus = Json(JsonConfiguration(ignoreUnknownKeys = true)).parseList(jsonString)
                } catch (_: Exception) {

                } finally {
                    inputStream.close()
                }
                boorus?.let {
                    booruViewModel.createBoorus(it)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_BACKUP_TO_FILE = 11
        private const val REQUEST_CODE_RESTORE_FROM_FILE = 12
    }
}
