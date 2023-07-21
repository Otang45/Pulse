package otang.pulse.lib;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.view.WindowManager;

import otang.pulse.lib.util.PulseConfig;

public abstract class Renderer implements VisualizerStreamHandler.Listener {

    protected Context mContext;
    protected VisualizerView mView;
    protected ColorController mColorController;
    protected boolean mIsValidStream;
    private final long mPulseFPSToMs;
    private long mRenderCounter;
    protected PulseConfig mPref;

    public Renderer(Context context, VisualizerView view, ColorController colorController) {
        mContext = context;
        mPref = new PulseConfig(context);
        mView = view;
        mColorController = colorController;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mRenderCounter = System.currentTimeMillis();
        long mPulseFPS = Build.VERSION.SDK_INT < Build.VERSION_CODES.R ? (int) wm.getDefaultDisplay().getRefreshRate() : (int) context.getDisplay().getRefreshRate();
        mPulseFPSToMs = 1000 / mPulseFPS;
    }

    protected final void postInvalidate() {
        long mCurrentTime = System.currentTimeMillis();
        long mCurrentCounter = mCurrentTime - mRenderCounter;
        if (mCurrentCounter >= mPulseFPSToMs) {
            mRenderCounter = mCurrentTime;
        }
        mView.invalidate();
    }

    public abstract void draw(Canvas canvas);

    @Override
    public void onWaveFormUpdate(byte[] bytes) {
    }

    @Override
    public void onFFTUpdate(byte[] fft) {
    }

    public void onVisualizerLinkChanged(boolean linked) {
    }

    public void destroy() {
    }

    public void setLeftInLandscape(boolean leftInLandscape) {
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
    }

    public void onUpdateColor(int color) {
    }

    public boolean isValidStream() {
        return mIsValidStream;
    }
}
