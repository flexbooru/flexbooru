/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.SharedElementCallback
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import kotlinx.android.synthetic.main.activity_main.*
import onlymash.flexbooru.App.Companion.app
import onlymash.flexbooru.BuildConfig
import onlymash.flexbooru.Constants
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings
import onlymash.flexbooru.api.AppUpdaterApi
import onlymash.flexbooru.database.BooruManager
import onlymash.flexbooru.database.UserManager
import onlymash.flexbooru.entity.Booru
import onlymash.flexbooru.entity.User
import onlymash.flexbooru.ui.adapter.NavPagerAdapter
import onlymash.flexbooru.util.getWidth
import onlymash.flexbooru.util.launchUrl
import onlymash.flexbooru.widget.drawerlayout.FullDrawerLayout
import java.net.URLDecoder

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "MainActivity"
        private const val HEADER_ITEM_ID_BOORU_MANAGE = -11L
        private const val DRAWER_ITEM_ID_ABOUT = -1L
        private const val DRAWER_ITEM_ID_COPYRIGHT = -2L
        private const val DRAWER_ITEM_ID_ACCOUNT = 1L
        private const val DRAWER_ITEM_ID_COMMENTS = 2L
        private const val DRAWER_ITEM_ID_MUZEI = 3L
        private const val DRAWER_ITEM_ID_SETTINGS = 4L
    }
    private lateinit var boorus: MutableList<Booru>
    private lateinit var users: MutableList<User>
    internal var sharedElement: View? = null
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
            .withDrawerLayout(FullDrawerLayout(this).apply {
                setLeftSwipeSize(this@MainActivity.getWidth())
            })
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
                    .withIdentifier(DRAWER_ITEM_ID_SETTINGS)
            )
            .addStickyDrawerItems(
                PrimaryDrawerItem()
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_info_outline_24dp))
                    .withName(R.string.title_about)
                    .withSelectable(false)
                    .withIconTintingEnabled(true)
                    .withIdentifier(DRAWER_ITEM_ID_ABOUT),
                PrimaryDrawerItem()
                    .withIcon(AppCompatResources.getDrawable(this, R.drawable.ic_copyright_24dp))
                    .withName(R.string.title_copyright)
                    .withSelectable(false)
                    .withIconTintingEnabled(true)
                    .withIdentifier(DRAWER_ITEM_ID_COPYRIGHT)
            )
            .withStickyFooterDivider(true)
            .withStickyFooterShadow(false)
            .withSavedInstance(savedInstanceState)
            .build()
        drawer.apply {
            setSelection(-3L)
            onDrawerItemClickListener = drawerItemClickListener
        }
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        pager_container.addOnPageChangeListener(pageChangeListener)
        if (!BooruManager.isNotEmpty()) {
            BooruManager.createBooru(
                Booru(
                    name = "Sample",
                    scheme = "https",
                    host = "moe.fiepi.com",
                    hash_salt = "onlymash--your-password--",
                    type = Constants.TYPE_MOEBOORU))
        }
        boorus = BooruManager.getAllBoorus() ?: mutableListOf()
        users = UserManager.getAllUsers() ?: mutableListOf()
        UserManager.listeners.add(userListener)
        BooruManager.listeners.add(booruListener)
        val size = boorus.size
        if (size > 0) {
            header.removeProfile(profileSettingDrawerItem)
            boorus.forEachIndexed { index, booru ->
                var host = booru.host
                if (booru.type == Constants.TYPE_SANKAKU && host.startsWith("capi-v2.")) host = host.replaceFirst("capi-v2.", "beta.")
                header.addProfile(
                    ProfileDrawerItem()
                        .withName(booru.name)
                        .withIcon(Uri.parse(String.format("%s://%s/favicon.ico", booru.scheme, host)))
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
        setExitSharedElementCallback(sharedElementCallback)
        if(!applicationContext.packageName.contains("play")) {
            checkForUpdate()
        }
    }

    private fun checkForUpdate() {
        AppUpdaterApi.checkUpdate {
            if (isFinishing) return@checkUpdate
            if (it != null && it.version_code > BuildConfig.VERSION_CODE) {
               AlertDialog.Builder(this)
                   .setTitle(R.string.update_found_update)
                   .setMessage(getString(R.string.update_version, it.version_name))
                   .setPositiveButton(R.string.dialog_yes) { _, _ ->
                       this@MainActivity.launchUrl(it.url)
                   }
                   .setNegativeButton(R.string.dialog_no, null)
                   .create()
                   .show()
            }
        }
    }

    private val booruListener = object : BooruManager.Listener {
        override fun onAdd(booru: Booru) {
            header.removeProfile(profileSettingDrawerItem)
            boorus.add(booru)
            var host = booru.host
            if (booru.type == Constants.TYPE_SANKAKU && host.startsWith("capi-v2.")) host = host.replaceFirst("capi-v2.", "beta.")
            header.addProfile(
                ProfileDrawerItem()
                    .withName(booru.name)
                    .withIcon(Uri.parse(String.format("%s://%s/favicon.ico", booru.scheme, host)))
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
            val index = boorus.indexOfFirst { it.uid == booruUid }
            boorus.removeAt(index)
            header.removeProfileByIdentifier(booruUid)
            if (boorus.size > 0) {
                if (Settings.instance().activeBooruUid == booruUid) {
                    val booru = boorus[0]
                    Settings.instance().activeBooruUid = booru.uid
                    header.setActiveProfile(booru.uid)
                    pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
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
                    it.booru_uid
                    it.password_hash = user.password_hash
                    it.api_key = user.api_key
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
                    Settings.instance().activeBooruUid = uid
                    boorus.forEach { booru ->
                        if (booru.uid == uid) {
                            pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
                            pager_container.currentItem = currentNavItem
                            return@forEach
                        }
                    }
                }
            }
            return false
        }
    }

    private val drawerItemClickListener = object : Drawer.OnDrawerItemClickListener {
        override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*, *>?): Boolean {
            when (drawerItem?.identifier) {
                DRAWER_ITEM_ID_ACCOUNT -> {
                    val booru = getCurrentBooru()
                    if (booru?.type == Constants.TYPE_GELBOORU) {
                        Toast.makeText(this@MainActivity,
                            getString(R.string.msg_not_supported), Toast.LENGTH_SHORT).show()
                        return true
                    }
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
                DRAWER_ITEM_ID_MUZEI -> {
                    if (getCurrentBooru() != null) {
                        startActivity(Intent(this@MainActivity, MuzeiActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, BooruActivity::class.java))
                    }
                }
                DRAWER_ITEM_ID_SETTINGS -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
                DRAWER_ITEM_ID_ABOUT -> {
                    startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                }
                DRAWER_ITEM_ID_COPYRIGHT -> {
                    startActivity(Intent(this@MainActivity, CopyrightActivity::class.java))
                }
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
            Settings.SAFE_MODE_KEY,
            Settings.PAGE_LIMIT_KEY,
            Settings.GRID_WIDTH,
            Settings.SHOW_INFO_BAR_KEY -> {
                val booru = getCurrentBooru() ?: return
                pager_container.adapter = NavPagerAdapter(supportFragmentManager, booru, getCurrentUser())
                pager_container.currentItem = currentNavItem
            }
            Settings.THEME_MODE_KEY -> {
                val mode = Settings.instance().themeMode
                AppCompatDelegate.setDefaultNightMode(mode)
                if (mode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                    recreate()
                }
            }
        }
    }

    private var isExit = false
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawer.isDrawerOpen) {
                drawer.closeDrawer()
            } else if (!isExit) {
                isExit = true
                Snackbar.make(navigation, getString(R.string.msg_press_twice_to_exit), Snackbar.LENGTH_LONG).show()
                Handler().postDelayed({
                    isExit = false
                }, 2000L)
            } else {
                finish()
            }
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            val docUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)) ?: return
            Settings.instance().downloadDirPath = URLDecoder.decode(docUri.toString(), "UTF-8")
        }
    }
}
