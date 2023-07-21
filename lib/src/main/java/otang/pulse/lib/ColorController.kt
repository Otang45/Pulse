package otang.pulse.lib;

import android.content.Context;
import android.content.SharedPreferences;

import otang.pulse.lib.util.ColorUtils;
import otang.pulse.lib.util.PulseConfig;

public class ColorController
        implements ColorAnimator.ColorAnimationListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int COLOR_TYPE_ACCENT = 0;
    public static final int COLOR_TYPE_USER = 1;
    public static final int COLOR_TYPE_LAVALAMP = 2;
    private final Context mContext;
    private Renderer mRenderer;
    private final ColorAnimator mLavaLamp;
    private int mColorType;
    private final int mAccentColor;
    private int mColor;
    private final PulseConfig mPref;

    public ColorController(Context context) {
        mContext = context;
        mLavaLamp = new ColorAnimator();
        mLavaLamp.setColorAnimatorListener(this);
        mAccentColor = getAccentColor();
        mPref = new PulseConfig(context);
        mPref.registerListener(this);
        updateSettings();
    }

    void setRenderer(Renderer renderer) {
        mRenderer = renderer;
        notifyRenderer();
    }

    void updateSettings() {
        if (mColorType == COLOR_TYPE_LAVALAMP) {
            stopLavaLamp();
        }
        mColorType = mPref.getPulseColor();
        mColor = mPref.getColorCustom();
        int lava_speed = mPref.getLavaSpeed();
        mLavaLamp.setAnimationTime(lava_speed);
        notifyRenderer();
    }

    void notifyRenderer() {
        if (mRenderer != null) {
            if (mColorType == COLOR_TYPE_ACCENT) {
                mRenderer.onUpdateColor(mAccentColor);
            } else if (mColorType == COLOR_TYPE_USER) {
                mRenderer.onUpdateColor(mColor);
            } else if (mColorType == COLOR_TYPE_LAVALAMP && mRenderer.isValidStream()) {
                startLavaLamp();
            }
        }
    }

    void startLavaLamp() {
        if (mColorType == COLOR_TYPE_LAVALAMP) {
            mLavaLamp.start();
        }
    }

    void stopLavaLamp() {
        mLavaLamp.stop();
    }

    int getAccentColor() {
        return ColorUtils.getPrimaryColor(mContext);
    }

    @Override
    public void onColorChanged(ColorAnimator colorAnimator, int color) {
        if (mRenderer != null) {
            mRenderer.onUpdateColor(color);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String keys) {
        updateSettings();
    }
}
