package onlymash.flexbooru.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.SharedElementCallback
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
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.ui.adapter.NavPagerAdapter

class MainActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "MainActivity"
        private const val BOORU_MANAGE_PROFILE_ID = -2L
        private const val SETTINGS_DRAWER_ITEM_ID = -1L
        private const val ACCOUNT_DRAWER_ITEM_ID = 1L
    }
    private lateinit var boorus: MutableList<Booru>
    private lateinit var users: MutableList<User>
    lateinit var drawer: Drawer
    private lateinit var header: AccountHeader
    private lateinit var profileSettingDrawerItem: ProfileSettingDrawerItem

    private var currentNavItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app.sp.registerOnSharedPreferenceChangeListener(this)
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
            .addDrawerItems(
                PrimaryDrawerItem()
                    .withIdentifier(ACCOUNT_DRAWER_ITEM_ID)
                    .withName(R.string.title_account)
                    .withSelectable(false)
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_account_circle_black_24dp))
                    .withIconTintingEnabled(true)
            )
            .addStickyDrawerItems(
                PrimaryDrawerItem()
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_settings_black_24dp))
                    .withName(R.string.title_settings)
                    .withSelectable(false)
                    .withIconTintingEnabled(true)
                    .withIdentifier(SETTINGS_DRAWER_ITEM_ID)
            )
            .withStickyFooterDivider(true)
            .withStickyFooterShadow(false)
            .withSavedInstance(savedInstanceState)
            .build()
        drawer.setSelection(-3L)
        drawer.onDrawerItemClickListener = drawerItemClickListener
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        pager_container.addOnPageChangeListener(pageChangeListener)
        boorus = BooruManager.getAllBoorus() ?: mutableListOf()
        users = UserManager.getAllUsers() ?: mutableListOf()
        UserManager.listeners.add(userListener)
        BooruManager.listeners.add(booruListener)
        val size = boorus.size
        if (size > 0) {
            header.removeProfile(profileSettingDrawerItem)
            boorus.forEachIndexed { index, booru ->
                header.addProfile(
                    ProfileDrawerItem()
                        .withName(booru.name)
                        .withIcon(Uri.parse(String.format("%s://%s/favicon.ico", booru.scheme, booru.host)))
                        .withEmail(String.format("%s://%s", booru.scheme, booru.host))
                        .withIdentifier(booru.uid), index)
            }
            header.addProfile(profileSettingDrawerItem, boorus.size)
        }
        val uid = Settings.instance().activeBooruUid
        when {
            uid < 0L && size > 0 -> {
                Settings.instance().activeBooruUid = boorus[0].uid
                header.setActiveProfile(Settings.instance().activeBooruUid)
                pager_container.adapter = NavPagerAdapter(supportFragmentManager, boorus[0], getCurrentUser())
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
                    pager_container.adapter = NavPagerAdapter(supportFragmentManager, boorus[i], getCurrentUser())
                } else {
                    Settings.instance().activeBooruUid = boorus[0].uid
                    header.setActiveProfile(Settings.instance().activeBooruUid)
                    pager_container.adapter = NavPagerAdapter(supportFragmentManager, boorus[0], getCurrentUser())
                }
            }
            else -> {
                startActivity(Intent(this, BooruActivity::class.java))
            }
        }
    }

    private val booruListener = object : BooruManager.Listener {
        override fun onAdd(booru: Booru) {
            header.removeProfile(profileSettingDrawerItem)
            boorus.add(booru)
            header.addProfile(
                ProfileDrawerItem()
                    .withName(booru.name)
                    .withIcon(Uri.parse(String.format("%s://%s/favicon.ico", booru.scheme, booru.host)))
                    .withEmail(String.format("%s://%s", booru.scheme, booru.host))
                    .withIdentifier(booru.uid), boorus.size - 1)
            header.addProfile(profileSettingDrawerItem, boorus.size)
            if (boorus.size == 1) {
                Settings.instance().activeBooruUid = booru.uid
                header.setActiveProfile(Settings.instance().activeBooruUid)
                pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
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
                if (Settings.instance().activeBooruUid == booruUid) {
                    Settings.instance().activeBooruUid = 0
                    header.setActiveProfile(Settings.instance().activeBooruUid)
                }
            } else {
                Settings.instance().activeBooruUid = -1
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
                    if (Settings.instance().activeBooruUid == booru.uid) {
                        pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
                    }
                    return@forEach
                }
            }
            header.updateProfile(
                ProfileDrawerItem()
                    .withName(booru.name)
                    .withIcon(Uri.parse(String.format("%s://%s/favicon.ico", booru.scheme, booru.host)))
                    .withEmail(String.format("%s://%s", booru.scheme, booru.host))
                    .withIdentifier(booru.uid))
        }
    }

    private val userListener =  object : UserManager.Listener {
        override fun onAdd(user: User) {
            users.add(user)
        }

        override fun onDelete(user: User) {
            users.forEachIndexed { i, u ->
                if (user.uid == u.uid) {
                    users.removeAt(i)
                    return@forEachIndexed
                }
            }
        }

        override fun onUpdate(user: User) {
            users.forEach {
                if (it.uid == user.uid) {
                    it.name = user.name
                    it.id = user.id
                    it.booru_uid
                    it.password_hash = user.password_hash
                    it.api_key = user.api_key
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
                    Settings.instance().activeBooruUid = uid
                    boorus.forEach { booru ->
                        if (booru.uid == uid) {
                            navigation.selectedItemId = R.id.navigation_posts
                            pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
                            return@forEach
                        }
                    }
                }
            }
            false
        }

    private val drawerItemClickListener =
        Drawer.OnDrawerItemClickListener { _, _, drawerItem ->
            when (drawerItem.identifier) {
                SETTINGS_DRAWER_ITEM_ID -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
                ACCOUNT_DRAWER_ITEM_ID -> {
                    val user = getCurrentUser()
                    val booru = getCurrentBooru()
                    if (user != null && booru != null) {
                        startActivity(Intent(this, AccountActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, AccountConfigActivity::class.java))
                    }
                }
                else -> {

                }
            }
            return@OnDrawerItemClickListener false
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
                        setExitSharedElementCallback(postExitSharedElementCallback)
                        if (navigation.selectedItemId != R.id.navigation_posts) {
                            navigation.selectedItemId = R.id.navigation_posts
                        }
                    }
                    1 -> {
                        setExitSharedElementCallback(popularExitSharedElementCallback)
                        if (navigation.selectedItemId != R.id.navigation_popular) {
                            navigation.selectedItemId = R.id.navigation_popular
                        }
                    }
                    2 -> {
                        if (navigation.selectedItemId != R.id.navigation_pools) {
                            navigation.selectedItemId = R.id.navigation_pools
                        }
                    }
                    3 -> {
                        if (navigation.selectedItemId != R.id.navigation_tags) {
                            navigation.selectedItemId = R.id.navigation_tags
                        }
                    }
                    else -> {
                        if (navigation.selectedItemId != R.id.navigation_artists) {
                            navigation.selectedItemId = R.id.navigation_artists
                        }
                    }
                }
            }
        }

    private var postExitSharedElementCallback: SharedElementCallback? = null

    fun setPostExitSharedElementCallback(callback: SharedElementCallback?) {
        postExitSharedElementCallback = callback
        setExitSharedElementCallback(postExitSharedElementCallback)
    }

    private var popularExitSharedElementCallback: SharedElementCallback? = null

    fun setPopularExitSharedElementCallback(callback: SharedElementCallback?) {
        popularExitSharedElementCallback = callback
    }

    private fun onNavPosition(position: Int) {
        if (pager_container.currentItem != position) {
            pager_container.currentItem = position
        } else if (currentNavItem == position){
            navigationListeners.forEach {
                it.onClickPosition(position)
            }
        }
        currentNavItem = position
    }

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_posts -> {
                    onNavPosition(0)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_popular -> {
                    onNavPosition(1)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_pools -> {
                    onNavPosition(2)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_tags -> {
                    onNavPosition(3)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_artists -> {
                    onNavPosition(4)
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
        app.sp.unregisterOnSharedPreferenceChangeListener(this)
        BooruManager.listeners.remove(booruListener)
        UserManager.listeners.remove(userListener)
        super.onDestroy()
    }

    internal fun getCurrentBooru(): Booru? {
        var booru: Booru? = null
        val uid = Settings.instance().activeBooruUid
        boorus.forEach {
            if (it.uid == uid) {
                booru = it
                return@forEach
            }
        }
        return booru
    }

    internal fun getCurrentUser(): User? {
        var user: User? = null
        val booruUid = Settings.instance().activeBooruUid
        users.forEach {
            if (it.booru_uid == booruUid) {
                user = it
                return@forEach
            }
        }
        return user
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Settings.SAFE_MODE_KEY, Settings.POST_LIMIT_KEY -> {
                val booru = getCurrentBooru() ?: return
                pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
            }
        }
    }
}
