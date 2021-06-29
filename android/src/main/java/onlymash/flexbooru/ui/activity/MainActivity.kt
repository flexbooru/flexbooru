/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.NavigationRes
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.BuildConfig
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.AUTO_HIDE_BOTTOM_BAR_KEY
import onlymash.flexbooru.app.Settings.BOORU_UID_ACTIVATED_KEY
import onlymash.flexbooru.app.Settings.NIGHT_THEME_KEY
import onlymash.flexbooru.app.Settings.ORDER_SUCCESS_KEY
import onlymash.flexbooru.app.Settings.activatedBooruUid
import onlymash.flexbooru.app.Settings.autoHideBottomBar
import onlymash.flexbooru.app.Settings.isAvailableOnStore
import onlymash.flexbooru.app.Settings.isGoogleSign
import onlymash.flexbooru.app.Settings.isOrderSuccess
import onlymash.flexbooru.app.Settings.latestVersionCode
import onlymash.flexbooru.app.Settings.latestVersionName
import onlymash.flexbooru.app.Settings.latestVersionUrl
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN
import onlymash.flexbooru.app.Values.BOORU_TYPE_DAN1
import onlymash.flexbooru.data.api.AppUpdaterApi
import onlymash.flexbooru.app.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.app.Values.BOORU_TYPE_MOE
import onlymash.flexbooru.app.Values.BOORU_TYPE_SANKAKU
import onlymash.flexbooru.app.Values.BOORU_TYPE_SHIMMIE
import onlymash.flexbooru.app.Values.BOORU_TYPE_UNKNOWN
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.databinding.ActivityMainBinding
import onlymash.flexbooru.extension.*
import onlymash.flexbooru.ui.base.SearchBarFragment
import onlymash.flexbooru.ui.viewmodel.BooruViewModel
import onlymash.flexbooru.ui.viewmodel.getBooruViewModel
import onlymash.flexbooru.extension.setupInsets
import onlymash.flexbooru.ui.base.PathActivity
import onlymash.flexbooru.ui.helper.isNightEnable
import onlymash.flexbooru.ui.viewbinding.viewBinding
import org.kodein.di.instance

