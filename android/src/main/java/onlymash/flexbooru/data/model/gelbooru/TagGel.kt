/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.data.model.gelbooru

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml
import onlymash.flexbooru.common.Values.BOORU_TYPE_GEL
import onlymash.flexbooru.data.model.common.Tag

@Xml(name = "tag")
data class TagGel(
    @Attribute(name = "id")
    val id: Int,
    @Attribute(name = "name")
    val name: String,
    @Attribute(name = "count")
    val count: Int,
    @Attribute(name = "type")
    val type: Int,
    @Attribute(name = "ambiguous")
    val ambiguous: Boolean
) {
    fun toTag(): Tag {
        return Tag(
            booruType = BOORU_TYPE_GEL,
            id = id,
            name = name,
            category = type,
            count = count
        )
    }
}