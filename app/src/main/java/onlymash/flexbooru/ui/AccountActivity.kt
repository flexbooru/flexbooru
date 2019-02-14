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
    }

    private var booru: Booru? = null
    private var user: User? = null

    private var findUserListener = object : FindUserListener {
        override fun onSuccess(user: User) {
            this@AccountActivity.user!!.name = user.name
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
        toolbar.title = String.format(getString(R.string.title_account_and_booru), booru!!.name)
        val extras = intent?.extras
        val type = booru!!.type
        when {
            type == Constants.TYPE_DANBOORU && extras != null -> {
                val id = extras.getInt(USER_ID_KEY, -1)
                val name = extras.getString(USER_NAME_KEY)
                if (id > 0 && !name.isNullOrBlank()) {
                    user = User(name = name, id = id)
                }
            }
            type == Constants.TYPE_MOEBOORU && extras != null -> {
                val id = extras.getInt(USER_ID_KEY, -1)
                if (id > 0) {
                    user = User(id = id, name = "")
                    ServiceLocator.instance().getUserFinder().apply {
                        setFindUserListener(findUserListener)
                        findMoeUserById(id, booru!!)
                    }
                }
            }
        }
        if (user == null) {
            user = UserManager.getUserByBooruUid(uid)
            if (user != null) {
                initToolbarMenu()
            }
        }
        init()
    }
    private fun initToolbarMenu() {
        toolbar.inflateMenu(R.menu.account)
        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_account_remove) {
                AlertDialog.Builder(this@AccountActivity)
                    .setTitle(R.string.account_user_dialog_title_remove)
                    .setPositiveButton(R.string.dialog_yes) {_, _ ->
                        UserManager.deleteUser(user!!)
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
        if (user == null) return
        username.text = user!!.name
        user_id.text = String.format(getString(R.string.account_user_id), user!!.id)
        when (booru!!.type) {
            Constants.TYPE_MOEBOORU -> {
                GlideApp.with(this)
                    .load(String.format(getString(R.string.account_user_avatars), booru!!.scheme, booru!!.host, user!!.id))
                    .placeholder(resources.getDrawable(R.drawable.avatar_account, theme))
                    .into(user_avatar)
            }
            Constants.TYPE_DANBOORU -> {

            }
        }
        fav_action_button.setOnClickListener {
            val keyword = when (booru!!.type) {
                Constants.TYPE_DANBOORU -> String.format("fav:%s", user!!.name)
                Constants.TYPE_MOEBOORU -> String.format("vote:3:%s order:vote", user!!.name)
                else -> throw IllegalStateException("unknown booru type: ${booru!!.type}")
            }
            SearchActivity.startActivity(this, keyword)
        }
        posts_action_button.setOnClickListener {
            val keyword = String.format("user:%s", user!!.name)
            SearchActivity.startActivity(this, keyword)
        }
    }
}
