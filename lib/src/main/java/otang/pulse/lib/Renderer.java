package otang.pulse.lib;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.view.WindowManager;
import otang.pulse.lib.util.PrefUtils;

public abstract class Renderer implements VisualizerStreamHandler.Listener {

  protected Context mContext;
  protected PulseView mView;
  protected ColorController mColorController;
  protected boolean mIsValidStream;
  private long mPulseFPS;
  private long mPulseFPSToMs;
  private long mCurrentTime;
  private long mRenderCounter;
  private long mCurrentCounter;
  protected PrefUtils mPref;

  public Renderer(Context context, PulseView view, ColorController colorController) {
    mContext = context;
    mPref = new PrefUtils(context);
    mView = view;
    mColorController = colorController;
    WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    mRenderCounter = System.currentTimeMillis();
    mPulseFPS =
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            ? (int) wm.getDefaultDisplay().getRefreshRate()
            : (int) context.getDisplay().getRefreshRate());
    mPulseFPSToMs = 1000 / mPulseFPS;
  }

  protected final void postInvalidate() {
    mCurrentTime = System.currentTimeMillis();
    mCurrentCounter = mCurrentTime - mRenderCounter;
    if (mCurrentCounter >= mPulseFPSToMs) {
      mRenderCounter = mCurrentTime;
      mView.postInvalidate();
    }
  }

  public abstract void draw(Canvas canvas);

  @Override
  public void onWaveFormUpdate(byte[] bytes) {}

  @Override
  public void onFFTUpdate(byte[] fft) {}

  public void onVisualizerLinkChanged(boolean linked) {}

  public void destroy() {}

  public void setLeftInLandscape(boolean leftInLandscape) {}

  public void onSizeChanged(int w, int h, int oldw, int oldh) {}

  public void onUpdateColor(int color) {}

  public boolean isValidStream() {
    return mIsValidStream;
  }
}
