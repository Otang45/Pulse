package otang.pulse.lib

import java.util.ArrayDeque
import kotlin.math.roundToInt

internal class FFTAverage {
    private val window = ArrayDeque<Float>(WINDOW_LENGTH)
    private var average = 0f
    fun average(dB: Int): Int {
        // Waiting until window is full
        if (window.size >= WINDOW_LENGTH) {
            val first = window.pollFirst()
            if (first != null) average -= first
        }
        val newValue = dB / WINDOW_LENGTH_F
        average += newValue
        window.offerLast(newValue)
        return average.roundToInt()
    }

    companion object {
        private const val WINDOW_LENGTH = 2
        private const val WINDOW_LENGTH_F = WINDOW_LENGTH.toFloat()
    }
}