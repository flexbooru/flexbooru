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

package onlymash.flexbooru.common.tracemoe.api

import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import onlymash.flexbooru.common.tracemoe.model.TraceResponse

class TraceMoeApiService(
    private val client: HttpClient,
    private val baseUrl: String
) : TraceMoeApi {

    override suspend fun fetch(imageBlob: ByteArray): TraceResponse =
        client.submitForm {
            val data = MultiPartFormDataContent(
                formData {
                    append(FormPart("image", "image.jpg"))
                    appendInput(
                        "image",
                        Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=image.jpg"
                            )
                        }) {
                        buildPacket { writeFully(imageBlob) }
                    }
                }
            )
            setBody(data)
            method = HttpMethod.Post
            apiUrl(baseUrl, "search")
        }.body()

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