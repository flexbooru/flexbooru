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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import kotlinx.coroutines.launch
import onlymash.flexbooru.R
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.databinding.ActivityPurchaseHistoryBinding
import onlymash.flexbooru.extension.formatDate
import onlymash.flexbooru.ui.base.BaseActivity
import onlymash.flexbooru.ui.viewbinding.viewBinding

class PurchaseHistoryActivity : BaseActivity() {

    private val binding by viewBinding(ActivityPurchaseHistoryBinding::inflate)

    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.purchase_history_title)
        }
        initBillingClient()
    }

    private fun initBillingClient() {
        billingClient = BillingClient
            .newBuilder(this)
            .enablePendingPurchases()
            .setListener { _, _ ->  }
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    queryPurchaseHistory()
                } else {
                    connectFailed()
                }
            }
            override fun onBillingServiceDisconnected() {
                connectFailed()
                billingClient.endConnection()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun queryPurchaseHistory() {
        lifecycleScope.launch {
            val queryPurchaseHistoryParams = QueryPurchaseHistoryParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            val result = billingClient.queryPurchaseHistory(queryPurchaseHistoryParams)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val records = result.purchaseHistoryRecordList
                binding.progressBar.isVisible = false
                binding.contentCard.isVisible = true
                if (records.isNullOrEmpty()) {
                    binding.content.text = "Not history found"
                } else {
                    Settings.isOrderSuccess = true
                    val content = StringBuilder()
                    val record = records[0]
                    val products = record.products
                    if (products.isNotEmpty()) {
                        content.append("Name: ${products[0]}\n\n")
                    }
                    val date = record.purchaseTime.formatDate(Values.DATE_PATTERN)
                    content.append("Date: $date")
                    binding.content.text = content
                }
            } else {
                connectFailed()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun connectFailed() {
        binding.progressBar.isVisible = false
        binding.contentCard.isVisible = true
        binding.content.text = "Connect failed"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
