package otang.pulse.lib;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class PulseView extends View {

  public static final String TAG = "PulseView";
  private PulseControllerImpl mPulse;

  public PulseView(Context context, PulseControllerImpl controller) {
    super(context);
    mPulse = controller;
    setLayoutParams(
        new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    setWillNotDraw(false);
    setTag(TAG);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    mPulse.onSizeChanged(w, h, oldw, oldh);
    super.onSizeChanged(w, h, oldw, oldh);
  }

  @Override
  public void onDraw(Canvas canvas) {
    mPulse.onDraw(canvas);
    super.onDraw(canvas);
  }
}
