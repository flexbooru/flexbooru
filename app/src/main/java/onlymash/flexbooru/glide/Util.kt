/*
 * Copyright (C) 2019 by onlymash <im@fiepi.me>, All rights reserved
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

package onlymash.flexbooru.glide

import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import onlymash.flexbooru.Constants
import onlymash.flexbooru.util.UserAgent

val glideHeader: Headers
    get() = LazyHeaders.Builder()
        .addHeader(Constants.USER_AGENT_KEY, UserAgent.get())
        .build()