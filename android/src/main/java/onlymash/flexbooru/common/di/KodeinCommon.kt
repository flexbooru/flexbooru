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

package onlymash.flexbooru.common.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import onlymash.flexbooru.common.saucenao.api.SauceNaoApi
import onlymash.flexbooru.common.saucenao.api.SauceNaoApiService
import onlymash.flexbooru.common.tracemoe.api.TraceMoeApi
import onlymash.flexbooru.common.tracemoe.api.TraceMoeApiService
import org.koin.dsl.module

private val json get() = Json {
    isLenient = true
    ignoreUnknownKeys = true
    allowSpecialFloatingPointValues = true
    useArrayPolymorphism = true
}

private val httpClient get() = HttpClient(OkHttp) {
    install(ContentNegotiation) {
        json(json)
    }
}

private val sauceNaoApi: SauceNaoApi get() = SauceNaoApiService(httpClient, "https://saucenao.com")
private val traceMoeApi: TraceMoeApi get() = TraceMoeApiService(httpClient, "https://api.trace.moe")

val commonModules = module {
    single<SauceNaoApi> { sauceNaoApi }
    single<TraceMoeApi> { traceMoeApi }
}