package onlymash.flexbooru.ui

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.database.BooruManager

class BooruConfigActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booru_config)
        initToolbar()
    }

    private fun initToolbar() {
        toolbar.setTitle(R.string.title_booru_config)
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.inflateMenu(R.menu.booru_config)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_booru_config_delete -> {
                    BooruManager.deleteBooru(BooruConfigFragment.booruUid)
                    val intent = Intent().apply {
                        putExtra(Constants.EXTRA_RESULT_KEY, Constants.RESULT_DELETE)
                    }
                    setResult(Constants.REQUEST_EDIT_CODE, intent)
                    finish()
                }
                R.id.action_booru_config_apply -> {
                    val booru = BooruConfigFragment.get()
                    when {
                        booru.name.isEmpty() -> Snackbar.make(toolbar, R.string.booru_config_name_cant_empty, Snackbar.LENGTH_LONG).show()
                        booru.host.isBlank() -> Snackbar.make(toolbar, R.string.booru_config_host_cant_empty, Snackbar.LENGTH_LONG).show()
                        booru.type == Constants.TYPE_MOEBOORU && booru.hash_salt.isNullOrEmpty() -> Snackbar.make(toolbar,
                            R.string.booru_config_hash_salt_cant_empty, Snackbar.LENGTH_LONG).show()
                        booru.uid == -1L -> {
                            BooruManager.createBooru(booru)
                            val intent = Intent().apply {
                                putExtra(Constants.EXTRA_RESULT_KEY, Constants.RESULT_ADD)
                            }
                            setResult(Constants.REQUEST_ADD_CODE, intent)
                            finish()
                        }
                        else -> {
                            BooruManager.updateBooru(booru)
                            val intent = Intent().apply {
                                putExtra(Constants.EXTRA_RESULT_KEY, Constants.RESULT_UPDATE)
                            }
                            setResult(Constants.REQUEST_EDIT_CODE, intent)
                            finish()
                        }
                    }
                }
            }
            return@setOnMenuItemClickListener true
        }
    }
}
