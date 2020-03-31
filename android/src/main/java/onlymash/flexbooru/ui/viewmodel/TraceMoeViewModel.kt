package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import onlymash.flexbooru.extension.NetResult
import onlymash.flexbooru.tracemoe.api.TraceMoeApi
import onlymash.flexbooru.tracemoe.model.TraceResponse

class TraceMoeViewModel(private val traceMoeApi: TraceMoeApi) : ScopeViewModel() {

    private val mutex = Mutex()

    val data = MutableLiveData<TraceResponse>()
    val isLoading = MutableLiveData(false)
    val error = MutableLiveData<String>()

    fun fetch(base64ImageString: String) {
        viewModelScope.launch {
            mutex.withLock {
                isLoading.postValue(true)
                val result = withContext(Dispatchers.IO) {
                    try {
                        val response = traceMoeApi.fetch(base64ImageString)
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