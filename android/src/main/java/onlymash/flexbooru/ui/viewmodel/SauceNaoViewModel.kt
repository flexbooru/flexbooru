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