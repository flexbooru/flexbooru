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

import okhttp3.Interceptor
import okhttp3.Response
import onlymash.flexbooru.app.Keys
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.extension.userAgent

class ApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requests =  chain.request().newBuilder()
            .removeHeader(Keys.HEADER_USER_AGENT)
            .addHeader(Keys.HEADER_USER_AGENT, userAgent)
            .build()
        return chain.proceed(requests)
    }
}

class ApiSankakuInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requests =  chain.request().newBuilder()
            .removeHeader(Keys.HEADER_USER_AGENT)
            .addHeader(Keys.HEADER_USER_AGENT, Values.PC_USER_AGENT)
            .build()
        return chain.proceed(requests)
    }
}