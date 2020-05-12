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

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import onlymash.flexbooru.BuildConfig
import onlymash.flexbooru.app.Settings
import onlymash.flexbooru.app.Values
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

fun createHttpClient(isSankaku: Boolean): OkHttpClient {
    val builder = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)

    if (Settings.isDohEnable) {
        builder.dns(Settings.doh)
    }

    if (isSankaku) {
        builder.addInterceptor(ApiSankakuInterceptor())
    } else {
        builder.addInterceptor(ApiInterceptor())
    }

    if (BuildConfig.DEBUG) {
        val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d("Api", message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        builder.addInterceptor(logger)
    }

    return builder.build()
}

inline fun <reified T> createApi(): T {
    val classJava = T::class.java
    val baseUrl = when (classJava) {
        AppUpdaterApi::class.java -> "https://raw.githubusercontent.com"
        OrderApi::class.java -> "https://flexbooru-pay.fiepi.com"
        else -> Values.BASE_URL
    }
    val converterFactory = when (classJava) {
        GelbooruApi::class.java,
        ShimmieApi::class.java-> {
            val tikXml = TikXml.Builder()
                .exceptionOnUnreadXml(false)
                .build()
            TikXmlConverterFactory.create(tikXml)
        }
        else -> {
            val contentType = "application/json".toMediaType()
            val jsonConfiguration = JsonConfiguration(
                ignoreUnknownKeys = true,
                isLenient = true
            )
            Json(jsonConfiguration).asConverterFactory(contentType)
        }
    }
    val isSankaku = classJava == SankakuApi::class.java
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(createHttpClient(isSankaku))
        .addConverterFactory(converterFactory)
        .build()
        .create(classJava)
}