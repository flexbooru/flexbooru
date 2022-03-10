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

package onlymash.flexbooru.common.saucenao.model


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