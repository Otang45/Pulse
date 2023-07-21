package otang.pulse.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.List;
import otang.pulse.lib.util.PrefUtils;

public class PulseControllerImpl implements PulseController, SharedPreferences.OnSharedPreferenceChangeListener {

	public static final boolean DEBUG = false;
	private static final String TAG = PulseControllerImpl.class.getSimpleName();
	private static final int RENDER_STYLE_LEGACY = 0;
	private static final int RENDER_STYLE_CM = 1;
	private Context mContext;
	private Renderer mRenderer;
	private VisualizerStreamHandler mStreamHandler;
	private ColorController mColorController;
	private final List<PulseStateListener> mStateListeners = new ArrayList<>();
	private PulseView mPulseView;
	private int mPulseStyle;
	private final PowerManager mPowerManager;
	// Pulse state
	private boolean mLinked;
	private boolean mPowerSaveModeEnabled;
	private boolean mScreenOn = true; // MUST initialize as true
	private boolean mLeftInLandscape;
	private boolean mAttached;
	private boolean mIsEnable;
	private VisualizerView vv;
	private int mAudioSessionId = -1;
	private SharedPreferences mPref;
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
	private final VisualizerStreamHandler.Listener mStreamListener = new VisualizerStreamHandler.Listener() {
		@Override
		public void onStreamAnalyzed(boolean isValid) {
			if (mRenderer != null) {
				mRenderer.onStreamAnalyzed(isValid);
			}
			notifyStateListeners(isValid);
			if (isValid) {
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

	public PulseControllerImpl(Context context, VisualizerView view) {
		mContext = context;
		vv = view;
		mPref = PreferenceManager.getDefaultSharedPreferences(context);
		mPref.registerOnSharedPreferenceChangeListener(this);
		mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mPowerSaveModeEnabled = mPowerManager.isPowerSaveMode();
		mStreamHandler = new VisualizerStreamHandler(mContext, mStreamListener);
		mPulseView = new PulseView(context, this);
		mColorController = new ColorController(mContext);
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
		context.registerReceiver(mBroadcastReceiver, filter, null, null);
		updateSettings();
	}

	void updateSettings() {
		mIsEnable = mPref.getInt(PrefUtils.PREF_PULSE, 1) == 1;
		mLeftInLandscape = mPref.getBoolean(PrefUtils.PREF_PULSE_LEFT, false);
		mPulseStyle = Integer.valueOf(mPref.getString(PrefUtils.PREF_PULSE_RENDER, "1"));
		if (mIsEnable) {
			attachPulseTo(vv);
		} else {
			detachPulseFrom(vv);
		}
		loadRenderer();
	}
	
	@Override
	public void attachPulseTo(FrameLayout parent) {
		if (parent == null)
			return;
		View v = parent.findViewWithTag(PulseView.TAG);
		if (v == null) {
			parent.addView(mPulseView);
			mAttached = true;
			doLinkage();
		}
	}

	@Override
	public void detachPulseFrom(FrameLayout parent) {
		if (parent == null)
			return;
		View v = parent.findViewWithTag(PulseView.TAG);
		if (v != null) {
			parent.removeView(v);
			mAttached = false;
			doLinkage();
		}
	}

	@NonNull
	public void setAudioSessionId(int id) {
		this.mAudioSessionId = id;
		doLinkVisualizer();
	}

	public void release() {
		doUnlinkVisualizer();
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

	private void notifyStateListeners(boolean isStarting) {
		for (PulseStateListener listener : mStateListeners) {
			if (listener != null) {
				if (isStarting) {
					listener.onPulseStateChanged(true);
				} else {
					listener.onPulseStateChanged(false);
				}
			}
		}
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

	public void onDraw(Canvas canvas) {
		if (shouldDrawPulse()) {
			mRenderer.draw(canvas);
		}
	}

	private void turnOnPulse() {
		if (shouldDrawPulse()) {
			mStreamHandler.resume(); // let bytes hit visualizer
		}
	}

	void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (mRenderer != null) {
			mRenderer.onSizeChanged(w, h, oldw, oldh);
		}
	}

	private Renderer getRenderer() {
		switch (mPulseStyle) {
		case RENDER_STYLE_LEGACY:
			return new FadingBlockRenderer(mContext, mPulseView, mColorController);
		case RENDER_STYLE_CM:
			return new SolidLineRenderer(mContext, mPulseView, mColorController);
		default:
			return new FadingBlockRenderer(mContext, mPulseView, mColorController);
		}
	}

	private boolean isUnlinkRequired() {
		return !mScreenOn || mPowerSaveModeEnabled || !mAttached;
	}

	private boolean isAbleToLink() {
		return mScreenOn && !mPowerSaveModeEnabled && mAttached;
	}

	public void doUnlinkVisualizer() {
		if (mStreamHandler != null) {
			mStreamHandler.unlink();
			mLinked = false;
			if (mRenderer != null) {
				mRenderer.onVisualizerLinkChanged(false);
			}
			mPulseView.postInvalidate();
			notifyStateListeners(false);
		}
	}

	private void doLinkage() {
		if (isUnlinkRequired()) {
			if (mLinked) {
				// explicitly unlink
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

	private void doSilentUnlinkVisualizer() {
		if (mStreamHandler != null) {
			if (mLinked) {
				mStreamHandler.unlink();
				mLinked = false;
			}
		}
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String keys) {
		if (keys.equals(PrefUtils.PREF_PULSE) || keys.equals(PrefUtils.PREF_PULSE_LEFT)
				|| keys.equals(PrefUtils.PREF_PULSE_RENDER)) {
			updateSettings();
		}
	}
}