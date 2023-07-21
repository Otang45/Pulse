package otang.pulse.lib;

import android.content.Context;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class VisualizerStreamHandler {

    public interface Listener {
        void onStreamAnalyzed(boolean isValid);

        void onFFTUpdate(byte[] bytes);

        void onWaveFormUpdate(byte[] bytes);
    }

    protected static final String TAG = VisualizerStreamHandler.class.getSimpleName();
    protected static final boolean ENABLE_WAVEFORM = false;
    protected static final int MSG_STREAM_VALID = 55;
    protected static final int MSG_STREAM_INVALID = 56;
    // we have 6 seconds to get three consecutive valid frames
    protected static final int VALIDATION_TIME_MILLIS = 6000;
    protected static final int VALID_BYTES_THRESHOLD = 3;
    protected Visualizer mVisualizer;
    // manage stream validation
    protected int mConsecutiveFrames;
    protected boolean mIsValidated;
    protected boolean mIsAnalyzed;
    protected boolean mIsPrepared;
    protected boolean mIsPaused;
    protected Context mContext;
    protected Listener mListener;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_STREAM_VALID:
                    mIsAnalyzed = true;
                    mIsValidated = true;
                    mIsPrepared = false;
                    mListener.onStreamAnalyzed(true);
                    break;
                case MSG_STREAM_INVALID:
                    mIsAnalyzed = true;
                    mIsValidated = false;
                    mIsPrepared = false;
                    mListener.onStreamAnalyzed(false);
                    break;
            }
        }
    };

    public VisualizerStreamHandler(Context context, VisualizerStreamHandler.Listener listener) {
        mContext = context;
        mListener = listener;
    }

    /**
     * Links the visualizer to a player
     */
    public final void link(int id) {
        pause();
        resetAnalyzer();
        mVisualizer = null;
        try {
            mVisualizer = new Visualizer(id);
        } catch (Exception e) {
            Log.e(TAG, "Error enabling visualizer!", e);
            return;
        }
        mVisualizer.setEnabled(false);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                if (ENABLE_WAVEFORM) {
                    analyze(bytes);
                    if (isValidStream() && !mIsPaused) {
                        mListener.onWaveFormUpdate(bytes);
                    }
                }
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                analyze(bytes);
                if (isValidStream() && !mIsPaused) {
                    mListener.onFFTUpdate(bytes);
                }
            }
        };
        mVisualizer.setDataCaptureListener(captureListener, (int) (Visualizer.getMaxCaptureRate() * 0.75), ENABLE_WAVEFORM, true);
        mVisualizer.setEnabled(true);
    }

    public final void unlink() {
        if (mVisualizer != null) {
            pause();
            mVisualizer.setEnabled(false);
            mVisualizer.release();
            mVisualizer = null;
            resetAnalyzer();
        }
    }

    public boolean isValidStream() {
        return mIsAnalyzed && mIsValidated;
    }

    public void resetAnalyzer() {
        mIsAnalyzed = false;
        mIsValidated = false;
        mIsPrepared = false;
        mConsecutiveFrames = 0;
    }

    public void pause() {
        mIsPaused = true;
    }

    public void resume() {
        mIsPaused = false;
    }

    private void analyze(byte[] data) {
        if (mIsAnalyzed) {
            return;
        }
        if (!mIsPrepared) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STREAM_INVALID), VALIDATION_TIME_MILLIS);
            mIsPrepared = true;
        }
        if (isDataEmpty(data)) {
            mConsecutiveFrames = 0;
        } else {
            mConsecutiveFrames++;
        }
        if (mConsecutiveFrames == VALID_BYTES_THRESHOLD) {
            mIsPaused = true;
            mHandler.removeMessages(MSG_STREAM_INVALID);
            mHandler.sendEmptyMessage(MSG_STREAM_VALID);
        }
    }

    private boolean isDataEmpty(byte[] data) {
        for (byte datum : data) {
            if (datum != 0) {
                return false;
            }
        }
        return true;
    }
}
