package otang.pulse.lib.util

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceManager

class PulseConfig(ctx: Context?) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        pref = PreferenceManager.getDefaultSharedPreferences(ctx!!)
        editor = pref.edit()
    }

    fun registerListener(listener: OnSharedPreferenceChangeListener?) {
        pref.registerOnSharedPreferenceChangeListener(listener)
    }

    var isPulseEnabled: Boolean
        get() = pref.getBoolean(PREF_PULSE, true)
        set(pulseEnabled) {
            editor.putBoolean(PREF_PULSE, pulseEnabled).commit()
        }
    var isLeftInLandscape: Boolean
        get() = pref.getBoolean(PREF_PULSE_LEFT, false)
        set(leftInLandscape) {
            editor.putBoolean(PREF_PULSE_LEFT, leftInLandscape).commit()
        }
    var renderStyle: Int
        get() = pref.getInt(PREF_PULSE_RENDER, 1)
        set(renderStyle) {
            editor.putInt(PREF_PULSE_RENDER, renderStyle).commit()
        }
    var isCenterMirror: Boolean
        get() = pref.getBoolean(PREF_PULSE_CENTER_MIRRORED, false)
        set(centerMirror) {
            editor.putBoolean(PREF_PULSE_CENTER_MIRRORED, centerMirror).commit()
        }
    var isVerticalMirror: Boolean
        get() = pref.getBoolean(PREF_PULSE_VERTICAL_MIRROR, false)
        set(verticalMirror) {
            editor.putBoolean(PREF_PULSE_VERTICAL_MIRROR, verticalMirror).commit()
        }
    var gravity: Int
        get() = pref.getInt(PREF_PULSE_GRAVITY, 0)
        set(gravity) {
            editor.putInt(PREF_PULSE_GRAVITY, gravity).commit()
        }
    var isSmoothEnabled: Boolean
        get() = pref.getBoolean(PREF_PULSE_SMOOTH, true)
        set(smoothEnabled) {
            editor.putBoolean(PREF_PULSE_SMOOTH, smoothEnabled).commit()
        }
    var pulseColor: Int
        get() = pref.getInt(PREF_PULSE_COLOR, 0)
        set(pulseColor) {
            editor.putInt(PREF_PULSE_COLOR, pulseColor).commit()
        }
    var colorCustom: Int
        get() = pref.getInt(PREF_PULSE_COLOR_CUSTOM, -0x6d000001)
        set(colorCustom) {
            editor.putInt(PREF_PULSE_COLOR_CUSTOM, colorCustom).commit()
        }
    var lavaSpeed: Int
        get() = pref.getInt(PREF_PULSE_LAVA_SPEED, 10000)
        set(lavaSpeed) {
            editor.putInt(PREF_PULSE_LAVA_SPEED, lavaSpeed).commit()
        }
    var fadingDim: Int
        get() = pref.getInt(PREF_PULSE_FADING_DIMEN, 14)
        set(fadingDim) {
            editor.putInt(PREF_PULSE_FADING_DIMEN, fadingDim).commit()
        }
    var fadingDiv: Int
        get() = pref.getInt(PREF_PULSE_FADING_DIV, 16)
        set(fadingDiv) {
            editor.putInt(PREF_PULSE_FADING_DIV, fadingDiv).commit()
        }
    var fillSize: Int
        get() = pref.getInt(PREF_PULSE_FILL_SIZE, 4)
        set(fillSize) {
            editor.putInt(PREF_PULSE_FILL_SIZE, fillSize).commit()
        }
    var emptySize: Int
        get() = pref.getInt(PREF_PULSE_EMPTY_SIZE, 1)
        set(emptySize) {
            editor.putInt(PREF_PULSE_EMPTY_SIZE, emptySize).commit()
        }
    var fadingFudge: Int
        get() = pref.getInt(PREF_PULSE_FADING_FUDGE, 5)
        set(fadingFudge) {
            editor.putInt(PREF_PULSE_FADING_FUDGE, fadingFudge).commit()
        }
    var isSolidRounded: Boolean
        get() = pref.getBoolean(PREF_PULSE_ROUNDED, true)
        set(solidRounded) {
            editor.putBoolean(PREF_PULSE_ROUNDED, solidRounded).commit()
        }
    var solidOvacity: Int
        get() = pref.getInt(PREF_PULSE_SOLID_OVACITY, 200)
        set(solidOvacity) {
            editor.putInt(PREF_PULSE_SOLID_OVACITY, solidOvacity).commit()
        }
    var solidLineCount: Int
        get() = pref.getInt(PREF_PULSE_LINE_COUNT, 32)
        set(solidLineCount) {
            editor.putInt(PREF_PULSE_LINE_COUNT, solidLineCount).commit()
        }
    var solidFudge: Int
        get() = pref.getInt(PREF_PULSE_SOLID_FUDGE, 5)
        set(solidFudge) {
            editor.putInt(PREF_PULSE_SOLID_FUDGE, solidFudge).commit()
        }

    companion object {
        const val PREF_PULSE = "pref_pulse"
        const val PREF_PULSE_LEFT = "pref_pulse_left"
        const val PREF_PULSE_RENDER = "pref_pulse_render"
        const val PREF_PULSE_GRAVITY = "pref_pulse_gravity"
        const val PREF_PULSE_CENTER_MIRRORED = "pref_pulse_center_mirrored"
        const val PREF_PULSE_VERTICAL_MIRROR = "pref_pulse_vertical_mirror"
        const val PREF_PULSE_SMOOTH = "pref_pulse_smooth"
        const val PREF_PULSE_COLOR = "pref_pulse_color"
        const val PREF_PULSE_COLOR_CUSTOM = "pref_pulse_color_custom"
        const val PREF_PULSE_LAVA_SPEED = "pref_pulse_lava_speed"
        const val PREF_PULSE_FADING_DIMEN = "pref_pulse_fading_dimen"
        const val PREF_PULSE_FADING_DIV = "pref_pulse_fading_div"
        const val PREF_PULSE_FILL_SIZE = "pref_pulse_fill_size"
        const val PREF_PULSE_EMPTY_SIZE = "pref_pulse_empty_size"
        const val PREF_PULSE_FADING_FUDGE = "pref_pulse_fading_fudge"
        const val PREF_PULSE_ROUNDED = "pref_pulse_rounded"
        const val PREF_PULSE_SOLID_OVACITY = "pref_pulse_solid_ovacity"
        const val PREF_PULSE_LINE_COUNT = "pref_pulse_line_count"
        const val PREF_PULSE_SOLID_FUDGE = "pref_pulse_solid_fudge"
    }
}