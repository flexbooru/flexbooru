/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.viewModelScope
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.TagBlacklist
import onlymash.flexbooru.repository.post.PostRepository

data class SearchData(
    val search: Search,
    val tagBlacklists: MutableList<TagBlacklist>
)

class PostViewModel(private val repo: PostRepository): ScopeViewModel() {

    private val searchData = MutableLiveData<SearchData>()
    private val danOneRepoResult = map(searchData) { search ->
        repo.getDanOnePosts(viewModelScope, search.search, search.tagBlacklists)
    }
    val postsDanOne = switchMap(danOneRepoResult) { it.pagedList }
    val networkStateDanOne = switchMap(danOneRepoResult) { it.networkState }
    val refreshStateDanOne = switchMap(danOneRepoResult) { it.refreshState }
    private val danRepoResult = map(searchData) { search ->
        repo.getDanPosts(viewModelScope, search.search, search.tagBlacklists)
    }
    val postsDan = switchMap(danRepoResult) { it.pagedList }
    val networkStateDan = switchMap(danRepoResult) { it.networkState }
    val refreshStateDan = switchMap(danRepoResult) { it.refreshState }
    private val moeRepoResult = map(searchData) { search ->
        repo.getMoePosts(viewModelScope, search.search, search.tagBlacklists)
    }
    val postsMoe = switchMap(moeRepoResult) { it.pagedList }
    val networkStateMoe = switchMap(moeRepoResult) { it.networkState }
    val refreshStateMoe = switchMap(moeRepoResult) { it.refreshState }
    private val gelRepoResult = map(searchData) { search ->
        repo.getGelPosts(viewModelScope, search.search, search.tagBlacklists)
    }
    val postsGel = switchMap(gelRepoResult) { it.pagedList }
    val networkStateGel = switchMap(gelRepoResult) { it.networkState }
    val refreshStateGel = switchMap(gelRepoResult) { it.refreshState }
    private val sankakuRepoResult = map(searchData) { search ->
        repo.getSankakuPosts(viewModelScope, search.search, search.tagBlacklists)
    }
    val postsSankaku = switchMap(sankakuRepoResult) { it.pagedList }
    val networkStateSankaku = switchMap(sankakuRepoResult) { it.networkState }
    val refreshStateSankaku = switchMap(sankakuRepoResult) { it.refreshState }
    fun show(search: Search, tagBlacklists: MutableList<TagBlacklist>): Boolean {
        val value = SearchData(
            search = search,
            tagBlacklists = tagBlacklists
        )
        if (searchData.value == value) {
            return false
        }
        searchData.value = value
        return true
    }
    fun refreshDanOne() {
        danOneRepoResult.value?.refresh?.invoke()
    }
    fun refreshDan() {
        danRepoResult.value?.refresh?.invoke()
    }
    fun refreshMoe() {
        moeRepoResult.value?.refresh?.invoke()
    }
    fun retryDanOne() {
        danOneRepoResult.value?.retry?.invoke()
    }
    fun retryDan() {
        danRepoResult.value?.retry?.invoke()
    }
    fun retryMoe() {
        moeRepoResult.value?.retry?.invoke()
    }
    fun refreshGel() {
        gelRepoResult.value?.refresh?.invoke()
    }
    fun retryGel() {
        gelRepoResult.value?.retry?.invoke()
    }
    fun refreshSankaku() {
        sankakuRepoResult.value?.refresh?.invoke()
    }
    fun retrySankaku() {
        sankakuRepoResult.value?.retry?.invoke()
    }
}