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

package onlymash.flexbooru.ui.activity

import android.os.Bundle
import android.view.MenuItem
import com.android.billingclient.api.*
import onlymash.flexbooru.R
import onlymash.flexbooru.databinding.ActivityPurchaseHistoryBinding
import onlymash.flexbooru.ui.base.BaseActivity
import onlymash.flexbooru.ui.viewbinding.viewBinding

class PurchaseHistoryActivity : BaseActivity() {

    private val binding by viewBinding(ActivityPurchaseHistoryBinding::inflate)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.purchase_history_title)
        }
        checkOrderFromCache()
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
                    val queryPurchaseHistoryParams = QueryPurchaseHistoryParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                    billingClient.queryPurchaseHistoryAsync(queryPurchaseHistoryParams) { result, records ->
                        records?.forEach { record ->
                            binding.content.text = record.originalJson
                        }
                    }
                    billingClient.endConnection()
                }
            }
            override fun onBillingServiceDisconnected() {
                billingClient.endConnection()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
