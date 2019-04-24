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
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_account_config.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.repository.account.FindUserListener
import onlymash.flexbooru.repository.account.UserRepository
import onlymash.flexbooru.util.HashUtil
import onlymash.flexbooru.util.launchUrl

class AccountConfigActivity : BaseActivity() {

    companion object {
        private const val TAG = "AccountConfigActivity"
    }

    private val findListener = object : FindUserListener {
        override fun onSuccess(user: User) {
            when (booru.type) {
                Constants.TYPE_DANBOORU -> {
                    user.apply {
                        booru_uid = booru.uid
                        api_key = pass
                    }
                    UserManager.createUser(user)
                }
                Constants.TYPE_MOEBOORU,
                Constants.TYPE_DANBOORU_ONE,
                Constants.TYPE_SANKAKU -> {
                    user.apply {
                        booru_uid = booru.uid
                        password_hash = pass
                    }
                    UserManager.createUser(user)
                }
            }
            startActivity(Intent(this@AccountConfigActivity, AccountActivity::class.java))
            finish()
        }
        override fun onFailed(msg: String) {
            error_msg.text = msg
            progress_bar.visibility = View.INVISIBLE
            set_account.visibility = View.VISIBLE
        }
    }

    private lateinit var booru: Booru
    private var username = ""
    private var pass = ""
    private var requesting = false

    private val userFinder: UserRepository by lazy { ServiceLocator.instance().getUserRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_config)
        val b = BooruManager.getBooruByUid(Settings.instance().activeBooruUid)
        if (b == null) {
            Toast.makeText(this, "ERROR: Booru not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        booru = b
        account_config_title.text = String.format(getString(R.string.title_account_config_and_booru), booru.name)
        if (booru.type == Constants.TYPE_DANBOORU) {
            password_edit_container.hint = getString(R.string.account_api_key)
            forgot_auth.setText(R.string.account_forgot_api_key)
        }
        forgot_auth.setOnClickListener {
            when (booru.type) {
                Constants.TYPE_DANBOORU -> {
                    launchUrl(String.format("%s://%s/session/new", booru.scheme, booru.host))
                }
                Constants.TYPE_MOEBOORU,
                Constants.TYPE_DANBOORU_ONE -> {
                    launchUrl(String.format("%s://%s/user/reset_password", booru.scheme, booru.host))
                }
                Constants.TYPE_SANKAKU -> {
                    var host = booru.host
                    if (host.startsWith("capi-v2.")) host = host.replaceFirst("capi-v2.", "beta.")
                    launchUrl(String.format("%s://%s/reset_password", booru.scheme, host))
                }
            }
        }
        userFinder.findUserListener = findListener
        set_account.setOnClickListener {
            attemptSetAccount()
        }
    }

    private fun attemptSetAccount() {
        if (requesting) return
        username = username_edit.text.toString()
        pass = password_edit.text.toString()
        if (username.isBlank() || pass.isBlank()) {
            Snackbar.make(account_config_title, getString(R.string.account_config_msg_tip_empty), Snackbar.LENGTH_LONG).show()
            return
        }
        val hashSalt = booru.hash_salt
        if ((booru.type == Constants.TYPE_MOEBOORU
                    || booru.type == Constants.TYPE_DANBOORU_ONE
                    || booru.type == Constants.TYPE_SANKAKU) && hashSalt.isNotBlank()) {
            pass = HashUtil.sha1(hashSalt.replace(Constants.HASH_SALT_CONTAINED, pass))
        }
        set_account.visibility = View.INVISIBLE
        progress_bar.visibility = View.VISIBLE
        userFinder.findUserByName(username, booru)
    }
}
