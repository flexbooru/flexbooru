/*
 * Copyright (C) 2020. by onlymash <im@fiepi.me>, All rights reserved
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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.saucenao.api.SauceNaoApi
import onlymash.flexbooru.saucenao.model.SauceNaoResponse

class SauceNaoViewModel(private val sauceNaoApi: SauceNaoApi) : ScopeViewModel() {

    private val mutex = Mutex()

    val data = MutableLiveData<SauceNaoResponse>()
    val isLoading = MutableLiveData(false)
    val error = MutableLiveData<String>()

    fun searchByUrl(imageUrl: String, apiKey: String) {
        viewModelScope.launch {
            mutex.withLock {
                isLoading.postValue(true)
                val result = withContext(Dispatchers.IO) {
                    try {
                        val response = sauceNaoApi.searchByUrl(imageUrl, apiKey)
                        NetResult.Success(response)
                    } catch (e: Exception) {
                        NetResult.Error(e.message.toString())
                    }
                }
                isLoading.postValue(false)
                when (result) {
                    is NetResult.Success -> {
                        data.postValue(result.data)
                    }
                    is NetResult.Error -> {
                        error.postValue(result.errorMsg)
                    }
                }
            }
        }
    }

    fun searchByImage(byteArray: ByteArray, fileExt: String, apiKey: String) {
        viewModelScope.launch {
            mutex.withLock {
                isLoading.postValue(true)
                val result = withContext(Dispatchers.IO) {
                    try {
                        val response = sauceNaoApi.searchByImage(apiKey, byteArray, fileExt)
                        NetResult.Success(response)
                    } catch (e: Exception) {
                        NetResult.Error(e.message.toString())
                    }
                }
                isLoading.postValue(false)
                when (result) {
                    is NetResult.Success -> {
                        data.postValue(result.data)
                    }
                    is NetResult.Error -> {
                        error.postValue(result.errorMsg)
                    }
                }
            }
        }
    }
}