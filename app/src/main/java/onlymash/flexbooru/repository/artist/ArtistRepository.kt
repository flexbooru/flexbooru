package onlymash.flexbooru.repository.artist

import onlymash.flexbooru.entity.SearchArtist
import onlymash.flexbooru.entity.ArtistDan
import onlymash.flexbooru.entity.ArtistMoe
import onlymash.flexbooru.repository.Listing

interface ArtistRepository {
    fun getDanArtists(search: SearchArtist): Listing<ArtistDan>
    fun getMoeArtists(search: SearchArtist): Listing<ArtistMoe>
}