package onlymash.flexbooru.crash

import android.content.Context
import java.io.File
import java.io.FileFilter

class CrashFile {

    fun getLogFile(context: Context): Array<File>? = try {
        getLogDir(context)?.listFiles(CrashLogFilter())
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    inner class CrashLogFilter : FileFilter {

        override fun accept(file: File): Boolean {
            return file.name.endsWith(".log")
        }
    }

    fun getLogDir(context: Context): File? {
        val path = context.getExternalFilesDir("logs")?.absolutePath ?: return null
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        } else {
            dir.delete()
            dir.mkdirs()
        }
        return dir
    }
}