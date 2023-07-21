package otang.pulse.lib;

import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.core.graphics.ColorUtils;
import otang.pulse.lib.util.PrefUtils;

public class SolidLineRenderer extends Renderer
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final int GRAVITY_BOTTOM = 0;
  private static final int GRAVITY_TOP = 1;
  private static final int GRAVITY_CENTER = 2;
  private Paint mPaint;
  private int mUnitsOpacity = 200;
  private int mColor = Color.WHITE;
  private ValueAnimator[] mValueAnimators;
  private FFTAverage[] mFFTAverage;
  private float[] mFFTPoints;
  private byte rfk, ifk;
  private int dbValue;
  private float magnitude;
  private int mDbFuzzFactor;
  private boolean mVertical;
  private boolean mLeftInLandscape;
  private int mWidth, mHeight, mUnits, mGravity;
  private boolean mSmoothingEnabled;
  private boolean mCenterMirrored;
  private boolean mVerticalMirror;
  private boolean mRounded;

  public SolidLineRenderer(Context context, PulseView view, ColorController colorController) {
    super(context, view, colorController);
    mPref.registerListener(this);
    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    updateSettings();
    loadValueAnimators();
    onSizeChanged(0, 0, 0, 0);
  }

  @Override
  public void setLeftInLandscape(boolean leftInLandscape) {
    if (mLeftInLandscape != leftInLandscape) {
      mLeftInLandscape = leftInLandscape;
      onSizeChanged(0, 0, 0, 0);
    }
  }

  private void loadValueAnimators() {
    if (mValueAnimators != null) {
      stopAnimation(mValueAnimators.length);
    }
    mValueAnimators = new ValueAnimator[mUnits];
    final boolean isVertical = mVertical;
    for (int i = 0; i < mUnits; i++) {
      final int j;
      if (isVertical) {
        j = i * 4;
      } else {
        j = i * 4 + 1;
      }
      mValueAnimators[i] = new ValueAnimator();
      mValueAnimators[i].setDuration(128);
      mValueAnimators[i].addUpdateListener(
          new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
              mFFTPoints[j] = (float) animation.getAnimatedValue();
              postInvalidate();
            }
          });
    }
  }

  private void stopAnimation(int index) {
    if (mValueAnimators == null) return;
    for (int i = 0; i < index; i++) {
      // prevent onAnimationUpdate existing listeners (by stopping them) to call
      // a wrong mFFTPoints index after mUnits gets updated by the user
      mValueAnimators[i].removeAllUpdateListeners();
      mValueAnimators[i].cancel();
    }
  }

  private void setPortraitPoints() {
    float units = Float.valueOf(mUnits);
    float barUnit = mWidth / units;
    float barWidth = barUnit * 8f / 9f;
    float startPoint = mHeight;
    if (mGravity == GRAVITY_BOTTOM) {
      startPoint = (float) mHeight;
    } else if (mGravity == GRAVITY_TOP) {
      startPoint = 0f;
    } else if (mGravity == GRAVITY_CENTER) {
      startPoint = (float) mHeight / 2f;
    }
    barUnit = barWidth + (barUnit - barWidth) * units / (units - 1);
    mPaint.setStrokeWidth(barWidth);
    mPaint.setStrokeCap(mRounded ? Paint.Cap.ROUND : Paint.Cap.BUTT);
    for (int i = 0; i < mUnits; i++) {
      mFFTPoints[i * 4] = mFFTPoints[i * 4 + 2] = i * barUnit + (barWidth / 2);
      mFFTPoints[i * 4 + 1] = startPoint;
      mFFTPoints[i * 4 + 3] = startPoint;
    }
  }

  private void setVerticalPoints() {
    float units = Float.valueOf(mUnits);
    float barUnit = mHeight / units;
    float barHeight = barUnit * 8f / 9f;
    float startPoint = mWidth;
    if (mGravity == GRAVITY_BOTTOM) {
      startPoint = (float) mWidth;
    } else if (mGravity == GRAVITY_TOP) {
      startPoint = 0f;
    } else if (mGravity == GRAVITY_CENTER) {
      startPoint = (float) mWidth / 2f;
    }
    barUnit = barHeight + (barUnit - barHeight) * units / (units - 1);
    mPaint.setStrokeWidth(barHeight);
    mPaint.setStrokeCap(mRounded ? Paint.Cap.ROUND : Paint.Cap.BUTT);
    for (int i = 0; i < mUnits; i++) {
      mFFTPoints[i * 4 + 1] = mFFTPoints[i * 4 + 3] = i * barUnit + (barHeight / 2);
      mFFTPoints[i * 4] = mLeftInLandscape ? 0 : startPoint;
      mFFTPoints[i * 4 + 2] = mLeftInLandscape ? 0 : startPoint;
    }
  }

  @Override
  public void onSizeChanged(int w, int h, int oldw, int oldh) {
    if (mView.getWidth() > 0 && mView.getHeight() > 0) {
      mWidth = mView.getWidth();
      mHeight = mView.getHeight();
      mVertical = mHeight < mWidth;
      loadValueAnimators();
      if (mVertical) {
        setVerticalPoints();
      } else {
        setPortraitPoints();
      }
    }
  }

  @Override
  public void onStreamAnalyzed(boolean isValid) {
    mIsValidStream = isValid;
    if (isValid) {
      onSizeChanged(0, 0, 0, 0);
      mColorController.startLavaLamp();
    }
  }

  @Override
  public void onWaveFormUpdate(byte[] fft) {
    onDataCapture(fft);
  }

  private void onDataCapture(byte[] fft) {
    int fudgeFactor = mDbFuzzFactor * 5;
    int i = 0;
    for (; i < (mCenterMirrored ? (mUnits / 2) : mUnits); i++) {
      if (mValueAnimators[i] == null) continue;
      mValueAnimators[i].cancel();
      rfk = fft[i * 2 + 2];
      ifk = fft[i * 2 + 3];
      magnitude = rfk * rfk + ifk * ifk;
      dbValue = magnitude > 0 ? (int) (10 * Math.log10(magnitude)) : 0;
      if (mSmoothingEnabled) {
        if (mFFTAverage == null) {
          setupFFTAverage();
        }
        dbValue = mFFTAverage[i].average(dbValue);
      }
      if (mVertical) {
        if (mLeftInLandscape || mGravity == GRAVITY_TOP) {
          mValueAnimators[i].setFloatValues(mFFTPoints[i * 4], dbValue * fudgeFactor);
        } else if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
          mValueAnimators[i].setFloatValues(
              mFFTPoints[i * 4], mFFTPoints[2] - (dbValue * fudgeFactor));
        }
      } else {
        if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
          mValueAnimators[i].setFloatValues(
              mFFTPoints[i * 4 + 1], mFFTPoints[3] - (dbValue * fudgeFactor));
        } else if (mGravity == GRAVITY_TOP) {
          mValueAnimators[i].setFloatValues(
              mFFTPoints[i * 4 + 1], mFFTPoints[3] + (dbValue * fudgeFactor));
        }
      }
      mValueAnimators[i].start();
    }
    if (mCenterMirrored) {
      for (; i < mUnits; i++) {
        int j = mUnits - (i + 1);
        if (mValueAnimators[i] == null) continue;
        mValueAnimators[i].cancel();
        rfk = fft[j * 2 + 2];
        ifk = fft[j * 2 + 3];
        magnitude = rfk * rfk + ifk * ifk;
        dbValue = magnitude > 0 ? (int) (10 * Math.log10(magnitude)) : 0;
        if (mSmoothingEnabled) {
          if (mFFTAverage == null) {
            setupFFTAverage();
          }
          dbValue = mFFTAverage[i].average(dbValue);
        }
        if (mVertical) {
          if (mLeftInLandscape || mGravity == GRAVITY_TOP) {
            mValueAnimators[i].setFloatValues(mFFTPoints[i * 4], dbValue * fudgeFactor);
          } else if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
            mValueAnimators[i].setFloatValues(
                mFFTPoints[i * 4], mFFTPoints[2] - (dbValue * fudgeFactor));
          }
        } else {
          if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
            mValueAnimators[i].setFloatValues(
                mFFTPoints[i * 4 + 1], mFFTPoints[3] - (dbValue * fudgeFactor));
          } else if (mGravity == GRAVITY_TOP) {
            mValueAnimators[i].setFloatValues(
                mFFTPoints[i * 4 + 1], mFFTPoints[3] + (dbValue * fudgeFactor));
          }
        }
        mValueAnimators[i].start();
      }
    }
  }

  @Override
  public void onFFTUpdate(byte[] fft) {
    onDataCapture(fft);
  }

  @Override
  public void draw(Canvas canvas) {
    canvas.scale(1, 1, mWidth / 2f, mHeight / 2f);
    canvas.drawLines(mFFTPoints, mPaint);
    if (mVerticalMirror) {
      if (mVertical) {
        canvas.scale(-1, 1, mWidth / 2f, mHeight / 2f);
      } else {
        canvas.scale(1, -1, mWidth / 2f, mHeight / 2f);
      }
      canvas.drawLines(mFFTPoints, mPaint);
    }
  }

  @Override
  public void destroy() {
    mColorController.stopLavaLamp();
  }

  @Override
  public void onVisualizerLinkChanged(boolean linked) {
    if (!linked) {
      mColorController.stopLavaLamp();
    }
  }

  @Override
  public void onUpdateColor(int color) {
    mColor = color;
    mPaint.setColor(ColorUtils.setAlphaComponent(mColor, mUnitsOpacity));
  }

  public void updateSettings() {
    // putFloat, getFloat is better. catch it next time
    mDbFuzzFactor = mPref.getInt(PrefUtils.PREF_PULSE_SOLID_FUDGE, 5);
    mSmoothingEnabled = mPref.getBoolean(PrefUtils.PREF_PULSE_SMOOTH, true);
    mCenterMirrored = mPref.getBoolean(PrefUtils.PREF_PULSE_CENTER_MIRRORED, false);
    mVerticalMirror = mPref.getBoolean(PrefUtils.PREF_PULSE_VERTICAL_MIRROR, false);
    mGravity = Integer.valueOf(mPref.getString(PrefUtils.PREF_PULSE_GRAVITY, "0"));
    mRounded = mPref.getBoolean(PrefUtils.PREF_PULSE_ROUNDED, true);
    int units = mPref.getInt(PrefUtils.PREF_PULSE_LINE_COUNT, 32);
    if (units != mUnits) {
      stopAnimation(mUnits);
      mUnits = units;
      mFFTPoints = new float[mUnits * 4];
      if (mSmoothingEnabled) {
        setupFFTAverage();
      }
      onSizeChanged(0, 0, 0, 0);
    }
    if (mSmoothingEnabled) {
      if (mFFTAverage == null) {
        setupFFTAverage();
      }
    } else {
      mFFTAverage = null;
    }
    mUnitsOpacity = mPref.getInt(PrefUtils.PREF_PULSE_SOLID_OVACITY, 200);
    mPaint.setColor(ColorUtils.setAlphaComponent(mColor, mUnitsOpacity));
    postInvalidate();
  }

  private void setupFFTAverage() {
    mFFTAverage = new FFTAverage[mUnits];
    for (int i = 0; i < mUnits; i++) {
      mFFTAverage[i] = new FFTAverage();
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences prefs, String keys) {
    if (keys.equals(PrefUtils.PREF_PULSE_CENTER_MIRRORED)
        || keys.equals(PrefUtils.PREF_PULSE_LINE_COUNT)
        || keys.equals(PrefUtils.PREF_PULSE_ROUNDED)
        || keys.equals(PrefUtils.PREF_PULSE_SOLID_FUDGE)
        || keys.equals(PrefUtils.PREF_PULSE_SOLID_OVACITY)
        || keys.equals(PrefUtils.PREF_PULSE_SMOOTH)
        || keys.equals(PrefUtils.PREF_PULSE_VERTICAL_MIRROR)
        || keys.equals(PrefUtils.PREF_PULSE_GRAVITY)) {
      updateSettings();
    }
  }
}
