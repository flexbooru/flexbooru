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

package onlymash.flexbooru.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import onlymash.flexbooru.common.Settings
import onlymash.flexbooru.data.model.app.OrderResponse
import onlymash.flexbooru.extension.NetResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OrderApi {

    companion object {

        suspend fun orderChecker(orderId: String, deviceId: String) {
            withContext(Dispatchers.IO) {
                try {
                    val response = createApi<OrderApi>().checker(orderId, deviceId)
                    val data = response.body()
                    if (response.isSuccessful && data != null) {
                        if (data.success) {
                            Settings.isOrderSuccess = data.activated
                        } else {
                            Settings.isOrderSuccess = false
                            Settings.orderId = ""
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        suspend fun orderRegister(orderId: String, deviceId: String): NetResult<OrderResponse> {
            return withContext(Dispatchers.IO) {
                try {
                    val response = createApi<OrderApi>().register(orderId, deviceId)
                    val data = response.body()
                    if (response.isSuccessful && data != null) {
                        NetResult.Success(data)
                    } else {
                        NetResult.Error("code: ${response.code()}")
                    }
                } catch (e: Exception) {
                    NetResult.Error(e.toString())
                }
            }
        }
    }

    @GET("/order/checker.json")
    suspend fun checker(
        @Query("order_id") orderId: String,
        @Query("device_id") deviceId: String): Response<OrderResponse>

    @GET("/order/register.json")
    suspend fun register(
        @Query("order_id") orderId: String,
        @Query("device_id") deviceId: String): Response<OrderResponse>
}