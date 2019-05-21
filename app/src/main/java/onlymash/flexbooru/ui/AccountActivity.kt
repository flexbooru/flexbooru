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

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.api.DanbooruApi
import onlymash.flexbooru.api.DanbooruOneApi
import onlymash.flexbooru.api.MoebooruApi
import onlymash.flexbooru.api.SankakuApi
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.CookieManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.repository.account.UserRepositoryImpl
import onlymash.flexbooru.util.launchUrl
import org.kodein.di.generic.instance
import kotlin.coroutines.CoroutineContext

class AccountActivity : BaseActivity(), CoroutineScope {

    companion object {
        const val USER_ID_KEY = "user_id"
        const val USER_NAME_KEY = "user_name"
        const val USER_AVATAR_KEY = "user_avatar"
    }

    private val danApi: DanbooruApi by instance()
    private val danOneApi: DanbooruOneApi by instance()
    private val moeApi: MoebooruApi by instance()
    private val sankakuApi: SankakuApi by instance()

    private lateinit var booru: Booru
    private lateinit var user: User

    private val userRepository by lazy {
        UserRepositoryImpl(
            danbooruApi = danApi,
            danbooruOneApi = danOneApi,
            moebooruApi = moeApi,
            sankakuApi = sankakuApi
        )
    }

    private var job: Job? = null

    override val coroutineContext: CoroutineContext
        get() {
            if (job == null) {
                job = Job()
            }
            return job!! + Dispatchers.Main
        }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        val uid = Settings.activeBooruUid
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
                        launch {
                            val result = userRepository.findUserById(id, booru)
                            handlerResult(result)
                        }
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
                        if (booru.type == Constants.TYPE_GELBOORU) {
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
            if (booru.type == Constants.TYPE_GELBOORU) {
                val url = "${booru.scheme}://${booru.host}/index.php?page=favorites&s=view&id=${user.id}"
                launchUrl(url)
                return@setOnClickListener
            }
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
            if (booru.type == Constants.TYPE_GELBOORU) {
                Snackbar.make(toolbar, getString(R.string.msg_not_supported), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val keyword = String.format("user:%s", user.name)
            SearchActivity.startActivity(this, keyword)
        }
        comments_action_button.setOnClickListener {
            if (booru.type == Constants.TYPE_GELBOORU) {
                Snackbar.make(toolbar, getString(R.string.msg_not_supported), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            CommentActivity.startActivity(this, username = user.name)
        }
        if (booru.type == Constants.TYPE_SANKAKU) {
            recommended_action_button.setOnClickListener {
                val keyword = String.format("recommended_for:%s", user.name)
                SearchActivity.startActivity(this, keyword)
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
