package onlymash.flexbooru.exoplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import onlymash.flexbooru.App.Companion.app
import onlymash.flexbooru.util.UserAgent
import java.io.File

class PlayerHolder(private val context: Context) {

    companion object {
        private var cache: SimpleCache? = null
        fun cache(): SimpleCache {
            if (cache == null) {
                cache = SimpleCache(File(app.cacheDir, "video"),
                    LeastRecentlyUsedCacheEvictor(1024 * 1024 * 256))
            }
            return cache!!
        }
    }
    private var currentPlayerState: PlayerState? = null
    private val playerStates: MutableList<PlayerState> = mutableListOf()
    // Create the player instance.
    val exoPlayer: ExoPlayer = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
        .apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ALL
        }

    private fun createExtractorMediaSource(uri: Uri): MediaSource {
        val sourceFactory = DefaultDataSourceFactory(context, UserAgent.get())
        val cacheSourceFactory = CacheDataSourceFactory(cache(), sourceFactory)
        return ExtractorMediaSource.Factory(cacheSourceFactory).createMediaSource(uri)
    }

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