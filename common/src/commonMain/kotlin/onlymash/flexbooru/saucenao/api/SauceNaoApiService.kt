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

package onlymash.flexbooru.saucenao.api

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.takeFrom
import onlymash.flexbooru.saucenao.model.SauceNaoResponse

class SauceNaoApiService(
    private val client: HttpClient,
    private val baseUrl: String) : SauceNaoApi {

    override suspend fun searchByUrl(
        url: String,
        apiKey: String): SauceNaoResponse =

        client.get{
            apiUrl(
                baseUrl = baseUrl,
                path = "search.php",
                apiKey = apiKey,
                url = url
            )
        }

    override suspend fun searchByImage(
        apiKey: String,
        byteArray: ByteArray,
        fileExt: String
    ): SauceNaoResponse =
        client.submitForm {
            body = MultiPartFormDataContent(
                formData {
                    append(
                        "file",
                        byteArray,
                        Headers.build {
                            append(HttpHeaders.ContentType, "image/$fileExt")
                            append(HttpHeaders.ContentDisposition, "filename=image.$fileExt")
                        }
                    )
                }
            )
            method = HttpMethod.Post
            apiUrl(
                baseUrl = baseUrl,
                path = "search.php",
                apiKey = apiKey
            )
        }

    private fun HttpRequestBuilder.apiUrl(
        baseUrl: String,
        path: String,
        apiKey: String,
        url: String? = null
    ) {
        url {
            takeFrom(baseUrl)
            encodedPath = path
            parameter("db", 999)
            parameter("output_type", 2)
            parameter("api_key", apiKey)
            if (!url.isNullOrEmpty()) {
                parameter("url", url)
            }
        }
    }
}