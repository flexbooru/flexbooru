/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import onlymash.flexbooru.entity.tag.SearchTag
import onlymash.flexbooru.repository.tag.TagRepository

class TagViewModel(private val repo: TagRepository) : ViewModel() {
    private val searchData = MutableLiveData<SearchTag>()
    private val danRepoResult = map(searchData) {
        repo.getDanTags(it)
    }
    private val danOneRepoResult = map(searchData) {
        repo.getDanOneTags(it)
    }
    private val moeRepoResult = map(searchData) {
        repo.getMoeTags(it)
    }
    private val gelRepoResult = map(searchData) {
        repo.getGelTags(it)
    }
    val tagsDanOne = Transformations.switchMap(danOneRepoResult) { it.pagedList }
    val networkStateDanOne = Transformations.switchMap(danOneRepoResult) { it.networkState }
    val refreshStateDanOne = Transformations.switchMap(danOneRepoResult) { it.refreshState }

    val tagsDan = Transformations.switchMap(danRepoResult) { it.pagedList }
    val networkStateDan = Transformations.switchMap(danRepoResult) { it.networkState }
    val refreshStateDan = Transformations.switchMap(danRepoResult) { it.refreshState }

    val tagsMoe = Transformations.switchMap(moeRepoResult) { it.pagedList }
    val networkStateMoe = Transformations.switchMap(moeRepoResult) { it.networkState }
    val refreshStateMoe = Transformations.switchMap(moeRepoResult) { it.refreshState }

    val tagsGel = Transformations.switchMap(gelRepoResult) { it.pagedList }
    val networkStateGel = Transformations.switchMap(gelRepoResult) { it.networkState }
    val refreshStateGel = Transformations.switchMap(gelRepoResult) { it.refreshState }

    fun show(search: SearchTag): Boolean {
        if (searchData.value == search) {
            return false
        }
        searchData.value = search
        return true
    }

    fun refreshDan() {
        danRepoResult.value?.refresh?.invoke()
    }

    fun refreshDanOne() {
        danOneRepoResult.value?.refresh?.invoke()
    }

    fun refreshMoe() {
        moeRepoResult.value?.refresh?.invoke()
    }

    fun refreshGel() {
        gelRepoResult.value?.refresh?.invoke()
    }

    fun retryDan() {
        danRepoResult.value?.retry?.invoke()
    }

    fun retryDanOne() {
        danOneRepoResult.value?.retry?.invoke()
    }

    fun retryMoe() {
        moeRepoResult.value?.retry?.invoke()
    }

    fun retryGel() {
        gelRepoResult.value?.retry?.invoke()
    }
}