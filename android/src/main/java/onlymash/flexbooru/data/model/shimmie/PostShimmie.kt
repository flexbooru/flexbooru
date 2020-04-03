package onlymash.flexbooru.data.model.shimmie

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml


@Xml(name = "post")
data class PostShimmie(
    @Attribute(name = "id")
    val id: Int,
    @Attribute(name = "preview_url")
    val previewUrl: String,
    @Attribute(name = "preview_width")
    val previewWidth: Int,
    @Attribute(name = "preview_height")
    val previewHeight: Int,
    @Attribute(name = "file_url")
    val fileUrl: String,
    @Attribute(name = "file_name")
    val fileName: String,
    @Attribute(name = "width")
    val width: Int,
    @Attribute(name = "height")
    val height: Int,
    @Attribute(name = "score")
    val score: Int,
    @Attribute(name = "tags")
    val tags: String,
    @Attribute(name = "date")
    val date: String,
    @Attribute(name = "author")
    val author: String,
    @Attribute(name = "rating")
    val rating: String,
    @Attribute(name = "")
    val source: String? = null
)