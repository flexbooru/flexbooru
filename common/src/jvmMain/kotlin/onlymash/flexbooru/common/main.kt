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

package onlymash.flexbooru.common

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import onlymash.flexbooru.common.di.kodeinCommon
import onlymash.flexbooru.common.saucenao.api.SauceNaoApi
import org.kodein.di.erased.instance

fun main() {
    val api by kodeinCommon.instance<SauceNaoApi>("SauceNaoApi")
    GlobalScope.launch {
        val response = try {
            api.searchByUrl(url = "https://i.pximg.net/img-master/img/2019/04/13/00/00/01/74170729_p0_master1200.jpg", apiKey = "bbfa3e504bacf627a130b07ac3ec2d1b94e3f2c4")
        } catch (ex: Exception) {
            println(ex.message)
            null
        }
        println(response.toString())
    }
    Thread.sleep(10000L) // block main thread for 10 seconds to keep JVM alive
}