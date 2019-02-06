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
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import onlymash.flexbooru.App.Companion.app
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.model.Booru
import onlymash.flexbooru.ui.adapter.NavViewPagerAdapter

class MainActivity : BaseActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val BOORU_MANAGE_PROFILE_ID = -2L
        private const val SETTINGS_DRAWER_ITEM_ID = -1L
    }
    private lateinit var boorus: MutableList<Booru>
    lateinit var drawer: Drawer
    private lateinit var header: AccountHeader
    private lateinit var profileSettingDrawerItem: ProfileSettingDrawerItem

    private var currentNavItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        pager_container.addOnPageChangeListener(pageChangeListener)
        boorus = BooruManager.getAllBoorus() ?: mutableListOf()
        BooruManager.listeners.add(booruListener)
        val size = boorus.size
        if (size > 0) {
            header.removeProfile(profileSettingDrawerItem)
            boorus.forEachIndexed { index, booru ->
                header.addProfile(
                    ProfileDrawerItem()
                        .withName(booru.name)
                        .withEmail(String.format("%s://%s", booru.scheme, booru.host))
                        .withIdentifier(booru.uid), index)
            }
            header.addProfile(profileSettingDrawerItem, boorus.size)
        }
        val uid = activeBooruUid
        when {
            uid < 0L && size > 0 -> {
                activeBooruUid = boorus[0].uid
                header.setActiveProfile(activeBooruUid)
                pager_container.adapter = NavViewPagerAdapter(supportFragmentManager, boorus[0])
            }
            uid >= 0L && size > 0 -> {
                var i = -1
                boorus.forEachIndexed { index, booru ->
                    if (uid == booru.uid) {
                        i = index
                        return@forEachIndexed
                    }
                }
                if (i >= 0) {
                    header.setActiveProfile(uid)
                    pager_container.adapter = NavViewPagerAdapter(supportFragmentManager, boorus[i])
                } else {
                    activeBooruUid = boorus[0].uid
                    header.setActiveProfile(activeBooruUid)
                    pager_container.adapter = NavViewPagerAdapter(supportFragmentManager, boorus[0])
                }
            }
            else -> {
                startActivity(Intent(this, BooruActivity::class.java))
            }
        }
    }

    private var activeBooruUid: Long
        get() = app.sp.getLong(Constants.ACTIVE_BOORU_UID_KEY, -1)
        set(value) = app.sp.edit().putLong(Constants.ACTIVE_BOORU_UID_KEY, value).apply()

    private val booruListener = object : BooruManager.Listener {
        override fun onAdd(booru: Booru) {
            header.removeProfile(profileSettingDrawerItem)
            boorus.add(booru)
            header.addProfile(
                ProfileDrawerItem()
                    .withName(booru.name)
                    .withEmail(String.format("%s://%s", booru.scheme, booru.host))
                    .withIdentifier(booru.uid), boorus.size - 1)
            header.addProfile(profileSettingDrawerItem, boorus.size)
            if (boorus.size == 1) {
                activeBooruUid = booru.uid
                header.setActiveProfile(activeBooruUid)
                pager_container.adapter = NavViewPagerAdapter(supportFragmentManager, booru)
            }
        }

        override fun onDelete(booruUid: Long) {
            boorus.forEach { booru ->
                if (booru.uid == booruUid) {
                    boorus.remove(booru)
                    return@forEach
                }
            }
            header.removeProfileByIdentifier(booruUid)
            if (boorus.size > 0) {
                if (activeBooruUid == booruUid) {
                    activeBooruUid = 0
                    header.setActiveProfile(activeBooruUid)
                }
            } else {
                activeBooruUid = -1
                pager_container.adapter = null
            }
        }

        override fun onUpdate(booru: Booru) {
            boorus.forEach {
                if (it.uid == booru.uid) {
                    it.name = booru.name
                    it.scheme = booru.scheme
                    it.host = booru.host
                    it.hash_salt = booru.hash_salt
                    it.type = booru.type
                    if (activeBooruUid == booru.uid) {
                        pager_container.adapter = NavViewPagerAdapter(supportFragmentManager, booru)
                    }
                    return@forEach
                }
            }
        }
    }

    private val headerItemClickListener =
        AccountHeader.OnAccountHeaderListener { _, profile, _ ->
            val uid = profile.identifier
            when (uid) {
                BOORU_MANAGE_PROFILE_ID -> {
                    startActivity(Intent(this, BooruActivity::class.java))
                }
                else -> {
                    activeBooruUid = uid
                    boorus.forEach { booru ->
                        if (booru.uid == uid) {
                            navigation.selectedItemId = R.id.navigation_posts
                            pager_container.adapter = NavViewPagerAdapter(supportFragmentManager, booru)
                            return@forEach
                        }
                    }
                }
            }
            false
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
                    0 -> {
                        if (navigation.selectedItemId != R.id.navigation_posts) {
                            navigation.selectedItemId = R.id.navigation_posts
                        }
                    }
                    1 -> {
                        if (navigation.selectedItemId != R.id.navigation_popular) {
                            navigation.selectedItemId = R.id.navigation_popular
                        }
                    }
                }
            }
        }

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_posts -> {
                    if (pager_container.currentItem != 0) {
                        pager_container.currentItem = 0
                    } else if (currentNavItem == 0){
                        navigationListeners.forEach {
                            it.onClickPosition(0)
                        }
                    }
                    currentNavItem = 0
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_popular -> {
                    if (pager_container.currentItem != 1) {
                        pager_container.currentItem = 1
                    } else if (currentNavItem == 1){
                        navigationListeners.forEach {
                            it.onClickPosition(1)
                        }
                    }
                    currentNavItem = 1
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

    private var navigationListeners: MutableList<NavigationListener> = mutableListOf()

    fun addNavigationListener(listener: NavigationListener) {
        navigationListeners.add(listener)
    }

    fun removeNavigationListener(listener: NavigationListener) {
        navigationListeners.remove(listener)
    }

    interface NavigationListener {
        fun onClickPosition(position: Int)
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
