package otang.pulse.lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;

import otang.pulse.lib.util.PulseConfig;

public class FadingBlockRenderer extends Renderer implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int DBFUZZ = 2;
    private static final int GRAVITY_BOTTOM = 0;
    private static final int GRAVITY_TOP = 1;
    private static final int GRAVITY_CENTER = 2;
    private final Paint mPaint;
    private final Paint mFadePaint;
    private boolean mVertical;
    private boolean mLeftInLandscape;
    private FFTAverage[] mFFTAverage;
    private float[] mFFTPoints;
    private int mDivisions;
    private int mDbFuzzFactor;
    private Bitmap mCanvasBitmap;
    private Canvas mCanvas;
    private final Matrix mMatrix;
    private int mWidth;
    private int mHeight;
    private int mGravity;
    private boolean mSmoothingEnabled;
    private boolean mCenterMirrored;
    private boolean mVerticalMirror;

    public FadingBlockRenderer(Context context, VisualizerView view, ColorController colorController) {
        super(context, view, colorController);
        mPref.registerListener(this);
        mPaint = new Paint();
        mFadePaint = new Paint();
        mFadePaint.setColor(Color.argb(200, 255, 255, 255));
        mFadePaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
        mMatrix = new Matrix();
        mPaint.setAntiAlias(true);
        updateSettings();
        onSizeChanged(0, 0, 0, 0);
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
    public void onWaveFormUpdate(byte[] bytes) {
        onDataCapture(bytes);
    }

    private void onDataCapture(byte[] bytes) {
        int fudgeFactor = mDbFuzzFactor * 5;
        if (bytes != null) {
            if (mFFTPoints == null || mFFTPoints.length < bytes.length * 4) {
                mFFTPoints = new float[bytes.length * 4];
            }
            int divisionLength = bytes.length / mDivisions;
            if (mSmoothingEnabled) {
                if (mFFTAverage == null || mFFTAverage.length != divisionLength) {
                    setupFFTAverage(divisionLength);
                }
            } else {
                mFFTAverage = null;
            }
            int i = 0;
            byte rfk;
            byte ifk;
            int dbValue;
            float magnitude;
            for (; i < (mCenterMirrored ? (divisionLength / 2) : divisionLength); i++) {
                if (mVertical) {
                    mFFTPoints[i * 4 + 1] = i * 4 * mDivisions;
                    mFFTPoints[i * 4 + 3] = i * 4 * mDivisions;
                } else {
                    mFFTPoints[i * 4] = i * 4 * mDivisions;
                    mFFTPoints[i * 4 + 2] = i * 4 * mDivisions;
                }
                rfk = bytes[mDivisions * i];
                ifk = bytes[mDivisions * i + 1];
                magnitude = (rfk * rfk + ifk * ifk);
                dbValue = magnitude > 0 ? (int) (10 * Math.log10(magnitude)) : 0;
                if (mSmoothingEnabled) {
                    dbValue = mFFTAverage[i].average(dbValue);
                }
                if (mVertical) {
                    float startPoint = mWidth;
                    if (mGravity == GRAVITY_BOTTOM) {
                        startPoint = (float) mWidth;
                    } else if (mGravity == GRAVITY_TOP) {
                        startPoint = 0f;
                    } else if (mGravity == GRAVITY_CENTER) {
                        startPoint = (float) mWidth / 2f;
                    }
                    mFFTPoints[i * 4] = mLeftInLandscape ? 0 : startPoint;
                    mFFTPoints[i * 4 + 2] = mLeftInLandscape ? (dbValue * fudgeFactor + DBFUZZ) : (startPoint - (dbValue * fudgeFactor + DBFUZZ));
                } else {
                    float startPoint = mHeight;
                    if (mGravity == GRAVITY_BOTTOM) {
                        startPoint = (float) mHeight;
                    } else if (mGravity == GRAVITY_TOP) {
                        startPoint = 0f;
                    } else if (mGravity == GRAVITY_CENTER) {
                        startPoint = (float) mHeight / 2f;
                    }
                    mFFTPoints[i * 4 + 1] = startPoint;
                    mFFTPoints[i * 4 + 3] = startPoint - (dbValue * fudgeFactor + DBFUZZ);
                }
            }
            if (mCenterMirrored) {
                for (; i < divisionLength; i++) {
                    int j = divisionLength - (i + 1);
                    if (mVertical) {
                        mFFTPoints[i * 4 + 1] = i * 4 * mDivisions;
                        mFFTPoints[i * 4 + 3] = i * 4 * mDivisions;
                    } else {
                        mFFTPoints[i * 4] = i * 4 * mDivisions;
                        mFFTPoints[i * 4 + 2] = i * 4 * mDivisions;
                    }
                    rfk = bytes[mDivisions * j];
                    ifk = bytes[mDivisions * j + 1];
                    magnitude = (rfk * rfk + ifk * ifk);
                    dbValue = magnitude > 0 ? (int) (10 * Math.log10(magnitude)) : 0;
                    if (mSmoothingEnabled) {
                        dbValue = mFFTAverage[i].average(dbValue);
                    }
                    if (mVertical) {
                        float startPoint = mWidth;
                        if (mGravity == GRAVITY_BOTTOM) {
                            startPoint = (float) mWidth;
                        } else if (mGravity == GRAVITY_TOP) {
                            startPoint = 0f;
                        } else if (mGravity == GRAVITY_CENTER) {
                            startPoint = (float) mWidth / 2f;
                        }
                        mFFTPoints[i * 4] = mLeftInLandscape ? 0 : startPoint;
                        mFFTPoints[i * 4 + 2] = mLeftInLandscape ? (dbValue * fudgeFactor + DBFUZZ) : (startPoint - (dbValue * fudgeFactor + DBFUZZ));
                    } else {
                        float startPoint = mHeight;
                        if (mGravity == GRAVITY_BOTTOM) {
                            startPoint = (float) mHeight;
                        } else if (mGravity == GRAVITY_TOP) {
                            startPoint = 0f;
                        } else if (mGravity == GRAVITY_CENTER) {
                            startPoint = (float) mHeight / 2f;
                        }
                        mFFTPoints[i * 4 + 1] = startPoint;
                        mFFTPoints[i * 4 + 3] = startPoint - (dbValue * fudgeFactor + DBFUZZ);
                    }
                }
            }
        }
        if (mCanvas != null) {
            mCanvas.drawLines(mFFTPoints, mPaint);
            mCanvas.drawPaint(mFadePaint);
        }
        postInvalidate();
    }

    @Override
    public void onFFTUpdate(byte[] bytes) {
        onDataCapture(bytes);
    }

    private void setupFFTAverage(int size) {
        mFFTAverage = new FFTAverage[size];
        for (int i = 0; i < size; i++) {
            mFFTAverage[i] = new FFTAverage();
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mView.getWidth() > 0 && mView.getHeight() > 0) {
            mWidth = mView.getWidth();
            mHeight = mView.getHeight();
            mVertical = mHeight < mWidth;
            mCanvasBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
            mCanvas = new Canvas(mCanvasBitmap);
        }
    }

    @Override
    public void setLeftInLandscape(boolean leftInLandscape) {
        if (mLeftInLandscape != leftInLandscape) {
            mLeftInLandscape = leftInLandscape;
            onSizeChanged(0, 0, 0, 0);
        }
    }

    @Override
    public void destroy() {
        mColorController.stopLavaLamp();
        mCanvasBitmap = null;
    }

    @Override
    public void onVisualizerLinkChanged(boolean linked) {
        if (!linked) {
            mColorController.stopLavaLamp();
        }
    }

    @Override
    public void onUpdateColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.scale(1, 1, mWidth / 2f, mHeight / 2f);
        canvas.drawBitmap(mCanvasBitmap, mMatrix, null);
        if (mVerticalMirror) {
            if (mVertical) {
                canvas.scale(-1, 1, mWidth / 2f, mHeight / 2f);
            } else {
                canvas.scale(1, -1, mWidth / 2f, mHeight / 2f);
            }
            canvas.drawBitmap(mCanvasBitmap, mMatrix, null);
        }
    }

    public void updateSettings() {
        final Resources res = mContext.getResources();
        int emptyBlock = mPref.getEmptySize();
        int customDimen = mPref.getFadingDim();
        int numDivision = mPref.getFadingDiv();
        int fudgeFactor = mPref.getFadingFudge();
        int filledBlock = mPref.getFillSize();
        int mPathEffect1 = getLimitedDimenValue(filledBlock, 4, 8, res);
        int mPathEffect2 = getLimitedDimenValue(emptyBlock, 0, 4, res);
        mPaint.setPathEffect(null);
        mPaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{mPathEffect1, mPathEffect2}, 0));
        mPaint.setStrokeWidth(getLimitedDimenValue(customDimen, 1, 30, res));
        mDivisions = validateDivision(numDivision);
        mDbFuzzFactor = Math.max(2, Math.min(6, fudgeFactor));
        mSmoothingEnabled = mPref.isSmoothEnabled();
        mCenterMirrored = mPref.isCenterMirror();
        mVerticalMirror = mPref.isVerticalMirror();
        mGravity = mPref.getGravity();
        postInvalidate();
    }

    private static int getLimitedDimenValue(int val, int min, int max, Resources res) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Math.max(min, Math.min(max, val)), res.getDisplayMetrics());
    }

    private static int validateDivision(int val) {
        // if a bad value was passed from settings (not divisible by 2)
        // reset to default value of 16. Validate range.
        if (val % 2 != 0) {
            val = 16;
        }
        return Math.max(2, Math.min(44, val));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String keys) {
        updateSettings();
    }
}
