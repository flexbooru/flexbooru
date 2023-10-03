/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

package onlymash.flexbooru.app

import android.app.Application
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.dispose
import coil.load
import coil.memory.MemoryCache
import com.android.billingclient.api.*
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.color.DynamicColors
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import onlymash.flexbooru.BuildConfig
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings.isGoogleSign
import onlymash.flexbooru.app.Settings.isOrderSuccess
import onlymash.flexbooru.app.Settings.nightMode
import onlymash.flexbooru.app.Settings.orderDeviceId
import onlymash.flexbooru.app.Settings.orderId
import onlymash.flexbooru.common.di.commonModules
import onlymash.flexbooru.data.api.OrderApi
import onlymash.flexbooru.extension.getSignMd5
import onlymash.flexbooru.okhttp.AndroidCookieJar
import onlymash.flexbooru.okhttp.CloudflareInterceptor
import onlymash.flexbooru.okhttp.ProgressInterceptor
import onlymash.flexbooru.okhttp.RequestHeaderInterceptor
import onlymash.flexbooru.ui.activity.PurchaseActivity
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application(), ImageLoaderFactory {

    companion object {
        lateinit var app: App
        private const val CACHE_MAX_PERCENT = 1.0
    }

    private val drawerImageLoader = object : AbstractDrawerImageLoader() {
        override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.load(uri) {
                placeholder(ContextCompat.getDrawable(imageView.context, R.drawable.avatar_account))
            }
        }
        override fun cancel(imageView: ImageView) {
            imageView.dispose()
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        startKoin {
            androidContext(app)
            modules(appModules + commonModules)
        }
        initial()
    }

    private fun initial() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
        DrawerImageLoader.init(drawerImageLoader)
        if (!isOrderSuccess) {
            MobileAds.initialize(this) {}
            MobileAds.setRequestConfiguration(RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("65DC68D21E774E5B6CAF511768A3E2D2")).build())
        }
        if (BuildConfig.DEBUG) {
            return
        }
        checkOrder()
    }

    private fun checkOrder() {
        val isPlayVersion = getSignMd5() == "777296a0fe4baa88c783d1cb18bdf1f2"
        isGoogleSign = isPlayVersion
        if (isPlayVersion) {
            val time = System.currentTimeMillis()
            if (!isOrderSuccess || time - Settings.orderTime > 7*24*60*60*1000) {
                Settings.orderTime = time
                checkOrderFromCache()
            }
        } else {
            val id = orderId
            if (id.isNullOrEmpty()) {
                isOrderSuccess = false
            } else {
                MainScope().launch {
                    OrderApi.orderChecker(id, orderDeviceId)
                }
            }
        }
    }

    private fun checkOrderFromCache() {
        val billingClient = BillingClient
            .newBuilder(this)
            .enablePendingPurchases()
            .setListener { _, _ ->  }
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    queryPurchases(billingClient)
                }
            }
            override fun onBillingServiceDisconnected() {
                billingClient.endConnection()
            }
        })
    }

    private fun queryPurchases(billingClient: BillingClient) {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(queryPurchasesParams) { _, purchases ->
            val success = if (purchases.isEmpty()) {
                false
            } else {
                val index = purchases.indexOfFirst {
                    it.products[0] == PurchaseActivity.SKU && it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (index >= 0) {
                    val purchase = purchases[index]
                    if (!purchase.isAcknowledged) {
                        val ackParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(ackParams){}
                    }
                    true
                } else false
            }
            if (success) {
                isOrderSuccess = true
            } else {
                queryPurchasesHistory(billingClient)
            }
        }
    }

    private fun queryPurchasesHistory(billingClient: BillingClient) {
        MainScope().launch {
            val queryPurchaseHistoryParams = QueryPurchaseHistoryParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            val result = billingClient.queryPurchaseHistory(queryPurchaseHistoryParams)
            isOrderSuccess = if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                !result.purchaseHistoryRecordList.isNullOrEmpty()
            } else {
                false
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        val builder = OkHttpClient.Builder()
            .cookieJar(AndroidCookieJar)
            .addNetworkInterceptor(RequestHeaderInterceptor())
            .addInterceptor(ProgressInterceptor())
        if (Settings.isBypassWAF) {
            builder.addInterceptor(CloudflareInterceptor(this))
        }
        if (Settings.isDohEnable) {
            builder.dns(Settings.doh)
        }
        return ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(CACHE_MAX_PERCENT)
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(1.0)
                    .build()
            }
            .allowHardware(false)
            .okHttpClient(builder.build())
            .crossfade(true)
            .build()
    }
}