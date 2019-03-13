package onlymash.flexbooru.entity.tag

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "tags")
data class TagGelResponse(
    @Attribute
    val type: String,
    @Element
    val tags: MutableList<TagGel>?
)