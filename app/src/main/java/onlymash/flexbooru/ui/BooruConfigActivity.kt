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

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.extension.isHost
import onlymash.flexbooru.ui.fragment.BooruConfigFragment

class BooruConfigActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booru_config)
        initToolbar()
    }

    private fun initToolbar() {
        toolbar.setTitle(R.string.title_booru_config)
        toolbar.setNavigationIcon(R.drawable.ic_close_24dp)
        toolbar.inflateMenu(R.menu.booru_config)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_booru_config_delete -> {
                    AlertDialog.Builder(this@BooruConfigActivity)
                        .setTitle(R.string.booru_config_dialog_title_delete)
                        .setPositiveButton(R.string.dialog_yes) { _, _ ->
                            BooruManager.deleteBooru(BooruConfigFragment.booruUid)
                            val intent = Intent().apply {
                                putExtra(Constants.EXTRA_RESULT_KEY, Constants.RESULT_DELETE)
                            }
                            setResult(Constants.REQUEST_EDIT_CODE, intent)
                            finish()
                        }
                        .setNegativeButton(R.string.dialog_no, null)
                        .create()
                        .show()
                }
                R.id.action_booru_config_apply -> {
                    val booru = BooruConfigFragment.get()
                    val hashBoorus = arrayOf(
                        Constants.TYPE_MOEBOORU,
                        Constants.TYPE_DANBOORU_ONE,
                        Constants.TYPE_SANKAKU
                        )
                    when {
                        booru.name.isEmpty() -> Snackbar.make(toolbar, R.string.booru_config_name_cant_empty, Snackbar.LENGTH_LONG).show()
                        booru.host.isEmpty() -> Snackbar.make(toolbar, R.string.booru_config_host_cant_empty, Snackbar.LENGTH_LONG).show()
                        !booru.host.isHost() -> {
                            Snackbar.make(toolbar, getString(R.string.booru_config_host_invalid), Snackbar.LENGTH_LONG).show()
                        }
                        booru.type in hashBoorus && booru.hash_salt.isEmpty() -> Snackbar.make(toolbar, R.string.booru_config_hash_salt_cant_empty, Snackbar.LENGTH_LONG).show()
                        booru.type in hashBoorus && !booru.hash_salt.contains(Constants.HASH_SALT_CONTAINED) -> {
                            Snackbar.make(toolbar, getString(R.string.booru_config_hash_salt_must_contain_yp), Snackbar.LENGTH_LONG).show()
                        }
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
