package otang.pulse.lib.util;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import com.google.android.material.R;

public class ColorUtils {

  @ColorInt
  public static int getColorAttr(Context ctx, int resId) {
    TypedValue typedValue = new TypedValue();
    ctx.getTheme().resolveAttribute(resId, typedValue, true);
    return typedValue.data;
  }

  @ColorInt
  public static int getPrimaryColor(Context ctx) {
    return getColorAttr(ctx, R.attr.colorPrimary);
  }

  @ColorInt
  public static int getOnPrimaryColor(Context ctx) {
    return getColorAttr(ctx, R.attr.colorOnPrimary);
  }

  @ColorInt
  public static int getSecondaryColor(Context ctx) {
    return getColorAttr(ctx, R.attr.colorSecondary);
  }

  @ColorInt
  public static int getTertiaryColor(Context ctx) {
    return getColorAttr(ctx, R.attr.colorTertiary);
  }

  @ColorInt
  public static int getSurfaceColor(Context ctx) {
    return getColorAttr(ctx, R.attr.colorSurface);
  }

  @ColorInt
  public static int getOnSurfaceColor(Context ctx) {
    return getColorAttr(ctx, R.attr.colorOnSurface);
  }

  @ColorInt
  public static int darker(@ColorInt int color, @FloatRange(from = 0.0, to = 1.0) float factor) {
    int a = Color.alpha(color);
    int r = Math.max(Color.red(color) * (int) factor, 0);
    int g = Math.max(Color.green(color) * (int) factor, 0);
    int b = Math.max(Color.blue(color) * (int) factor, 0);
    return Color.argb(a, r, g, b);
  }

  @ColorInt
  public static int darker(@ColorInt int color) {
    float factor = 0.85f;
    int a = Color.alpha(color);
    int r = Math.max(Color.red(color) * (int) factor, 0);
    int g = Math.max(Color.green(color) * (int) factor, 0);
    int b = Math.max(Color.blue(color) * (int) factor, 0);
    return Color.argb(a, r, g, b);
  }

  @ColorInt
  public static int lighter(@ColorInt int color, @FloatRange(from = 0.0, to = 1.0) float factor) {
    int a = Color.alpha(color);
    int r = (Color.red(color) * (int) (1 - factor) / 255 + (int) factor) * 255;
    int g = (Color.green(color) * (int) (1 - factor) / 255 + (int) factor) * 255;
    int b = (Color.blue(color) * (int) (1 - factor) / 255 + (int) factor) * 255;
    return Color.argb(a, r, g, b);
  }

  @ColorInt
  public static int lighter(@ColorInt int color) {
    float factor = 0.15f;
    int a = Color.alpha(color);
    int r = (Color.red(color) * (int) (1 - factor) / 255 + (int) factor) * 255;
    int g = (Color.green(color) * (int) (1 - factor) / 255 + (int) factor) * 255;
    int b = (Color.blue(color) * (int) (1 - factor) / 255 + (int) factor) * 255;
    return Color.argb(a, r, g, b);
  }

  public static boolean isDarkColor(@ColorInt int color) {
    return isDarkColor(color, 0.5);
  }

  public static boolean isDarkColor(
      @ColorInt int color, @FloatRange(from = 0.0, to = 1.0) double luminance) {
    return androidx.core.graphics.ColorUtils.calculateLuminance(color) <= luminance;
  }

  public static boolean isLightColor(@ColorInt int color) {
    return isLightColor(color, 0.5);
  }

  public static boolean isLightColor(
      @ColorInt int color, @FloatRange(from = 0.0, to = 1.0) double luminance) {
    return androidx.core.graphics.ColorUtils.calculateLuminance(color) >= luminance;
  }

  @ColorInt
  public static int adjustAlpha(
      @ColorInt int color, @FloatRange(from = 0.0, to = 1.0) float factor) {
    int a = Math.round(Color.alpha(color) * factor);
    int r = Color.red(color);
    int g = Color.green(color);
    int b = Color.blue(color);
    return Color.argb(a, r, g, b);
  }

  @ColorInt
  public static int stripAlpha(@ColorInt int color) {
    return Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
  }

  public static String toHex(@ColorInt int color, boolean alpha) {
    return "#" + String.format(alpha ? "%08X" : "%06X", alpha ? color : 0xFFFFFF & color);
  }

  @ColorInt
  public static int parseColor(String color) {
    try {
      if (color.startsWith("#")) {
        return Color.parseColor(color.substring(1));
      }
      int length = color.length();
      int a, r, g, b;
      if (length == 0) {
        a = 255;
        r = 0;
        g = 0;
        b = 0;
      } else if (length <= 2) {
        a = 255;
        r = 0;
        g = 0;
        b = Integer.parseInt(color, 16);
      } else if (length == 3) {
        a = 255;
        r = Integer.parseInt(color.substring(0, 1), 16);
        g = Integer.parseInt(color.substring(1, 2), 16);
        b = Integer.parseInt(color.substring(2, 3), 16);
      } else if (length == 4) {
        a = 255;
        r = 0;
        g = Integer.parseInt(color.substring(0, 2), 16);
        b = Integer.parseInt(color.substring(2, 4), 16);
      } else if (length == 5) {
        a = 255;
        r = Integer.parseInt(color.substring(0, 1), 16);
        g = Integer.parseInt(color.substring(1, 3), 16);
        b = Integer.parseInt(color.substring(3, 5), 16);
      } else if (length == 6) {
        a = 255;
        r = Integer.parseInt(color.substring(0, 2), 16);
        g = Integer.parseInt(color.substring(2, 4), 16);
        b = Integer.parseInt(color.substring(4, 6), 16);
      } else if (length == 7) {
        a = Integer.parseInt(color.substring(0, 1), 16);
        r = Integer.parseInt(color.substring(1, 3), 16);
        g = Integer.parseInt(color.substring(3, 5), 16);
        b = Integer.parseInt(color.substring(5, 7), 16);
      } else if (length == 8) {
        a = Integer.parseInt(color.substring(0, 2), 16);
        r = Integer.parseInt(color.substring(2, 4), 16);
        g = Integer.parseInt(color.substring(4, 6), 16);
        b = Integer.parseInt(color.substring(6, 8), 16);
      } else {
        a = -1;
        r = -1;
        g = -1;
        b = -1;
      }
      return Color.argb(a, r, g, b);
    } catch (NumberFormatException e) {
      return Color.parseColor(color);
    }
  }
}
