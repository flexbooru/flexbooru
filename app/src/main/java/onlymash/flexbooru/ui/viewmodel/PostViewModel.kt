package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import onlymash.flexbooru.entity.SearchPost
import onlymash.flexbooru.repository.post.PostRepository

class PostViewModel(private val repo: PostRepository): ViewModel() {
    private val searchData = MutableLiveData<SearchPost>()
    private val danRepoResult = map(searchData) { search ->
        repo.getDanPosts(search)
    }
    val postsDan = switchMap(danRepoResult) { it.pagedList }
    val networkStateDan = switchMap(danRepoResult) { it.networkState }
    val refreshStateDan = switchMap(danRepoResult) { it.refreshState }

    private val moeRepoResult = map(searchData) { search ->
        repo.getMoePosts(search)
    }
    val postsMoe = switchMap(moeRepoResult) { it.pagedList }
    val networkStateMoe = switchMap(moeRepoResult) { it.networkState }
    val refreshStateMoe = switchMap(moeRepoResult) { it.refreshState }

    fun show(search: SearchPost): Boolean {
        if (searchData.value == search) {
            return false
        }
        searchData.value = search
        return true
    }

    fun refreshDan() {
        danRepoResult.value?.refresh?.invoke()
    }

    fun refreshMoe() {
        moeRepoResult.value?.refresh?.invoke()
    }

    fun retryDan() {
        danRepoResult?.value?.retry?.invoke()
    }

    fun retryMoe() {
        moeRepoResult?.value?.retry?.invoke()
    }
}