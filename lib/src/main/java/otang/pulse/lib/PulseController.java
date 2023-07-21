package otang.pulse.lib;

public interface PulseController {

  interface PulseStateListener {
    void onPulseStateChanged(boolean isRunning);
  }

  void addCallback(PulseStateListener listener);

  void removeCallback(PulseStateListener listener);
}
