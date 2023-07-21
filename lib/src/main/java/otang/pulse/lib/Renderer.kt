@file:Suppress("DEPRECATION")

package otang.pulse.lib

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.view.WindowManager
import otang.pulse.lib.util.PulseConfig

abstract class Renderer(
    protected var mContext: Context,
    view: VisualizerView,
    colorController: ColorController
) : VisualizerStreamHandler.Listener {
    protected var mView: VisualizerView
    protected var mColorController: ColorController
    var mIsValidStream = false
    private val mPulseFPSToMs: Long
    private var mRenderCounter: Long
    protected var mPref: PulseConfig = PulseConfig(mContext)

    init {
        mView = view
        mColorController = colorController
        val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mRenderCounter = System.currentTimeMillis()
        val mPulseFPS =
            (if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) wm.defaultDisplay.refreshRate.toInt() else mContext.display!!
                .refreshRate.toInt()).toLong()
        mPulseFPSToMs = 1000 / mPulseFPS
    }

    protected fun postInvalidate() {
        val mCurrentTime = System.currentTimeMillis()
        val mCurrentCounter = mCurrentTime - mRenderCounter
        if (mCurrentCounter >= mPulseFPSToMs) {
            mRenderCounter = mCurrentTime
        }
        mView.invalidate()
    }

    abstract fun draw(canvas: Canvas)
    override fun onWaveFormUpdate(bytes: ByteArray?) {}
    override fun onFFTUpdate(bytes: ByteArray?) {}
    open fun onVisualizerLinkChanged(linked: Boolean) {}
    open fun destroy() {}
    open fun setLeftInLandscape(leftInLandscape: Boolean) {}
    open fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {}
    open fun onUpdateColor(color: Int) {}
}