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