package otang.pulse.lib

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.TypedValue
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

class FadingBlockRenderer(
    context: Context,
    view: VisualizerView,
    colorController: ColorController
) : Renderer(context, view, colorController), OnSharedPreferenceChangeListener {
    private val mPaint: Paint
    private val mFadePaint: Paint
    private var mVertical = false
    private var mLeftInLandscape: Boolean = false
    private var mFFTAverage: Array<FFTAverage?>? = null
    private var mFFTPoints: FloatArray? = null
    private var mDivisions = 0
    private var mDbFuzzFactor = 0
    private var mCanvasBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private val mMatrix: Matrix
    private var mWidth = 0
    private var mHeight = 0
    private var mGravity = 0
    private var mSmoothingEnabled = false
    private var mCenterMirrored = false
    private var mVerticalMirror = false

    init {
        mPref.registerListener(this)
        mPaint = Paint()
        mFadePaint = Paint()
        mFadePaint.color = Color.argb(200, 255, 255, 255)
        mFadePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        mMatrix = Matrix()
        mPaint.isAntiAlias = true
        updateSettings()
        onSizeChanged(0, 0, 0, 0)
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

    private fun onDataCapture(bytes: ByteArray?) {
        val fudgeFactor = mDbFuzzFactor * 5
        if (bytes != null) {
            if (mFFTPoints == null || mFFTPoints!!.size < bytes.size * 4) {
                mFFTPoints = FloatArray(bytes.size * 4)
            }
            val divisionLength = bytes.size / mDivisions
            if (mSmoothingEnabled) {
                if (mFFTAverage == null || mFFTAverage!!.size != divisionLength) {
                    setupFFTAverage(divisionLength)
                }
            } else {
                mFFTAverage = null
            }
            var i = 0
            var rfk: Byte
            var ifk: Byte
            var dbValue: Int
            var magnitude: Float
            while (i < if (mCenterMirrored) divisionLength / 2 else divisionLength) {
                if (mVertical) {
                    mFFTPoints!![i * 4 + 1] = (i * 4 * mDivisions).toFloat()
                    mFFTPoints!![i * 4 + 3] = (i * 4 * mDivisions).toFloat()
                } else {
                    mFFTPoints!![i * 4] = (i * 4 * mDivisions).toFloat()
                    mFFTPoints!![i * 4 + 2] = (i * 4 * mDivisions).toFloat()
                }
                rfk = bytes[mDivisions * i]
                ifk = bytes[mDivisions * i + 1]
                magnitude = (rfk * rfk + ifk * ifk).toFloat()
                dbValue = if (magnitude > 0) (10 * log10(magnitude.toDouble())).toInt() else 0
                if (mSmoothingEnabled) {
                    dbValue = mFFTAverage!![i]!!.average(dbValue)
                }
                if (mVertical) {
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
                    mFFTPoints!![i * 4] = (if (mLeftInLandscape) 0 else startPoint) as Float
                    mFFTPoints!![i * 4 + 2] =
                        if (mLeftInLandscape) (dbValue * fudgeFactor + DBFUZZ).toFloat() else startPoint - (dbValue * fudgeFactor + DBFUZZ)
                } else {
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
                    mFFTPoints!![i * 4 + 1] = startPoint
                    mFFTPoints!![i * 4 + 3] = startPoint - (dbValue * fudgeFactor + DBFUZZ)
                }
                i++
            }
            if (mCenterMirrored) {
                while (i < divisionLength) {
                    val j = divisionLength - (i + 1)
                    if (mVertical) {
                        mFFTPoints!![i * 4 + 1] = (i * 4 * mDivisions).toFloat()
                        mFFTPoints!![i * 4 + 3] = (i * 4 * mDivisions).toFloat()
                    } else {
                        mFFTPoints!![i * 4] = (i * 4 * mDivisions).toFloat()
                        mFFTPoints!![i * 4 + 2] = (i * 4 * mDivisions).toFloat()
                    }
                    rfk = bytes[mDivisions * j]
                    ifk = bytes[mDivisions * j + 1]
                    magnitude = (rfk * rfk + ifk * ifk).toFloat()
                    dbValue =
                        if (magnitude > 0) (10 * log10(magnitude.toDouble())).toInt() else 0
                    if (mSmoothingEnabled) {
                        dbValue = mFFTAverage!![i]!!.average(dbValue)
                    }
                    if (mVertical) {
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
                        mFFTPoints!![i * 4] = (if (mLeftInLandscape) 0 else startPoint) as Float
                        mFFTPoints!![i * 4 + 2] =
                            if (mLeftInLandscape) (dbValue * fudgeFactor + DBFUZZ).toFloat() else startPoint - (dbValue * fudgeFactor + DBFUZZ)
                    } else {
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
                        mFFTPoints!![i * 4 + 1] = startPoint
                        mFFTPoints!![i * 4 + 3] = startPoint - (dbValue * fudgeFactor + DBFUZZ)
                    }
                    i++
                }
            }
        }
        if (mCanvas != null) {
            mCanvas!!.drawLines(mFFTPoints!!, mPaint)
            mCanvas!!.drawPaint(mFadePaint)
        }
        postInvalidate()
    }

    override fun onFFTUpdate(bytes: ByteArray?) {
        onDataCapture(bytes)
    }

    private fun setupFFTAverage(size: Int) {
        mFFTAverage = arrayOfNulls(size)
        for (i in 0 until size) {
            mFFTAverage!![i] = FFTAverage()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (mView.width > 0 && mView.height > 0) {
            mWidth = mView.width
            mHeight = mView.height
            mVertical = mHeight < mWidth
            mCanvasBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mCanvasBitmap!!)
        }
    }

    override fun setLeftInLandscape(leftInLandscape: Boolean) {
        if (mLeftInLandscape != leftInLandscape) {
            mLeftInLandscape = leftInLandscape
            onSizeChanged(0, 0, 0, 0)
        }
    }

    override fun destroy() {
        mColorController.stopLavaLamp()
        mCanvasBitmap = null
    }

    override fun onVisualizerLinkChanged(linked: Boolean) {
        if (!linked) {
            mColorController.stopLavaLamp()
        }
    }

    override fun onUpdateColor(color: Int) {
        mPaint.color = color
    }

    override fun draw(canvas: Canvas) {
        canvas.scale(1f, 1f, mWidth / 2f, mHeight / 2f)
        canvas.drawBitmap(mCanvasBitmap!!, mMatrix, null)
        if (mVerticalMirror) {
            if (mVertical) {
                canvas.scale(-1f, 1f, mWidth / 2f, mHeight / 2f)
            } else {
                canvas.scale(1f, -1f, mWidth / 2f, mHeight / 2f)
            }
            canvas.drawBitmap(mCanvasBitmap!!, mMatrix, null)
        }
    }

    private fun updateSettings() {
        val res = mContext.resources
        val emptyBlock = mPref.emptySize
        val customDimen = mPref.fadingDim
        val numDivision = mPref.fadingDiv
        val fudgeFactor = mPref.fadingFudge
        val filledBlock = mPref.fillSize
        val mPathEffect1 = getLimitedDimenValue(filledBlock, 4, 8, res)
        val mPathEffect2 = getLimitedDimenValue(emptyBlock, 0, 4, res)
        mPaint.pathEffect = null
        mLeftInLandscape = mPref.isLeftInLandscape
        mPaint.pathEffect =
            DashPathEffect(floatArrayOf(mPathEffect1.toFloat(), mPathEffect2.toFloat()), 0f)
        mPaint.strokeWidth =
            getLimitedDimenValue(customDimen, 1, 30, res).toFloat()
        mDivisions = validateDivision(numDivision)
        mDbFuzzFactor = max(2, min(6, fudgeFactor))
        mSmoothingEnabled = mPref.isSmoothEnabled
        mCenterMirrored = mPref.isCenterMirror
        mVerticalMirror = mPref.isVerticalMirror
        mGravity = mPref.gravity
        postInvalidate()
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, keys: String?) {
        updateSettings()
    }

    companion object {
        private const val DBFUZZ = 2
        private const val GRAVITY_BOTTOM = 0
        private const val GRAVITY_TOP = 1
        private const val GRAVITY_CENTER = 2
        private fun getLimitedDimenValue(`val`: Int, min: Int, max: Int, res: Resources): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                max(min, min(max, `val`)).toFloat(),
                res.displayMetrics
            ).toInt()
        }

        @Suppress("NAME_SHADOWING")
        private fun validateDivision(`val`: Int): Int {
            // if a bad value was passed from settings (not divisible by 2)
            // reset to default value of 16. Validate range.
            var `val` = `val`
            if (`val` % 2 != 0) {
                `val` = 16
            }
            return max(2, min(44, `val`))
        }
    }
}