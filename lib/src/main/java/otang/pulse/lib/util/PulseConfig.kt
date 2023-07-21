package otang.pulse.lib.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class PulseConfig {

    public static final String PREF_PULSE = "pref_pulse";
    public static final String PREF_PULSE_LEFT = "pref_pulse_left";
    public static final String PREF_PULSE_RENDER = "pref_pulse_render";
    public static final String PREF_PULSE_GRAVITY = "pref_pulse_gravity";
    public static final String PREF_PULSE_CENTER_MIRRORED = "pref_pulse_center_mirrored";
    public static final String PREF_PULSE_VERTICAL_MIRROR = "pref_pulse_vertical_mirror";
    public static final String PREF_PULSE_SMOOTH = "pref_pulse_smooth";
    public static final String PREF_PULSE_COLOR = "pref_pulse_color";
    public static final String PREF_PULSE_COLOR_CUSTOM = "pref_pulse_color_custom";
    public static final String PREF_PULSE_LAVA_SPEED = "pref_pulse_lava_speed";
    public static final String PREF_PULSE_FADING_DIMEN = "pref_pulse_fading_dimen";
    public static final String PREF_PULSE_FADING_DIV = "pref_pulse_fading_div";
    public static final String PREF_PULSE_FILL_SIZE = "pref_pulse_fill_size";
    public static final String PREF_PULSE_EMPTY_SIZE = "pref_pulse_empty_size";
    public static final String PREF_PULSE_FADING_FUDGE = "pref_pulse_fading_fudge";
    public static final String PREF_PULSE_ROUNDED = "pref_pulse_rounded";
    public static final String PREF_PULSE_SOLID_OVACITY = "pref_pulse_solid_ovacity";
    public static final String PREF_PULSE_LINE_COUNT = "pref_pulse_line_count";
    public static final String PREF_PULSE_SOLID_FUDGE = "pref_pulse_solid_fudge";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public PulseConfig(Context ctx) {
        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.editor = pref.edit();
    }

    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        pref.registerOnSharedPreferenceChangeListener(listener);
    }

    public boolean isPulseEnabled() {
        return pref.getBoolean(PREF_PULSE, true);
    }

    public void setPulseEnabled(boolean pulseEnabled) {
        editor.putBoolean(PREF_PULSE, pulseEnabled).commit();
    }

    public boolean isLeftInLandscape() {
        return pref.getBoolean(PREF_PULSE_LEFT, false);
    }

    public void setLeftInLandscape(boolean leftInLandscape) {
        editor.putBoolean(PREF_PULSE_LEFT, leftInLandscape).commit();
    }

    public int getRenderStyle() {
        return pref.getInt(PREF_PULSE_RENDER, 1);
    }

    public void setRenderStyle(int renderStyle) {
        editor.putInt(PREF_PULSE_RENDER, renderStyle).commit();
    }

    public boolean isCenterMirror() {
        return pref.getBoolean(PREF_PULSE_CENTER_MIRRORED, false);
    }

    public void setCenterMirror(boolean centerMirror) {
        editor.putBoolean(PREF_PULSE_CENTER_MIRRORED, centerMirror).commit();
    }

    public boolean isVerticalMirror() {
        return pref.getBoolean(PREF_PULSE_VERTICAL_MIRROR, false);
    }

    public void setVerticalMirror(boolean verticalMirror) {
        editor.putBoolean(PREF_PULSE_VERTICAL_MIRROR, verticalMirror).commit();
    }

    public int getGravity() {
        return pref.getInt(PREF_PULSE_GRAVITY, 0);
    }

    public void setGravity(int gravity) {
        editor.putInt(PREF_PULSE_GRAVITY, gravity).commit();
    }

    public boolean isSmoothEnabled() {
        return pref.getBoolean(PREF_PULSE_SMOOTH, true);
    }

    public void setSmoothEnabled(boolean smoothEnabled) {
        editor.putBoolean(PREF_PULSE_SMOOTH, smoothEnabled).commit();
    }

    public int getPulseColor() {
        return pref.getInt(PREF_PULSE_COLOR, 0);
    }

    public void setPulseColor(int pulseColor) {
        editor.putInt(PREF_PULSE_COLOR, pulseColor).commit();
    }

    public int getColorCustom() {
        return pref.getInt(PREF_PULSE_COLOR_CUSTOM, 0x92FFFFFF);
    }

    public void setColorCustom(int colorCustom) {
        editor.putInt(PREF_PULSE_COLOR_CUSTOM, colorCustom).commit();
    }

    public int getLavaSpeed() {
        return pref.getInt(PREF_PULSE_LAVA_SPEED, 10000);
    }

    public void setLavaSpeed(int lavaSpeed) {
        editor.putInt(PREF_PULSE_LAVA_SPEED, lavaSpeed).commit();
    }

    public int getFadingDim() {
        return pref.getInt(PREF_PULSE_FADING_DIMEN, 14);
    }

    public void setFadingDim(int fadingDim) {
        editor.putInt(PREF_PULSE_FADING_DIMEN, fadingDim).commit();
    }

    public int getFadingDiv() {
        return pref.getInt(PREF_PULSE_FADING_DIV, 16);
    }

    public void setFadingDiv(int fadingDiv) {
        editor.putInt(PREF_PULSE_FADING_DIV, fadingDiv).commit();
    }

    public int getFillSize() {
        return pref.getInt(PREF_PULSE_FILL_SIZE, 4);
    }

    public void setFillSize(int fillSize) {
        editor.putInt(PREF_PULSE_FILL_SIZE, fillSize).commit();
    }

    public int getEmptySize() {
        return pref.getInt(PREF_PULSE_EMPTY_SIZE, 1);
    }

    public void setEmptySize(int emptySize) {
        editor.putInt(PREF_PULSE_EMPTY_SIZE, emptySize).commit();
    }

    public int getFadingFudge() {
        return pref.getInt(PREF_PULSE_FADING_FUDGE, 5);
    }

    public void setFadingFudge(int fadingFudge) {
        editor.putInt(PREF_PULSE_FADING_FUDGE, fadingFudge).commit();
    }

    public boolean isSolidRounded() {
        return pref.getBoolean(PREF_PULSE_ROUNDED, true);
    }

    public void setSolidRounded(boolean solidRounded) {
        editor.putBoolean(PREF_PULSE_ROUNDED, solidRounded).commit();
    }

    public int getSolidOvacity() {
        return pref.getInt(PREF_PULSE_SOLID_OVACITY, 200);
    }

    public void setSolidOvacity(int solidOvacity) {
        editor.putInt(PREF_PULSE_SOLID_OVACITY, solidOvacity).commit();
    }

    public int getSolidLineCount() {
        return pref.getInt(PREF_PULSE_LINE_COUNT, 32);
    }

    public void setSolidLineCount(int solidLineCount) {
        editor.putInt(PREF_PULSE_LINE_COUNT, solidLineCount).commit();
    }

    public int getSolidFudge() {
        return pref.getInt(PREF_PULSE_SOLID_FUDGE, 5);
    }

    public void setSolidFudge(int solidFudge) {
        editor.putInt(PREF_PULSE_SOLID_FUDGE, solidFudge).commit();
    }

}
