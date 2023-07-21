package otang.pulse.lib.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.google.android.material.R

object ColorUtils {
    @ColorInt
    fun getColorAttr(ctx: Context, resId: Int): Int {
        val typedValue = TypedValue()
        ctx.theme.resolveAttribute(resId, typedValue, true)
        return typedValue.data
    }

    @ColorInt
    fun getPrimaryColor(ctx: Context): Int {
        return getColorAttr(ctx, R.attr.colorPrimary)
    }
}