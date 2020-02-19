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

package onlymash.flexbooru.exoplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import onlymash.flexbooru.common.App.Companion.app
import onlymash.flexbooru.extension.getUserAgent
import java.io.File

/**
 * [ExoPlayer] holder
 * */
class PlayerHolder(private val context: Context) {

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
    // Create the player instance.
    private val exoPlayer: ExoPlayer = SimpleExoPlayer.Builder(context)
        .build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ALL
        }

    private fun createExtractorMediaSource(uri: Uri): MediaSource {
        val sourceFactory = DefaultDataSourceFactory(context, getUserAgent())
        val cacheSourceFactory = CacheDataSourceFactory(cache(), sourceFactory)
        return ProgressiveMediaSource.Factory(cacheSourceFactory).createMediaSource(uri)
    }

    //start play
    fun start(uri: Uri, playerView: PlayerView) {
        val mediaSource = createExtractorMediaSource(uri)
        val loopingSource = LoopingMediaSource(mediaSource)
        playerView.player = exoPlayer
        currentPlayerState = null
        playerStates.forEach {
            if (it.uri == uri) {
                currentPlayerState = it
            }
        }
        if (currentPlayerState == null) {
            currentPlayerState = PlayerState(uri = uri)
            playerStates.add(currentPlayerState!!)
        }
        // Load media.
        exoPlayer.prepare(loopingSource)
        // Restore state (after onResume()/onStart())
        with(currentPlayerState!!) {
            // Start playback when media has buffered enough
            exoPlayer.seekTo(window, position)
        }
    }
    // Stop playback and release resources, but re-use the player instance.
    fun stop() {
        with(exoPlayer) {
            if (currentPlayerState != null) {
                // Save state
                with(currentPlayerState!!) {
                    position = currentPosition
                    window = currentWindowIndex
                    whenReady = playWhenReady
                }
            }
            // Stop the player (and release it's resources). The player instance can be reused.
            stop(true)
        }
    }

    // Destroy the player instance.
    fun release() {
        playerStates.clear()
        exoPlayer.release() // player instance can't be used again.
    }
}