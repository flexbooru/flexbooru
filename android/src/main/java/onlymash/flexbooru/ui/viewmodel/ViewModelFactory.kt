package onlymash.flexbooru.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import onlymash.flexbooru.data.database.dao.BooruDao
import onlymash.flexbooru.data.repository.artist.ArtistRepository
import onlymash.flexbooru.data.repository.pool.PoolRepository
import onlymash.flexbooru.data.repository.post.PostRepository
import onlymash.flexbooru.data.repository.suggestion.SuggestionRepository
import onlymash.flexbooru.data.repository.tag.TagRepository
import onlymash.flexbooru.data.repository.tagfilter.TagFilterRepository
import onlymash.flexbooru.extension.getViewModel


fun ViewModelStoreOwner.getBooruViewModel(booruDao: BooruDao): BooruViewModel =
    getViewModel(object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BooruViewModel(booruDao) as T
        }
    })

fun ViewModelStoreOwner.getPostViewModel(repository: PostRepository): PostViewModel =
    getViewModel(object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(repository) as T
        }
    })

fun ViewModelStoreOwner.getSuggestionViewModel(repository: SuggestionRepository): SuggestionViewModel =
    getViewModel(object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SuggestionViewModel(repository) as T
        }
    })

fun ViewModelStoreOwner.getTagFilterViewModel(repository: TagFilterRepository): TagFilterViewModel =
    getViewModel(object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return TagFilterViewModel(repository) as T
        }
    })

fun ViewModelStoreOwner.getPoolViewModel(repository: PoolRepository): PoolViewModel =
    getViewModel(object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PoolViewModel(repository) as T
        }
    })

fun ViewModelStoreOwner.getTagViewModel(repository: TagRepository): TagViewModel =
    getViewModel(object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return TagViewModel(repository) as T
        }
    })

fun ViewModelStoreOwner.getArtistViewModel(repository: ArtistRepository): ArtistViewModel =
    getViewModel(object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ArtistViewModel(repository) as T
        }
    })