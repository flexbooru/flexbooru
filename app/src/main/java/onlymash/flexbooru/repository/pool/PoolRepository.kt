package onlymash.flexbooru.repository.pool

import onlymash.flexbooru.entity.PoolDan
import onlymash.flexbooru.entity.PoolMoe
import onlymash.flexbooru.repository.Listing

interface PoolRepository {
    fun getDanPools(): Listing<PoolDan>
    fun getMoePools(): Listing<PoolMoe>
}