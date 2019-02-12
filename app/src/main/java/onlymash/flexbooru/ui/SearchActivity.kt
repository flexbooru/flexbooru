package onlymash.flexbooru.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.User

class SearchActivity : BaseActivity() {

    companion object {
        private const val BUNDLE_KEY = "bundle"
        fun startActivity(context: Context, keyword: String, booru: Booru, user: User?) {
            val bundle = when (booru.type) {
                Constants.TYPE_DANBOORU -> Bundle().apply {
                    putString(Constants.SCHEME_KEY, booru.scheme)
                    putString(Constants.HOST_KEY, booru.host)
                    putInt(Constants.TYPE_KEY, Constants.TYPE_DANBOORU)
                    putString(Constants.KEYWORD_KEY, keyword)
                    if (user != null) {
                        putString(Constants.USERNAME_KEY, user.name)
                        putString(Constants.AUTH_KEY, user.api_key)
                    } else {
                        putString(Constants.USERNAME_KEY, "")
                        putString(Constants.AUTH_KEY, "")
                    }
                }
                Constants.TYPE_MOEBOORU -> Bundle().apply {
                    putString(Constants.SCHEME_KEY, booru.scheme)
                    putString(Constants.HOST_KEY, booru.host)
                    putInt(Constants.TYPE_KEY, Constants.TYPE_MOEBOORU)
                    putString(Constants.KEYWORD_KEY, keyword)
                    if (user != null) {
                        putString(Constants.USERNAME_KEY, user.name)
                        putString(Constants.AUTH_KEY, user.password_hash)
                    } else {
                        putString(Constants.USERNAME_KEY, "")
                        putString(Constants.AUTH_KEY, "")
                    }
                }
                else -> throw IllegalArgumentException("unknown booru type ${booru.type}")
            }
            context.startActivity(Intent(context, SearchActivity::class.java).putExtra(BUNDLE_KEY, bundle))
        }
    }

    internal var type = -1
    internal var search: Search? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.extras?.getBundle(BUNDLE_KEY)?.apply {
            type = getInt(Constants.TYPE_KEY, Constants.TYPE_UNKNOWN)
            search = Search(
                scheme = getString(Constants.SCHEME_KEY, ""),
                host = getString(Constants.HOST_KEY, ""),
                keyword = getString(Constants.KEYWORD_KEY, ""),
                username = getString(Constants.USERNAME_KEY, ""),
                auth_key = getString(Constants.AUTH_KEY, ""),
                limit = Settings.instance().postLimit
            )
        }
        setContentView(R.layout.activity_search)
    }
}
