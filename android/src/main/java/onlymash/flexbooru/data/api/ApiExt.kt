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

package onlymash.flexbooru.data.api

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.BuildConfig
import onlymash.flexbooru.app.App
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.okhttp.AndroidCookieJar
import onlymash.flexbooru.okhttp.CloudflareInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

fun createHttpClient(isSankaku: Boolean): OkHttpClient {
    val builder = OkHttpClient.Builder()
        .cookieJar(AndroidCookieJar)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)

    if (Settings.isDohEnable) {
        builder.dns(Settings.doh)
    }

    if (isSankaku) {
        builder.addInterceptor(ApiSankakuInterceptor())
    } else {
        builder.addInterceptor(ApiInterceptor())
    }

    if (Settings.isBypassWAF) {
        builder.addInterceptor(CloudflareInterceptor(App.app))
    }

    if (BuildConfig.DEBUG) {
        val logger = HttpLoggingInterceptor { message -> Log.d("Api", message) }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        builder.addInterceptor(logger)
    }

    return builder.build()
}

val defaultJson get() = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

inline fun <reified T> createApi(): T {
    val classJava = T::class
    val baseUrl = when (classJava) {
        AppUpdaterApi::class -> "https://raw.githubusercontent.com"
        OrderApi::class -> "https://flexbooru-pay.fiepi.com"
        else -> Values.BASE_URL
    }
    val converterFactory = when (classJava) {
        GelbooruApi::class,
        ShimmieApi::class-> {
            XML {
                policy = DefaultXmlSerializationPolicy(
                    pedantic = false,
                    autoPolymorphic = true,
                    unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                )
            }.asConverterFactory("application/xml".toMediaType())
        }
        else -> {
            defaultJson.asConverterFactory("application/json".toMediaType())
        }
    }
    val isSankaku = classJava == SankakuApi::class
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(createHttpClient(isSankaku))
        .addConverterFactory(converterFactory)
        .build()
        .create(classJava.java)
}