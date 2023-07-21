@file:Suppress("DEPRECATION")

package otang.lib.pulse.example

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class AudioPlayer(private val context: Context) {
    private var exoPlayer: ExoPlayer? = null

    init {
        init()
    }

    private fun init() {
        exoPlayer = ExoPlayer.Builder(context).build()
    }

    fun setSource(url: String?) {
        exoPlayer!!.setMediaItem(MediaItem.fromUri(url!!))
        exoPlayer!!.prepare()
    }

    fun setListener(listener: Player.Listener?) {
        exoPlayer!!.addListener(listener!!)
    }

    val audioSessionId: Int
        get() = if (exoPlayer != null) {
            exoPlayer!!.audioSessionId
        } else {
            0
        }

    fun play() {
        exoPlayer!!.playWhenReady = true
    }

    fun pause() {
        exoPlayer!!.playWhenReady = false
    }

    val isPlaying: Boolean
        get() = exoPlayer != null && exoPlayer!!.isPlaying
}