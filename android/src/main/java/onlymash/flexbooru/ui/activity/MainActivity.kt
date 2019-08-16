/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.SharedElementCallback
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.*
import onlymash.flexbooru.api.AppUpdaterApi
import onlymash.flexbooru.common.Constants
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.common.Booru
import onlymash.flexbooru.entity.common.User
import onlymash.flexbooru.extension.*
import onlymash.flexbooru.ui.adapter.NavPagerAdapter
import org.kodein.di.erased.instance

class MainActivity : PostActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "MainActivity"
        private const val BOORUS_LIMIT = 3
        private const val HEADER_ITEM_ID_BOORU_MANAGE = -100L
        private const val DRAWER_ITEM_ID_ACCOUNT = 1L
        private const val DRAWER_ITEM_ID_COMMENTS = 2L
        private const val DRAWER_ITEM_ID_TAG_BLACKLIST = 3L
        private const val DRAWER_ITEM_ID_MUZEI = 4L
        private const val DRAWER_ITEM_ID_SETTINGS = 5L
        private const val DRAWER_ITEM_ID_SAUCE_NAO = 6L
        private const val DRAWER_ITEM_ID_WHAT_ANIME = 7L
        private const val DRAWER_ITEM_ID_NIGHT_MODE = 8L
        private const val DRAWER_ITEM_ID_ABOUT = 9L
        private const val DRAWER_ITEM_ID_PURCHASE = 10L
        private const val DRAWER_ITEM_ID_PURCHASE_POSITION = 8
    }
    private lateinit var boorus: MutableList<Booru>
    private lateinit var users: MutableList<User>
    internal var sharedElement: View? = null
    lateinit var drawer: Drawer
    private lateinit var header: AccountHeader
    private lateinit var profileSettingDrawerItem: ProfileSettingDrawerItem

    private val sp: SharedPreferences by instance()

    override var currentNavItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Main)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sp.registerOnSharedPreferenceChangeListener(this)
        profileSettingDrawerItem = ProfileSettingDrawerItem()
            .withName(R.string.title_manage_boorus)
            .withIdentifier(HEADER_ITEM_ID_BOORU_MANAGE)
            .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_settings_outline_24dp))
            .withIconTinted(true)
        header = AccountHeaderBuilder()
            .withActivity(this)
            .withOnAccountHeaderListener(headerItemClickListener)
            .build()
        header.addProfile(profileSettingDrawerItem, header.profiles?.size ?: 0)
        drawer = DrawerBuilder()
            .withActivity(this)
            .withTranslucentStatusBar(false)
            .withAccountHeader(header, false)
            .addDrawerItems(
                PrimaryDrawerItem()
                    .withIdentifier(DRAWER_ITEM_ID_ACCOUNT)
                    .withName(R.string.title_account)
                    .withSelectable(false)
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_account_circle_outline_24dp))
                    .withIconTintingEnabled(true),
                PrimaryDrawerItem()
                    .withIdentifier(DRAWER_ITEM_ID_COMMENTS)
                    .withName(R.string.title_comments)
                    .withSelectable(false)
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_comment_outline_24dp))
                    .withIconTintingEnabled(true),
                PrimaryDrawerItem()
                    .withIdentifier(DRAWER_ITEM_ID_TAG_BLACKLIST)
                    .withName(R.string.title_tag_blacklist)
                    .withSelectable(false)
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_visibility_off_outline_24dp))
                    .withIconTintingEnabled(true),
                PrimaryDrawerItem()
                    .withIdentifier(DRAWER_ITEM_ID_MUZEI)
                    .withName(R.string.title_muzei)
                    .withSelectable(false)
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_muzei_24dp))
                    .withIconTintingEnabled(true),
                PrimaryDrawerItem()
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_settings_outline_24dp))
                    .withName(R.string.title_settings)
                    .withSelectable(false)
                    .withIconTintingEnabled(true)
                    .withIdentifier(DRAWER_ITEM_ID_SETTINGS),
                PrimaryDrawerItem()
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_search_24dp))
                    .withName(R.string.title_sauce_nao)
                    .withSelectable(false)
                    .withIconTintingEnabled(true)
                    .withIdentifier(DRAWER_ITEM_ID_SAUCE_NAO),
                PrimaryDrawerItem()
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_youtube_searched_for_24dp))
                    .withName(R.string.title_what_anime)
                    .withSelectable(false)
                    .withIconTintingEnabled(true)
                    .withIdentifier(DRAWER_ITEM_ID_WHAT_ANIME)
            )
            .addStickyDrawerItems(
                SwitchDrawerItem()
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_brightness_2_outline_24dp))
                    .withName(R.string.title_night_mode)
                    .withSelectable(false)
                    .withIconTintingEnabled(true)
                    .withIdentifier(DRAWER_ITEM_ID_NIGHT_MODE)
                    .withChecked(Settings.isNightMode)
                    .withOnCheckedChangeListener(object : OnCheckedChangeListener {
                        override fun onCheckedChanged(
                            drawerItem: IDrawerItem<*>,
                            buttonView: CompoundButton,
                            isChecked: Boolean
                        ) {
                            drawer.closeDrawer()
                            Settings.isNightMode = isChecked
                        }
                    }),
                PrimaryDrawerItem()
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_info_outline_24dp))
                    .withName(R.string.title_about)
                    .withSelectable(false)
                    .withIconTintingEnabled(true)
                    .withIdentifier(DRAWER_ITEM_ID_ABOUT)
            )
            .withStickyFooterShadow(false)
            .withStickyFooterDivider(true)
            .withSavedInstance(savedInstanceState)
            .build()
        drawer.apply {
            setSelection(-3L)
            onDrawerItemClickListener = drawerItemClickListener
        }
        if (!Settings.isOrderSuccess) {
            drawer.addItemAtPosition(
                PrimaryDrawerItem()
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_payment_24dp))
                    .withName(R.string.purchase_title)
                    .withSelectable(false)
                    .withIconTintingEnabled(true)
                    .withIdentifier(DRAWER_ITEM_ID_PURCHASE),
                DRAWER_ITEM_ID_PURCHASE_POSITION
                )
        }
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        pager_container.addOnPageChangeListener(pageChangeListener)
        if (!BooruManager.isNotEmpty()) {
            BooruManager.createBooru(
                Booru(
                    name = "Sample",
                    scheme = "https",
                    host = "moe.fiepi.com",
                    hashSalt = "onlymash--your-password--",
                    type = Constants.TYPE_MOEBOORU
                )
            )
        }
        boorus = BooruManager.getAllBoorus() ?: mutableListOf()
        users = UserManager.getAllUsers() ?: mutableListOf()
        UserManager.listeners.add(userListener)
        BooruManager.listeners.add(booruListener)
        initDrawerHeader()
        setExitSharedElementCallback(sharedElementCallback)
        checkUpdate()
    }

    private fun checkUpdate() {
        GlobalScope.launch {
            AppUpdaterApi.checkUpdate()
        }
        if (BuildConfig.VERSION_CODE < Settings.latestVersionCode) {
            AlertDialog.Builder(this)
                .setTitle(R.string.update_found_update)
                .setMessage(getString(R.string.update_version, Settings.latestVersionName))
                .setPositiveButton(R.string.dialog_update) { _, _ ->
                    if (Settings.isGoogleSign && Settings.isAvailableOnStore) {
                        openAppInMarket(applicationContext.packageName)
                    } else {
                        launchUrl(Settings.latestVersionUrl)
                    }
                    finish()
                }
                .setNegativeButton(R.string.dialog_exit) { _, _ ->
                    finish()
                }
                .create().apply {
                    setCancelable(false)
                    setCanceledOnTouchOutside(false)
                    show()
                }
        }
    }

    private fun initDrawerHeader() {
        val success = Settings.isOrderSuccess
        val size = boorus.size
        var uid = Settings.activeBooruUid
        var i = -1
        header.clear()
        boorus.forEachIndexed { index, booru ->
            if (!success && index >= BOORUS_LIMIT) {
                return@forEachIndexed
            }
            if (i == -1 && booru.uid == uid) {
                i = index
            }
            var host = booru.host
            if (booru.type == Constants.TYPE_SANKAKU && host.startsWith("capi-v2.")) {
                host = host.replaceFirst("capi-v2.", "beta.")
            }
            header.addProfile(
                ProfileDrawerItem()
                    .withName(booru.name)
                    .withIcon(Uri.parse(String.format("%s://%s/favicon.ico", booru.scheme, host)))
                    .withEmail(String.format("%s://%s", booru.scheme, booru.host))
                    .withIdentifier(booru.uid),
                index
            )
        }
        header.addProfile(
            profileSettingDrawerItem,
            if (success || size < BOORUS_LIMIT) boorus.size else BOORUS_LIMIT
        )
        if (size == 0) return
        val booru: Booru
        if (i == -1) {
            booru = boorus[0]
            uid = booru.uid
            Settings.activeBooruUid = uid
        } else {
            booru = boorus[i]
        }
        header.setActiveProfile(uid)
        pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
    }

    private val booruListener = object : BooruManager.Listener {
        override fun onChanged(boorus: MutableList<Booru>) {
            this@MainActivity.boorus.apply {
                clear()
                addAll(boorus)
            }
            initDrawerHeader()
        }

        override fun onAdd(booru: Booru) {
            boorus.add(booru)
            if (!Settings.isOrderSuccess &&
                header.profiles?.size ?: 0 == BOORUS_LIMIT + 1) {
                return
            }
            initDrawerHeader()
        }

        override fun onDelete(booruUid: Long) {
            val index = boorus.indexOfFirst { it.uid == booruUid }
            boorus.removeAt(index)
            header.removeProfileByIdentifier(booruUid)
            if (boorus.size > 0) {
                if (Settings.activeBooruUid == booruUid) {
                    val booru = boorus[0]
                    Settings.activeBooruUid = booru.uid
                    header.setActiveProfile(booru.uid)
                    pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
                }
            } else {
                Settings.activeBooruUid = -1
                pager_container.adapter = null
            }
        }

        override fun onUpdate(booru: Booru) {
            boorus.forEach {
                if (it.uid == booru.uid) {
                    it.name = booru.name
                    it.scheme = booru.scheme
                    it.host = booru.host
                    it.hashSalt = booru.hashSalt
                    it.type = booru.type
                    if (Settings.activeBooruUid == booru.uid) {
                        pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
                    }
                    return@forEach
                }
            }
            var host = booru.host
            if (booru.type == Constants.TYPE_SANKAKU && host.startsWith("capi-v2.")) host = host.replaceFirst("capi-v2.", "beta.")
            header.updateProfile(
                ProfileDrawerItem()
                    .withName(booru.name)
                    .withIcon(Uri.parse(String.format("%s://%s/favicon.ico", booru.scheme, host)))
                    .withEmail(String.format("%s://%s", booru.scheme, booru.host))
                    .withIdentifier(booru.uid))
        }
    }

    private val userListener =  object : UserManager.Listener {
        override fun onAdd(user: User) {
            users.add(user)
        }

        override fun onDelete(user: User) {
            var position = -1
            users.forEachIndexed { i, u ->
                if (user.uid == u.uid) {
                    position = i
                    return@forEachIndexed
                }
            }
            if (position >= 0) {
                users.removeAt(position)
            }
        }

        override fun onUpdate(user: User) {
            users.forEach {
                if (it.uid == user.uid) {
                    it.name = user.name
                    it.id = user.id
                    it.booruUid
                    it.passwordHash = user.passwordHash
                    it.apiKey = user.apiKey
                    return@forEach
                }
            }
        }
    }

    private val headerItemClickListener = object : AccountHeader.OnAccountHeaderListener {
        override fun onProfileChanged(view: View?, profile: IProfile<*>, current: Boolean): Boolean {
            when (val uid = profile.identifier) {
                HEADER_ITEM_ID_BOORU_MANAGE -> {
                    startActivity(Intent(this@MainActivity, BooruActivity::class.java))
                }
                else -> {
                    Settings.activeBooruUid = uid
                    boorus.forEach { booru ->
                        if (booru.uid == uid) {
                            pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
                            pager_container.setCurrentItem(currentNavItem, false)
                            return@forEach
                        }
                    }
                }
            }
            return false
        }
    }

    private val drawerItemClickListener = object : Drawer.OnDrawerItemClickListener {
        override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
            when (drawerItem.identifier) {
                DRAWER_ITEM_ID_ACCOUNT -> {
                    val booru = getCurrentBooru()
                    val user = getCurrentUser()
                    if (user != null && booru != null) {
                        startActivity(Intent(this@MainActivity, AccountActivity::class.java))
                    } else if (booru == null){
                        startActivity(Intent(this@MainActivity, BooruActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, AccountConfigActivity::class.java))
                    }
                }
                DRAWER_ITEM_ID_COMMENTS -> {
                    if (getCurrentBooru() != null) {
                        CommentActivity.startActivity(this@MainActivity)
                    } else {
                        startActivity(Intent(this@MainActivity, BooruActivity::class.java))
                    }
                }
                DRAWER_ITEM_ID_TAG_BLACKLIST -> {
                    if (getCurrentBooru() != null) {
                        startActivity(Intent(this@MainActivity, TagBlacklistActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, BooruActivity::class.java))
                    }
                }
                DRAWER_ITEM_ID_MUZEI -> {
                    if (getCurrentBooru() != null) {
                        startActivity(Intent(this@MainActivity, MuzeiActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, BooruActivity::class.java))
                    }
                }
                DRAWER_ITEM_ID_SETTINGS -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                DRAWER_ITEM_ID_SAUCE_NAO -> startActivity(Intent(this@MainActivity, SauceNaoActivity::class.java))
                DRAWER_ITEM_ID_WHAT_ANIME -> startActivity(Intent(this@MainActivity, WhatAnimeActivity::class.java))
                DRAWER_ITEM_ID_ABOUT -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                DRAWER_ITEM_ID_PURCHASE -> startActivity(Intent(this@MainActivity, PurchaseActivity::class.java))
                DRAWER_ITEM_ID_NIGHT_MODE -> return false
            }
            return false
        }
    }

    private val sharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
            if (names == null || sharedElements == null) return
            names.clear()
            sharedElements.clear()
            sharedElement?.let { view ->
                view.transitionName?.let { name ->
                    names.add(name)
                    sharedElements[name] = view
                }
            }
        }
    }

    private val pageChangeListener =
        object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
