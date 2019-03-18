package onlymash.flexbooru

import android.app.Application
import com.squareup.leakcanary.LeakCanary

fun Application.setupLeakCanary() {
    if (LeakCanary.isInAnalyzerProcess(this)) {
        // This process is dedicated to LeakCanary for heap analysis.
        // You should not init your app in this process.
        return
    }
    LeakCanary.install(this)
}
