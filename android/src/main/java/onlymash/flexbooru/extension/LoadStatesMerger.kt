package onlymash.flexbooru.extension

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadState.NotLoading
import androidx.paging.LoadState.Loading
import androidx.paging.LoadStates
import androidx.paging.PagingSource.LoadResult.Error
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan
import onlymash.flexbooru.extension.MergedState.NOT_LOADING
import onlymash.flexbooru.extension.MergedState.REMOTE_ERROR
import onlymash.flexbooru.extension.MergedState.REMOTE_STARTED
import onlymash.flexbooru.extension.MergedState.SOURCE_ERROR
import onlymash.flexbooru.extension.MergedState.SOURCE_LOADING


//https://github.com/android/architecture-components-samples/blob/main/PagingWithNetworkSample/lib/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/paging/LoadStatesMerger.kt

/**
 * Converts the raw [CombinedLoadStates] [Flow] from [androidx.paging.PagingDataAdapter.loadStateFlow] into a new
 * [Flow] of [CombinedLoadStates] that track [CombinedLoadStates.mediator] states as they are
 * synchronously applied in the UI. Any [androidx.paging.LoadState.Loading] state triggered by [androidx.paging.RemoteMediator] will only
 * transition back to [androidx.paging.LoadState.NotLoading] after the fetched items have been synchronously shown in UI by a
 * successful [androidx.paging.PagingSource] load of type [androidx.paging.LoadType.REFRESH].
 *
 * Note: This class assumes that the [androidx.paging.RemoteMediator] implementation always invalidates
 * [androidx.paging.PagingSource] on a successful fetch, even if no data was modified (which Room does by default).
 * Using this class without this guarantee can cause [LoadState] to get indefinitely stuck as
 * [androidx.paging.LoadState.Loading] in cases where invalidation doesn't happen because the fetched network data represents
 * exactly what is already cached in DB.
 */
fun Flow<CombinedLoadStates>.asMergedLoadStates(): Flow<LoadStates> {
    val syncRemoteState = LoadStatesMerger()
    return scan(syncRemoteState.toLoadStates()) { _, combinedLoadStates ->
        syncRemoteState.updateFromCombinedLoadStates(combinedLoadStates)
        syncRemoteState.toLoadStates()
    }
}

/**
 * Track the combined [LoadState] of [RemoteMediator] and [PagingSource], so that each load type
 * is only set to [NotLoading] when [RemoteMediator] load is applied on presenter-side.
 */
private class LoadStatesMerger {
    var refresh: LoadState = NotLoading(endOfPaginationReached = false)
        private set
    var prepend: LoadState = NotLoading(endOfPaginationReached = false)
        private set
    var append: LoadState = NotLoading(endOfPaginationReached = false)
        private set
    var refreshState: MergedState = NOT_LOADING
        private set
    var prependState: MergedState = NOT_LOADING
        private set
    var appendState: MergedState = NOT_LOADING
        private set

    fun toLoadStates() = LoadStates(
        refresh = refresh,
        prepend = prepend,
        append = append
    )

    /**
     * For every new emission of [CombinedLoadStates] from the original [Flow], update the
     * [MergedState] of each [LoadType] and compute the new [LoadState].
     */
    fun updateFromCombinedLoadStates(combinedLoadStates: CombinedLoadStates) {
        computeNextLoadStateAndMergedState(
            sourceRefreshState = combinedLoadStates.source.refresh,
            sourceState = combinedLoadStates.source.refresh,
            remoteState = combinedLoadStates.mediator?.refresh,
            currentMergedState = refreshState,
        ).also {
            refresh = it.first
            refreshState = it.second
        }
        computeNextLoadStateAndMergedState(
            sourceRefreshState = combinedLoadStates.source.refresh,
            sourceState = combinedLoadStates.source.prepend,
            remoteState = combinedLoadStates.mediator?.prepend,
            currentMergedState = prependState,
        ).also {
            prepend = it.first
            prependState = it.second
        }
        computeNextLoadStateAndMergedState(
            sourceRefreshState = combinedLoadStates.source.refresh,
            sourceState = combinedLoadStates.source.append,
            remoteState = combinedLoadStates.mediator?.append,
            currentMergedState = appendState,
        ).also {
            append = it.first
            appendState = it.second
        }
    }

    /**
     * Compute which [LoadState] and [MergedState] to transition, given the previous and current
     * state for a particular [LoadType].
     */
    private fun computeNextLoadStateAndMergedState(
        sourceRefreshState: LoadState,
        sourceState: LoadState,
        remoteState: LoadState?,
        currentMergedState: MergedState,
    ): Pair<LoadState, MergedState> {
        if (remoteState == null) return sourceState to NOT_LOADING

        return when (currentMergedState) {
            NOT_LOADING -> when (remoteState) {
                is Loading -> Loading to REMOTE_STARTED
                is Error<*, *> -> remoteState to REMOTE_ERROR
                else -> NotLoading(remoteState.endOfPaginationReached) to NOT_LOADING
            }
            REMOTE_STARTED -> when {
                remoteState is Error<*, *> -> remoteState to REMOTE_ERROR
                sourceRefreshState is Loading -> Loading to SOURCE_LOADING
                else -> Loading to REMOTE_STARTED
            }
            REMOTE_ERROR -> when (remoteState) {
                is Error<*, *> -> remoteState to REMOTE_ERROR
                else -> Loading to REMOTE_STARTED
            }
            SOURCE_LOADING -> when {
                sourceRefreshState is Error<*, *> -> sourceRefreshState to SOURCE_ERROR
                remoteState is Error<*, *> -> remoteState to REMOTE_ERROR
                sourceRefreshState is NotLoading -> {
                    NotLoading(remoteState.endOfPaginationReached) to NOT_LOADING
                }
                else -> Loading to SOURCE_LOADING
            }
            SOURCE_ERROR -> when (sourceRefreshState) {
                is Error<*, *> -> sourceRefreshState to SOURCE_ERROR
                else -> sourceRefreshState to SOURCE_LOADING
            }
        }
    }
}

/**
 * State machine used to compute [LoadState] values in [LoadStatesMerger].
 *
 * This allows [LoadStatesMerger] to track whether to block transitioning to [NotLoading] from the
 * [Loading] state if it was triggered by [RemoteMediator], until [PagingSource] invalidates and
 * completes [REFRESH].
 */
private enum class MergedState {
    /**
     * Idle state; defer to remote state for endOfPaginationReached.
     */
    NOT_LOADING,

    /**
     * Remote load triggered; start listening for source refresh.
     */
    REMOTE_STARTED,

    /**
     * Waiting for remote in error state to get retried
     */
    REMOTE_ERROR,

    /**
     * Source refresh triggered by remote invalidation, once this completes we can be sure
     * the next generation was loaded.
     */
    SOURCE_LOADING,

    /**
     *  Remote load completed, but waiting for source refresh in error state to get retried.
     */
    SOURCE_ERROR,
}