//                sharedElement = null
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

    private fun onNavPosition(position: Int) {
        if (pager_container.currentItem != position) {
            pager_container.setCurrentItem(position, false)
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

    override fun onDestroy() {
        sp.unregisterOnSharedPreferenceChangeListener(this)
        BooruManager.listeners.remove(booruListener)
        UserManager.listeners.remove(userListener)
        super.onDestroy()
    }

    internal fun getCurrentBooru(): Booru? {
        var booru: Booru? = null
        val uid = Settings.activeBooruUid
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
        val booruUid = Settings.activeBooruUid
        users.forEach {
            if (it.booruUid == booruUid) {
                user = it
                return@forEach
            }
        }
        return user
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Settings.SAFE_MODE_KEY,
            Settings.PAGE_LIMIT_KEY,
            Settings.GRID_WIDTH_KEY,
            Settings.SHOW_INFO_BAR_KEY -> {
                val booru = getCurrentBooru() ?: return
                pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
                pager_container.setCurrentItem(currentNavItem, false)
            }
            Settings.NIGHT_MODE_KEY -> {
                AppCompatDelegate.setDefaultNightMode(Settings.nightMode)
            }
            Settings.ORDER_SUCCESS_KEY -> {
                if (Settings.isOrderSuccess) {
                    drawer.removeItem(DRAWER_ITEM_ID_PURCHASE)
                } else {
                    val index = drawer.drawerItems.indexOfFirst {
                        it.identifier == DRAWER_ITEM_ID_PURCHASE
                    }
                    if (index < 0) {
                        drawer.addItemAtPosition(
                            PrimaryDrawerItem()
                                .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_payment_24dp))
                                .withName(R.string.purchase_title)
                                .withSelectable(false)
                                .withIconTintingEnabled(true)
                                .withIdentifier(DRAWER_ITEM_ID_PURCHASE),
                            DRAWER_ITEM_ID_PURCHASE_POSITION
                        )
                    }
                }
                if (boorus.size > BOORUS_LIMIT) {
                    initDrawerHeader()
                }
            }
        }
    }

    override fun onBackPressed() {
        when {
            drawer.isDrawerOpen -> drawer.closeDrawer()
            currentNavItem != 0 -> pager_container.setCurrentItem(0, false)
            else -> super.onBackPressed()
        }
    }
}
