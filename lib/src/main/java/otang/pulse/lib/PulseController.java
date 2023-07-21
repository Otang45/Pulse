package otang.pulse.lib;

import android.widget.FrameLayout;

public interface PulseController {

  public interface PulseStateListener {
    public void onPulseStateChanged(boolean isRunning);
  }

  public void attachPulseTo(FrameLayout parent);

  public void detachPulseFrom(FrameLayout parent);

  public void addCallback(PulseStateListener listener);

  public void removeCallback(PulseStateListener listener);
}
