package onlymash.flexbooru.repository.browse

import onlymash.flexbooru.entity.PostDan
import onlymash.flexbooru.entity.PostMoe

interface PostLoadedListener {
    fun onDanItemsLoaded(posts: MutableList<PostDan>)
    fun onMoeItemsLoaded(posts: MutableList<PostMoe>)
}
