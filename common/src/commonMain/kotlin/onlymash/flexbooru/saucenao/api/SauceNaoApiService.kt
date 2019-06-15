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
        byteArray: ByteArray
    ): SauceNaoResponse =
        client.submitForm {
            body = MultiPartFormDataContent(
                formData {
                    append(
                        "file",
                        byteArray,
                        Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=image.png")
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