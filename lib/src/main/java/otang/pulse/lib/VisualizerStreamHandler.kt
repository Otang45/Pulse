package otang.pulse.lib

import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log

class VisualizerStreamHandler(private var mListener: Listener) {
    interface Listener {
        fun onStreamAnalyzed(isValid: Boolean)
        fun onFFTUpdate(bytes: ByteArray?)
        fun onWaveFormUpdate(bytes: ByteArray?)
    }

    private var mVisualizer: Visualizer? = null

    // manage stream validation
    private var mConsecutiveFrames = 0
    private var mIsValidated = false
    private var mIsAnalyzed = false
    private var mIsPrepared = false
    private var mIsPaused = false
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(m: Message) {
            when (m.what) {
                MSG_STREAM_VALID -> {
                    mIsAnalyzed = true
                    mIsValidated = true
                    mIsPrepared = false
                    mListener.onStreamAnalyzed(true)
                }

                MSG_STREAM_INVALID -> {
                    mIsAnalyzed = true
                    mIsValidated = false
                    mIsPrepared = false
                    mListener.onStreamAnalyzed(false)
                }
            }
        }
    }

    /**
     * Links the visualizer to a player
     */
    fun link(id: Int) {
        pause()
        resetAnalyzer()
        mVisualizer = null
        mVisualizer = try {
            Visualizer(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling visualizer!", e)
            return
        }
        mVisualizer!!.enabled = false
        mVisualizer!!.captureSize = Visualizer.getCaptureSizeRange()[1]
        val captureListener: OnDataCaptureListener = object : OnDataCaptureListener {
            override fun onWaveFormDataCapture(
                visualizer: Visualizer,
                bytes: ByteArray,
                samplingRate: Int
            ) {
                if (ENABLE_WAVEFORM) {
                    analyze(bytes)
                    if (isValidStream && !mIsPaused) {
                        mListener.onWaveFormUpdate(bytes)
                    }
                }
            }

            override fun onFftDataCapture(
                visualizer: Visualizer,
                bytes: ByteArray,
                samplingRate: Int
            ) {
                analyze(bytes)
                if (isValidStream && !mIsPaused) {
                    mListener.onFFTUpdate(bytes)
                }
            }
        }
        mVisualizer!!.setDataCaptureListener(
            captureListener,
            (Visualizer.getMaxCaptureRate() * 0.75).toInt(),
            ENABLE_WAVEFORM,
            true
        )
        mVisualizer!!.enabled = true
    }

    fun unlink() {
        if (mVisualizer != null) {
            pause()
            mVisualizer!!.enabled = false
            mVisualizer!!.release()
            mVisualizer = null
            resetAnalyzer()
        }
    }

    val isValidStream: Boolean
        get() = mIsAnalyzed && mIsValidated

    private fun resetAnalyzer() {
        mIsAnalyzed = false
        mIsValidated = false
        mIsPrepared = false
        mConsecutiveFrames = 0
    }

    fun pause() {
        mIsPaused = true
    }

    fun resume() {
        mIsPaused = false
    }

    private fun analyze(data: ByteArray) {
        if (mIsAnalyzed) {
            return
        }
        if (!mIsPrepared) {
            mHandler.sendMessageDelayed(
                mHandler.obtainMessage(MSG_STREAM_INVALID),
                VALIDATION_TIME_MILLIS.toLong()
            )
            mIsPrepared = true
        }
        if (isDataEmpty(data)) {
            mConsecutiveFrames = 0
        } else {
            mConsecutiveFrames++
        }
        if (mConsecutiveFrames == VALID_BYTES_THRESHOLD) {
            mIsPaused = true
            mHandler.removeMessages(MSG_STREAM_INVALID)
            mHandler.sendEmptyMessage(MSG_STREAM_VALID)
        }
    }

    private fun isDataEmpty(data: ByteArray): Boolean {
        for (datum in data) {
            if (datum.toInt() != 0) {
                return false
            }
        }
        return true
    }

    companion object {
        private val TAG = VisualizerStreamHandler::class.java.simpleName
        private const val ENABLE_WAVEFORM = false
        private const val MSG_STREAM_VALID = 55
        private const val MSG_STREAM_INVALID = 56

        // we have 6 seconds to get three consecutive valid frames
        private const val VALIDATION_TIME_MILLIS = 6000
        private const val VALID_BYTES_THRESHOLD = 3
    }
}