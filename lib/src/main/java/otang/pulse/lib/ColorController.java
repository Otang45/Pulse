package otang.pulse.lib;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.TypedValue;
import otang.pulse.lib.util.ColorUtils;
import otang.pulse.lib.util.PrefUtils;

public class ColorController
    implements ColorAnimator.ColorAnimationListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

  public static final int COLOR_TYPE_ACCENT = 0;
  public static final int COLOR_TYPE_USER = 1;
  public static final int COLOR_TYPE_LAVALAMP = 2;
  public static final int LAVA_LAMP_SPEED_DEF = 10000;
  private Context mContext;
  private Renderer mRenderer;
  private ColorAnimator mLavaLamp;
  private int mColorType;
  private int mAccentColor;
  private int mColor;
  private PrefUtils mPref;

  public ColorController(Context context) {
    mContext = context;
    mLavaLamp = new ColorAnimator();
    mLavaLamp.setColorAnimatorListener(this);
    mAccentColor = getAccentColor();
    mPref = new PrefUtils(context);
    mPref.registerListener(this);
    updateSettings();
  }

  void setRenderer(Renderer renderer) {
    mRenderer = renderer;
    notifyRenderer();
  }

  void updateSettings() {
    ContentResolver resolver = mContext.getContentResolver();
    if (mColorType == COLOR_TYPE_LAVALAMP) {
      stopLavaLamp();
    }
    mColorType =
        Integer.valueOf(
            mPref.getString(PrefUtils.PREF_PULSE_COLOR, String.valueOf(COLOR_TYPE_ACCENT)));
    mColor = mPref.getInt(PrefUtils.PREF_PULSE_COLOR_CUSTOM, 0x92FFFFFF);
    int lava_speed = mPref.getInt(PrefUtils.PREF_PULSE_LAVA_SPEED, LAVA_LAMP_SPEED_DEF);
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
    if (keys.equals(PrefUtils.PREF_PULSE_COLOR)
        || keys.equals(PrefUtils.PREF_PULSE_COLOR_CUSTOM)
        || keys.equals(PrefUtils.PREF_PULSE_LAVA_SPEED)) {
      updateSettings();
    }
  }
}
