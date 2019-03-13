package onlymash.flexbooru.repository.browse

interface PostLoaderRepository {
    var postLoadedListener: PostLoadedListener?
    var postLoadedLiveDataListener: PostLoadedLiveDataListener?
    fun loadPosts(host: String, keyword: String, type: Int)
    fun loadPostsLiveData(host: String, keyword: String, type: Int)
}