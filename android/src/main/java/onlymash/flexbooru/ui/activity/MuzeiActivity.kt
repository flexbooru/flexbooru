package onlymash.flexbooru.ui.activity

import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_muzei.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.data.database.MuzeiManager
import onlymash.flexbooru.data.database.dao.MuzeiDao
import onlymash.flexbooru.data.model.common.Muzei
import onlymash.flexbooru.extension.openAppInMarket
import onlymash.flexbooru.ui.adapter.MuzeiAdapter
import onlymash.flexbooru.ui.viewmodel.MuzeiViewModel
import onlymash.flexbooru.ui.viewmodel.getMuzeiViewModel
import onlymash.flexbooru.worker.MuzeiArtWorker
import org.kodein.di.erased.instance

class MuzeiActivity : KodeinActivity() {

    private val muzeiDao by instance<MuzeiDao>()

    private lateinit var muzeiViewModel: MuzeiViewModel
    private lateinit var muzeiAdapter: MuzeiAdapter
    private val booruUid: Long = activatedBooruUid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muzei)
        initView()
        muzeiViewModel = getMuzeiViewModel(muzeiDao)
        muzeiViewModel.loadMuzei(booruUid).observe(this, Observer {
            muzeiAdapter.updateData(it)
        })
    }

    private fun initView() {
        toolbar.apply {
            setTitle(R.string.title_muzei)
            inflateMenu(R.menu.muzei)
            setNavigationOnClickListener {
                onBackPressed()
            }
            setOnMenuItemClickListener {
                if (it.itemId == R.id.action_muzei_add) {
                    val padding = resources.getDimensionPixelSize(R.dimen.spacing_mlarge)
                    val layout = FrameLayout(this@MuzeiActivity).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        setPadding(padding, padding / 2, padding, 0)
                    }
                    val editText = EditText(this@MuzeiActivity)
                    layout.addView(editText)
                    AlertDialog.Builder(this@MuzeiActivity)
                        .setTitle(R.string.muzei_add)
                        .setView(layout)
                        .setPositiveButton(R.string.dialog_yes) { _, _ ->
                            val text = (editText.text ?: "").toString().trim()
                            if (!text.isBlank()) {
                                MuzeiManager.createMuzei(
                                    Muzei(
                                        booruUid = booruUid,
                                        query = text
                                    )
                                )
                            } else {
                                Snackbar.make(toolbar, getString(R.string.muzei_input_cant_be_empty), Snackbar.LENGTH_LONG).show()
                            }
                        }
                        .setNegativeButton(R.string.dialog_no, null)
                        .create()
                        .show()
                } else if (it.itemId == R.id.action_muzei_fetch) {
                    MuzeiArtWorker.enqueueLoad()
                }
                true
            }
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
}