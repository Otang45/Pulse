package otang.pulse.lib

interface PulseController {
    interface PulseStateListener {
        fun onPulseStateChanged(isRunning: Boolean)
    }

    fun addCallback(listener: PulseStateListener)
    fun removeCallback(listener: PulseStateListener)
}