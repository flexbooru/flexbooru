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

package onlymash.flexbooru.extension

import android.os.Build
import onlymash.flexbooru.BuildConfig
import java.util.*

sealed class NetResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : NetResult<T>()
    data class Error(val errorMsg: String): NetResult<Nothing>()
    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$errorMsg]"
        }
    }
}


/**
 * Constructs a User-Agent string including application name and version,
 * system build version, model and Id
 * @return User-Agent string.
 */
fun getUserAgent(): String {

    val builder = StringBuilder().apply {
        append("Mozilla/5.0 (Linux; U; Android ")
        append(Build.VERSION.RELEASE)
        append("; ")
        append(Locale.getDefault().toString())
    }

    val model = Build.MODEL
    if (model.isNotEmpty()) {
        builder.apply {
            append("; ")
            append(model)
        }
    }

    val id = Build.ID
    if (id.isNotEmpty()) {
        builder.apply {
            append("; Build/")
            append(id)
        }
    }

    builder.apply {
        append("; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 ")
        append(BuildConfig.APPLICATION_ID)
        append("/")
        append(BuildConfig.VERSION_NAME)
        append(" Mobile Safari/537.36")
    }

    return builder.toString()
}