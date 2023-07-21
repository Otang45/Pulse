package otang.pulse.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import otang.pulse.lib.util.PulseConfig;

public class VisualizerView extends View implements PulseController, SharedPreferences.OnSharedPreferenceChangeListener {

    public VisualizerView(@NonNull Context context) {
        this(context, null);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
        init();
    }

    private static final int RENDER_STYLE_CM = 1;
    private PulseConfig mPref;
    private PowerManager mPowerManager;
    private boolean mPowerSaveModeEnabled;
    private VisualizerStreamHandler mStreamHandler;
    private final VisualizerStreamHandler.Listener mStreamListener = new VisualizerStreamHandler.Listener() {
        @Override
        public void onStreamAnalyzed(boolean isValid) {
            if (mRenderer != null) {
                mRenderer.onStreamAnalyzed(isValid);
            }
            if (isValid) {
                notifyStateListeners(true);
                turnOnPulse();
            } else {
                doSilentUnlinkVisualizer();
            }
        }

        @Override
        public void onFFTUpdate(byte[] bytes) {
            if (mRenderer != null && bytes != null) {
                mRenderer.onFFTUpdate(bytes);
            }
        }

        @Override
        public void onWaveFormUpdate(byte[] bytes) {
            if (mRenderer != null && bytes != null) {
                mRenderer.onWaveFormUpdate(bytes);
            }
        }
    };
    private ColorController mColorController;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenOn = false;
                doLinkage();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenOn = true;
                doLinkage();
            } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(action)) {
                mPowerSaveModeEnabled = mPowerManager.isPowerSaveMode();
                doLinkage();
            }
        }
    };
    private boolean mScreenOn = true;
    private boolean mLeftInLandscape;
    private int mPulseStyle;
    private boolean mAttached;
    private boolean mLinked;
    private Renderer mRenderer;
    private final List<PulseStateListener> mStateListeners = new ArrayList<>();
    private int mAudioSessionId = -1;

    void init() {
        mPref = new PulseConfig(getContext());
        mPref.registerListener(this);
        mPowerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        mPowerSaveModeEnabled = mPowerManager.isPowerSaveMode();
        mStreamHandler = new VisualizerStreamHandler(getContext(), mStreamListener);
        mColorController = new ColorController(getContext());
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        getContext().registerReceiver(mBroadcastReceiver, filter, null, null);
        updateSettings();
        doLinkage();
    }

    void updateSettings() {
        boolean mIsEnable = mPref.isPulseEnabled();
        mLeftInLandscape = mPref.isLeftInLandscape();
        mPulseStyle = mPref.getRenderStyle();
        mAttached = mIsEnable;
        loadRenderer();
        doLinkage();
        invalidate();
    }

    private void loadRenderer() {
        final boolean isRendering = shouldDrawPulse();
        if (isRendering) {
            mStreamHandler.pause();
        }
        if (mRenderer != null) {
            mRenderer.destroy();
            mRenderer = null;
        }
        mRenderer = getRenderer();
        mColorController.setRenderer(mRenderer);
        mRenderer.setLeftInLandscape(mLeftInLandscape);
        if (isRendering) {
            mRenderer.onStreamAnalyzed(true);
            mStreamHandler.resume();
        }
    }

    public boolean shouldDrawPulse() {
        return mLinked && mStreamHandler.isValidStream() && mRenderer != null;
    }

    private Renderer getRenderer() {
        if (mPulseStyle == RENDER_STYLE_CM) {
            return new SolidLineRenderer(getContext(), this, mColorController);
        }
        return new FadingBlockRenderer(getContext(), this, mColorController);
    }

    private void notifyStateListeners(boolean isStarting) {
        for (PulseStateListener listener : mStateListeners) {
            if (listener != null) {
                listener.onPulseStateChanged(isStarting);
            }
        }
    }

    private void turnOnPulse() {
        if (shouldDrawPulse()) {
            mStreamHandler.resume(); // let bytes hit visualizer
        }
    }

    private void doSilentUnlinkVisualizer() {
        if (mStreamHandler != null) {
            if (mLinked) {
                mStreamHandler.unlink();
                mLinked = false;
            }
        }
    }

    private void doLinkage() {
        if (isUnlinkRequired()) {
            if (mLinked) {
                doUnlinkVisualizer();
            }
        } else {
            if (isAbleToLink()) {
                doLinkVisualizer();
            } else if (mLinked) {
                doUnlinkVisualizer();
            }
        }
    }

    private boolean isUnlinkRequired() {
        return !mScreenOn || mPowerSaveModeEnabled || !mAttached;
    }

    public void doUnlinkVisualizer() {
        if (mStreamHandler != null) {
            mStreamHandler.unlink();
            mLinked = false;
            if (mRenderer != null) {
                mRenderer.onVisualizerLinkChanged(false);
            }
            this.postInvalidate();
            notifyStateListeners(false);
        }
    }

    private boolean isAbleToLink() {
        return mScreenOn && !mPowerSaveModeEnabled && mAttached;
    }

    public void doLinkVisualizer() {
        if (mStreamHandler != null) {
            if (!mLinked && mAudioSessionId != -1) {
                mStreamHandler.link(mAudioSessionId);
                mLinked = true;
                if (mRenderer != null) {
                    mRenderer.onVisualizerLinkChanged(true);
                }
            }
        }
    }

    public void setAudioSessionId(int id) {
        mAudioSessionId = id;
        doLinkage();
    }

    public PulseConfig getPulsePref() {
        return mPref;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mRenderer != null) {
            mRenderer.onSizeChanged(w, h, oldw, oldh);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shouldDrawPulse()) {
            mRenderer.draw(canvas);
        }
    }

    @Override
    public void addCallback(PulseStateListener listener) {
        mStateListeners.add(listener);
        if (shouldDrawPulse()) {
            listener.onPulseStateChanged(true);
        }
    }

    @Override
    public void removeCallback(PulseStateListener listener) {
        mStateListeners.remove(listener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String keys) {
        updateSettings();
    }
}
