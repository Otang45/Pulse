package otang.pulse.lib.util;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.ColorInt;

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


}
