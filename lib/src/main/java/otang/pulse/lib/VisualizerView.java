package otang.pulse.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VisualizerView extends FrameLayout {

  private boolean mAttached;

  public VisualizerView(@NonNull Context context) {
    super(context);
  }

  public VisualizerView(@NonNull Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public VisualizerView(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void onAttachedToWindow() {
    mAttached = true;
    super.onAttachedToWindow();
  }

  @Override
  public void onDetachedFromWindow() {
    mAttached = false;
    super.onDetachedFromWindow();
  }

  public boolean isAttached() {
    return mAttached;
  }
}
