package onlymash.flexbooru.repository.pool

import onlymash.flexbooru.entity.PoolDan
import onlymash.flexbooru.entity.PoolMoe
import onlymash.flexbooru.entity.Search
import onlymash.flexbooru.repository.Listing

interface PoolRepository {
    fun getDanPools(search: Search): Listing<PoolDan>
    fun getMoePools(search: Search): Listing<PoolMoe>
}