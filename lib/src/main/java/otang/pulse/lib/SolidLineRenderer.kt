package otang.pulse.lib

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.ColorUtils
import otang.pulse.lib.util.PulseConfig
import kotlin.math.log10

class SolidLineRenderer(context: Context, view: VisualizerView, colorController: ColorController) :
    Renderer(context, view, colorController), OnSharedPreferenceChangeListener {
    private val mPaint: Paint
    private var mUnitsOpacity = 200
    private var mColor = Color.WHITE
    private var mValueAnimators: Array<ValueAnimator?>? = null
    private var mFFTAverage: Array<FFTAverage?>? = null
    private lateinit var mFFTPoints: FloatArray
    private var mDbFuzzFactor = 0
    private var mVertical = false
    private var mLeftInLandscape = false
    private var mWidth = 0
    private var mHeight = 0
    private var mUnits = 0
    private var mGravity = 0
    private var mSmoothingEnabled = false
    private var mCenterMirrored = false
    private var mVerticalMirror = false
    private var mRounded = false

    init {
        mPref.registerListener(this)
        mPaint = Paint()
        mPaint.isAntiAlias = true
        updateSettings()
        loadValueAnimators()
        onSizeChanged(0, 0, 0, 0)
    }

    override fun setLeftInLandscape(leftInLandscape: Boolean) {
        if (mLeftInLandscape != leftInLandscape) {
            mLeftInLandscape = leftInLandscape
            onSizeChanged(0, 0, 0, 0)
        }
    }

    @SuppressLint("Recycle")
    private fun loadValueAnimators() {
        if (mValueAnimators != null) {
            stopAnimation(mValueAnimators!!.size)
        }
        mValueAnimators = arrayOfNulls(mUnits)
        val isVertical = mVertical
        for (i in 0 until mUnits) {
            val j: Int = if (isVertical) {
                i * 4
            } else {
                i * 4 + 1
            }
            mValueAnimators!![i] = ValueAnimator()
            mValueAnimators!![i]!!.duration = 128
            mValueAnimators!![i]!!.addUpdateListener { animation: ValueAnimator ->
                mFFTPoints[j] = animation.animatedValue as Float
                postInvalidate()
            }
        }
    }

    private fun stopAnimation(index: Int) {
        if (mValueAnimators == null) return
        for (i in 0 until index) {
            // prevent onAnimationUpdate existing listeners (by stopping them) to call
            // a wrong mFFTPoints index after mUnits gets updated by the user
            mValueAnimators!![i]!!.removeAllUpdateListeners()
            mValueAnimators!![i]!!.cancel()
        }
    }

    private fun setPortraitPoints() {
        val units = mUnits.toFloat()
        var barUnit = mWidth / units
        val barWidth = barUnit * 8f / 9f
        var startPoint = mHeight.toFloat()
        when (mGravity) {
            GRAVITY_BOTTOM -> {
                startPoint = mHeight.toFloat()
            }

            GRAVITY_TOP -> {
                startPoint = 0f
            }

            GRAVITY_CENTER -> {
                startPoint = mHeight.toFloat() / 2f
            }
        }
        barUnit = barWidth + (barUnit - barWidth) * units / (units - 1)
        mPaint.strokeWidth = barWidth
        mPaint.strokeCap = if (mRounded) Paint.Cap.ROUND else Paint.Cap.BUTT
        for (i in 0 until mUnits) {
            mFFTPoints[i * 4 + 2] = i * barUnit + barWidth / 2
            mFFTPoints[i * 4] = mFFTPoints[i * 4 + 2]
            mFFTPoints[i * 4 + 1] = startPoint
            mFFTPoints[i * 4 + 3] = startPoint
        }
    }

    private fun setVerticalPoints() {
        val units = mUnits.toFloat()
        var barUnit = mHeight / units
        val barHeight = barUnit * 8f / 9f
        var startPoint = mWidth.toFloat()
        when (mGravity) {
            GRAVITY_BOTTOM -> {
                startPoint = mWidth.toFloat()
            }

            GRAVITY_TOP -> {
                startPoint = 0f
            }

            GRAVITY_CENTER -> {
                startPoint = mWidth.toFloat() / 2f
            }
        }
        barUnit = barHeight + (barUnit - barHeight) * units / (units - 1)
        mPaint.strokeWidth = barHeight
        mPaint.strokeCap = if (mRounded) Paint.Cap.ROUND else Paint.Cap.BUTT
        for (i in 0 until mUnits) {
            mFFTPoints[i * 4 + 3] = i * barUnit + barHeight / 2
            mFFTPoints[i * 4 + 1] = mFFTPoints[i * 4 + 3]
            mFFTPoints[i * 4] = (if (mLeftInLandscape) 0 else startPoint) as Float
            mFFTPoints[i * 4 + 2] = (startPoint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (mView.width > 0 && mView.height > 0) {
            mWidth = mView.width
            mHeight = mView.height
            mVertical = mHeight < mWidth
            loadValueAnimators()
            if (mVertical) {
                setVerticalPoints()
            } else {
                setPortraitPoints()
            }
        }
    }

    override fun onStreamAnalyzed(isValid: Boolean) {
        mIsValidStream = isValid
        if (isValid) {
            onSizeChanged(0, 0, 0, 0)
            mColorController.startLavaLamp()
        }
    }

    override fun onWaveFormUpdate(bytes: ByteArray?) {
        onDataCapture(bytes)
    }

    private fun onDataCapture(fft: ByteArray?) {
        val fudgeFactor = mDbFuzzFactor * 5
        var i = 0
        var rfk: Byte
        var ifk: Byte
        var dbValue: Int
        var magnitude: Float
        while (i < if (mCenterMirrored) mUnits / 2 else mUnits) {
            if (mValueAnimators!![i] == null) {
                i++
                continue
            }
            mValueAnimators!![i]!!.cancel()
            rfk = fft!![i * 2 + 2]
            ifk = fft[i * 2 + 3]
            magnitude = (rfk * rfk + ifk * ifk).toFloat()
            dbValue = if (magnitude > 0) (10 * log10(magnitude.toDouble())).toInt() else 0
            if (mSmoothingEnabled) {
                if (mFFTAverage == null) {
                    setupFFTAverage()
                }
                dbValue = mFFTAverage!![i]!!.average(dbValue)
            }
            if (mVertical) {
                if (mLeftInLandscape || mGravity == GRAVITY_TOP) {
                    mValueAnimators!![i]!!.setFloatValues(
                        mFFTPoints[i * 4], (dbValue * fudgeFactor).toFloat()
                    )
                } else if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
                    mValueAnimators!![i]!!.setFloatValues(
                        mFFTPoints[i * 4], mFFTPoints[2] - dbValue * fudgeFactor
                    )
                }
            } else {
                if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
                    mValueAnimators!![i]!!.setFloatValues(
                        mFFTPoints[i * 4 + 1], mFFTPoints[3] - dbValue * fudgeFactor
                    )
                } else if (mGravity == GRAVITY_TOP) {
                    mValueAnimators!![i]!!.setFloatValues(
                        mFFTPoints[i * 4 + 1], mFFTPoints[3] + dbValue * fudgeFactor
                    )
                }
            }
            mValueAnimators!![i]!!.start()
            i++
        }
        if (mCenterMirrored) {
            while (i < mUnits) {
                val j = mUnits - (i + 1)
                if (mValueAnimators!![i] == null) {
                    i++
                    continue
                }
                mValueAnimators!![i]!!.cancel()
                rfk = fft!![j * 2 + 2]
                ifk = fft[j * 2 + 3]
                magnitude = (rfk * rfk + ifk * ifk).toFloat()
                dbValue = if (magnitude > 0) (10 * log10(magnitude.toDouble())).toInt() else 0
                if (mSmoothingEnabled) {
                    if (mFFTAverage == null) {
                        setupFFTAverage()
                    }
                    dbValue = mFFTAverage!![i]!!.average(dbValue)
                }
                if (mVertical) {
                    if (mLeftInLandscape || mGravity == GRAVITY_TOP) {
                        mValueAnimators!![i]!!.setFloatValues(
                            mFFTPoints[i * 4], (dbValue * fudgeFactor).toFloat()
                        )
                    } else if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
                        mValueAnimators!![i]!!.setFloatValues(
                            mFFTPoints[i * 4], mFFTPoints[2] - dbValue * fudgeFactor
                        )
                    }
                } else {
                    if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
                        mValueAnimators!![i]!!.setFloatValues(
                            mFFTPoints[i * 4 + 1], mFFTPoints[3] - dbValue * fudgeFactor
                        )
                    } else if (mGravity == GRAVITY_TOP) {
                        mValueAnimators!![i]!!.setFloatValues(
                            mFFTPoints[i * 4 + 1], mFFTPoints[3] + dbValue * fudgeFactor
                        )
                    }
                }
                mValueAnimators!![i]!!.start()
                i++
            }
        }
    }

    override fun onFFTUpdate(bytes: ByteArray?) {
        onDataCapture(bytes)
    }

    override fun draw(canvas: Canvas) {
        canvas.scale(1f, 1f, mWidth / 2f, mHeight / 2f)
        canvas.drawLines(mFFTPoints, mPaint)
        if (mVerticalMirror) {
            if (mVertical) {
                canvas.scale(-1f, 1f, mWidth / 2f, mHeight / 2f)
            } else {
                canvas.scale(1f, -1f, mWidth / 2f, mHeight / 2f)
            }
            canvas.drawLines(mFFTPoints, mPaint)
        }
    }

    override fun destroy() {
        mColorController.stopLavaLamp()
    }

    override fun onVisualizerLinkChanged(linked: Boolean) {
        if (!linked) {
            mColorController.stopLavaLamp()
        }
    }

    override fun onUpdateColor(color: Int) {
        mColor = color
        mPaint.color = ColorUtils.setAlphaComponent(mColor, mUnitsOpacity)
    }

    private fun updateSettings() {
        mDbFuzzFactor = mPref.solidFudge
        mSmoothingEnabled = mPref.isSmoothEnabled
        mCenterMirrored = mPref.isCenterMirror
        mVerticalMirror = mPref.isVerticalMirror
        mLeftInLandscape = mPref.isLeftInLandscape
        mGravity = mPref.gravity
        mRounded = mPref.isSolidRounded
        val units = mPref.solidLineCount
        if (units != mUnits) {
            stopAnimation(mUnits)
            mUnits = units
            mFFTPoints = FloatArray(mUnits * 4)
            if (mSmoothingEnabled) {
                setupFFTAverage()
            }
            onSizeChanged(0, 0, 0, 0)
        }
        if (mSmoothingEnabled) {
            if (mFFTAverage == null) {
                setupFFTAverage()
            }
        } else {
            mFFTAverage = null
        }
        mUnitsOpacity = mPref.solidOvacity
        mPaint.color = ColorUtils.setAlphaComponent(mColor, mUnitsOpacity)
        postInvalidate()
    }

    private fun setupFFTAverage() {
        mFFTAverage = arrayOfNulls(mUnits)
        for (i in 0 until mUnits) {
            mFFTAverage!![i] = FFTAverage()
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, keys: String?) {
        if (keys == PulseConfig.PREF_PULSE_SOLID_FUDGE || keys == PulseConfig.PREF_PULSE_LINE_COUNT || keys == PulseConfig.PREF_PULSE_ROUNDED || keys == PulseConfig.PREF_PULSE_SOLID_OVACITY || keys == PulseConfig.PREF_PULSE_GRAVITY || keys == PulseConfig.PREF_PULSE_CENTER_MIRRORED || keys == PulseConfig.PREF_PULSE_LEFT || keys == PulseConfig.PREF_PULSE_VERTICAL_MIRROR || keys == PulseConfig.PREF_PULSE_SMOOTH) {
            updateSettings()
        }
    }

    companion object {
        private const val GRAVITY_BOTTOM = 0
        private const val GRAVITY_TOP = 1
        private const val GRAVITY_CENTER = 2
    }
}