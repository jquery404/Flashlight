package com.jquery404.flashlight.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

import com.jquery404.flashlight.R;

/**
 * Created by Faisal on 7/26/17.
 */

public class Utils {

    public static int convertDpToPixel(Context context, float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * (metrics.densityDpi / 160f));
        return px;
    }

    public static int cycleColor(float colorCounter) {
        int r = (int) Math.floor(128 * (Math.sin(colorCounter) + 3));
        int g = (int) Math.floor(128 * (Math.sin(colorCounter + 1) + 1));
        int b = (int) Math.floor(128 * (Math.sin(colorCounter + 7) + 1));
        return Color.argb(128, r, g, b);
    }

    public static int getColorId(Context context) {
        int i = R.color.bit3;
        if (Math.random() * 10 < 3 && Math.random() * 10 > 5) {
            i = R.color.bit4;
        } else if (Math.random() * 10 < 5 && Math.random() * 10 > 7) {
            i = R.color.bit6;
        } else if (Math.random() * 10 < 7 && Math.random() * 10 > 9) {
            i = R.color.bit2;
        }
        return ContextCompat.getColor(context, i);
    }
}
