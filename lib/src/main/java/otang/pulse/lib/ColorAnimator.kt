package otang.pulse.lib

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.graphics.Color

class ColorAnimator @JvmOverloads constructor(
    @SuppressLint("Recycle") valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f),
    private var mAnimTime: Long = ANIM_DEF_DURATION.toLong(),
    fromColor: Int = Color.parseColor(
        RED
    ),
    toColor: Int = Color.parseColor(BLUE)
) : AnimatorUpdateListener {
    interface ColorAnimationListener {
        fun onColorChanged(colorAnimator: ColorAnimator?, color: Int) {}
        fun onStartAnimation(colorAnimator: ColorAnimator?, firstColor: Int) {}
        fun onStopAnimation(colorAnimator: ColorAnimator?, lastColor: Int) {}
    }

    private val from = FloatArray(3)
    private val to = FloatArray(3)
    private val hsv = FloatArray(3)
    private var mColorAnim: ValueAnimator
    private var mFromColor = Color.parseColor(RED)
    private var mToColor = Color.parseColor(BLUE)
    private var currentColor = Color.parseColor(RED)
    private var isRunning = false
    private var mListener: ColorAnimationListener? = null

    init {
        mFromColor = fromColor
        mToColor = toColor
        mColorAnim = valueAnimator
        mColorAnim.addUpdateListener(this)
    }

    fun start() {
        stop()
        Color.colorToHSV(mFromColor, from)
        Color.colorToHSV(mToColor, to)
        mColorAnim.duration = mAnimTime
        mColorAnim.repeatMode = ValueAnimator.REVERSE
        mColorAnim.repeatCount = ValueAnimator.INFINITE
        if (mListener != null) {
            mListener!!.onStartAnimation(this, mFromColor)
        }
        mColorAnim.start()
        isRunning = true
    }

    fun stop() {
        if (mColorAnim.isStarted) {
            mColorAnim.end()
            isRunning = false
            if (mListener != null) {
                mListener!!.onStopAnimation(this, currentColor)
            }
        }
    }

    fun setAnimationTime(millis: Long) {
        if (mAnimTime != millis) {
            mAnimTime = millis
            if (mColorAnim.isRunning) {
                start()
            }
        }
    }

    fun setColorAnimatorListener(listener: ColorAnimationListener) {
        mListener = listener
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        hsv[0] = from[0] + (to[0] - from[0]) * animation.animatedFraction
        hsv[1] = from[1] + (to[1] - from[1]) * animation.animatedFraction
        hsv[2] = from[2] + (to[2] - from[2]) * animation.animatedFraction
        currentColor = Color.HSVToColor(hsv)
        if (mListener != null) {
            mListener!!.onColorChanged(this, currentColor)
        }
    }

    companion object {
        const val ANIM_DEF_DURATION = 10 * 1000
        const val RED = "#ffff8080"
        const val BLUE = "#ff8080ff"
    }
}