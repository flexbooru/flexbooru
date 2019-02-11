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
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.model.User
import onlymash.flexbooru.repository.account.FindUserListener
import onlymash.flexbooru.repository.account.UserFinder
import onlymash.flexbooru.util.HashUtil
import onlymash.flexbooru.util.launchUrl

class AccountConfigActivity : BaseActivity() {

    companion object {
        private const val TAG = "AccountConfigActivity"
    }

    private val findUserListener = object : FindUserListener {
        override fun onSuccess(user: User) {
            when (booru!!.type) {
                Constants.TYPE_DANBOORU -> {
                    user.apply {
                        booru_uid = booru!!.uid
                        api_key = pass
                    }
                    UserManager.createUser(user)
                }
                Constants.TYPE_MOEBOORU -> {
                    user.apply {
                        booru_uid = booru!!.uid
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

    private var booru: Booru? = null
    private var username = ""
    private var pass = ""
    private var requesting = false

    private lateinit var userFinder: UserFinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_config)
        booru = BooruManager.getBooruByUid(Settings.instance().activeBooruUid)
        if (booru == null) {
            Toast.makeText(this, "ERROR: Booru not found", Toast.LENGTH_LONG).show()
            finish()
        }
        account_config_title.text = String.format(getString(R.string.title_account_config_and_booru), booru!!.name)
        if (booru!!.type == Constants.TYPE_DANBOORU) {
            password_title.setText(R.string.account_api_key)
            forgot_auth.setText(R.string.account_forgot_api_key)
        }
        forgot_auth.setOnClickListener {
            when (booru!!.type) {
                Constants.TYPE_DANBOORU -> {
                    launchUrl(String.format("%s://%s/session/new", booru!!.scheme, booru!!.host))
                }
                Constants.TYPE_MOEBOORU -> {
                    launchUrl(String.format("%s://%s/user/reset_password", booru!!.scheme, booru!!.host))
                }
            }
        }
        userFinder = ServiceLocator.instance().getUserFinder().apply {
            setFindUserListener(findUserListener)
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
            Snackbar.make(password_title, "Username or Password/Api Key cannot be empty.", Snackbar.LENGTH_LONG).show()
            return
        }
        val hashSalt = booru!!.hash_salt
        if (booru!!.type == Constants.TYPE_MOEBOORU && !hashSalt.isNullOrBlank()) {
            pass = HashUtil.sha1(hashSalt.replace(Constants.HASH_SALT_CONTAINED, pass))
        }
        set_account.visibility = View.INVISIBLE
        progress_bar.visibility = View.VISIBLE
        userFinder.findUser(username, booru!!)
    }
}
