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
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.MenuRes
import androidx.annotation.NavigationRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.mikepenz.materialdrawer.util.addItemAtPosition
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.util.getDrawerItem
import com.mikepenz.materialdrawer.util.removeItems
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.BuildConfig
import onlymash.flexbooru.R
import onlymash.flexbooru.common.Settings.BOORU_UID_ACTIVATED_KEY
import onlymash.flexbooru.common.Settings.ORDER_SUCCESS_KEY
import onlymash.flexbooru.common.Settings.activatedBooruUid
import onlymash.flexbooru.common.Settings.isAvailableOnStore
import onlymash.flexbooru.common.Settings.isGoogleSign
import onlymash.flexbooru.common.Settings.isOrderSuccess
import onlymash.flexbooru.common.Settings.latestVersionCode
import onlymash.flexbooru.common.Settings.latestVersionName
import onlymash.flexbooru.common.Settings.latestVersionUrl
import onlymash.flexbooru.data.api.AppUpdaterApi
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.common.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.common.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.extension.*
import onlymash.flexbooru.ui.fragment.SearchBarFragment
import onlymash.flexbooru.ui.viewmodel.BooruViewModel
import onlymash.flexbooru.ui.viewmodel.getBooruViewModel
import org.kodein.di.erased.instance

class MainActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val BOORUS_LIMIT = 3
        private const val HEADER_ITEM_ID_BOORU_MANAGE = -100L
        private const val DRAWER_ITEM_ID_ACCOUNT = 1L
        private const val DRAWER_ITEM_ID_COMMENTS = 2L
        private const val DRAWER_ITEM_ID_TAG_BLACKLIST = 3L
        private const val DRAWER_ITEM_ID_MUZEI = 4L
        private const val DRAWER_ITEM_ID_SAUCE_NAO = 5L
        private const val DRAWER_ITEM_ID_WHAT_ANIME = 6L
        private const val DRAWER_ITEM_ID_SETTINGS = 7L
        private const val DRAWER_ITEM_ID_ABOUT = 8L
        private const val DRAWER_ITEM_ID_PURCHASE = 9L
        private const val DRAWER_ITEM_ID_PURCHASE_POSITION = 7
    }

    private var currentBooru: Booru? = null
    private var boorus: MutableList<Booru> = mutableListOf()

    internal var sharedElement: View? = null

    private lateinit var headerView: AccountHeaderView
    private lateinit var profileSettingDrawerItem: ProfileSettingDrawerItem

    private val sp: SharedPreferences by instance()
    private val booruDao: BooruDao by instance()

    private lateinit var booruViewModel: BooruViewModel

    private lateinit var navController: NavController

    private val drawerItemClickListener: ((v: View?, item: IDrawerItem<*>, position: Int) -> Boolean) = { _: View?, item: IDrawerItem<*>, _: Int ->
        when (item.identifier) {
            DRAWER_ITEM_ID_ACCOUNT -> {
                currentBooru?.let {
                    if (it.user == null) {
                        toActivity(AccountConfigActivity::class.java)
                    } else {
                        toActivity(AccountActivity::class.java)
                    }
                }
            }
            DRAWER_ITEM_ID_COMMENTS -> toActivity(CommentActivity::class.java )
            DRAWER_ITEM_ID_TAG_BLACKLIST -> toActivity(TagBlacklistActivity::class.java)
            DRAWER_ITEM_ID_MUZEI -> toActivity(MuzeiActivity::class.java)
            DRAWER_ITEM_ID_SETTINGS -> toActivity(SettingsActivity::class.java)
            DRAWER_ITEM_ID_SAUCE_NAO -> toActivity(SauceNaoActivity::class.java)
            DRAWER_ITEM_ID_WHAT_ANIME -> toActivity(WhatAnimeActivity::class.java)
            DRAWER_ITEM_ID_ABOUT -> toActivity(AboutActivity::class.java)
            DRAWER_ITEM_ID_PURCHASE -> toActivity(PurchaseActivity::class.java)
        }
        false
    }

    private fun toActivity(activityCls: Class<*>) {
        startActivity(Intent(this, activityCls))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Main)
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val windowHeight = resources.displayMetrics.heightPixels
            val gestureWidth = resources.getDimensionPixelSize(R.dimen.gesture_exclusion_width)
            val gestureHeight = resources.getDimensionPixelSize(R.dimen.gesture_exclusion_height)
            val gestureOffset = resources.getDimensionPixelSize(R.dimen.gesture_exclusion_offset)
            window.decorView.systemGestureExclusionRects = listOf(Rect(0, windowHeight - gestureHeight - gestureOffset, gestureWidth, windowHeight - gestureOffset))
        }
        setContentView(R.layout.activity_main)
        navController = findNavController(R.id.nav_host_fragment)
        navigation.setupWithNavController(navController)
        booruViewModel = getBooruViewModel(booruDao)
        if (!booruViewModel.isNotEmpty()) {
            activatedBooruUid = createDefaultBooru()
        }
        sp.registerOnSharedPreferenceChangeListener(this)
        val currentBooruUid = activatedBooruUid
        profileSettingDrawerItem = ProfileSettingDrawerItem().apply {
            name = StringHolder(R.string.title_manage_boorus)
            identifier = HEADER_ITEM_ID_BOORU_MANAGE
            icon = ImageHolder(AppCompatResources.getDrawable(this@MainActivity, R.drawable.ic_settings_outline_24dp))
            isIconTinted = true
        }
        headerView = AccountHeaderView(this).apply {
            attachToSliderView(slider)
            addProfile(profileSettingDrawerItem, profiles?.size ?: 0)
            withSavedInstance(savedInstanceState)
            onAccountHeaderListener = { _: View?, profile: IProfile, _: Boolean ->
                when (val uid = profile.identifier) {
                    HEADER_ITEM_ID_BOORU_MANAGE -> startActivity(Intent(this@MainActivity, BooruActivity::class.java))
                    else -> activatedBooruUid = uid
                }
                false
            }
        }
        slider.apply {
            addItems(
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_account)
                    icon = ImageHolder(AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_account_circle_outline_24dp))
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_ACCOUNT
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_comments)
                    icon = ImageHolder(AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_comment_outline_24dp))
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_COMMENTS
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_tag_blacklist)
                    icon = ImageHolder(AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_visibility_off_outline_24dp))
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_TAG_BLACKLIST
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_muzei)
                    icon = ImageHolder(AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_muzei_24dp))
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_MUZEI
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_sauce_nao)
                    icon = ImageHolder(AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_search_24dp))
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_SAUCE_NAO
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_what_anime)
                    icon = ImageHolder(AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_youtube_searched_for_24dp))
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_WHAT_ANIME
                }
            )
            stickyDrawerItems = arrayListOf(
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_settings)
                    icon = ImageHolder(AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_settings_outline_24dp))
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_SETTINGS
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_about)
                    icon = ImageHolder(AppCompatResources.getDrawable(this@MainActivity, R.drawable.ic_info_outline_24dp))
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_ABOUT
                }
            )
            stickyFooterShadow = false
            stickyFooterDivider = true
            setSelection(-3L)
            onDrawerItemClickListener = drawerItemClickListener
        }
        booruViewModel.loadBoorus().observe(this, Observer {
            boorus.clear()
            boorus.addAll(it)
            initDrawerHeader()
        })
        booruViewModel.booru.observe(this, Observer { booru: Booru? ->
            currentBooru = booru
            setupNavigation(booru?.type ?: -1)
        })
        booruViewModel.loadBooru(currentBooruUid)
        if (!isOrderSuccess) {
            slider.addItemAtPosition(
                DRAWER_ITEM_ID_PURCHASE_POSITION,
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.purchase_title)
                    icon = ImageHolder(AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_payment_24dp))
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_PURCHASE
                }
            )
        }
        checkUpdate()
    }

    private fun setupNavigation(booruType: Int) {
        when (booruType) {
            BOORU_TYPE_SANKAKU -> setupNavigationMenu(R.menu.navigation_sankaku, R.navigation.main_navigation_sankaku)
            BOORU_TYPE_GEL -> setupNavigationMenu(R.menu.navigation_gel, R.navigation.main_navigation_gel)
            else -> setupNavigationMenu(R.menu.navigation, R.navigation.main_navigation)
        }
    }

    private fun setupNavigationMenu(@MenuRes menuRes: Int, @NavigationRes navRes: Int) {
        navigation.menu.clear()
        navigation.inflateMenu(menuRes)
        navController.graph = navController.navInflater.inflate(navRes)
    }

    private fun createDefaultBooru(): Long {
        return booruViewModel.createBooru(
            Booru(
                name = "Sample",
                scheme = "https",
                host = "moe.fiepi.com",
                hashSalt = "onlymash--your-password--",
                type = BOORU_TYPE_MOE
            )
        )
    }

    private fun checkUpdate() {
        GlobalScope.launch {
            AppUpdaterApi.checkUpdate()
        }
        if (BuildConfig.VERSION_CODE < latestVersionCode) {
            AlertDialog.Builder(this)
                .setTitle(R.string.update_found_update)
                .setMessage(getString(R.string.update_version, latestVersionName))
                .setPositiveButton(R.string.dialog_update) { _, _ ->
                    if (isGoogleSign && isAvailableOnStore) {
                        openAppInMarket(applicationContext.packageName)
                    } else {
                        launchUrl(latestVersionUrl)
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
        val success = isOrderSuccess
        val size = boorus.size
        var uid = activatedBooruUid
        var i = -1
        headerView.clear()
        boorus.forEachIndexed { index, booru ->
            if (!success && index >= BOORUS_LIMIT) {
                return@forEachIndexed
            }
            if (i == -1 && booru.uid == uid) {
                i = index
            }
            var host = booru.host
            if (booru.type == BOORU_TYPE_SANKAKU && host.startsWith("capi-v2.")) {
                host = host.replaceFirst("capi-v2.", "beta.")
            }
            headerView.addProfile(
                ProfileDrawerItem().apply {
                    icon = ImageHolder(Uri.parse(String.format("%s://%s/favicon.ico", booru.scheme, host)))
                    name = StringHolder(booru.name)
                    description = StringHolder(String.format("%s://%s", booru.scheme, booru.host))
                    identifier = booru.uid
                },
                index
            )
        }
        headerView.addProfile(
            profileSettingDrawerItem,
            if (success || size < BOORUS_LIMIT) size else BOORUS_LIMIT
        )
        if (size == 0) {
            activatedBooruUid = -1
            return
        }
        if (i == -1) {
            uid = boorus[0].uid
            activatedBooruUid = uid
        }
        headerView.setActiveProfile(uid)
    }

    override fun onDestroy() {
        sp.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BOORU_UID_ACTIVATED_KEY -> booruViewModel.loadBooru(activatedBooruUid)
            ORDER_SUCCESS_KEY -> {
                if (isOrderSuccess) {
                    slider.removeItems(DRAWER_ITEM_ID_PURCHASE)
                } else {
                    if (slider.getDrawerItem(DRAWER_ITEM_ID_PURCHASE) == null) {
                        slider.addItemAtPosition(
                            DRAWER_ITEM_ID_PURCHASE_POSITION,
                            PrimaryDrawerItem().apply {
                                name = StringHolder(R.string.purchase_title)
                                icon = ImageHolder(AppCompatResources.getDrawable(
                                    this@MainActivity,
                                    R.drawable.ic_payment_24dp))
                                isSelectable = false
                                isIconTinted = true
                                identifier = DRAWER_ITEM_ID_PURCHASE
                            }
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
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (isFragmentCanBack()) {
            super.onBackPressed()
        }
    }

    private fun isFragmentCanBack(): Boolean {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)?.let { navHost ->
            val currentFragment = navHost.childFragmentManager.fragments.last()
            if (currentFragment is SearchBarFragment) {
                return currentFragment.onBackPressed()
            }
        }
        return true
    }

    fun openDrawer() {
        drawer_layout.openDrawer(GravityCompat.START)
    }
}
