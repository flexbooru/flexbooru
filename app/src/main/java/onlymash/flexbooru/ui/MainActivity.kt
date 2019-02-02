package onlymash.flexbooru.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.model.Booru

class MainActivity : AppCompatActivity() {

    private lateinit var booru: Booru

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        booru = Booru(
            uid = null,
            name = "Danbooru",
            scheme = "https",
            host = "danbooru.donmai.us",
            hash_salt = Constants.EMPTY_STRING_VALUE,
            type = Constants.TYPE_DANBOORU)
        if (savedInstanceState == null) {
            displayFragment(PostFragment.newInstance(booru, Constants.EMPTY_STRING_VALUE))
        }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_posts -> {
                if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is PostFragment) {
                    displayFragment(PostFragment.newInstance(booru, Constants.EMPTY_STRING_VALUE))
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_popular -> {
                if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is PopularFragment) {
                    displayFragment(PopularFragment.newInstance(booru))
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_downloads -> {

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun displayFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }
}