class MainActivity : PathActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val BOORUS_LIMIT = 3
        private const val HEADER_ITEM_ID_BOORU_MANAGE = -100L
        private const val DRAWER_ITEM_ID_ACCOUNT = 1L
        private const val DRAWER_ITEM_ID_COMMENTS = 2L
        private const val DRAWER_ITEM_ID_HISTORY = 3L
        private const val DRAWER_ITEM_ID_TAG_BLACKLIST = 4L
        private const val DRAWER_ITEM_ID_MUZEI = 5L
        private const val DRAWER_ITEM_ID_SAUCE_NAO = 6L
        private const val DRAWER_ITEM_ID_WHAT_ANIME = 7L
        private const val DRAWER_ITEM_ID_SETTINGS = 8L
        private const val DRAWER_ITEM_ID_ABOUT = 9L
        private const val DRAWER_ITEM_ID_PURCHASE = 10L
        private const val DRAWER_ITEM_ID_PURCHASE_POSITION = 8
    }

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private var boorus: MutableList<Booru> = mutableListOf()

    private val bottomNavView get() = binding.bottomNavView
    private val drawerSliderView get() = binding.slider
    private val drawerLayout get() = binding.drawerLayout
    private lateinit var headerView: AccountHeaderView
    private lateinit var profileSettingDrawerItem: ProfileSettingDrawerItem

    private val sp by instance<SharedPreferences>()
    private val booruDao by instance<BooruDao>()

    private lateinit var booruViewModel: BooruViewModel

    private lateinit var navController: NavController

    private val drawerItemClickListener: ((v: View?, item: IDrawerItem<*>, position: Int) -> Boolean) = { _: View?, item: IDrawerItem<*>, _: Int ->
        when (item.identifier) {
            DRAWER_ITEM_ID_ACCOUNT -> {
                booruViewModel.currentBooru?.let {
                    if (it.type != BOORU_TYPE_SHIMMIE) {
                        if (it.user == null) {
                            toActivity(AccountConfigActivity::class.java)
                        } else {
                            toActivity(AccountActivity::class.java)
                        }
                    } else {
                        notSupportedToast()
                    }
                }
            }
            DRAWER_ITEM_ID_COMMENTS -> {
                if (booruViewModel.currentBooru?.type != BOORU_TYPE_SHIMMIE) {
                    toActivity(CommentActivity::class.java )
                } else {
                    notSupportedToast()
                }
            }
            DRAWER_ITEM_ID_HISTORY -> {
                if (booruViewModel.currentBooru != null) {
                    toActivity(HistoryActivity::class.java)
                }
            }
            DRAWER_ITEM_ID_TAG_BLACKLIST -> {
                if (booruViewModel.currentBooru?.type ?: BOORU_TYPE_UNKNOWN
                    in intArrayOf(BOORU_TYPE_MOE, BOORU_TYPE_DAN, BOORU_TYPE_DAN1, BOORU_TYPE_GEL)) {
                    toActivity(TagBlacklistActivity::class.java)
                } else {
                    notSupportedToast()
                }
            }
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

    private fun notSupportedToast() {
        Toast.makeText(this, getString(R.string.msg_not_supported), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val windowHeight = resources.displayMetrics.heightPixels
            val gestureWidth = resources.getDimensionPixelSize(R.dimen.gesture_exclusion_width)
            val gestureHeight = resources.getDimensionPixelSize(R.dimen.gesture_exclusion_height)
            val gestureOffset = resources.getDimensionPixelSize(R.dimen.gesture_exclusion_offset)
            window.decorView.systemGestureExclusionRects = listOf(Rect(0, windowHeight - gestureHeight - gestureOffset, gestureWidth, windowHeight - gestureOffset))
        }
        setContentView(binding.root)
        navController = findNavController(R.id.nav_host_fragment)
        setupNavigationBarBehavior()
        bottomNavView.setupWithNavController(navController)
        bottomNavView.setOnItemReselectedListener {
            toListTop()
        }
        booruViewModel = getBooruViewModel(booruDao)
        if (!booruViewModel.isNotEmpty()) {
            activatedBooruUid = createDefaultBooru()
        }
        sp.registerOnSharedPreferenceChangeListener(this)
        setupDrawer()
        booruViewModel.loadBoorus().observe(this, {
            boorus.clear()
            boorus.addAll(it)
            initDrawerHeader()
        })
        booruViewModel.booru.observe(this, { booru: Booru? ->
            if (booru != null)
                setupNavigationMenu(booru.type)
            else
                setupNavigationMenu(BOORU_TYPE_UNKNOWN)
        })
        booruViewModel.loadBooru(activatedBooruUid)
        if (!isOrderSuccess) {
            drawerSliderView.addItemAtPosition(
                DRAWER_ITEM_ID_PURCHASE_POSITION,
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.purchase_title)
                    icon = createImageHolder(R.drawable.ic_payment_24dp)
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_PURCHASE
                }
            )
        }
        setupInsets { insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            drawerSliderView.recyclerView.updatePadding(bottom = bottom)
            drawerSliderView.stickyFooterView?.updatePadding(bottom = bottom)
        }
        checkUpdate()
    }

    private fun setupNavigationMenu(booruType: Int) {
        when (booruType) {
            BOORU_TYPE_SANKAKU -> setupNavigationMenu(4, R.menu.navigation_sankaku, R.navigation.main_navigation_sankaku)
            BOORU_TYPE_GEL -> setupNavigationMenu(2, R.menu.navigation_gel, R.navigation.main_navigation_gel)
            BOORU_TYPE_SHIMMIE -> setupNavigationMenu(1, R.menu.navigation_shimmie, R.navigation.main_navigation_shimmie)
            else -> setupNavigationMenu(5, R.menu.navigation, R.navigation.main_navigation)
        }
    }

    private fun setupNavigationMenu(menuSize: Int, @MenuRes menuRes: Int, @NavigationRes navRes: Int) {
        if (bottomNavView.menu.size() != menuSize) {
            bottomNavView.menu.clear()
            bottomNavView.inflateMenu(menuRes)
        }
        navController.graph = navController.navInflater.inflate(navRes)
        val currentId = navController.currentDestination?.id ?: R.id.nav_posts
        if (currentId != bottomNavView.selectedItemId) {
            bottomNavView.selectedItemId = currentId
        }
        forceShowNavBar()
    }

    private fun setupDrawer() {
        profileSettingDrawerItem = ProfileSettingDrawerItem().apply {
            name = StringHolder(R.string.title_manage_boorus)
            identifier = HEADER_ITEM_ID_BOORU_MANAGE
            icon = createImageHolder(R.drawable.ic_settings_outline_24dp)
            isIconTinted = true
        }
        headerView = AccountHeaderView(this).apply {
            attachToSliderView(drawerSliderView)
            addProfile(profileSettingDrawerItem, profiles?.size ?: 0)
            onAccountHeaderListener = { _: View?, profile: IProfile, _: Boolean ->
                when (val uid = profile.identifier) {
                    HEADER_ITEM_ID_BOORU_MANAGE -> toActivity(BooruActivity::class.java)
                    else -> activatedBooruUid = uid
                }
                false
            }
        }
        drawerSliderView.apply {
            addItems(
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_account)
                    icon = createImageHolder(R.drawable.ic_account_circle_outline_24dp)
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_ACCOUNT
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_comments)
                    icon = createImageHolder(R.drawable.ic_comment_outline_24dp)
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_COMMENTS
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_history)
                    icon = createImageHolder(R.drawable.ic_history_24dp)
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_HISTORY
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_tag_blacklist)
                    icon = createImageHolder(R.drawable.ic_visibility_off_outline_24dp)
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_TAG_BLACKLIST
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_muzei)
                    icon = createImageHolder(R.drawable.ic_muzei_24dp)
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_MUZEI
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_sauce_nao)
                    icon = createImageHolder(R.drawable.ic_search_24dp)
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_SAUCE_NAO
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_what_anime)
                    icon = createImageHolder(R.drawable.ic_youtube_searched_for_24dp)
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_WHAT_ANIME
                }
            )
            stickyDrawerItems = arrayListOf(
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_settings)
                    icon = createImageHolder(R.drawable.ic_settings_outline_24dp)
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_SETTINGS
                },
                PrimaryDrawerItem().apply {
                    name = StringHolder(R.string.title_about)
                    icon = createImageHolder(R.drawable.ic_info_outline_24dp)
                    isSelectable = false
                    isIconTinted = true
                    identifier = DRAWER_ITEM_ID_ABOUT
                }
            )
            stickyFooterShadow = false
            stickyFooterDivider = true
            setSelection(-3L)
            onDrawerItemClickListener = drawerItemClickListener
            tintNavigationBar = false
        }
    }

    private fun createImageHolder(@DrawableRes resId: Int): ImageHolder =
        ImageHolder(ResourcesCompat.getDrawable(resources, resId, theme))

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
        if (BuildConfig.VERSION_CODE >= latestVersionCode || isFinishing) {
            return
        }
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
            val host = booru.host
            val url = if (booru.type == BOORU_TYPE_SANKAKU && host.startsWith("capi-v2.")) {
                "https://sankaku.app/images/192x192.png"
            } else {
                String.format("%s://%s/favicon.ico", booru.scheme, host)
            }
            headerView.addProfile(
                ProfileDrawerItem().apply {
                    icon = ImageHolder(Uri.parse(url))
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
            NIGHT_THEME_KEY -> {
                if (resources.configuration.isNightEnable()) {
                    recreate()
                }
            }
            BOORU_UID_ACTIVATED_KEY -> {
                val identifier = headerView.activeProfile?.identifier ?: -1
                val uid = activatedBooruUid
                if (uid >= 0 && uid != identifier) {
                    headerView.setActiveProfile(identifier = uid, fireOnProfileChanged = false)
                }
                booruViewModel.loadBooru(uid)
            }
            ORDER_SUCCESS_KEY -> {
                if (isOrderSuccess) {
                    drawerSliderView.removeItems(DRAWER_ITEM_ID_PURCHASE)
                } else {
                    if (drawerSliderView.getDrawerItem(DRAWER_ITEM_ID_PURCHASE) == null) {
                        drawerSliderView.addItemAtPosition(
                            DRAWER_ITEM_ID_PURCHASE_POSITION,
                            PrimaryDrawerItem().apply {
                                name = StringHolder(R.string.purchase_title)
                                icon = createImageHolder(R.drawable.ic_payment_24dp)
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
            AUTO_HIDE_BOTTOM_BAR_KEY -> setupNavigationBarBehavior()
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (isFragmentCanBack()) {
            super.onBackPressed()
        }
    }

    private fun isFragmentCanBack(): Boolean {
        val currentFragment = getCurrentFragment()
        if (currentFragment is SearchBarFragment) {
            return currentFragment.onBackPressed()
        }
        return true
    }

    private fun toListTop() {
        val currentFragment = getCurrentFragment()
        if (currentFragment is SearchBarFragment) {
            currentFragment.toListTop()
        }
    }

    private fun getCurrentFragment(): Fragment? {
        val fragments = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                as? NavHostFragment)?.childFragmentManager?.fragments
        if (fragments.isNullOrEmpty()) {
            return null
        }
        return fragments.last()
    }

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun setupNavigationBarBehavior() {
        val layoutParams = bottomNavView.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior
        layoutParams.behavior =
            if (autoHideBottomBar) {
                HideBottomViewOnScrollBehavior<BottomNavigationView>()
            } else {
                if (behavior is HideBottomViewOnScrollBehavior) {
                    behavior.slideUp(bottomNavView)
                }
                null
            }
    }

    fun forceShowNavBar() {
        val behavior = (bottomNavView.layoutParams as CoordinatorLayout.LayoutParams).behavior
        if (behavior is HideBottomViewOnScrollBehavior) {
            behavior.slideUp(bottomNavView)
        }
    }
}
