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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL_LEGACY
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.api.BooruApis
import onlymash.flexbooru.data.database.BooruManager
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.extension.launchUrl
import onlymash.flexbooru.data.repository.user.UserRepositoryImpl
import onlymash.flexbooru.databinding.ActivityAccountBinding
import onlymash.flexbooru.ui.base.PathActivity
import onlymash.flexbooru.ui.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class AccountActivity : PathActivity() {

    companion object {
        const val USER_ID_KEY = "user_id"
        const val USER_NAME_KEY = "user_name"
        const val USER_AVATAR_KEY = "user_avatar"
    }

    private val booruApis by inject<BooruApis>()

    private val binding by viewBinding(ActivityAccountBinding::inflate)

    private lateinit var booru: Booru
    private lateinit var user: User

    private val userRepository by lazy {
        UserRepositoryImpl(booruApis)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uid = activatedBooruUid
        val booru = BooruManager.getBooruByUid(uid)
        if (booru == null) {
            finish()
            return
        }
        this.booru = booru
        setContentView(binding.root)
        supportActionBar?.apply {
            title = String.format(getString(R.string.title_account_and_booru), booru.name)
            setDisplayHomeAsUpEnabled(true)
        }
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
            booru.user?.let {
                user = it
                init()
            }
        } else {
            user = u
            init()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (booru.user != null) {
            menuInflater.inflate(R.menu.account, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_account_remove -> {
                createDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createDialog() {
        if (isFinishing) {
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.account_user_dialog_title_remove)
            .setPositiveButton(R.string.dialog_yes) {_, _ ->
                booru.user = null
                BooruManager.updateBooru(booru)
                finish()
            }
            .setNegativeButton(R.string.dialog_no, null)
            .create()
            .show()
    }

    private fun init() {
        binding.username.text = user.name
        binding.userId.text = String.format(getString(R.string.account_user_id), user.id)
        if (booru.type == BOORU_TYPE_MOE) {
            binding.userAvatar.load(String.format(getString(R.string.account_user_avatars), booru.scheme, booru.host, user.id)) {
                placeholder(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.avatar_account,
                        theme
                    )
                )
                error(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.avatar_account,
                        theme
                    )
                )
            }
        } else if (booru.type == BOORU_TYPE_SANKAKU && !user.avatar.isNullOrEmpty()) {
            binding.userAvatar.load(user.avatar) {
                placeholder(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.avatar_account,
                        theme
                    )
                )
                error(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.avatar_account,
                        theme
                    )
                )
            }
        }
        binding.favActionButton.setOnClickListener {
            if (booru.type == BOORU_TYPE_GEL_LEGACY) {
                val url = "${booru.scheme}://${booru.host}/index.php?page=favorites&s=view&id=${user.id}"
                launchUrl(url)
            } else {
                val query = when (booru.type) {
                    BOORU_TYPE_DAN,
                    BOORU_TYPE_DAN1,
                    BOORU_TYPE_SANKAKU -> String.format("fav:%s", user.name)
                    BOORU_TYPE_MOE -> String.format("vote:3:%s order:vote", user.name)
                    BOORU_TYPE_GEL -> "fav:${user.id}"
                    else -> null
                }
                searchPost(query)
            }
        }
        binding.postsActionButton.setOnClickListener {
            if (booru.type == BOORU_TYPE_GEL || booru.type == BOORU_TYPE_GEL_LEGACY) {
                Snackbar.make(binding.root, getString(R.string.msg_not_supported), Snackbar.LENGTH_LONG).show()
            } else {
                val query = String.format("user:%s", user.name)
                searchPost(query)
            }
        }
        binding.commentsActionButton.setOnClickListener {
            if (booru.type == BOORU_TYPE_GEL || booru.type == BOORU_TYPE_GEL_LEGACY) {
                Snackbar.make(binding.root, getString(R.string.msg_not_supported), Snackbar.LENGTH_LONG).show()
            } else {
                val query = if (booru.type != BOORU_TYPE_DAN) "user:${user.name}" else user.name
                CommentActivity.startActivity(this, query = query)
            }
        }
        if (booru.type == BOORU_TYPE_SANKAKU) {
            binding.recommendedActionButton.setOnClickListener {
                val query = String.format("recommended_for:%s", user.name)
                searchPost(query)
            }
        } else {
            binding.recommendedActionButtonContainer.visibility = View.GONE
        }
    }

    private fun searchPost(query: String?) {
        if (query == null) {
            return
        }
        SearchActivity.startSearch(this, query)
    }

    private fun handlerResult(result: NetResult<User>) {
        when (result) {
            is NetResult.Success -> {
                user.name = result.data.name
                binding.username.text = user.name
            }
            is NetResult.Error -> {
                Snackbar.make(this.findViewById(R.id.root_container), result.errorMsg, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
