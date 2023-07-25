package otang.pulse.lib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Canvas
import android.os.PowerManager
import android.util.AttributeSet
import android.view.View
import otang.pulse.lib.PulseController.PulseStateListener
import otang.pulse.lib.util.PulseConfig

class VisualizerView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet?, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes), PulseController,
    OnSharedPreferenceChangeListener {
    constructor(context: Context) : this(context, null)

    lateinit var pulsePref: PulseConfig
    private lateinit var mPowerManager: PowerManager
    private var mPowerSaveModeEnabled = false
    private lateinit var mStreamHandler: VisualizerStreamHandler
    private val mStreamListener: VisualizerStreamHandler.Listener =
        object : VisualizerStreamHandler.Listener {
            override fun onStreamAnalyzed(isValid: Boolean) {
                mRenderer.onStreamAnalyzed(isValid)
                if (isValid) {
                    notifyStateListeners(true)
                    turnOnPulse()
                } else {
                    doSilentUnlinkVisualizer()
                }
            }

            override fun onFFTUpdate(bytes: ByteArray?) {
                if (bytes != null) {
                    mRenderer.onFFTUpdate(bytes)
                }
            }

            override fun onWaveFormUpdate(bytes: ByteArray?) {
                if (bytes != null) {
                    mRenderer.onWaveFormUpdate(bytes)
                }
            }
        }
    private lateinit var mColorController: ColorController
    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Intent.ACTION_SCREEN_OFF == action) {
                mScreenOn = false
                doLinkage()
            } else if (Intent.ACTION_SCREEN_ON == action) {
                mScreenOn = true
                doLinkage()
            } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED == action) {
                mPowerSaveModeEnabled = mPowerManager.isPowerSaveMode
                doLinkage()
            }
        }
    }
    private var mScreenOn = true
    private var mLeftInLandscape = false
    private var mPulseStyle = 0
    private var mAttached = false
    private var mLinked = false
    private lateinit var mRenderer: Renderer
    private val mStateListeners: MutableList<PulseStateListener> = ArrayList()
    private var mAudioSessionId = -1

    init {
        setWillNotDraw(false)
        init()
    }

    private fun init() {
        pulsePref = PulseConfig(context)
        pulsePref.registerListener(this)
        mPowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mPowerSaveModeEnabled = mPowerManager.isPowerSaveMode
        mStreamHandler = VisualizerStreamHandler(mStreamListener)
        mColorController = ColorController(context)
        mColorController.setRenderer(renderer)
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        context.registerReceiver(mBroadcastReceiver, filter, null, null)
        updateSettings()
        doLinkage()
    }

    private fun updateSettings() {
        val mIsEnable = pulsePref.isPulseEnabled
        mLeftInLandscape = pulsePref.isLeftInLandscape
        mPulseStyle = pulsePref.renderStyle
        mAttached = mIsEnable
        loadRenderer()
        doLinkage()
        invalidate()
    }

    private fun loadRenderer() {
        mRenderer = renderer
        val isRendering = shouldDrawPulse()
        if (isRendering) {
            mStreamHandler.pause()
        }
        mRenderer.destroy()
        mRenderer = renderer
        mColorController.setRenderer(mRenderer)
        mRenderer.setLeftInLandscape(mLeftInLandscape)
        if (isRendering) {
            mRenderer.onStreamAnalyzed(true)
            mStreamHandler.resume()
        }
    }

    private fun shouldDrawPulse(): Boolean {
        return mLinked && mStreamHandler.isValidStream
    }

    private val renderer: Renderer
        get() = if (mPulseStyle == RENDER_STYLE_CM) {
            SolidLineRenderer(context, this, mColorController)
        } else FadingBlockRenderer(context, this, mColorController)

    private fun notifyStateListeners(isStarting: Boolean) {
        for (listener in mStateListeners) {
            listener.onPulseStateChanged(isStarting)
        }
    }

    private fun turnOnPulse() {
        if (shouldDrawPulse()) {
            mStreamHandler.resume() // let bytes hit visualizer
        }
    }

    private fun doSilentUnlinkVisualizer() {
        if (mLinked) {
            mStreamHandler.unlink()
            mLinked = false
        }
    }

    private fun doLinkage() {
        if (isUnlinkRequired) {
            if (mLinked) {
                doUnlinkVisualizer()
            }
        } else {
            if (isAbleToLink) {
                doLinkVisualizer()
            } else if (mLinked) {
                doUnlinkVisualizer()
            }
        }
    }

    private val isUnlinkRequired: Boolean
        get() = !mScreenOn || mPowerSaveModeEnabled || !mAttached

    private fun doUnlinkVisualizer() {
        mStreamHandler.unlink()
        mLinked = false
        mRenderer.onVisualizerLinkChanged(false)
        this.postInvalidate()
        notifyStateListeners(false)
    }

    private val isAbleToLink: Boolean
        get() = mScreenOn && !mPowerSaveModeEnabled && mAttached

    private fun doLinkVisualizer() {
        if (!mLinked && mAudioSessionId != -1) {
            mStreamHandler.link(mAudioSessionId)
            mLinked = true
            mRenderer.onVisualizerLinkChanged(true)
        }
    }

    fun setAudioSessionId(id: Int) {
        mAudioSessionId = id
        doLinkage()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mRenderer.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        if (shouldDrawPulse()) {
            mRenderer.draw(canvas)
        }
    }

    override fun addCallback(listener: PulseStateListener) {
        mStateListeners.add(listener)
        if (shouldDrawPulse()) {
            listener.onPulseStateChanged(true)
        }
    }

    override fun removeCallback(listener: PulseStateListener) {
        mStateListeners.remove(listener)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, keys: String?) {
        if (keys == PulseConfig.PREF_PULSE || keys == PulseConfig.PREF_PULSE_ROUNDED   || keys == PulseConfig.PREF_PULSE_GRAVITY || keys == PulseConfig.PREF_PULSE_RENDER || keys == PulseConfig.PREF_PULSE_LEFT) {
            updateSettings()
        }
    }

    companion object {
        private const val RENDER_STYLE_CM = 1
    }
}