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

package onlymash.flexbooru.api

import android.util.Log
import androidx.annotation.Keep
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.Constants
import onlymash.flexbooru.Settings
import onlymash.flexbooru.util.UserAgent
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@Keep
data class OrderResponse(
    val success: Boolean,
    val activated: Boolean
)

@Keep
interface OrderApi {
    companion object {
        private const val BASE_URL = "https://flexbooru-pay.fiepi.com"
        private fun create(): OrderApi {

            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { log ->
                Log.d("OrderApi", log)
            }).apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val interceptor = Interceptor { chain ->
                val requests =  chain.request().newBuilder()
                    .removeHeader(Constants.USER_AGENT_KEY)
                    .addHeader(Constants.USER_AGENT_KEY, UserAgent.get())
                    .build()
                chain.proceed(requests)
            }

            val client = OkHttpClient.Builder().apply {
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .addInterceptor(logger)
            }
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OrderApi::class.java)
        }

        fun orderChecker(orderId: String, deviceId: String) {
            create().checker(orderId, deviceId).enqueue(object : retrofit2.Callback<OrderResponse> {
                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {

                }

                override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                    val data = response.body() ?: return
                    if (data.success) {
                        Settings.isOrderSuccess = data.activated
                    } else {
                        Settings.isOrderSuccess = false
                        Settings.orderId = ""
                    }
                }
            })
        }
        fun orderRegister(orderId: String, deviceId: String, success: (Boolean) -> Unit) {
            create().register(orderId, deviceId).enqueue(object : retrofit2.Callback<OrderResponse> {
                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    success(false)
                }

                override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                    val data = response.body() ?: return
                    if (data.success) {
                        success(true)
                        Settings.isOrderSuccess = data.activated
                        Settings.orderId = orderId
                    } else {
                        success(false)
                        Settings.isOrderSuccess = false
                        Settings.orderId = ""
                    }
                }
            })
        }
    }

    @GET("/order/checker.json")
    fun checker(
        @Query("order_id") orderId: String,
        @Query("device_id") deviceId: String): Call<OrderResponse>

    @GET("/order/register.json")
    fun register(
        @Query("order_id") orderId: String,
        @Query("device_id") deviceId: String): Call<OrderResponse>
}