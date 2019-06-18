package onlymash.flexbooru.crash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import onlymash.flexbooru.extension.getUriForFile

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
        context.startActivity(
            Intent.createChooser(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, context.getUriForFile(file))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Send crash log"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}