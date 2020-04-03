package onlymash.flexbooru.data.model.shimmie

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml
import onlymash.flexbooru.data.model.common.Post
import onlymash.flexbooru.data.model.common.TagBase
import onlymash.flexbooru.data.model.common.User
import onlymash.flexbooru.data.utils.formatDateShimmie
import onlymash.flexbooru.data.utils.toSafeUrl


@Xml(name = "post")
data class PostShimmie(
    @Attribute(name = "id")
    val id: Int,
    @Attribute(name = "preview_url")
    val previewUrl: String?,
    @Attribute(name = "preview_width")
    val previewWidth: Int,
    @Attribute(name = "preview_height")
    val previewHeight: Int,
    @Attribute(name = "file_url")
    val fileUrl: String?,
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
) {
    fun toPost(booruUid: Long, query: String, scheme: String, host: String, index: Int): Post {
        return Post(
            booruUid = booruUid,
            query = query,
            index = index,
            id = id,
            width = width,
            height = height,
            size = 0,
            score = score,
            rating = rating,
            date = date.formatDateShimmie().toString(),
            tags = tags.split(" ").map { TagBase(it, -1) },
            preview = previewUrl?.toSafeUrl(scheme, host) ?: "",
            sample = fileUrl?.toSafeUrl(scheme, host) ?: "",
            medium = fileUrl?.toSafeUrl(scheme, host) ?: "",
            origin = fileUrl?.toSafeUrl(scheme, host) ?: "",
            source = source,
            uploader = User(id = 1, name = author),
            isFavored = false
        )
    }
}