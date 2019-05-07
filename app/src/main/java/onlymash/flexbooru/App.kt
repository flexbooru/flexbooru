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

package onlymash.flexbooru

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
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import onlymash.flexbooru.api.OrderApi
import onlymash.flexbooru.glide.GlideApp
import onlymash.flexbooru.ui.PurchaseActivity
import onlymash.flexbooru.util.getSignMd5

class App : Application() {
    companion object {
        lateinit var app: App
    }
    val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(app) }
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
        Settings.instance().isGoogleSign = isGoogleSign
        AppCompatDelegate.setDefaultNightMode(Settings.instance().nightMode)
        DrawerImageLoader.init(drawerImageLoader)
        if (isGoogleSign) {
            checkOrderFromCache()
        } else {
            val orderId = Settings.instance().orderId
            if (orderId.isNotEmpty()) {
                OrderApi.orderChecker(orderId, Settings.instance().orderDeviceId)
            } else {
                Settings.instance().isOrderSuccess = false
            }
        }
        MobileAds.initialize(this, "ca-app-pub-1547571472841615~2418349121")
    }
    private fun checkOrderFromCache() {
        val billingClient = BillingClient
            .newBuilder(this)
            .setListener { _, _ ->

            }
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                billingClient.endConnection()
            }
            override fun onBillingSetupFinished(responseCode: Int) {
                if (billingClient.isReady) {
                    val purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList
                    if (purchases != null) {
                        val index = purchases.indexOfFirst {
                            it.sku == PurchaseActivity.SKU
                        }
                        Settings.instance().isOrderSuccess = index >= 0
                    } else {
                        Settings.instance().isOrderSuccess = false
                    }
                    billingClient.endConnection()
                }
            }
        })
    }
}