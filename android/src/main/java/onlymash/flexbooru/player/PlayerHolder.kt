/*
 * Copyright (C) 2020. by onlymash <fiepi.dev@gmail.com>, All rights reserved
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

package onlymash.flexbooru.player

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import onlymash.flexbooru.app.App.Companion.app
import onlymash.flexbooru.app.Keys
import onlymash.flexbooru.app.Values
import onlymash.flexbooru.data.model.common.Booru
import onlymash.flexbooru.okhttp.AndroidCookieJar
import java.io.File

/**
 * [ExoPlayer] holder
 * */
@SuppressLint("UnsafeOptInUsageError")
class PlayerHolder(
    private val booru: Booru? = null
) {

    companion object {
        private var cache: SimpleCache? = null
        /**
         * return player network cache [SimpleCache]
         * */
        fun cache(): SimpleCache {
            if (cache == null) {
                cache = SimpleCache(
                    File(app.cacheDir, "video"),
                    LeastRecentlyUsedCacheEvictor(1024 * 1024 * 256L),
                    DefaultDatabaseProvider.databaseProvider())
            }
            return cache!!
        }
    }
    private var currentPlayerState: PlayerState? = null
    private val playerStates: MutableList<PlayerState> = mutableListOf()
    private var player: ExoPlayer? = null

    private fun createExtractorMediaSource(context: Context, uri: Uri): MediaSource {
        val booru = booru
        var interceptor: Interceptor? = null
        if (booru != null) {
            val referer = when (booru.type) {
                Values.BOORU_TYPE_DAN1, Values.BOORU_TYPE_MOE -> "${booru.scheme}://${booru.host}/post/"
                Values.BOORU_TYPE_SANKAKU -> Values.SANKAKU_REFERER
                else -> "${booru.scheme}://${booru.host}/"
            }
            interceptor = Interceptor {
                val request = it.request()
                it.proceed(request.newBuilder().addHeader(Keys.HEADER_REFERER, referer).build())
            }
        }
        val okHttpClientBuilder = OkHttpClient.Builder()
            .cookieJar(AndroidCookieJar)
        if (interceptor != null) {
            okHttpClientBuilder.addInterceptor(interceptor)
        }
        val okHttpClient = okHttpClientBuilder.build()
        val okHttpFactory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent(Values.PC_USER_AGENT)
        val sourceFactory = DefaultDataSource.Factory(context, okHttpFactory)
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
                    window = currentMediaItemIndex
                    whenReady = playWhenReady
                }
                playWhenReady = false
                playbackState
            }
        }
    }

    fun create(context: Context): ExoPlayer {
        val player = ExoPlayer.Builder(context).build().apply {
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