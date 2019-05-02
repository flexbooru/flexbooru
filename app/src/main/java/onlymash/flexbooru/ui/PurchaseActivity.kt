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

package onlymash.flexbooru.ui

import android.os.Bundle
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_purchase.*
import kotlinx.android.synthetic.main.toolbar.*
import onlymash.flexbooru.R
import onlymash.flexbooru.Settings

class PurchaseActivity : BaseActivity(), PurchasesUpdatedListener {

    companion object {
        const val SKU = "flexbooru_pro"
    }
    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)
        toolbar.setTitle(R.string.purchase_title)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        billingClient = BillingClient
            .newBuilder(this)
            .setListener(this)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }
            override fun onBillingSetupFinished(responseCode: Int) {

            }
        })
        pay_google_play.setOnClickListener {
            if (billingClient.isReady) {
                val params = SkuDetailsParams
                    .newBuilder()
                    .setSkusList(listOf(SKU))
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
                billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                    if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                        val index = skuDetailsList.indexOfFirst { it.sku == SKU }
                        if (index >= 0) {
                            val billingFlowParams = BillingFlowParams
                                .newBuilder()
                                .setSkuDetails(skuDetailsList[index])
                                .build()
                            billingClient.launchBillingFlow(this, billingFlowParams)
                        }
                    }
                }
            }
        }
        pay_alipay.setOnClickListener {

        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            val index = purchases.indexOfFirst { it.sku == SKU }
            if (index >= 0) {
                val purchase = purchases[index]
                Settings.instance().orderId = purchase.orderId
                Settings.instance().orderTime = purchase.purchaseTime
                Settings.instance().orderToken = purchase.purchaseToken
                Settings.instance().isOrderSuccess = true
            }
        } else if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED) {
            Settings.instance().isOrderSuccess = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
