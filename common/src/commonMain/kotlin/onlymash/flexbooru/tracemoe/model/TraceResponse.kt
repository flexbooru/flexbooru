package onlymash.flexbooru.tracemoe.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TraceResponse(
    @SerialName("CacheHit")
    val cacheHit: Boolean,
    @SerialName("docs")
    var docs: List<Doc> = emptyList(),
    @SerialName("limit")
    val limit: Int,
    @SerialName("limit_ttl")
    val limitTtl: Int,
    @SerialName("quota")
    val quota: Int,
    @SerialName("quota_ttl")
    val quotaTtl: Int,
    @SerialName("RawDocsCount")
    val rawDocsCount: Int,
    @SerialName("RawDocsSearchTime")
    val rawDocsSearchTime: Long,
    @SerialName("ReRankSearchTime")
    val reRankSearchTime: Long,
    @SerialName("trial")
    val trial: Int
)