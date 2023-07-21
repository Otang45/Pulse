package otang.pulse.lib;

import java.util.ArrayDeque;

class FFTAverage {

  private static final int WINDOW_LENGTH = 2;
  private static final float WINDOW_LENGTH_F = WINDOW_LENGTH;
  private ArrayDeque<Float> window = new ArrayDeque<>(WINDOW_LENGTH);
  private float average;

  int average(int dB) {
    // Waiting until window is full
    if (window.size() >= WINDOW_LENGTH) {
      Float first = window.pollFirst();
      if (first != null) average -= first;
    }
    float newValue = dB / WINDOW_LENGTH_F;
    average += newValue;
    window.offerLast(newValue);

    return Math.round(average);
  }
}
