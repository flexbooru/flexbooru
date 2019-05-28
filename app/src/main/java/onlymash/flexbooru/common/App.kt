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

package onlymash.flexbooru.common

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.bumptech.glide.Glide
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.api.*
import onlymash.flexbooru.database.FlexbooruDatabase
import onlymash.flexbooru.extension.getSignMd5
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.repository.tagfilter.TagFilterRepositoryImpl
import onlymash.flexbooru.ui.PurchaseActivity
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.*
import java.util.concurrent.Executors

class App : Application(), KodeinAware {
    companion object {
        lateinit var app: App
    }

    override val kodein by Kodein.lazy {
        bind<Context>() with instance(this@App)
        bind<SharedPreferences>() with provider { PreferenceManager.getDefaultSharedPreferences(instance()) }
        bind() from singleton { FlexbooruDatabase(instance()) }
        bind() from singleton { instance<FlexbooruDatabase>().booruDao() }
        bind() from singleton { instance<FlexbooruDatabase>().userDao() }
        bind() from singleton { instance<FlexbooruDatabase>().cookieDao() }
        bind() from singleton { instance<FlexbooruDatabase>().suggestionDao() }
        bind() from singleton { instance<FlexbooruDatabase>().tagFilterDao() }
        bind() from singleton { instance<FlexbooruDatabase>().tagBlacklistDao() }
        bind() from singleton { instance<FlexbooruDatabase>().muzeiDao() }
        bind() from singleton { instance<FlexbooruDatabase>().postDanDao() }
        bind() from singleton { instance<FlexbooruDatabase>().postDanOneDao() }
        bind() from singleton { instance<FlexbooruDatabase>().postMoeDao() }
        bind() from singleton { instance<FlexbooruDatabase>().postGelDao() }
        bind() from singleton { instance<FlexbooruDatabase>().postSankakuDao() }
        bind() from singleton { DanbooruApi() }
        bind() from singleton { DanbooruOneApi() }
        bind() from singleton { MoebooruApi() }
        bind() from singleton { GelbooruApi() }
        bind() from singleton { SankakuApi() }
        bind() from singleton { Executors.newSingleThreadExecutor() }
        bind() from singleton { TagFilterRepositoryImpl(instance()) }
    }
    val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private val drawerImageLoader = object : AbstractDrawerImageLoader() {
        override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
            GlideApp.with(imageView.context).load(uri).placeholder(placeholder).into(imageView)
        }
        override fun cancel(imageView: ImageView) {
            Glide.with(imageView.context).clear(imageView)
        }
    }
    override fun onCreate() {
        super.onCreate()
        app = this
        initial()
    }
    private fun initial() {
        val isGoogleSign = getSignMd5() == "777296a0fe4baa88c783d1cb18bdf1f2"
        Settings.isGoogleSign = isGoogleSign
        AppCompatDelegate.setDefaultNightMode(Settings.nightMode)
        DrawerImageLoader.init(drawerImageLoader)
        if (isGoogleSign) {
            checkOrderFromCache()
        } else {
            val orderId = Settings.orderId
            if (orderId.isNotEmpty()) {
                GlobalScope.launch {
                    OrderApi.orderChecker(orderId, Settings.orderDeviceId)
                }
            } else {
                Settings.isOrderSuccess = false
            }
        }
    }
    private fun checkOrderFromCache() {
        val billingClient = BillingClient
            .newBuilder(this)
            .enablePendingPurchases()
            .setListener { _, _ ->

            }
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                if (billingClient.isReady) {
                    val purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList
                    if (purchases != null) {
                        val index = purchases.indexOfFirst {
                            it.sku == PurchaseActivity.SKU && it.purchaseState == Purchase.PurchaseState.PURCHASED
                        }
                        Settings.isOrderSuccess = index >= 0
                    } else {
                        Settings.isOrderSuccess = false
                    }
                    billingClient.endConnection()
                }
            }
            override fun onBillingServiceDisconnected() {
                billingClient.endConnection()
            }
        })
    }
}