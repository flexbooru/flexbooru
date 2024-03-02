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

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.app.Settings.isOrderSuccess
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.databinding.ActivityBooruBinding
import onlymash.flexbooru.extension.getScreenWidthDp
import onlymash.flexbooru.extension.safeCloseQuietly
import onlymash.flexbooru.ui.adapter.BooruAdapter
import onlymash.flexbooru.ui.base.BaseActivity
import onlymash.flexbooru.ui.helper.ItemTouchCallback
import onlymash.flexbooru.ui.helper.ItemTouchHelperCallback
import onlymash.flexbooru.ui.viewmodel.BooruViewModel
import onlymash.flexbooru.ui.viewmodel.getBooruViewModel
import onlymash.flexbooru.ui.fragment.QRCodeDialog
import onlymash.flexbooru.ui.helper.CreateFileLifecycleObserver
import onlymash.flexbooru.ui.helper.OpenFileLifecycleObserver
import onlymash.flexbooru.ui.viewbinding.viewBinding
import org.koin.android.ext.android.inject
import java.io.IOException
import java.io.InputStream

class BooruActivity : BaseActivity() {

    private val booruDao by inject<BooruDao>()

    private val binding by viewBinding(ActivityBooruBinding::inflate)
    private val list get() = binding.list
    private lateinit var booruAdapter: BooruAdapter
    private lateinit var booruViewModel: BooruViewModel
    private lateinit var createFileObserver: CreateFileLifecycleObserver
    private lateinit var openFileObserver: OpenFileLifecycleObserver

    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    private val itemTouchCallback = object : ItemTouchCallback {
        override val isDragEnabled: Boolean
            get() = false

        override val isSwipeEnabled: Boolean
            get() = true

        override fun onDragItem(position: Int, targetPosition: Int) {

        }

        override fun onSwipeItem(position: Int) {
            val uid = booruAdapter.getUidByPosition(position)
            if (uid != null) {
                booruViewModel.deleteBooru(uid)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.apply {
            setTitle(R.string.title_manage_boorus)
            setDisplayHomeAsUpEnabled(true)
        }
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val animator = DefaultItemAnimator().apply {
            supportsChangeAnimations = false
        }
        booruAdapter = BooruAdapter {
            supportFragmentManager
                .beginTransaction()
                .add(QRCodeDialog(it), "qr")
                .commitAllowingStateLoss()
        }
        list.apply {
            setLayoutManager(layoutManager)
            addItemDecoration(DividerItemDecoration(this@BooruActivity, layoutManager.orientation))
            itemAnimator = animator
            adapter = booruAdapter
            ItemTouchHelper(ItemTouchHelperCallback(itemTouchCallback)).attachToRecyclerView(this)
        }
        booruViewModel = getBooruViewModel(booruDao)
        booruViewModel.loadBoorus().observe(this) { boorus ->
            booruAdapter.updateBoorus(boorus)
            if (boorus.isNullOrEmpty()) {
                activatedBooruUid = createDefaultBooru()
            } else {
                val uid = activatedBooruUid
                if (boorus.indexOfFirst { it.uid == uid } < 0) {
                    activatedBooruUid = boorus[0].uid
                }
            }
        }
        createFileObserver = CreateFileLifecycleObserver(activityResultRegistry) { uri ->
            saveFile(uri)
        }
        openFileObserver = OpenFileLifecycleObserver(activityResultRegistry) { uri ->
            openFile(uri)
        }
        lifecycle.addObserver(createFileObserver)
        lifecycle.addObserver(openFileObserver)
        if (intent != null) {
            handleShareIntent(intent)
        }
        if (!isOrderSuccess) {
            val adView = AdView(this)
            binding.container.addView(adView, 0, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            })
            var adWidth = getScreenWidthDp()
            if (adWidth > 500) {
                adWidth = 500
            }
            adView.apply {
                visibility = View.VISIBLE
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this@BooruActivity, adWidth))
                adUnitId = "ca-app-pub-1547571472841615/5647147698"
                loadAd(AdRequest.Builder().build())
            }
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
        if (sharedStr.isNullOrEmpty() || isFinishing) {
            return
        }
        val booru = Booru.url2Booru(sharedStr) ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.booru_add_title_dialog)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                booruViewModel.createBooru(booru)
            }
            .setNegativeButton(R.string.dialog_no, null)
            .setMessage(sharedStr)
            .create()
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.booru, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_booru_add_qr -> {
                addConfigFromQRCode()
                true
            }
            R.id.action_booru_add_clipboard -> {
                addConfigFromClipboard()
                true
            }
            R.id.action_booru_add_manual -> {
                addConfig()
                true
            }
            R.id.action_booru_backup_to_file -> {
                backupToFile()
                true
            }
            R.id.action_booru_restore_from_file -> {
                restoreFromFile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backupToFile() {
        if (!isOrderSuccess) {
            startActivity(Intent(this, PurchaseActivity::class.java))
            return
        }
        createFileObserver.createDocument("boorus.json")
    }

    private fun restoreFromFile() {
        if (!isOrderSuccess) {
            startActivity(Intent(this, PurchaseActivity::class.java))
            return
        }
        val mimeType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "application/json" else "application/octet-stream"
        openFileObserver.openDocument(mimeType)
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
                Snackbar.make(binding.root, R.string.booru_add_error, Snackbar.LENGTH_LONG).show()
            }
        } else {
            Snackbar.make(binding.root, R.string.booru_add_error, Snackbar.LENGTH_LONG).show()
        }
    }

    private val json get() = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private fun saveFile(uri: Uri) {
        GlobalScope.launch(Dispatchers.Main) {
            val success = withContext(Dispatchers.IO) {
                val boorus = booruAdapter.getBoorus()
                if (boorus.isEmpty()) return@withContext false
                val outputStream = contentResolver.openOutputStream(uri) ?: return@withContext false
                var inputStream: InputStream? = null
                try {
                    inputStream = json.encodeToString(boorus).byteInputStream()
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

    private fun openFile(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri) ?: return
        var boorus: List<Booru>? = null
        try {
            val jsonString = inputStream.readBytes().toString(Charsets.UTF_8)
            boorus = json.decodeFromString(jsonString)
        } catch (_: Exception) {

        } finally {
            inputStream.close()
        }
        boorus?.let {
            booruViewModel.createBoorus(it)
        }
    }
}
