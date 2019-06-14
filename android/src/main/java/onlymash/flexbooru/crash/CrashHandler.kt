package onlymash.flexbooru.crash

import android.annotation.SuppressLint
import android.content.Context

import java.io.File

class CrashHandler : CrashLog() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: CrashHandler? = null
        fun getInstance(): CrashHandler {
            if (instance == null) {
                instance = CrashHandler()
            }
            return instance!!
        }
    }

    override fun initParams(context: Context, crashLog: CrashLog) {
        crashLog.setCrashLogPath(context.getExternalFilesDir("logs")!!.absolutePath)
        crashLog.setCrashLogLimit(5)
    }

    override fun sendCrashLog(context: Context, folder: File, file: File) {

    }
}