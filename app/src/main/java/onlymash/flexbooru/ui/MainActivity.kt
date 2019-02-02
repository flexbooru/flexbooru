package onlymash.flexbooru.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_main.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.ui.adapter.NavViewPagerAdapter

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
        pager_container.adapter = NavViewPagerAdapter(supportFragmentManager, booru)
        pager_container.addOnPageChangeListener(pageChangeListener)
    }

    private val pageChangeListener =
        object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> navigation.selectedItemId = R.id.navigation_posts
                    1 -> navigation.selectedItemId = R.id.navigation_popular
                }
            }
        }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_posts -> {
                pager_container.currentItem = 0
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_popular -> {
                pager_container.currentItem = 1
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
}
