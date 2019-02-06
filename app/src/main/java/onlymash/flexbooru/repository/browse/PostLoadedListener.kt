package onlymash.flexbooru.repository.browse

import onlymash.flexbooru.model.PostDan
import onlymash.flexbooru.model.PostMoe

interface PostLoadedListener {
    fun onDanItemsLoaded(posts: MutableList<PostDan>)
    fun onMoeItemsLoaded(posts: MutableList<PostMoe>)
}
