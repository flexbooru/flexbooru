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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_account_config.*
import kotlinx.coroutines.*
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.api.*
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.common.Booru
import onlymash.flexbooru.entity.common.User
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.extension.sha1
import onlymash.flexbooru.repository.account.UserRepositoryImpl
import onlymash.flexbooru.repository.account.UserRepository
import org.kodein.di.erased.instance

class AccountConfigActivity : BaseActivity() {

    companion object {
        private const val TAG = "AccountConfigActivity"
    }

    private val danApi: DanbooruApi by instance()
    private val danOneApi: DanbooruOneApi by instance()
    private val moeApi: MoebooruApi by instance()
    private val sankakuApi: SankakuApi by instance()

    private lateinit var booru: Booru
    private var username = ""
    private var pass = ""
    private var requesting = false

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(
            danbooruApi = danApi,
            danbooruOneApi = danOneApi,
            moebooruApi = moeApi,
            sankakuApi = sankakuApi
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_config)
        val b = BooruManager.getBooruByUid(Settings.activeBooruUid)
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
        val hashSalt = booru.hashSalt
        if ((booru.type == Constants.TYPE_MOEBOORU
                    || booru.type == Constants.TYPE_DANBOORU_ONE
                    || booru.type == Constants.TYPE_SANKAKU) && hashSalt.isNotBlank()) {
            pass = hashSalt.replace(Constants.HASH_SALT_CONTAINED, pass).sha1()
        }
        set_account.visibility = View.INVISIBLE
        progress_bar.visibility = View.VISIBLE
        if (booru.type == Constants.TYPE_GELBOORU) {
            lifecycleScope.launch {
                val result = userRepository.gelLogin(username, pass, booru)
                handlerResult(result)
            }
        } else {
            lifecycleScope.launch {
                val result = userRepository.findUserByName(username, booru)
                handlerResult(result)
            }
        }
    }

    private fun handlerResult(result: NetResult<User>) {
        when (result) {
            is NetResult.Success -> {
                val user = result.data
                when (booru.type) {
                    Constants.TYPE_DANBOORU -> {
                        user.apply {
                            booruUid = booru.uid
                            apiKey = pass
                        }
                        UserManager.createUser(user)
                    }
                    Constants.TYPE_MOEBOORU,
                    Constants.TYPE_DANBOORU_ONE,
                    Constants.TYPE_SANKAKU -> {
                        user.apply {
                            booruUid = booru.uid
                            passwordHash = pass
                        }
                        UserManager.createUser(user)
                    }
                    Constants.TYPE_GELBOORU -> {
                        UserManager.createUser(user)
                    }
                }
                startActivity(Intent(this@AccountConfigActivity, AccountActivity::class.java))
                finish()
            }
            is NetResult.Error -> {
                error_msg.text = result.errorMsg
                progress_bar.visibility = View.INVISIBLE
                set_account.visibility = View.VISIBLE
            }
        }
    }
}
