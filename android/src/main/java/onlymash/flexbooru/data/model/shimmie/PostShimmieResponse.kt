package onlymash.flexbooru.data.model.shimmie

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "posts")
data class PostShimmieResponse(
    @Attribute(name = "offset")
    val offset: Int,
    @Attribute(name = "count")
    val count: Int,
    @Element
    val posts: List<PostShimmie>? = null
)