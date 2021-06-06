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

package onlymash.flexbooru.exoplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import onlymash.flexbooru.app.App.Companion.app
import onlymash.flexbooru.app.Values.PC_USER_AGENT
import java.io.File

/**
 * [ExoPlayer] holder
 * */
class PlayerHolder {

    companion object {
        private var cache: SimpleCache? = null
        /**
         * return player network cache [SimpleCache]
         * */
        fun cache(): SimpleCache {
            if (cache == null) {
                cache = SimpleCache(
                    File(app.cacheDir, "video"),
                    LeastRecentlyUsedCacheEvictor(1024 * 1024 * 256),
                    DefaultDatabaseProvider.databaseProvider())
            }
            return cache!!
        }
    }
    private var currentPlayerState: PlayerState? = null
    private val playerStates: MutableList<PlayerState> = mutableListOf()
    private var player: SimpleExoPlayer? = null

    private fun createExtractorMediaSource(context: Context, uri: Uri): MediaSource {
        val sourceFactory = DefaultDataSourceFactory(context, PC_USER_AGENT)
        val cacheSourceFactory = CacheDataSource.Factory().apply {
            setCache(cache())
            setUpstreamDataSourceFactory(sourceFactory)
        }
        return ProgressiveMediaSource.Factory(cacheSourceFactory)
            .createMediaSource(MediaItem.Builder().setUri(uri).build())
    }

    //start play
    fun start(context: Context, uri: Uri, playerView: PlayerView) {
        playerView.player = player
        val mediaSource = createExtractorMediaSource(context, uri)
        val index = playerStates.indexOfFirst { it.uri == uri }
        currentPlayerState = if (index >= 0) {
            playerStates[index]
        } else {
            PlayerState(uri = uri).also {
                playerStates.add(it)
            }
        }
        player?.apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
            // Restore state (after onResume()/onStart())
            currentPlayerState?.apply {
                // Start playback when media has buffered enough
                seekTo(window, position)
            }
        }
    }

    fun pause() {
        player?.apply {
            if (playWhenReady) {
                currentPlayerState?.apply {
                    position = currentPosition
                    window = currentWindowIndex
                    whenReady = playWhenReady
                }
                playWhenReady = false
                playbackState
            }
        }
    }

    fun create(context: Context): SimpleExoPlayer {
        val player = SimpleExoPlayer.Builder(context).build().apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ALL
            }
        this.player = player
        return player
    }

    fun release() {
        player?.release()
        player = null
    }

    fun playerIsNull() = player == null
}