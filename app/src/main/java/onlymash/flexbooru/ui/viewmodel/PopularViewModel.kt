package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import onlymash.flexbooru.entity.SearchPopular
import onlymash.flexbooru.repository.popular.PopularRepository

class PopularViewModel(private val repo: PopularRepository) : ViewModel() {
    private val popularData = MutableLiveData<SearchPopular>()
    private val danRepoResult = map(popularData) { popular ->
        repo.getDanPopular(popular)
    }
    private val moeRepoResult = map(popularData) { popular ->
        repo.getMoePopular(popular)
    }
    val postsDan = Transformations.switchMap(danRepoResult) { it.pagedList }!!
    val networkStateDan = Transformations.switchMap(danRepoResult) { it.networkState }!!
    val refreshStateDan = Transformations.switchMap(danRepoResult) { it.refreshState }!!

    val postsMoe = Transformations.switchMap(moeRepoResult) { it.pagedList }!!
    val networkStateMoe = Transformations.switchMap(moeRepoResult) { it.networkState }!!
    val refreshStateMoe = Transformations.switchMap(moeRepoResult) { it.refreshState }!!

    fun show(popular: SearchPopular): Boolean {
        if (popularData.value == popular) {
            return false
        }
        popularData.value = popular
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