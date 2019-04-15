package onlymash.flexbooru

import android.os.Environment
import java.io.File

object Path {
    private fun getRootDir(): File {
        val path = Settings.instance().downloadDirPath
        if (path.isNullOrEmpty()) {
            return getDefaultRootDir()
        }
        return File(path)
    }
    fun getDefaultRootDir(): File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), App.app.getString(R.string.app_name))

    fun getSaveDir(): File {
        val dir = File(getRootDir(), "save")
        if (!dir.exists()) {
            dir.mkdirs()
        } else if (dir.isFile) {
            dir.delete()
            dir.mkdirs()
        }
        return dir
    }

    fun getDownloadFilePath(host: String, fileName: String): File =
        File(getRootDir(), "$host/$fileName")
}