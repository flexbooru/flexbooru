package onlymash.flexbooru.ui

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User

class AccountActivity : BaseActivity() {

    private var booru: Booru? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        val uid = Settings.instance().activeBooruUid
        booru = BooruManager.getBooruByUid(uid) ?: return
        user = UserManager.getUserByBooruUid(uid)
        init()
    }
    private fun init() {
        toolbar.title = String.format(getString(R.string.title_account_and_booru), booru!!.name)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar.inflateMenu(R.menu.account)
        if (user == null) return
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
        username.text = user!!.name
        user_id.text = String.format(getString(R.string.account_user_id), user!!.id)
        when (booru!!.type) {
            Constants.TYPE_MOEBOORU -> {
                GlideApp.with(this)
                    .load(String.format(getString(R.string.account_user_avatars), booru!!.scheme, booru!!.host, user!!.id))
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
            SearchActivity.startActivity(this, keyword, booru!!, user)
        }
        posts_action_button.setOnClickListener {
            val keyword = String.format("user:%s", user!!.name)
            SearchActivity.startActivity(this, keyword, booru!!, user)
        }
    }
}
