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
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.data.model.app.UpdateInfo
import retrofit2.Response
import retrofit2.http.GET

/**
 * App update api
 * */
interface AppUpdaterApi {

    companion object {

        suspend fun checkUpdate() {
            withContext(Dispatchers.IO) {
                try {
                    val response = createApi<AppUpdaterApi>().checkUpdate()
                    val data = response.body()
                    if (response.isSuccessful && data != null) {
                        Settings.latestVersionUrl = data.url
                        Settings.latestVersionName = data.versionName
                        Settings.latestVersionCode = data.versionCode
                        Settings.isAvailableOnStore = data.isAvailableStore
                    }
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * check app new version
     * */
    @GET("/flexbooru/flexbooru/master/update.json")
    suspend fun checkUpdate(): Response<UpdateInfo>
}