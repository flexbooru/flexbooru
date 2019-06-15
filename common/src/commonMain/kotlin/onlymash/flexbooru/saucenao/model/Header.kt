package onlymash.flexbooru.saucenao.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Header(

    @SerialName("user_id")
    val userId: String,

    @SerialName("account_type")
    val accountType: String,

    @SerialName("short_limit")
    val shortLimit: String,

    @SerialName("long_limit")
    val longLimit: String,

    @SerialName("long_remaining")
    val longRemaining: Int,

    @SerialName("short_remaining")
    val shortRemaining: Int,

    @SerialName("status")
    val status: Int,

    @SerialName("results_requested")
    val resultsRequested: String,

//    @SerialName("index")
//    val index: List<HeaderIndex>,

    @SerialName("search_depth")
    var searchDepth: String? = null,

    @SerialName("minimum_similarity")
    var minimumSimilarity: Double = 0.0,

    @SerialName("query_image_display")
    var queryImageDisplay: String? = null,

    @SerialName("query_image")
    val queryImage: String? = null,

    @SerialName("results_returned")
    var resultsReturned: Int = 0
)