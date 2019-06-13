package onlymash.flexbooru.saucenao.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.takeFrom
import onlymash.flexbooru.saucenao.model.SauceNaoResponse

class SauceNaoApiService(
    private val client: HttpClient,
    private val host: String) : SauceNaoApi {

    override suspend fun searchByUrl(
        url: String,
        apiKey: String): SauceNaoResponse =
        client.get{
            apiUrl(
                host = host,
                path = "search.php",
                apiKey = apiKey,
                url = url
            )
        }

    private fun HttpRequestBuilder.apiUrl(
        host: String,
        path: String,
        apiKey: String,
        url: String
    ) {
        url {
            takeFrom(host)
            encodedPath = path
            parameter("db", 999)
            parameter("output_type", 2)
            parameter("api_key", apiKey)
            parameter("url", url)
        }
    }
}