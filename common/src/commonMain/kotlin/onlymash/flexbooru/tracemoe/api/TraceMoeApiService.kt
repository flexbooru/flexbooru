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

package onlymash.flexbooru.tracemoe.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.http.HttpMethod
import io.ktor.http.takeFrom
import onlymash.flexbooru.tracemoe.model.TraceResponse

class TraceMoeApiService(
    private val client: HttpClient,
    private val baseUrl: String
) : TraceMoeApi {

    override suspend fun fetch(base64Image: String): TraceResponse =
        client.submitForm {
            body = MultiPartFormDataContent(
                formData {
                    append("image", base64Image)
                }
            )
            method = HttpMethod.Post
            apiUrl(baseUrl, "api/search")
        }

    private fun HttpRequestBuilder.apiUrl(
        baseUrl: String,
        path: String
    ) {
        url {
            takeFrom(baseUrl)
            encodedPath = path
        }
    }
}