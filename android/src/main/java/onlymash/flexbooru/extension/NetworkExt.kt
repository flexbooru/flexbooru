/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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
