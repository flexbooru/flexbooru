/*
 * Copyright (C) 2019. by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.util

import android.content.Context
import com.crashlytics.android.Crashlytics
import onlymash.flexbooru.ServiceLocator
import java.io.*

object FileUtil {
    fun copy(src: File, dst: File): Boolean = try {
        FileInputStream(src).use { inStream ->
            FileOutputStream(dst).use { outStream ->
                val inChannel = inStream.channel
                val outChannel = outStream.channel
                inChannel.transferTo(0, inChannel.size(), outChannel)
                inChannel.close()
                outChannel.close()
            }
        }
        true
    } catch (ex: IOException) {
        Crashlytics.log(ex.message)
        false
    }
}

fun Context.trimCache() {
    ServiceLocator.instance().getDiskIOExecutor().execute {
        try {
            cacheDir?.deleteAll()
        } catch (_: IOException) {}
    }
}

private fun File.deleteAll() {
    if (isDirectory) {
        list().forEach {
            File(this, it).deleteAll()
        }
    } else {
        delete()
    }
}
