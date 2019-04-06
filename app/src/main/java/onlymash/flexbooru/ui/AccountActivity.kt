/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.ServiceLocator
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.repository.account.FindUserListener

class AccountActivity : BaseActivity() {

    companion object {
        const val USER_ID_KEY = "user_id"
        const val USER_NAME_KEY = "user_name"
        const val USER_AVATAR_KEY = "user_avatar"
    }

    private lateinit var booru: Booru
    private lateinit var user: User

    private val userFinder by lazy { ServiceLocator.instance().getUserRepository() }

    private var findUserListener = object : FindUserListener {
        override fun onSuccess(user: User) {
            this@AccountActivity.user.name = user.name
            username.text = user.name
        }

        override fun onFailed(msg: String) {
            Snackbar.make(this@AccountActivity.findViewById(R.id.root_container), msg, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        val uid = Settings.instance().activeBooruUid
        booru = BooruManager.getBooruByUid(uid) ?: return
        toolbar.title = String.format(getString(R.string.title_account_and_booru), booru.name)
        val extras = intent?.extras
        val type = booru.type
        var u: User? = null
        when {
            (type == Constants.TYPE_DANBOORU || type == Constants.TYPE_SANKAKU) && extras != null -> {
                val id = extras.getInt(USER_ID_KEY, -1)
                val name = extras.getString(USER_NAME_KEY)
                if (id > 0 && !name.isNullOrBlank()) {
                    u = User(name = name, id = id)
                    if (type == Constants.TYPE_SANKAKU) {
                        u.avatar_url = extras.getString(USER_AVATAR_KEY)
                    }
                }
            }
            (type == Constants.TYPE_MOEBOORU || type == Constants.TYPE_DANBOORU_ONE) && extras != null -> {
                val id = extras.getInt(USER_ID_KEY, -1)
                val name = extras.getString(USER_NAME_KEY) ?: ""
                if (id > 0) {
                    u = User(id = id, name = name)
                    if (name.isBlank()) {
                        userFinder.findUserListener = findUserListener
                        userFinder.findUserById(id, booru)
                    }
                }
            }
        }
        if (u == null) {
            u = UserManager.getUserByBooruUid(uid)
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
                        UserManager.deleteUser(user)
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
        if (booru.type == Constants.TYPE_MOEBOORU) {
            GlideApp.with(this)
                .load(String.format(getString(R.string.account_user_avatars), booru.scheme, booru.host, user.id))
                .placeholder(resources.getDrawable(R.drawable.avatar_account, theme))
                .into(user_avatar)
        } else if (booru.type == Constants.TYPE_SANKAKU && !user.avatar_url.isNullOrEmpty()) {
            GlideApp.with(this)
                .load(user.avatar_url)
                .placeholder(resources.getDrawable(R.drawable.avatar_account, theme))
                .into(user_avatar)
        }
        fav_action_button.setOnClickListener {
            val keyword = when (booru.type) {
                Constants.TYPE_DANBOORU,
                Constants.TYPE_DANBOORU_ONE,
                Constants.TYPE_SANKAKU -> String.format("fav:%s", user.name)
                Constants.TYPE_MOEBOORU -> String.format("vote:3:%s order:vote", user.name)
                else -> throw IllegalStateException("unknown booru type: ${booru.type}")
            }
            SearchActivity.startActivity(this, keyword)
        }
        posts_action_button.setOnClickListener {
            val keyword = String.format("user:%s", user.name)
            SearchActivity.startActivity(this, keyword)
        }
        comments_action_button.setOnClickListener {
            CommentActivity.startActivity(this, username = user.name)
        }
    }
}
