package onlymash.flexbooru.saucenao.api

import onlymash.flexbooru.saucenao.model.SauceNaoResponse

interface SauceNaoApi {

    suspend fun searchByUrl(
        url: String,
        apiKey: String): SauceNaoResponse
}