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

package onlymash.flexbooru.repository.pool

import onlymash.flexbooru.entity.pool.PoolDan
import onlymash.flexbooru.entity.pool.PoolDanOne
import onlymash.flexbooru.entity.pool.PoolMoe
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.entity.pool.PoolSankaku
import onlymash.flexbooru.repository.Listing

interface PoolRepository {
    fun getDanPools(search: Search): Listing<PoolDan>
    fun getMoePools(search: Search): Listing<PoolMoe>
    fun getDanOnePools(search: Search): Listing<PoolDanOne>
    fun getSankakuPools(search: Search): Listing<PoolSankaku>
}