package onlymash.flexbooru.saucenao.api

import onlymash.flexbooru.saucenao.model.SauceNaoResponse

interface SauceNaoApi {

    suspend fun searchByUrl(
        url: String,
        apiKey: String): SauceNaoResponse

    suspend fun searchByImage(
        apiKey: String,
        byteArray: ByteArray,
        fileExt: String
    ): SauceNaoResponse
}