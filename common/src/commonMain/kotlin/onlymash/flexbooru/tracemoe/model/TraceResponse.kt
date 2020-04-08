/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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