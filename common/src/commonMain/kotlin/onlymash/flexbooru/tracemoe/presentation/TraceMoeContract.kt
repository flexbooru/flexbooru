package onlymash.flexbooru.tracemoe.presentation

import onlymash.flexbooru.tracemoe.model.TraceResponse
import onlymash.flexbooru.common.BaseView

interface TraceMoeView : BaseView {
    fun onUpdate(data: TraceResponse)
}

interface TraceMoeActions {
    fun onRequestData(base64Image: String)
}