package onlymash.flexbooru.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.model.User

class AccountActivity : BaseActivity() {

    companion object {
        private const val BOORU_SCHEME_KEY = "booru_scheme"
        private const val BOORU_HOST_KEY = "booru_host"
        private const val BOORU_NAME_KEY = "booru_name"
        private const val BOORU_TYPE_KEY = "booru_type"
        private const val USER_NAME_KEY = "user_name"
        private const val USER_ID_KEY = "user_id"
        fun startActivity(context: Context, user: User, booru: Booru) {
            context.startActivity(Intent(context, AccountActivity::class.java).apply {
                putExtra(BOORU_SCHEME_KEY, booru.scheme)
                putExtra(BOORU_HOST_KEY, booru.host)
                putExtra(BOORU_NAME_KEY, booru.name)
                putExtra(BOORU_TYPE_KEY, booru.type) //int
                putExtra(USER_NAME_KEY, user.name)
                putExtra(USER_ID_KEY, user.id) //int
            })
        }
    }

    private var booruScheme: String = ""
    private var booruHost: String = ""
    private var booruName: String = ""
    private var booruType: Int = -1
    private var userName: String = ""
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        val bundle = intent?.extras ?: return
        bundle.apply {
            booruScheme = getString(BOORU_SCHEME_KEY) ?: return
            booruHost = getString(BOORU_HOST_KEY) ?: return
            booruName = getString(BOORU_NAME_KEY) ?: return
            booruType = getInt(BOORU_TYPE_KEY)
            userName = getString(USER_NAME_KEY) ?: return
            userId = getInt(USER_ID_KEY)
        }
        init()
    }
    private fun init() {
        toolbar.title = String.format(getString(R.string.title_account_and_booru), booruName)
        username.text = userName
        user_id.text = String.format(getString(R.string.account_user_id), userId)
        when (booruType) {
            Constants.TYPE_MOEBOORU -> {
                GlideApp.with(this)
                    .load(String.format(getString(R.string.account_user_avatars), booruScheme, booruHost, userId))
                    .into(user_avatar)
            }
            Constants.TYPE_DANBOORU -> {

            }
        }
    }
}
