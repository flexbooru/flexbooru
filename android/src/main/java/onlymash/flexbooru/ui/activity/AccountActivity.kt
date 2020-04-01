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

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.*
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.common.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.database.CookieManager
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.data.repository.user.UserRepositoryImpl
import org.kodein.di.erased.instance

class AccountActivity : BaseActivity() {

    companion object {
        const val USER_ID_KEY = "user_id"
        const val USER_NAME_KEY = "user_name"
        const val USER_AVATAR_KEY = "user_avatar"
    }

    private val booruApis: BooruApis by instance()

    private lateinit var booru: Booru
    private lateinit var user: User

    private val userRepository by lazy {
        UserRepositoryImpl(booruApis)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        val uid = activatedBooruUid
        booru = BooruManager.getBooruByUid(uid) ?: return
        toolbar.title = String.format(getString(R.string.title_account_and_booru), booru.name)
        val extras = intent?.extras
        val type = booru.type
        var u: User? = null
        when {
            (type == BOORU_TYPE_DAN || type == BOORU_TYPE_SANKAKU) && extras != null -> {
                val id = extras.getInt(USER_ID_KEY, -1)
                val name = extras.getString(USER_NAME_KEY)
                if (id > 0 && !name.isNullOrBlank()) {
                    u = User(
                        name = name,
                        id = id
                    )
                    if (type == BOORU_TYPE_SANKAKU) {
                        u.avatar = extras.getString(USER_AVATAR_KEY)
                    }
                }
            }
            (type == BOORU_TYPE_MOE || type == BOORU_TYPE_DAN1) && extras != null -> {
                val id = extras.getInt(USER_ID_KEY, -1)
                val name = extras.getString(USER_NAME_KEY) ?: ""
                if (id > 0) {
                    u = User(
                        id = id,
                        name = name
                    )
                    if (name.isBlank()) {
                        lifecycleScope.launch {
                            val result = userRepository.findUserById(id, booru)
                            handlerResult(result)
                        }
                    }
                }
            }
        }
        if (u == null) {
            u = booru.user
            if (u != null) {
                user = u
                initToolbarMenu()
                init()
            }
        } else {
            user = u
            init()
        }
    }
    private fun initToolbarMenu() {
        toolbar.inflateMenu(R.menu.account)
        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_account_remove) {
                AlertDialog.Builder(this@AccountActivity)
                    .setTitle(R.string.account_user_dialog_title_remove)
                    .setPositiveButton(R.string.dialog_yes) {_, _ ->
                        booru.user = null
                        BooruManager.updateBooru(booru)
                        if (booru.type == BOORU_TYPE_GEL) {
                            CookieManager.deleteByBooruUid(booru.uid)
                        }
                        finish()
                    }
                    .setNegativeButton(R.string.dialog_no, null)
                    .create()
                    .show()
            }
            return@setOnMenuItemClickListener true
        }
    }
    private fun init() {
        username.text = user.name
        user_id.text = String.format(getString(R.string.account_user_id), user.id)
        if (booru.type == BOORU_TYPE_MOE) {
            GlideApp.with(this)
                .load(String.format(getString(R.string.account_user_avatars), booru.scheme, booru.host, user.id))
                .placeholder(resources.getDrawable(R.drawable.avatar_account, theme))
                .into(user_avatar)
        } else if (booru.type == BOORU_TYPE_SANKAKU && !user.avatar.isNullOrEmpty()) {
            GlideApp.with(this)
                .load(user.avatar)
                .placeholder(resources.getDrawable(R.drawable.avatar_account, theme))
                .into(user_avatar)
        }
        fav_action_button.setOnClickListener {
            if (booru.type == BOORU_TYPE_GEL) {
                val url = "${booru.scheme}://${booru.host}/index.php?page=favorites&s=view&id=${user.id}"
                launchUrl(url)
                return@setOnClickListener
            }
            val query = when (booru.type) {
                BOORU_TYPE_DAN,
                BOORU_TYPE_DAN1,
                BOORU_TYPE_SANKAKU -> String.format("fav:%s", user.name)
                BOORU_TYPE_MOE -> String.format("vote:3:%s order:vote", user.name)
                else -> throw IllegalStateException("unknown booru type: ${booru.type}")
            }
            SearchActivity.startSearch(this, query)
        }
        posts_action_button.setOnClickListener {
            if (booru.type == BOORU_TYPE_GEL) {
                Snackbar.make(toolbar, getString(R.string.msg_not_supported), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val query = String.format("user:%s", user.name)
            SearchActivity.startSearch(this, query)
        }
        comments_action_button.setOnClickListener {
            if (booru.type == BOORU_TYPE_GEL) {
                Snackbar.make(toolbar, getString(R.string.msg_not_supported), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val query = if (booru.type != BOORU_TYPE_DAN) "user:${user.name}" else user.name
            CommentActivity.startActivity(this, query = query)
        }
        if (booru.type == BOORU_TYPE_SANKAKU) {
            recommended_action_button.setOnClickListener {
                val query = String.format("recommended_for:%s", user.name)
                SearchActivity.startSearch(this, query)
            }
        } else {
            recommended_action_button_container.visibility = View.GONE
        }
    }

    private fun handlerResult(result: NetResult<User>) {
        when (result) {
            is NetResult.Success -> {
                user.name = result.data.name
                username.text = user.name
            }
            is NetResult.Error -> {
                Snackbar.make(this.findViewById(R.id.root_container), result.errorMsg, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
