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