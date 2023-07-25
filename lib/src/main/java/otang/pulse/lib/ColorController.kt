package otang.pulse.lib

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import otang.pulse.lib.ColorAnimator.ColorAnimationListener
import otang.pulse.lib.util.ColorUtils
import otang.pulse.lib.util.PulseConfig

class ColorController(private val mContext: Context) : ColorAnimationListener,
    OnSharedPreferenceChangeListener {
    private var mRenderer: Renderer? = null
    private val mLavaLamp: ColorAnimator = ColorAnimator()
    private var mColorType = 0
    private val mAccentColor: Int
    private var mColor = 0
    private val mPref: PulseConfig

    init {
        mLavaLamp.setColorAnimatorListener(this)
        mAccentColor = accentColor
        mPref = PulseConfig(mContext)
        mPref.registerListener(this)
        updateSettings()
    }

    fun setRenderer(renderer: Renderer) {
        mRenderer = renderer
        notifyRenderer()
    }

    private fun updateSettings() {
        if (mColorType == COLOR_TYPE_LAVALAMP) {
            stopLavaLamp()
        }
        mColorType = mPref.pulseColor
        mColor = mPref.colorCustom
        val lavaSpeed = mPref.lavaSpeed
        mLavaLamp.setAnimationTime(lavaSpeed.toLong())
        notifyRenderer()
    }

    private fun notifyRenderer() {
        if (mColorType == COLOR_TYPE_ACCENT) {
            mRenderer?.onUpdateColor(mAccentColor)
        } else if (mColorType == COLOR_TYPE_USER) {
            mRenderer?.onUpdateColor(mColor)
        } else if (mColorType == COLOR_TYPE_LAVALAMP && mRenderer?.mIsValidStream == true) {
            startLavaLamp()
        }
    }

    fun startLavaLamp() {
        if (mColorType == COLOR_TYPE_LAVALAMP) {
            mLavaLamp.start()
        }
    }

    fun stopLavaLamp() {
        mLavaLamp.stop()
    }

    private val accentColor: Int
        get() = ColorUtils.getPrimaryColor(mContext)

    override fun onColorChanged(colorAnimator: ColorAnimator?, color: Int) {
        mRenderer?.onUpdateColor(color)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, keys: String?) {
        if (keys == PulseConfig.PREF_PULSE_COLOR || keys == PulseConfig.PREF_PULSE_COLOR_CUSTOM || keys == PulseConfig.PREF_PULSE_LAVA_SPEED) {
            updateSettings()
        }
    }

    companion object {
        const val COLOR_TYPE_ACCENT = 0
        const val COLOR_TYPE_USER = 1
        const val COLOR_TYPE_LAVALAMP = 2
    }
}