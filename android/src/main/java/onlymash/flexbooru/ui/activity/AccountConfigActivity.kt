/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_account_config.*
import kotlinx.coroutines.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.common.Values.HASH_SALT_CONTAINED
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.extension.sha1
import onlymash.flexbooru.data.repository.user.UserRepositoryImpl
import onlymash.flexbooru.data.repository.user.UserRepository
import org.kodein.di.erased.instance

class AccountConfigActivity : BaseActivity() {

    private val booruApis: BooruApis by instance()

    private lateinit var booru: Booru
    private var username = ""
    private var userToken = ""
    private var requesting = false

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(booruApis)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_config)
        val b = BooruManager.getBooruByUid(Settings.activatedBooruUid)
        if (b == null) {
            Toast.makeText(this, "ERROR: Booru not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        booru = b
        account_config_title.text = String.format(getString(R.string.title_account_config_and_booru), booru.name)
        if (booru.type == BOORU_TYPE_DAN) {
            password_edit_container.hint = getString(R.string.account_api_key)
            forgot_auth.setText(R.string.account_forgot_api_key)
        }
        forgot_auth.setOnClickListener {
            when (booru.type) {
                BOORU_TYPE_DAN -> {
                    launchUrl(String.format("%s://%s/session/new", booru.scheme, booru.host))
                }
                BOORU_TYPE_MOE,
                BOORU_TYPE_DAN1 -> {
                    launchUrl(String.format("%s://%s/user/reset_password", booru.scheme, booru.host))
                }
                BOORU_TYPE_SANKAKU -> {
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
        userToken = password_edit.text.toString()
        if (username.isBlank() || userToken.isBlank()) {
            Snackbar.make(account_config_title, getString(R.string.account_config_msg_tip_empty), Snackbar.LENGTH_LONG).show()
            return
        }
        val hashSalt = booru.hashSalt
        if ((booru.type == BOORU_TYPE_MOE
                    || booru.type == BOORU_TYPE_DAN1
                    || booru.type == BOORU_TYPE_SANKAKU) && hashSalt.isNotBlank()) {
            userToken = hashSalt.replace(HASH_SALT_CONTAINED, userToken).sha1()
        }
        set_account.isVisible = false
        progress_bar.isVisible = true
        when (booru.type) {
            BOORU_TYPE_GEL -> {
                lifecycleScope.launch {
                    val result = userRepository.gelLogin(username, userToken, booru)
                    handlerResult(result)
                }
            }
            else -> {
                lifecycleScope.launch {
                    val result = userRepository.findUserByName(username, booru)
                    handlerResult(result)
                }
            }
        }
    }

    private fun handlerResult(result: NetResult<User>) {
        when (result) {
            is NetResult.Success -> {
                val user = result.data
                when (booru.type) {
                    BOORU_TYPE_DAN,
                    BOORU_TYPE_MOE,
                    BOORU_TYPE_DAN1,
                    BOORU_TYPE_SANKAKU -> {
                        user.token = userToken
                        updateUser(user)
                    }
                    BOORU_TYPE_GEL -> {
                        updateUser(user)
                    }
                }
                startActivity(Intent(this@AccountConfigActivity, AccountActivity::class.java))
                finish()
            }
            is NetResult.Error -> {
                error_msg.text = result.errorMsg
                progress_bar.isVisible = false
                set_account.isVisible = true
            }
        }
    }

    private fun updateUser(user: User) {
        booru.user = user
        BooruManager.updateBooru(booru)
    }
}
