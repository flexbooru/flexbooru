package onlymash.flexbooru.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.content.res.AppCompatResources
import androidx.viewpager.widget.ViewPager
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.ui.adapter.NavViewPagerAdapter

private const val BOORU_MANAGE_PROFILE_ID = -2L
private const val SETTINGS_DRAWER_ITEM_ID = -1L

class MainActivity : BaseActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }
    private lateinit var booru: Booru
    private lateinit var boorus: MutableList<Booru>
    lateinit var drawer: Drawer
    private lateinit var header: AccountHeader
    private lateinit var profileSettingDrawerItem: ProfileSettingDrawerItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        boorus = BooruManager.getAllBoorus() ?: mutableListOf()
        BooruManager.listeners.add(booruListener)
        profileSettingDrawerItem = ProfileSettingDrawerItem()
            .withName(R.string.title_manage_boorus)
            .withIdentifier(BOORU_MANAGE_PROFILE_ID)
            .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_settings_black_24dp))
            .withIconTinted(true)
        header = AccountHeaderBuilder()
            .withActivity(this)
            .withHeaderBackground(R.color.white)
            .withOnAccountHeaderListener(headerItemClickListener)
            .build()
        header.addProfile(profileSettingDrawerItem, header.profiles.size)
        drawer = DrawerBuilder()
            .withActivity(this)
            .withTranslucentStatusBar(false)
            .withSliderBackgroundColor(getColor(R.color.white))
            .withAccountHeader(header, false)
            .addStickyDrawerItems(
                PrimaryDrawerItem()
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_settings_black_24dp))
                    .withName(R.string.title_settings)
                    .withIconTintingEnabled(true)
                    .withIdentifier(SETTINGS_DRAWER_ITEM_ID)
            )
            .withStickyFooterDivider(true)
            .withStickyFooterShadow(false)
            .withSavedInstance(savedInstanceState)
            .build()
        drawer.onDrawerItemClickListener = drawerItemClickListener
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        booru = Booru(
            uid = 1,
            name = "yande.re",
            scheme = "https",
            host = "yande.re",
            hash_salt = Constants.EMPTY_STRING_VALUE,
            type = Constants.TYPE_MOEBOORU)
        if (boorus.size > 0) booru = boorus[0]
        pager_container.adapter = NavViewPagerAdapter(supportFragmentManager, booru)
        pager_container.addOnPageChangeListener(pageChangeListener)
        Log.i(TAG, "${BooruManager.createBooru(booru)}")
    }

    private val booruListener = object : BooruManager.Listener {
        override fun onAdd(booru: Booru) {

        }

        override fun onDelete(booruUid: Long) {

        }

        override fun onUpdate(booru: Booru) {

        }

    }

    private val headerItemClickListener =
        AccountHeader.OnAccountHeaderListener { _, profile, _ ->
            when (profile.identifier) {
                BOORU_MANAGE_PROFILE_ID -> {
                    startActivity(Intent(this, BooruActivity::class.java))
                }
                else -> {

                }
            }
            true
        }

    private val drawerItemClickListener =
        Drawer.OnDrawerItemClickListener { _, position, drawerItem ->
            Log.i("MainActivity", "id: ${drawerItem.identifier}; position: $position")
            when (drawerItem.identifier) {
                SETTINGS_DRAWER_ITEM_ID -> {

                }
                else -> {

                }
            }
            return@OnDrawerItemClickListener true
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

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
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

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else super.onBackPressed()
    }

    override fun onDestroy() {
        BooruManager.listeners.remove(booruListener)
        super.onDestroy()
    }
}
