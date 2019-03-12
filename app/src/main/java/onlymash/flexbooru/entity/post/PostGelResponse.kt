package onlymash.flexbooru.entity.post

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "posts")
data class PostGelResponse (
    @Attribute
    val count: Int,
    @Attribute
    val offset: Int,
    @Element
    val posts: MutableList<PostGel>?
)