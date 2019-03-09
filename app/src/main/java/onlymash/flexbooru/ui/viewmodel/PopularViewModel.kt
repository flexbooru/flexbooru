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
import onlymash.flexbooru.entity.SearchPopular
import onlymash.flexbooru.repository.popular.PopularRepository

class PopularViewModel(private val repo: PopularRepository) : ViewModel() {
    private val popularData = MutableLiveData<SearchPopular>()
    private val danRepoResult = map(popularData) { popular ->
        repo.getDanPopular(popular)
    }
    private val danOneRepoResult = map(popularData) { popular ->
        repo.getDanOnePopular(popular)
    }
    private val moeRepoResult = map(popularData) { popular ->
        repo.getMoePopular(popular)
    }
    val postsDan = Transformations.switchMap(danRepoResult) { it.pagedList }!!
    val networkStateDan = Transformations.switchMap(danRepoResult) { it.networkState }!!
    val refreshStateDan = Transformations.switchMap(danRepoResult) { it.refreshState }!!

    val postsDanOne = Transformations.switchMap(danOneRepoResult) { it.pagedList }!!
    val networkStateDanOne = Transformations.switchMap(danOneRepoResult) { it.networkState }!!
    val refreshStateDanOne = Transformations.switchMap(danOneRepoResult) { it.refreshState }!!

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

    fun refreshDanOne() {
        danOneRepoResult.value?.refresh?.invoke()
    }

    fun refreshMoe() {
        moeRepoResult.value?.refresh?.invoke()
    }

    fun retryDan() {
        danRepoResult?.value?.retry?.invoke()
    }

    fun retryDanOne() {
        danOneRepoResult?.value?.retry?.invoke()
    }

    fun retryMoe() {
        moeRepoResult?.value?.retry?.invoke()
    }
}