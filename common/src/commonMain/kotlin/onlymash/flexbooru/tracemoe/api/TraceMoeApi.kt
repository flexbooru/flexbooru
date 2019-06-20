package onlymash.flexbooru.tracemoe.api

import onlymash.flexbooru.tracemoe.model.TraceResponse

interface TraceMoeApi {
    suspend fun fetch(base64Image: String): TraceResponse
}